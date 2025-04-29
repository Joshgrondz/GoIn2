package com.example.goin2.teacher.ActiveEvent

import android.os.Bundle
import android.util.Log
import android.view.View
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

    private lateinit var eventName: String
    private var teacherRadius = 10
    private var eventRadius = 100
    private var eventLatLng: LatLng? = null

    private val handler = android.os.Handler()
    private val locationRunnable = object : Runnable {
        override fun run() {
            updateTeacherLocation()
            handler.postDelayed(this, 10_000)  // every 10 seconds
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher_view_event)

        eventName = intent.getStringExtra("eventName") ?: ""
        if (eventName.isEmpty()) {
            Log.e("TeacherViewEvent", "❌ No eventName passed")
            finish()
            return
        }

        fusedClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
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
                    zoomToEvent()                      // ✅ Initial zoom to event only
                    handler.post(locationRunnable)    // 🔁 Start 10s update loop for teacher location
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



    private fun zoomToEvent() {
        val center = eventLatLng ?: return
        val radius = eventRadius * 1.4  // +40%

        val bounds = LatLngBounds.builder()
            .include(SphericalUtil.computeOffset(center, radius.toDouble(), 0.0))    // North
            .include(SphericalUtil.computeOffset(center, radius.toDouble(), 90.0))   // East
            .include(SphericalUtil.computeOffset(center, radius.toDouble(), 180.0))  // South
            .include(SphericalUtil.computeOffset(center, radius.toDouble(), 270.0))  // West
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




    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(locationRunnable)
    }
}
