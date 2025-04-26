package com.example.goin2.student

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.goin2.R
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray

class StudentNameSelectActivity : AppCompatActivity() {

    private val client = OkHttpClient()
    private val apiUrlBase = "https://webapplication120250408230542-draxa5ckg5gabacc.canadacentral-01.azurewebsites.net/api/StudentsInEventsView"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_name_select)

        val eventName = intent.getStringExtra("eventName") ?: "Unknown Event"
        val eventId = intent.getIntExtra("eventId", -1)

        val eventNameText = findViewById<TextView>(R.id.textEventName)
        val studentList = findViewById<LinearLayout>(R.id.studentListContainer)

        eventNameText.text = "Event: $eventName"

        if (eventId != -1) {
            fetchStudentsForEvent(eventId, studentList)
        } else {
            Toast.makeText(this, "Invalid event ID", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchStudentsForEvent(eventId: Int, studentList: LinearLayout) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = Request.Builder()
                    .url("$apiUrlBase/$eventId")
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val jsonArray = JSONArray(responseBody)

                    withContext(Dispatchers.Main) {
                        studentList.removeAllViews()

                        for (i in 0 until jsonArray.length()) {
                            val studentObject = jsonArray.getJSONObject(i)
                            val firstName = studentObject.optString("firstName", "")
                            val lastName = studentObject.optString("lastName", "")
                            val studentId = studentObject.optInt("studentId", -1)

                            val fullName = "$firstName $lastName".trim()

                            val row = LinearLayout(this@StudentNameSelectActivity).apply {
                                orientation = LinearLayout.HORIZONTAL
                                setPadding(0, 12, 0, 12)
                                weightSum = 1f

                                val nameView = TextView(this@StudentNameSelectActivity).apply {
                                    text = fullName
                                    textSize = 20f
                                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.7f)
                                }

                                val selectButton = Button(this@StudentNameSelectActivity).apply {
                                    text = "Select"
                                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.3f)
                                    setOnClickListener {
                                        // TODO: Replace this placeholder later with navigation to Student Profile screen
                                        Toast.makeText(
                                            this@StudentNameSelectActivity,
                                            "Student selected",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }

                                addView(nameView)
                                addView(selectButton)
                            }

                            studentList.addView(row)
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@StudentNameSelectActivity, "Failed to fetch students", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@StudentNameSelectActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
