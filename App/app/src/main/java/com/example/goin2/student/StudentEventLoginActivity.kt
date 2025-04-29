package com.example.goin2.student

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.goin2.API_and_location.ApiClient
import com.example.goin2.R
import com.example.goin2.main.MainActivity
import org.json.JSONArray

class StudentEventLoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_event_login)

        val eventInput = findViewById<EditText>(R.id.editTextEventName)
        val enterButton = findViewById<Button>(R.id.buttonEnterEvent)

        enterButton.setOnClickListener {
            val enteredEventName = eventInput.text.toString().trim()

            if (enteredEventName.isEmpty()) {
                Toast.makeText(this, "Enter event name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            ApiClient.getAllEvents { body ->
                val events = JSONArray(body)
                var foundEventId: Int? = null

                for (i in 0 until events.length()) {
                    val event = events.getJSONObject(i)
                    val name = event.getString("eventName")
                    val active = event.getBoolean("status")

                    if (active && name.equals(enteredEventName, ignoreCase = true)) {
                        foundEventId = event.getInt("id")
                        val teacherId = event.getInt("teacherid")
                        MainActivity.currentTeacherId = teacherId
                        MainActivity.currentEventId = foundEventId

                        break
                    }
                }

                runOnUiThread {
                    if (foundEventId != null) {
                        val intent = Intent(this, StudentNameSelectActivity::class.java)
                        intent.putExtra("eventId", foundEventId)
                        intent.putExtra("eventName", enteredEventName)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "Active event not found", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
