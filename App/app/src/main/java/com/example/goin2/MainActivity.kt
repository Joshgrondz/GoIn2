package com.example.goin2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import org.json.JSONArray

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
        // Simulated API JSON response
        val json = """
        [
          { "id": 1, "name": "Lucas" },
          { "id": 2, "name": "Josh" },
          { "id": 3, "name": "Eian" },
          { "id": 4, "name": "Aiden" },
          { "id": 5, "name": "Antonio" }
        ]
    """.trimIndent()

        return try {
            val names = JSONArray(json)
            val result = StringBuilder()

            for (i in 0 until names.length()) {
                val student = names.getJSONObject(i)
                result.append("${student.getString("name")}\n")
            }

            result.toString().trim()
        } catch (e: Exception) {
            "Failed to parse response"
        }
    }


    fun startLocationService() {
        val intent = Intent(this, LocationService::class.java)
        ContextCompat.startForegroundService(this, intent)
        Toast.makeText(this, "Location tracking started", Toast.LENGTH_SHORT).show()
    }
}
