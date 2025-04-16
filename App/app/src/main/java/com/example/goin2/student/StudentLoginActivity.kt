package com.example.goin2.student

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.goin2.API_and_location.ApiClient
import com.example.goin2.R

class StudentLoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_login)

        val nameInput = findViewById<EditText>(R.id.loginNameInput)
        val loginButton = findViewById<Button>(R.id.buttonLogin)
        val createButton = findViewById<Button>(R.id.buttonCreate)

        loginButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            ApiClient.getStudentIdByName(name) { studentId ->
                if (studentId != null) {
                    val intent = Intent(this, StudentActivity::class.java)
                    intent.putExtra("student_id", studentId)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Invalid name", Toast.LENGTH_SHORT).show()
                }
            }
        }

        createButton.setOnClickListener {
            Toast.makeText(this, "TODO: Create student", Toast.LENGTH_SHORT).show()
        }
    }
}