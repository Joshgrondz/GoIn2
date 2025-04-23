package com.example.goin2.teacher

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.goin2.API_and_location.ApiClient
import com.example.goin2.R
import com.example.goin2.main.MainActivity

class TeacherLoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher_login)

        val firstNameInput = findViewById<EditText>(R.id.editTextFirstName)
        val lastNameInput = findViewById<EditText>(R.id.editTextLastName)
        val loginBtn = findViewById<Button>(R.id.buttonTeacherLogin)
        val createBtn = findViewById<Button>(R.id.buttonCreateTeacher)

        loginBtn.setOnClickListener {
            val first = firstNameInput.text.toString().trim()
            val last = lastNameInput.text.toString().trim()

            if (first.isNotEmpty() && last.isNotEmpty()) {
                ApiClient.loginTeacher(first, last, this) { userId ->
                    if (userId != null) {
                        startActivity(Intent(this, TeacherActivity::class.java))
                    }
                }
            }
        }

        createBtn.setOnClickListener {
            val first = firstNameInput.text.toString().trim()
            val last = lastNameInput.text.toString().trim()

            if (first.isNotEmpty() && last.isNotEmpty()) {
                ApiClient.createTeacher(first, last, this) { userId ->
                    if (userId != null) {
                        startActivity(Intent(this, TeacherActivity::class.java))
                    }
                }
            }
        }
    }
}
