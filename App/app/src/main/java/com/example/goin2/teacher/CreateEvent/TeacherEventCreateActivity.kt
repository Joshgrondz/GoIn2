package com.example.goin2.teacher.CreateEvent

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.setPadding
import com.example.goin2.API_and_location.ApiClient
import com.example.goin2.R
import com.example.goin2.main.MainActivity
import com.google.android.gms.maps.model.LatLng
import com.squareup.picasso.Picasso
import org.json.JSONArray
import org.json.JSONObject

class TeacherEventCreateActivity : AppCompatActivity() {
    var reloadAttempted = false

    private lateinit var container: LinearLayout
    private lateinit var addButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher_event_create)

        container = findViewById(R.id.eventListContainer)
        addButton = findViewById(R.id.floatingAddEventButton)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { view, insets ->
            val topInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
            view.setPadding(0, topInset + 24, 0, 24)
            insets
        }

        setSupportActionBar(toolbar)
        supportActionBar?.title = "Your Events"

        addButton.setOnClickListener {
            val frag = UnifiedCreateEventFragment { refreshEventList() }
            supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, frag)
                .addToBackStack(null)
                .commit()
        }

        refreshEventList()
    }

    private fun refreshEventList() {
        runOnUiThread {
            container.removeAllViews()
        }

        ApiClient.getAllEvents { body ->
            val events = JSONArray(body)
            for (i in 0 until events.length()) {
                val e = events.getJSONObject(i)
                if (e.getInt("teacherid") == MainActivity.currentTeacherId) {
                    val eventId = e.getInt("id")
                    val eventName = e.getString("eventName")
                    val eventLocation = e.getString("eventLocation")
                    val geofenceId = e.getInt("geofenceid")

                    ApiClient.getGeoFence(geofenceId) { geoBody ->
                        val geo = JSONObject(geoBody)
                        val lat = geo.getDouble("latitude")
                        val lng = geo.getDouble("longitude")
                        val eventRadius = geo.getInt("eventRadius")
                        runOnUiThread {
                            addEventView(eventName, eventLocation, eventId, lat, lng, eventRadius)
                        }
                    }
                }
            }
        }
    }


    private fun addEventView(
        eventName: String,
        eventLocation: String,
        eventId: Int,
        lat: Double,
        lng: Double,
        eventRadius: Int
    ) {
        val latLng = LatLng(lat, lng)

        val wrapper = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16)
            setBackgroundColor(ContextCompat.getColor(this@TeacherEventCreateActivity, R.color.custom_grey))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 24 }
        }

        val title = TextView(this).apply {
            text = eventName
            textSize = 25f
            setTextColor(ContextCompat.getColor(this@TeacherEventCreateActivity, android.R.color.white))
        }

        val dropdownContent = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
            setPadding(16)
        }

        val topRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val leftColumn = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val locationText = TextView(this).apply {
            text = "Location: $eventLocation"
            setTextColor(ContextCompat.getColor(this@TeacherEventCreateActivity, android.R.color.white))
            textSize = 20f
        }

        val classLabel = TextView(this).apply {
            text = "Classes:"
            setTextColor(ContextCompat.getColor(this@TeacherEventCreateActivity, android.R.color.white))
            textSize = 20f
            setPadding(0, 12, 0, 4)
        }

        val classListLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 0, 0, 0)
        }

        leftColumn.addView(locationText)
        leftColumn.addView(classLabel)
        leftColumn.addView(classListLayout)


        val mapViewContainer = FrameLayout(this).apply {
            id = View.generateViewId() // 🔥 Need a unique ID to insert Fragment
            layoutParams = LinearLayout.LayoutParams(600, 600)
            setBackgroundColor(ContextCompat.getColor(this@TeacherEventCreateActivity, android.R.color.black))
        }

        val mapImageView = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(600, 600)
            setBackgroundColor(ContextCompat.getColor(this@TeacherEventCreateActivity, android.R.color.black))
        }
        mapViewContainer.addView(mapImageView)



        topRow.addView(leftColumn)
        topRow.addView(mapViewContainer)

        dropdownContent.addView(topRow)

        var dataLoaded = false
        title.setOnClickListener {
            dropdownContent.visibility =
                if (dropdownContent.visibility == View.VISIBLE) View.GONE else View.VISIBLE

            if (!dataLoaded && dropdownContent.visibility == View.VISIBLE) {
                ApiClient.getAllClassEvents { classEventBody ->
                    val matchingClassIds = mutableSetOf<Int>()
                    val entries = JSONArray(classEventBody)

                    for (i in 0 until entries.length()) {
                        val obj = entries.getJSONObject(i)
                        if (obj.getInt("eventid") == eventId) {
                            val classId = obj.getInt("classid")
                            matchingClassIds.add(classId)
                        }
                    }

                    if (matchingClassIds.isEmpty()) {
                        runOnUiThread {
                            classListLayout.removeAllViews()
                            val noClassText = TextView(this).apply {
                                text = "No classes assigned"
                                setTextColor(ContextCompat.getColor(this@TeacherEventCreateActivity, android.R.color.white))
                                textSize = 16f
                                setPadding(16, 4, 0, 4)
                            }
                            classListLayout.addView(noClassText)
                        }
                        return@getAllClassEvents
                    }

                    ApiClient.getAllClasses { allClassesBody ->
                        val allClasses = JSONArray(allClassesBody)
                        val matchedClassNames = mutableListOf<String>()

                        for (i in 0 until allClasses.length()) {
                            val obj = allClasses.getJSONObject(i)
                            val classId = obj.getInt("id")
                            if (matchingClassIds.contains(classId)) {
                                val className = obj.getString("className")
                                matchedClassNames.add(className)
                            }
                        }

                        runOnUiThread {
                            classListLayout.removeAllViews()
                            for (name in matchedClassNames) {
                                val classNameText = TextView(this).apply {
                                    text = name
                                    setTextColor(ContextCompat.getColor(this@TeacherEventCreateActivity, android.R.color.white))
                                    textSize = 16f
                                    setPadding(16, 4, 0, 4)
                                }
                                classListLayout.addView(classNameText)
                            }
                        }
                    }
                }

                val staticMapUrl = "https://maps.googleapis.com/maps/api/staticmap?" +
                        "center=${lat},${lng}" +
                        "&zoom=18" +
                        "&size=600x600" +
                        "&maptype=roadmap" +
                        "&markers=color:red%7C${lat},${lng}" +
                        "&key=AIzaSyCCktqbp8hutj7fqDvnwcFranSeMW00i0g"

                Picasso.get()
                    .load(staticMapUrl)
                    .placeholder(android.R.color.darker_gray)
                    .into(mapImageView, object : com.squareup.picasso.Callback {
                        override fun onSuccess() {
                            // Loaded fine, nothing special
                        }

                        override fun onError(e: Exception?) {
                            if (!reloadAttempted) {
                                reloadAttempted = true
                                println("DEBUG: Map load failed, retrying whole page refresh...")
                                runOnUiThread {
                                    refreshEventList() // ⬅️ Reload everything once
                                }
                            } else {
                                println("DEBUG: Map load failed again, not retrying.")
                            }
                        }
                    })

                dataLoaded = true
            }
        }

        wrapper.addView(title)
        wrapper.addView(dropdownContent)
        container.addView(wrapper)
    }

}
