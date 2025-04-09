package com.example.goin2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

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

        val apiButton = findViewById<Button>(R.id.buttonApiCall)
        val resultBox = findViewById<TextView>(R.id.apiResultBox)

        apiButton?.setOnClickListener {
            resultBox.text = "" // Clear previous result

            // Simulate an API call — replace with real logic later
            val simulatedResponse: String? = testApiCall()

            resultBox.text = simulatedResponse ?: "Nothing received"
        }

    }

    private fun testApiCall(): String? {
        // TODO: Replace this with actual network call logic later
        return "Sample result from API (NOT REAL, not yet implemented)"
        // return null to simulate failure or no response
    }

    fun startLocationService() {
        val intent = Intent(this, LocationService::class.java)
        ContextCompat.startForegroundService(this, intent)
        Toast.makeText(this, "Location tracking started", Toast.LENGTH_SHORT).show()
    }
}
