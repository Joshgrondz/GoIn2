package com.example.goin2.teacher

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.goin2.R

class TeacherLoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher_login)

        val firstNameInput = findViewById<EditText>(R.id.editTextFirstName)
        val lastNameInput = findViewById<EditText>(R.id.editTextLastName)
        val loginBtn = findViewById<Button>(R.id.buttonTeacherLogin)

        loginBtn.setOnClickListener {
            val first = firstNameInput.text.toString().trim()
            val last = lastNameInput.text.toString().trim()

            if (first.isEmpty() || last.isEmpty()) {
                Toast.makeText(this, "Enter full name", Toast.LENGTH_SHORT).show()
            } else {
                // Replace this with real API logic
                val intent = Intent(this, TeacherActivity::class.java)
                intent.putExtra("teacher_name", "$first $last")
                startActivity(intent)
            }
        }
    }
}
