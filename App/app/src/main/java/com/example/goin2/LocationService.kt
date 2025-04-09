package com.example.goin2

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class LocationService : Service() {

    override fun onCreate() {
        super.onCreate()
        startForeground(1, createNotification()) // MUST be called immediately
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Optional: track location here
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(): Notification {
        val channelId = "location_channel"
        val channelName = "Location Tracking"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW // Must be LOW or higher to show
            ).apply {
                description = "Used for foreground location tracking"
                enableLights(false)
                enableVibration(false)
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Tracking Active")
            .setContentText("Your location is being tracked for safety.")
            .setSmallIcon(android.R.drawable.ic_dialog_map) // Guaranteed system icon
            .setOngoing(true)
            .build()
    }
}
