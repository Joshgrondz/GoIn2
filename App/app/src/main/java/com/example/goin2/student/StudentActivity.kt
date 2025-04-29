package com.example.goin2.student

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.goin2.API_and_location.ApiClient
import com.example.goin2.R
import com.example.goin2.main.MainActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.SphericalUtil
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject

class StudentActivity : AppCompatActivity() {

    private lateinit var eventName: String
    private lateinit var studentName: String
    private var studentId: Int = -1
    private var eventId: Int = -1
    private var teacherId: Int = -1

    private lateinit var map: GoogleMap
    private var eventLatLng: LatLng? = null
    private var teacherRadius = 10
    private var eventRadius = 100

    private var teacherCircle: Circle? = null
    private var studentMarker: Marker? = null
    private var teacherMarker: Marker? = null

    private val groupHandler = Handler(Looper.getMainLooper())
    private var groupCheckRunnable: Runnable? = null
    private val groupCheckIntervalMillis = 20_000L

    private val mapHandler = Handler(Looper.getMainLooper())
    private val mapUpdateRunnable = object : Runnable {
        override fun run() {
            updateTeacherAndStudentLocation()
            mapHandler.postDelayed(this, 10_000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student)

        eventName = MainActivity.currentEventName ?: "Unknown Event"
        studentName = "${MainActivity.currentStudentFirstName ?: ""} ${MainActivity.currentStudentLastName ?: ""}".trim()
        studentId = MainActivity.currentStudentId
        eventId = MainActivity.currentEventId

        findViewById<TextView>(R.id.textViewEventName).text = "Event: $eventName"
        findViewById<TextView>(R.id.textViewStudentName).text = "Student: $studentName"

        if (studentId != -1 && eventId != -1) {
            startCheckingGoIn2Group()
            resolveTeacherIdAndSetupMap()
        } else {
            Toast.makeText(this, "Missing event or student information.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopCheckingGoIn2Group()
        mapHandler.removeCallbacks(mapUpdateRunnable)
    }

    private fun startCheckingGoIn2Group() {
        groupCheckRunnable = object : Runnable {
            override fun run() {
                checkGroupStatus()
                groupHandler.postDelayed(this, groupCheckIntervalMillis)
            }
        }
        groupHandler.post(groupCheckRunnable!!)
    }

    private fun stopCheckingGoIn2Group() {
        groupHandler.removeCallbacks(groupCheckRunnable!!)
    }

    private fun checkGroupStatus() {
        ApiClient.getActivePair(eventId, studentId) { pair ->
            runOnUiThread {
                val statusText = findViewById<TextView>(R.id.textViewPairedStatus)

                if (pair != null) {
                    val student1 = pair.optInt("student1id", -1)
                    val student2 = pair.optInt("student2id", -1)
                    val status = pair.optBoolean("status", false)

                    if (status && (student1 == studentId || student2 == studentId)) {
                        val buddyId = if (student1 == studentId) student2 else student1

                        ApiClient.getBuddyName(buddyId) { buddyName ->
                            runOnUiThread {
                                statusText.text = if (buddyName != null) {
                                    "You are paired with: $buddyName,\n stay close to them"
                                } else {
                                    "Paired student info unavailable."
                                }
                            }
                        }
                    } else {
                        statusText.text = "You are not currently paired."
                    }
                } else {
                    statusText.text = "You are not currently paired."
                }
            }
        }
    }

    private fun resolveTeacherIdAndSetupMap() {
        ApiClient.getAllEvents { response ->
            val events = JSONArray(response)
            for (i in 0 until events.length()) {
                val obj = events.getJSONObject(i)
                if (obj.getInt("id") == eventId) {
                    teacherId = obj.getInt("teacherid")
                    break
                }
            }

            val mapFragment = supportFragmentManager.findFragmentById(R.id.studentMapFragment) as? SupportMapFragment
            mapFragment?.getMapAsync { googleMap ->
                map = googleMap

                // ✅ Enforce 2D-only map UI
                map.uiSettings.apply {
                    isZoomControlsEnabled = false           // Remove zoom +/- buttons
                    isCompassEnabled = false
                    map.isBuildingsEnabled = false
                    isMapToolbarEnabled = false             // Remove bottom marker toolbar
                    isIndoorLevelPickerEnabled = false      // Remove floor picker (L1/L2/L3)
                    isTiltGesturesEnabled = false           // 🔒 Prevent 3D tilt
                    isRotateGesturesEnabled = false         // 🔒 Prevent rotation
                    isScrollGesturesEnabled = true
                    isZoomGesturesEnabled = true
                }

                map.setIndoorEnabled(false)                 // 🔒 No indoor map mode
                map.mapType = GoogleMap.MAP_TYPE_NORMAL     // 📄 Flat 2D road map

                drawEventCircleAndZoom()
                mapHandler.post(mapUpdateRunnable)
            }

        }
    }

    private fun drawEventCircleAndZoom() {
        ApiClient.getAllEvents { response ->
            val events = JSONArray(response)
            val match = (0 until events.length())
                .map { events.getJSONObject(it) }
                .find { it.getInt("id") == eventId }

            if (match != null) {
                val geofenceId = match.getInt("geofenceid")
                ApiClient.getGeoFence(geofenceId) { geoResponse ->
                    val geo = JSONObject(geoResponse)
                    eventLatLng = LatLng(geo.getDouble("latitude"), geo.getDouble("longitude"))
                    eventRadius = geo.getInt("eventRadius")
                    teacherRadius = geo.getInt("teacherRadius")

                    runOnUiThread {
                        eventLatLng?.let {
                            map.addCircle(
                                CircleOptions()
                                    .center(it)
                                    .radius(eventRadius.toDouble())
                                    .strokeColor(0xFFAA66CC.toInt())
                                    .fillColor(0x22AA66CC)
                            )
                            zoomToEvent(it)
                        }
                    }
                }
            }
        }
    }

    private fun zoomToEvent(center: LatLng) {
        val radius = eventRadius * 1.4
        val bounds = LatLngBounds.builder()
            .include(SphericalUtil.computeOffset(center, radius.toDouble(), 0.0))
            .include(SphericalUtil.computeOffset(center, radius.toDouble(), 90.0))
            .include(SphericalUtil.computeOffset(center, radius.toDouble(), 180.0))
            .include(SphericalUtil.computeOffset(center, radius.toDouble(), 270.0))
            .build()

        map.setOnMapLoadedCallback {
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
        }
    }

    private fun updateTeacherAndStudentLocation() {
        // Teacher
        if (teacherId != -1) {
            ApiClient.getLastKnownLocation(teacherId) { lat, lng ->
                if (lat != 0.0 && lng != 0.0) {
                    val teacherLatLng = LatLng(lat, lng)
                    runOnUiThread {
                        teacherCircle?.remove()
                        teacherMarker?.remove()

                        teacherCircle = map.addCircle(
                            CircleOptions()
                                .center(teacherLatLng)
                                .radius(teacherRadius.toDouble())
                                .strokeColor(0xFF66BB6A.toInt())
                                .fillColor(0x2266BB6A)
                        )

                        teacherMarker = map.addMarker(
                            MarkerOptions()
                                .position(teacherLatLng)
                                .title("Teacher")
                        )
                    }
                }
            }
        }

        // Student
        ApiClient.getLastKnownLocation(studentId) { lat, lng ->
            if (lat != 0.0 && lng != 0.0) {
                val studentLatLng = LatLng(lat, lng)
                runOnUiThread {
                    studentMarker?.remove()
                    studentMarker = map.addMarker(
                        MarkerOptions()
                            .position(studentLatLng)
                            .title("You")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    )
                }
            }
        }
    }
}
