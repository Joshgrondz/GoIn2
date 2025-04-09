package com.example.goin2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import okhttp3.OkHttpClient
import okhttp3.Request
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

            Thread {
                val simulatedResponse: String? = testApiCall()

                runOnUiThread {
                    resultBox.text = simulatedResponse ?: "Nothing received"
                }
            }.start()
        }

    }

    private fun testApiCall(): String? {
        return try {
            val client = OkHttpClient()

            val request = Request.Builder()
                .url("http://10.0.2.2:5017/api/Student")
                .addHeader("accept", "text/plain")
                .build()

            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                return "HTTP error: ${response.code}"
            }

            val bodyString = response.body?.string()
            if (bodyString == null) return "Response body is null"

            val jsonArray = JSONArray(bodyString)
            val result = StringBuilder()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                result.append("${obj.getString("name")}\n")
            }

            result.toString().trim()
        } catch (e: Exception) {
            "Exception: ${e::class.simpleName} - ${e.message ?: "No message"}"
        }
    }




    fun startLocationService() {
        val intent = Intent(this, LocationService::class.java)
        ContextCompat.startForegroundService(this, intent)
        Toast.makeText(this, "Location tracking started", Toast.LENGTH_SHORT).show()
    }
}
