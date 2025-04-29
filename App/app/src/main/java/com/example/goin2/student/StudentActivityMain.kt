package com.example.goin2.student

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.goin2.R
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class StudentActivityMain : AppCompatActivity() {

    private lateinit var eventName: String
    private lateinit var studentName: String
    private var studentId: Int = -1
    private var eventId: Int = -1

    private val client = OkHttpClient()

    // Adjustable delay (milliseconds)
    private val pairCheckIntervalMillis: Long = 20000 // 20 seconds

    private val pairApiBase = "https://webapplication120250408230542-draxa5ckg5gabacc.canadacentral-01.azurewebsites.net/api/Pair"
    private val userApiBase = "https://webapplication120250408230542-draxa5ckg5gabacc.canadacentral-01.azurewebsites.net/api/User"

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var pairStatusChecker: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_main)

        eventName = intent.getStringExtra("eventName") ?: "Unknown Event"
        studentName = intent.getStringExtra("studentName") ?: "Unknown Student"
        studentId = intent.getIntExtra("studentId", -1)
        eventId = intent.getIntExtra("eventId", -1)

        val eventNameTextView = findViewById<TextView>(R.id.textViewEventName)
        val studentNameTextView = findViewById<TextView>(R.id.textViewStudentName)

        eventNameTextView.text = "Event: $eventName"
        studentNameTextView.text = "Student: $studentName"

        if (studentId != -1 && eventId != -1) {
            checkStudentPairStatus()
            startPairStatusChecking()
        } else {
            Toast.makeText(this, "Missing event or student information.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startPairStatusChecking() {
        pairStatusChecker = object : Runnable {
            override fun run() {
                checkStudentPairStatus()
                handler.postDelayed(this, pairCheckIntervalMillis)
            }
        }
        handler.postDelayed(pairStatusChecker, pairCheckIntervalMillis)
    }

    private fun stopPairStatusChecking() {
        handler.removeCallbacks(pairStatusChecker)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPairStatusChecking()
    }

    private fun checkStudentPairStatus() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = Request.Builder()
                    .url("$pairApiBase/Event/$eventId/Student/$studentId/Active")
                    .build()

                val response = client.newCall(request).execute()

                if (response.code == 404) { // No pair found
                    withContext(Dispatchers.Main) {
                        val pairedTextView = findViewById<TextView>(R.id.textViewPairedStatus)
                        pairedTextView.text = "You are not currently paired."
                    }
                    return@launch
                }

                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val jsonObject = JSONObject(responseBody)

                    val status = jsonObject.optBoolean("status", false)
                    val student1id = jsonObject.optInt("student1id", -1)
                    val student2id = jsonObject.optInt("student2id", -1)

                    if (status && student1id != -1 && student2id != -1) {
                        val buddyId = if (student1id == studentId) student2id else student1id
                        fetchBuddyName(buddyId)
                    } else {
                        withContext(Dispatchers.Main) {
                            val pairedTextView = findViewById<TextView>(R.id.textViewPairedStatus)
                            pairedTextView.text = "You are not currently paired."
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@StudentActivityMain, "Error checking pair status.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@StudentActivityMain, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun fetchBuddyName(buddyId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = Request.Builder()
                    .url("$userApiBase/$buddyId")
                    .build()

                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val jsonObject = JSONObject(responseBody)

                    val firstName = jsonObject.optString("firstName", "")
                    val lastName = jsonObject.optString("lastName", "")
                    val fullName = "$firstName $lastName".trim()

                    withContext(Dispatchers.Main) {
                        val pairedTextView = findViewById<TextView>(R.id.textViewPairedStatus)
                        pairedTextView.text = "You are paired with: $fullName"
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        val pairedTextView = findViewById<TextView>(R.id.textViewPairedStatus)
                        pairedTextView.text = "Paired student info unavailable."
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    val pairedTextView = findViewById<TextView>(R.id.textViewPairedStatus)
                    pairedTextView.text = "Network error retrieving buddy info."
                }
            }
        }
    }
}
