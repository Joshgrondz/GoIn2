//private val apiUrl = "https://webapplication120250408230542-draxa5ckg5gabacc.canadacentral-01.azurewebsites.net/api/Event"
package com.example.goin2.student

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.goin2.R
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray

class StudentEventLoginActivity : AppCompatActivity() {

    private val client = OkHttpClient()
    private val apiUrl = "https://webapplication120250408230542-draxa5ckg5gabacc.canadacentral-01.azurewebsites.net/api/Event"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_event_login)

        val eventInput = findViewById<EditText>(R.id.editTextEventName)
        val enterButton = findViewById<Button>(R.id.buttonEnterEvent)

        enterButton.setOnClickListener {
            val eventName = eventInput.text.toString().trim()
            if (eventName.isEmpty()) {
                Toast.makeText(this, "Enter event name", Toast.LENGTH_SHORT).show()
            } else {
                validateEventName(eventName)
            }
        }
    }

    private fun validateEventName(enteredEventName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = Request.Builder()
                    .url(apiUrl)
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val jsonArray = JSONArray(responseBody)

                    var matchFound = false
                    var matchedEventId = -1

                    for (i in 0 until jsonArray.length()) {
                        val eventObject = jsonArray.getJSONObject(i)
                        val apiEventName = eventObject.getString("eventName")
                        val apiEventId = eventObject.getInt("id")

                        if (enteredEventName == apiEventName) { // Case-sensitive check
                            matchFound = true
                            matchedEventId = apiEventId
                            break
                        }
                    }

                    withContext(Dispatchers.Main) {
                        if (matchFound) {
                            val intent = Intent(this@StudentEventLoginActivity, StudentNameSelectActivity::class.java)
                            intent.putExtra("eventName", enteredEventName)
                            intent.putExtra("eventId", matchedEventId) // 🚀 PASS event ID
                            startActivity(intent)
                        } else {
                            Toast.makeText(this@StudentEventLoginActivity, "Invalid event", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@StudentEventLoginActivity, "Error fetching events", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@StudentEventLoginActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
