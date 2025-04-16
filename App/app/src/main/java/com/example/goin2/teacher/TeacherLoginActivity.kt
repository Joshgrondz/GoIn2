package com.example.goin2.teacher

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.goin2.API_and_location.ApiClient
import com.example.goin2.R

class TeacherLoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher_login)

        val nameInput = findViewById<EditText>(R.id.loginTeacherNameInput)
        val loginButton = findViewById<Button>(R.id.buttonTeacherLogin)
        val createButton = findViewById<Button>(R.id.buttonCreateTeacher)

        loginButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            ApiClient.getStudentIdByName(name) { teacherId ->
                if (teacherId != null) {
                    val intent = Intent(this, TeacherActivity::class.java)
                    intent.putExtra("teacher_id", teacherId)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Invalid teacher name", Toast.LENGTH_SHORT).show()
                }
            }
        }

        createButton.setOnClickListener {
            Toast.makeText(this, "TODO: Create teacher", Toast.LENGTH_SHORT).show()
        }
    }
}
