package com.example.goin2.API_and_location

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import com.example.goin2.R
import com.example.goin2.main.MainActivity
import com.google.android.gms.location.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var userId: Int = -1
    private var lastNotificationTimestamp: String = ""
    private var handler: Handler? = null
    private var runnable: Runnable? = null
    private var offsetFlag = true

    private val locationChannelId = "location_tracking"
    private val serverNotificationChannelId = "server_notifications"

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val incomingUserId = intent?.getIntExtra("userId", -1) ?: -1

        if (incomingUserId == -1) {
            Log.e("LocationService", "Invalid userId, stopping service.")
            stopSelf()
            return START_NOT_STICKY
        }

        if (this.userId != -1 && this.userId != incomingUserId) {
            Log.i("LocationService", "New userId detected. Restarting tracking.")
            stopLocationTracking()
        }

        userId = incomingUserId

        if (!this::fusedLocationClient.isInitialized) {
            startForeground(1, createPersistentNotification())
            beginLocationTracking()
            scheduleAlternatingTasks()
        }

        return START_STICKY
    }

    private fun stopLocationTracking() {
        if (this::fusedLocationClient.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            Log.i("LocationService", "Stopped location tracking.")
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun beginLocationTracking() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 20_000L).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return

                val payload = JSONObject().apply {
                    put("userid", userId)
                    put("latitude", location.latitude)
                    put("longitude", location.longitude)
                    put("locAccuracy", location.accuracy)
                    put("locAltitude", location.altitude)
                    put("locSpeed", location.speed)
                    put("locBearing", location.bearing)
                    put("locProvider", location.provider ?: "unknown")
                    put("timestampMs", System.currentTimeMillis())
                }

                ApiClient.postLocation(payload)
                Log.d("LocationService", "Location posted: $payload")
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    private fun scheduleAlternatingTasks() {
        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                if (offsetFlag) {
                    Log.d("LocationService", "[Cycle] Location update happening.")
                    // Already managed by fusedLocationClient
                } else {
                    Log.d("LocationService", "[Cycle] Checking for notifications.")
                    checkForNotifications()
                }
                offsetFlag = !offsetFlag
                handler?.postDelayed(this, 20_000L)
            }
        }
        handler?.post(runnable!!)
    }

    private fun checkForNotifications() {
        ApiClient.getNotificationsByUser(userId) { notificationList ->
            if (notificationList.isNotEmpty()) {
                val mostRecent = notificationList.maxByOrNull { it.notificationTimestamp } ?: return@getNotificationsByUser

                val serverTime = parseServerTime(mostRecent.notificationTimestamp)
                val now = System.currentTimeMillis()

                if (serverTime != null && now - serverTime <= 10 * 60 * 1000) { // within 10 minutes
                    if (mostRecent.notificationTimestamp != lastNotificationTimestamp) {
                        showLocalNotification(mostRecent.notificationDescription)
                        lastNotificationTimestamp = mostRecent.notificationTimestamp
                    }
                } else {
                    Log.w("LocationService", "Ignoring stale notification: ${mostRecent.notificationTimestamp}")
                }
            }
        }
    }

    private fun parseServerTime(timestamp: String): Long? {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US)
            format.parse(timestamp)?.time
        } catch (e: Exception) {
            Log.e("LocationService", "Failed to parse server timestamp: $timestamp", e)
            null
        }
    }

    private fun showLocalNotification(text: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                serverNotificationChannelId,
                "Server Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, serverNotificationChannelId)
            .setContentTitle("Update")
            .setContentText(text)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val id = (System.currentTimeMillis() % 100000).toInt()
        manager.notify(id, notification)
    }

    private fun createPersistentNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                locationChannelId,
                "Location Tracking",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, locationChannelId)
            .setContentTitle("Tracking Active")
            .setContentText("Teacher Will Be Notified If Closed")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.w("LocationService", "Service destroyed. Sending disconnect alert.")

        if (this::fusedLocationClient.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }

        handler?.removeCallbacks(runnable!!)

        // Notify teacher if possible
        val eventId = MainActivity.currentEventId
        val teacherId = MainActivity.currentTeacherId
        val studentFirst = MainActivity.currentStudentFirstName
        val studentLast = MainActivity.currentStudentLastName

        if (eventId != null && teacherId != null && !studentFirst.isNullOrBlank() && !studentLast.isNullOrBlank()) {
            val payload = JSONObject().apply {
                put("userid", teacherId)
                put("eventid", eventId)
                put("notificationDescription", "$studentFirst $studentLast disconnected from location tracking.")
                put("notificationTimestamp", getCurrentTimestampString())
                put("sent", true)
            }

            ApiClient.sendNotification(payload) { success ->
                if (success) {
                    Log.i("LocationService", "Teacher successfully notified of disconnect.")
                } else {
                    Log.e("LocationService", "Failed to send disconnect alert.")
                }
            }
        }
    }

    private fun getCurrentTimestampString(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US)
        return formatter.format(Date())
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
