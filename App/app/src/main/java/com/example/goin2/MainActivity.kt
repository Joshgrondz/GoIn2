package com.example.goin2

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val actionButton = findViewById<Button>(R.id.api_button)
        actionButton.setOnClickListener {
            // TODO: Add API call here
        }
    }
}
