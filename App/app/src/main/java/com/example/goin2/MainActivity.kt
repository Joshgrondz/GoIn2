package com.example.goin2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import androidx.core.widget.doAfterTextChanged
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import android.os.Looper
import android.Manifest
import android.content.pm.PackageManager


class MainActivity : AppCompatActivity() {

    companion object {
        var currentStudentId: Int = 7
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES) // always dark

        // Show the permission UI as a fragment
        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, PermissionFragment())
            .commit()

        findViewById<Button>(R.id.buttonTeacher)?.setOnClickListener {
            Toast.makeText(this, "Teacher mode (to be implemented)", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.buttonStudent)?.setOnClickListener {
            Toast.makeText(this, "Student mode (to be implemented)", Toast.LENGTH_SHORT).show()
        }

        val studentIdInput = findViewById<EditText>(R.id.studentIdInput)

        studentIdInput.doAfterTextChanged {
            val input = it?.toString()?.trim()
            currentStudentId = input?.toIntOrNull() ?: 7
            Log.d("MainActivity", "Student ID set to $currentStudentId")
        }


        val apiButton = findViewById<Button>(R.id.buttonApiCall)
        val resultBox = findViewById<TextView>(R.id.apiResultBox)

        apiButton?.setOnClickListener {
            resultBox.text = "" // Clear previous result

            ApiClient.getStudents { result ->
                resultBox.text = result
            }
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as? SupportMapFragment

        var googleMap: GoogleMap? = null

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

            // First try to use last known location
            fusedLocationClient.lastLocation.addOnSuccessListener { lastLocation ->
                if (lastLocation != null) {
                    val latLng = LatLng(lastLocation.latitude, lastLocation.longitude)
                    googleMap?.apply {
                        clear()
                        addMarker(MarkerOptions().position(latLng).title("Your Location"))
                        moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                    }
                } else {
                    Log.d("MainActivity", "lastLocation was null — polling for fresh location")

                    // Build location request
                    val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 0).apply {
                        setMinUpdateIntervalMillis(0)
                        setMaxUpdateDelayMillis(0)
                        setMaxUpdates(1)
                    }.build()

                    // Build callback
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
                                Toast.makeText(this@MainActivity, "Still couldn't get location", Toast.LENGTH_SHORT).show()
                            }
                            fusedLocationClient.removeLocationUpdates(this)
                        }
                    }

                    // Make request
                    fusedLocationClient.requestLocationUpdates(
                        request,
                        callback,
                        Looper.getMainLooper()
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



    fun startLocationService() {
        val intent = Intent(this, LocationService::class.java)
        ContextCompat.startForegroundService(this, intent)
        Toast.makeText(this, "Location tracking started", Toast.LENGTH_SHORT).show()
    }
}
