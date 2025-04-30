package com.example.goin2.teacher.ActiveEvent

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.goin2.API_and_location.ApiClient
import com.example.goin2.R
import com.example.goin2.main.MainActivity
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.SphericalUtil
import org.json.JSONArray
import org.json.JSONObject

class TeacherViewEventActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var fusedClient: FusedLocationProviderClient
    private var teacherCircle: Circle? = null
    private var eventId: Int = -1
    private lateinit var eventName: String
    private var teacherRadius = 10
    private var eventRadius = 100
    private var eventLatLng: LatLng? = null

    private val handler = android.os.Handler()
    private val locationRunnable = object : Runnable {
        override fun run() {
            updateTeacherLocation()
            handler.postDelayed(this, 10_000)
        }
    }

    private val studentUpdateHandler = android.os.Handler()
    private val studentMarkers = mutableMapOf<Int, Marker>()
    private var showStudents = false

    private val studentUpdateRunnable = object : Runnable {
        override fun run() {
            fetchAndDisplayStudentLocations()
            studentUpdateHandler.postDelayed(this, 60_000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher_view_event)

        eventId = intent.getIntExtra("eventId", -1)
        eventName = intent.getStringExtra("eventName") ?: ""

        if (eventId == -1 || eventName.isEmpty()) {
            Log.e("TeacherViewEvent", "❌ Missing eventId or eventName")
            finish()
            return
        }

        fusedClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        findViewById<Button>(R.id.buttonCreateGroup)?.setOnClickListener {
            val frag = GoIn2GroupFragment(eventId) {
                Toast.makeText(this, "GoIn2 Pair Created", Toast.LENGTH_SHORT).show()
            }

            supportFragmentManager.beginTransaction()
                .add(android.R.id.content, frag)
                .addToBackStack(null)
                .commit()
        }

        findViewById<Button>(R.id.buttonEndGroup)?.setOnClickListener {
            val frag = EndGoIn2GroupFragment(eventId) {
                Toast.makeText(this, "Pair ended.", Toast.LENGTH_SHORT).show()
            }

            supportFragmentManager.beginTransaction()
                .add(android.R.id.content, frag)
                .addToBackStack(null)
                .commit()
        }

        findViewById<Button>(R.id.buttonShowStudents)?.setOnClickListener { view ->
            val btn = view as Button  // ✅ Cast it manually to Button

            showStudents = !showStudents

            if (showStudents) {
                btn.text = "Hide Students"
                studentUpdateHandler.post(studentUpdateRunnable)
            } else {
                btn.text = "Show All Students"
                studentUpdateHandler.removeCallbacks(studentUpdateRunnable)
                clearStudentMarkers()
            }
        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        map.uiSettings.apply {
            isZoomControlsEnabled = false
            isCompassEnabled = false
            isMapToolbarEnabled = false
            isIndoorLevelPickerEnabled = false
            isTiltGesturesEnabled = false
            isRotateGesturesEnabled = false
            isScrollGesturesEnabled = true
            isZoomGesturesEnabled = true
        }

        map.setIndoorEnabled(false)
        map.isBuildingsEnabled = false
        map.mapType = GoogleMap.MAP_TYPE_NORMAL

        loadEventData()
    }

    private fun loadEventData() {
        ApiClient.getAllEvents { response ->
            val events = JSONArray(response)
            val match = (0 until events.length())
                .map { events.getJSONObject(it) }
                .find { it.getString("eventName").equals(eventName, ignoreCase = true) }

            if (match == null) {
                Log.e("TeacherViewEvent", "❌ Event not found: $eventName")
                runOnUiThread { finish() }
                return@getAllEvents
            }

            val geofenceId = match.getInt("geofenceid")
            ApiClient.getGeoFence(geofenceId) { geoResponse ->
                val geo = JSONObject(geoResponse)
                eventLatLng = LatLng(geo.getDouble("latitude"), geo.getDouble("longitude"))
                eventRadius = geo.getInt("eventRadius")
                teacherRadius = geo.getInt("teacherRadius")

                runOnUiThread {
                    drawEventCircle()
                    zoomToEvent()
                    handler.post(locationRunnable)
                }
            }
        }
    }

    private fun drawEventCircle() {
        eventLatLng?.let {
            map.addCircle(
                CircleOptions()
                    .center(it)
                    .radius(eventRadius.toDouble())
                    .strokeColor(0xFFAA66CC.toInt())
                    .fillColor(0x22AA66CC)
            )
        }
    }

    private fun zoomToEvent() {
        val center = eventLatLng ?: return
        val radius = eventRadius * 1.4

        val bounds = LatLngBounds.builder()
            .include(SphericalUtil.computeOffset(center, radius.toDouble(), 0.0))
            .include(SphericalUtil.computeOffset(center, radius.toDouble(), 90.0))
            .include(SphericalUtil.computeOffset(center, radius.toDouble(), 180.0))
            .include(SphericalUtil.computeOffset(center, radius.toDouble(), 270.0))
            .build()

        map.setOnMapLoadedCallback {
            try {
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
                Log.d("ZoomToEvent", "✅ Zoomed to event radius with +40% padding")
            } catch (e: Exception) {
                Log.e("ZoomToEvent", "❌ Zoom failed: ${e.message}")
            }
        }
    }

    private fun updateTeacherLocation() {
        ApiClient.getLastKnownLocation(MainActivity.currentTeacherId) { lat, lng ->
            if (lat == 0.0 && lng == 0.0) {
                Log.w("TeacherViewEvent", "⚠️ No valid teacher location from API.")
                return@getLastKnownLocation
            }

            val teacherLatLng = LatLng(lat, lng)
            Log.d("TeacherViewEvent", "✅ Teacher location from API: $lat, $lng")

            teacherCircle?.remove()
            teacherCircle = map.addCircle(
                CircleOptions()
                    .center(teacherLatLng)
                    .radius(teacherRadius.toDouble())
                    .strokeColor(0xFF66BB6A.toInt())
                    .fillColor(0x2266BB6A)
            )

            map.addMarker(
                MarkerOptions()
                    .position(teacherLatLng)
                    .title("Teacher")
            )
        }
    }

    private fun fetchAndDisplayStudentLocations() {
        ApiClient.getAllClassEvents { classEventBody ->
            val classIds = mutableSetOf<Int>()
            val entries = JSONArray(classEventBody)
            for (i in 0 until entries.length()) {
                val obj = entries.getJSONObject(i)
                if (obj.getInt("eventid") == eventId) {
                    classIds.add(obj.getInt("classid"))
                }
            }

            if (classIds.isEmpty()) return@getAllClassEvents

            ApiClient.getAllClassRosters { rosterBody ->
                val rosters = JSONArray(rosterBody)
                val studentIds = mutableSetOf<Int>()
                for (i in 0 until rosters.length()) {
                    val obj = rosters.getJSONObject(i)
                    if (classIds.contains(obj.getInt("classid"))) {
                        studentIds.add(obj.getInt("studentid"))
                    }
                }

                // 🔥 Clear all previous student markers before adding new ones
                runOnUiThread {
                    clearStudentMarkers()
                }

                // 🔁 Re-fetch and redraw new student markers
                for (id in studentIds) {
                    ApiClient.getLastKnownLocation(id) { lat, lng ->
                        if (lat == 0.0 && lng == 0.0) return@getLastKnownLocation
                        runOnUiThread {
                            val marker = map.addMarker(
                                MarkerOptions()
                                    .position(LatLng(lat, lng))
                                    .title("Student $id")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                            )
                            if (marker != null) {
                                studentMarkers[id] = marker
                            }
                        }
                    }
                }

            }
        }
    }

    private fun clearStudentMarkers() {
        for ((_, marker) in studentMarkers) {
            marker.remove()
        }
        studentMarkers.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(locationRunnable)
        studentUpdateHandler.removeCallbacks(studentUpdateRunnable)
    }
}
