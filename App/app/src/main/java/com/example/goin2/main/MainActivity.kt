package com.example.goin2.main

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.goin2.API_and_location.ApiClient
import com.example.goin2.R
import com.example.goin2.teacher.TeacherLoginActivity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.goin2.student.StudentEventLoginActivity

class MainActivity : AppCompatActivity() {

    companion object {
        var currentEventName: String? = null
        var currentEventId: Int = 0
        var currentTeacherId: Int = 0
        var currentStudentId: Int = 7
        var currentStudentFirstName: String? = null
        var currentStudentLastName: String? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val pingLoader = findViewById<ProgressBar>(R.id.pingLoader)
        val pingIcon = findViewById<ImageView>(R.id.pingStatusIcon)

        pingLoader.visibility = View.VISIBLE
        pingIcon.visibility = View.GONE

        ApiClient.pingServer { success ->
            pingLoader.visibility = View.GONE
            pingIcon.visibility = View.VISIBLE
            pingIcon.setImageResource(
                if (success) android.R.drawable.checkbox_on_background
                else android.R.drawable.ic_delete
            )
        }


        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        // Load permission screen
        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, PermissionFragment())
            .commit()

        findViewById<Button>(R.id.buttonStudent)?.setOnClickListener {
            val intent = Intent(this, StudentEventLoginActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.buttonTeacher)?.setOnClickListener {
            val intent = Intent(this, TeacherLoginActivity::class.java)
            startActivity(intent)
        }

        // Create notification channel for Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "main_notifications",
                "Main Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

    }

}
