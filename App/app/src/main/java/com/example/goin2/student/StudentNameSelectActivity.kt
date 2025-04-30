package com.example.goin2.student

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.goin2.API_and_location.ApiClient
import com.example.goin2.API_and_location.LocationService
import com.example.goin2.R
import com.example.goin2.main.MainActivity
import org.json.JSONArray

class StudentNameSelectActivity : AppCompatActivity() {

    private var eventName: String = "Unknown Event"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_name_select)

        val eventId = intent.getIntExtra("eventId", -1)
        eventName = intent.getStringExtra("eventName") ?: "Unknown Event"

        if (eventId == -1) {
            Toast.makeText(this, "Invalid event", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val eventNameText = findViewById<TextView>(R.id.textEventName)
        val studentListContainer = findViewById<LinearLayout>(R.id.studentListContainer)

        eventNameText.text = "Select Your Name"
        loadStudents(eventId, studentListContainer)
    }

    private fun loadStudents(eventId: Int, container: LinearLayout) {
        ApiClient.getAllClassEvents { classEventBody ->
            val classEventList = JSONArray(classEventBody)
            val classIds = mutableSetOf<Int>()

            for (i in 0 until classEventList.length()) {
                val obj = classEventList.getJSONObject(i)
                if (obj.getInt("eventid") == eventId) {
                    classIds.add(obj.getInt("classid"))
                }
            }

            if (classIds.isEmpty()) {
                runOnUiThread {
                    Toast.makeText(this, "No classes linked to this event.", Toast.LENGTH_SHORT).show()
                }
                return@getAllClassEvents
            }

            ApiClient.getAllClassRosters { rosterBody ->
                val rosterList = JSONArray(rosterBody)
                val studentIds = mutableSetOf<Int>()

                for (i in 0 until rosterList.length()) {
                    val obj = rosterList.getJSONObject(i)
                    if (classIds.contains(obj.getInt("classid"))) {
                        studentIds.add(obj.getInt("studentid"))
                    }
                }

                if (studentIds.isEmpty()) {
                    runOnUiThread {
                        Toast.makeText(this, "No students found.", Toast.LENGTH_SHORT).show()
                    }
                    return@getAllClassRosters
                }

                for (studentId in studentIds) {
                    ApiClient.getUserById(studentId) { namePair ->
                        if (namePair != null) {
                            val (firstName, lastName) = namePair
                            runOnUiThread {
                                addStudentRow(container, studentId, firstName, lastName)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun addStudentRow(container: LinearLayout, userId: Int, firstName: String, lastName: String) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 12, 0, 12)
            weightSum = 1f
        }

        val nameView = TextView(this).apply {
            text = "$firstName $lastName"
            textSize = 20f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.7f)
        }

        val loginButton = Button(this).apply {
            text = "Login"
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.3f)

            setOnClickListener {
                // Save student identity globally
                MainActivity.currentStudentId = userId
                MainActivity.currentStudentFirstName = firstName
                MainActivity.currentStudentLastName = lastName

                // Start foreground location service
                val serviceIntent = Intent(this@StudentNameSelectActivity, LocationService::class.java).apply {
                    putExtra("userId", userId)
                    putExtra("userType", "student")
                }
                startForegroundService(serviceIntent)

                // Transition to StudentActivity
                val intent = Intent(this@StudentNameSelectActivity, StudentActivity::class.java).apply {
                    putExtra("eventName", eventName)
                }
                MainActivity.currentEventName = eventName
                startActivity(intent)

            }
        }

        row.addView(nameView)
        row.addView(loginButton)
        container.addView(row)
    }
}
