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
import kotlinx.coroutines.*
import org.json.JSONObject

class StudentActivity : AppCompatActivity() {

    private lateinit var eventName: String
    private lateinit var studentName: String
    private var studentId: Int = -1
    private var eventId: Int = -1

    private val handler = Handler(Looper.getMainLooper())
    private var groupCheckRunnable: Runnable? = null
    private val groupCheckIntervalMillis: Long = 20_000L // 20 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student)

        eventName = MainActivity.currentEventName ?: "Unknown Event"
        studentName = "${MainActivity.currentStudentFirstName ?: ""} ${MainActivity.currentStudentLastName ?: ""}".trim()
        studentId = MainActivity.currentStudentId
        eventId = MainActivity.currentEventId


        val eventNameTextView = findViewById<TextView>(R.id.textViewEventName)
        val studentNameTextView = findViewById<TextView>(R.id.textViewStudentName)

        eventNameTextView.text = "Event: $eventName"
        studentNameTextView.text = "Student: $studentName"

        if (studentId != -1 && eventId != -1) {
            startCheckingGoIn2Group()
        } else {
            Toast.makeText(this, "Missing event or student information.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopCheckingGoIn2Group()
    }

    private fun startCheckingGoIn2Group() {
        groupCheckRunnable = object : Runnable {
            override fun run() {
                checkGroupStatus()
                handler.postDelayed(this, groupCheckIntervalMillis)
            }
        }
        handler.post(groupCheckRunnable!!)
    }

    private fun stopCheckingGoIn2Group() {
        handler.removeCallbacks(groupCheckRunnable!!)
    }

    private fun checkGroupStatus() {
        val eventId = MainActivity.currentEventId
        val studentId = MainActivity.currentStudentId

        if (eventId == null || studentId == null) {
            Log.w("StudentActivityMain", "Event ID or Student ID is missing.")
            return
        }

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
                                if (buddyName != null) {
                                    statusText.text = "You are paired with: $buddyName"
                                } else {
                                    statusText.text = "Paired student info unavailable."
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
}
