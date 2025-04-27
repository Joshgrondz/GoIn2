package com.example.goin2.student

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.goin2.R

class StudentActivityMain : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_main)

        val eventName = intent.getStringExtra("eventName") ?: "Unknown Event"
        val studentName = intent.getStringExtra("studentName") ?: "Unknown Student"

        val eventNameTextView = findViewById<TextView>(R.id.textViewEventName)
        val studentNameTextView = findViewById<TextView>(R.id.textViewStudentName)

        eventNameTextView.text = "Event: $eventName"
        studentNameTextView.text = "Student: $studentName"

        // TODO: Here we can later add more buttons, lists, or functions related to the event or student!
    }
}
