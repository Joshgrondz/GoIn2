package com.example.goin2.student

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.goin2.R

class StudentNameSelectActivity : AppCompatActivity() {

    private val dummyStudents = listOf("Alice Johnson", "Bob Smith", "Charlie Lee")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_name_select)

        val eventName = intent.getStringExtra("eventName") ?: "Unknown Event"
        val eventNameText = findViewById<TextView>(R.id.textEventName)
        val studentList = findViewById<LinearLayout>(R.id.studentListContainer)

        eventNameText.text = "Event: $eventName"

        dummyStudents.forEachIndexed { index, name ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 12, 0, 12)
                weightSum = 1f

                val nameView = TextView(this@StudentNameSelectActivity).apply {
                    text = name
                    textSize = 20f
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.7f)
                }

                val loginButton = Button(this@StudentNameSelectActivity).apply {
                    text = "Login"
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.3f)
                    setOnClickListener {
                        val intent = Intent(this@StudentNameSelectActivity, StudentActivity::class.java)
                        intent.putExtra("student_id", index + 1)
                        startActivity(intent)
                    }
                }

                addView(nameView)
                addView(loginButton)
            }

            studentList.addView(row)
        }
    }
}
