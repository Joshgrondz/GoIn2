package com.example.goin2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class MainActivity : AppCompatActivity() {

    companion object {
        var currentStudentId: Int = 7
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        // Start permission fragment immediately
        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, PermissionFragment())
            .commit()

        findViewById<Button>(R.id.buttonTeacher)?.setOnClickListener {
            Toast.makeText(this, "Teacher mode (to be implemented)", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.buttonStudent)?.setOnClickListener {
            val intent = Intent(this, StudentLoginActivity::class.java)
            startActivity(intent)
        }
    }
}