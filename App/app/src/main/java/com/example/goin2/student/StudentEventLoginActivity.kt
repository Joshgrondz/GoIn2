package com.example.goin2.student

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.goin2.R

class StudentEventLoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_event_login)

        val eventInput = findViewById<EditText>(R.id.editTextEventName)
        val enterButton = findViewById<Button>(R.id.buttonEnterEvent)

        enterButton.setOnClickListener {
            val eventName = eventInput.text.toString().trim()
            if (eventName.isEmpty()) {
                Toast.makeText(this, "Enter event name", Toast.LENGTH_SHORT).show()
            } else {
                // TODO: Replace with real API call to validate event
                val isValidEvent = true
                if (isValidEvent) {
                    val intent = Intent(this, StudentNameSelectActivity::class.java)
                    intent.putExtra("eventName", eventName)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Invalid event", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
