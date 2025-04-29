package com.example.goin2.student

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.goin2.API_and_location.LocationService
import com.example.goin2.R
import com.example.goin2.main.MainActivity

class StudentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student)

        // Set student ID from intent
        val passedId = intent.getIntExtra("student_id", -1)
        if (passedId != -1) {
            MainActivity.currentStudentId = passedId
        } else {
            Toast.makeText(this, "No student ID passed", Toast.LENGTH_SHORT).show()
        }

        // Start location service now that ID is set
        val intent = Intent(this, LocationService::class.java).apply {
            putExtra("userId", MainActivity.currentStudentId)
        }
        ContextCompat.startForegroundService(this, intent)

        Toast.makeText(this, "Location tracking started", Toast.LENGTH_SHORT).show()
    }
}
