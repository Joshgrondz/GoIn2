package com.example.goin2.teacher

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.goin2.API_and_location.LocationService
import com.example.goin2.R
import com.example.goin2.main.MainActivity
import com.example.goin2.teacher.ActiveEvent.TeacherActiveEventActivity
import com.example.goin2.teacher.CreateClass.TeacherClassManagementActivity
import com.example.goin2.teacher.CreateEvent.TeacherEventCreateActivity

class TeacherActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher)

        // ✅ Start Location Service for the teacher
        startTeacherLocationService()

        findViewById<Button>(R.id.buttonAddStudents).setOnClickListener {
            startActivity(Intent(this, TeacherClassManagementActivity::class.java))
        }

        findViewById<Button>(R.id.buttonCreateEvent).setOnClickListener {
            startActivity(Intent(this, TeacherEventCreateActivity::class.java))
        }

        findViewById<Button>(R.id.buttonStartEvent).setOnClickListener {
            startActivity(Intent(this, TeacherActiveEventActivity::class.java))
        }
    }

    private fun startTeacherLocationService() {
        val teacherId = MainActivity.currentTeacherId

        if (teacherId != -1) {
            val intent = Intent(this, LocationService::class.java).apply {
                putExtra("userId", teacherId)
            }
            ContextCompat.startForegroundService(this, intent)
            Toast.makeText(this, "Teacher location tracking started", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "No teacher ID found, cannot start tracking.", Toast.LENGTH_SHORT).show()
        }
    }
}
