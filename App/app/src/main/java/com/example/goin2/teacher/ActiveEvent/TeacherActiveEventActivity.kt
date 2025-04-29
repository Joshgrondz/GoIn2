package com.example.goin2.teacher.ActiveEvent

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.goin2.API_and_location.ApiClient
import com.example.goin2.R
import com.example.goin2.main.MainActivity
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class TeacherActiveEventActivity : AppCompatActivity() {

    private lateinit var eventListContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher_active_event)

        eventListContainer = findViewById(R.id.eventListContainer)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { view, insets ->
            val topInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
            view.setPadding(0, topInset, 0, 0)
            insets
        }
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Manage Active Events"

        loadEvents()
    }

    private fun loadEvents() {
        eventListContainer.removeAllViews()

        ApiClient.getAllEvents { body ->
            val events = JSONArray(body)
            runOnUiThread {
                for (i in 0 until events.length()) {
                    val e = events.getJSONObject(i)
                    if (e.getInt("teacherid") == MainActivity.currentTeacherId) {
                        addEventRow(e)
                    }
                }
            }
        }
    }

    private fun addEventRow(eventObj: JSONObject) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(16, 16, 16, 16)
            gravity = Gravity.CENTER_VERTICAL
        }

        val nameView = TextView(this).apply {
            text = eventObj.getString("eventName")
            textSize = 24f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val toggleButton = Button(this).apply {
            text = if (eventObj.getBoolean("status")) "End Event" else "Start Event"
            textSize = 18f
            setBackgroundColor(resources.getColor(R.color.purple_200, theme))
            setTextColor(resources.getColor(R.color.white, theme))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = 16  // <-- 🔥 ADD THIS to create the gap between buttons
            }
        }

        val viewButton = Button(this).apply {
            text = "View Event"
            textSize = 18f
            setBackgroundColor(resources.getColor(R.color.purple_200, theme))
            setTextColor(resources.getColor(R.color.white, theme))
        }

        row.addView(nameView)
        row.addView(toggleButton)
        row.addView(viewButton)
        eventListContainer.addView(row)

        toggleButton.setOnClickListener {
            val newStatus = !eventObj.getBoolean("status")
            val updatedEvent = JSONObject().apply {
                put("eventName", eventObj.getString("eventName"))
                put("eventDate", eventObj.getString("eventDate"))
                put("eventLocation", eventObj.getString("eventLocation"))
                put("status", newStatus)
                put("teacherid", eventObj.getInt("teacherid"))
                put("geofenceid", eventObj.getInt("geofenceid"))
            }

            ApiClient.updateEvent(eventObj.getInt("id"), updatedEvent) { success ->
                if (success) {
                    runOnUiThread {
                        Toast.makeText(this, if (newStatus) "Event Started" else "Event Ended", Toast.LENGTH_SHORT).show()
                        if (!newStatus) {
                            sendEndNotifications(eventObj.getInt("id"), eventObj.getString("eventName"))
                        }
                        loadEvents()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this, "Failed to update event", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        viewButton.setOnClickListener {
            val intent = Intent(this, TeacherViewEventActivity::class.java)
            intent.putExtra("eventId", eventObj.getInt("id"))
            intent.putExtra("eventName", eventObj.getString("eventName"))
            intent.putExtra("geofenceId", eventObj.getInt("geofenceid"))
            startActivity(intent)
        }

    }

    private fun sendEndNotifications(eventId: Int, eventName: String) {
        ApiClient.getAllClassEvents { classEventBody ->
            val classEventList = JSONArray(classEventBody)
            val classIds = mutableSetOf<Int>()

            for (i in 0 until classEventList.length()) {
                val obj = classEventList.getJSONObject(i)
                if (obj.getInt("eventid") == eventId) {
                    classIds.add(obj.getInt("classid"))
                }
            }

            if (classIds.isEmpty()) return@getAllClassEvents

            ApiClient.getAllClassRosters { classRosterBody ->
                val rosterList = JSONArray(classRosterBody)
                val studentIds = mutableSetOf<Int>()

                for (i in 0 until rosterList.length()) {
                    val obj = rosterList.getJSONObject(i)
                    if (classIds.contains(obj.getInt("classid"))) {
                        studentIds.add(obj.getInt("studentid"))  // ✅ We trust studentid == userid
                    }
                }

                val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US).format(Date())

                for (userId in studentIds) {
                    val notification = JSONObject().apply {
                        put("userid", userId)  // ✅ use studentid directly
                        put("eventid", eventId)
                        put("notificationDescription", "Event $eventName is over")
                        put("notificationTimestamp", now)
                        put("sent", true)
                    }

                    ApiClient.sendNotification(notification) { success ->
                        if (!success) {
                            runOnUiThread {
                                Toast.makeText(this@TeacherActiveEventActivity, "Failed to send notification", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }
}
