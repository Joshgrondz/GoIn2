package com.example.goin2.student

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.goin2.API_and_location.ApiClient
import com.example.goin2.API_and_location.LocationService
import com.example.goin2.R
import com.example.goin2.main.MainActivity
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class StudentActivity : AppCompatActivity() {

    private var googleMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student)

        // Set student ID from intent
        val passedId = intent.getIntExtra("student_id", -1)
        if (passedId != -1) {
            MainActivity.currentStudentId = passedId
        } else {
            Toast.makeText(this, "No student ID passed", Toast.LENGTH_SHORT).show()
        }

        // Start location service now that ID is set
        val intent = Intent(this, LocationService::class.java)
        ContextCompat.startForegroundService(this, intent)
        Toast.makeText(this, "Location tracking started", Toast.LENGTH_SHORT).show()

        // Set up API call button
        val apiButton = findViewById<Button>(R.id.buttonApiCall)
        val resultBox = findViewById<TextView>(R.id.apiResultBox)
        apiButton.setOnClickListener {
            resultBox.text = ""
            ApiClient.getStudents { result ->
                resultBox.text = result
            }
        }

        // Set up map
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as? SupportMapFragment

        mapFragment?.getMapAsync { map ->
            googleMap = map
            map.uiSettings.isZoomControlsEnabled = true
        }

        val deviceButton = findViewById<Button>(R.id.buttonShowDeviceLocation)
        val serverButton = findViewById<Button>(R.id.buttonShowServerLocation)

        deviceButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Location permissions are not granted", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

            fusedLocationClient.lastLocation.addOnSuccessListener { lastLocation ->
                val now = System.currentTimeMillis()

                if (lastLocation != null && now - lastLocation.time <= 15_000) {
                    // Use cached location (≤ 15 seconds old)
                    val latLng = LatLng(lastLocation.latitude, lastLocation.longitude)
                    googleMap?.apply {
                        clear()
                        addMarker(MarkerOptions().position(latLng).title("Your Location"))
                        moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                    }
                } else {
                    // Fallback to fresh request
                    Log.d("StudentActivity", "Requesting fresh location")

                    val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 0).apply {
                        setMinUpdateIntervalMillis(0)
                        setMaxUpdateDelayMillis(0)
                        setMaxUpdates(1)
                    }.build()

                    val callback = object : LocationCallback() {
                        override fun onLocationResult(result: LocationResult) {
                            val location = result.lastLocation
                            if (location != null) {
                                val latLng = LatLng(location.latitude, location.longitude)
                                googleMap?.apply {
                                    clear()
                                    addMarker(MarkerOptions().position(latLng).title("Your Location"))
                                    moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                                }
                            } else {
                                Toast.makeText(this@StudentActivity, "Still couldn't get location", Toast.LENGTH_SHORT).show()
                            }
                            fusedLocationClient.removeLocationUpdates(this)
                        }
                    }

                    fusedLocationClient.requestLocationUpdates(
                        request,
                        callback,
                        mainLooper
                    )
                }
            }
        }



        serverButton.setOnClickListener {
            ApiClient.getLastKnownLocation(studentId = MainActivity.currentStudentId) { lat, lng ->
                val latLng = LatLng(lat, lng)
                googleMap?.apply {
                    clear()
                    addMarker(MarkerOptions().position(latLng).title("Last Server Location"))
                    moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                }
            }
        }
    }
}
