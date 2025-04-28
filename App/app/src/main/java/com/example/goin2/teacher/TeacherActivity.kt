package com.example.goin2.teacher

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.goin2.R
import com.example.goin2.teacher.ActiveEvent.TeacherActiveEventActivity
import com.example.goin2.teacher.CreateClass.TeacherClassManagementActivity
import com.example.goin2.teacher.CreateEvent.TeacherEventCreateActivity

class TeacherActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher)

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
}
