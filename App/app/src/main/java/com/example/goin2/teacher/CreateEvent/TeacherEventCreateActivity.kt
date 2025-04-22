package com.example.goin2.teacher.CreateEvent

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.goin2.R
import com.google.android.gms.maps.model.LatLng
import android.view.View


class TeacherEventCreateActivity : AppCompatActivity() {

    private var eventName: String? = null
    private var eventCenter: LatLng? = null
    private var eventRadius: Int = 0
    private var teacherDistance: Int = 0
    private var groupDistance: Int = 0
    private val selectedClasses = mutableSetOf<String>()
    private lateinit var creationContainer: ScrollView
    private var eventFormVisible = false


    // UI elements
    private lateinit var buttonCreate: Button
    private lateinit var inputTeacherDist: EditText
    private lateinit var inputGroupDist: EditText
    private lateinit var sliderTeacherDist: SeekBar
    private lateinit var sliderGroupDist: SeekBar
    private lateinit var checkboxOne: CheckBox
    private lateinit var checkboxTwo: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        creationContainer = findViewById(R.id.createEventContainer)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher_event_create)
        creationContainer = findViewById(R.id.createEventContainer)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Event Creation"

        // Initialize UI
        buttonCreate = findViewById(R.id.buttonCreateEvent)
        inputTeacherDist = findViewById(R.id.inputDistanceTeacher)
        inputGroupDist = findViewById(R.id.inputDistanceGroups)
        sliderTeacherDist = findViewById(R.id.sliderDistanceTeacher)
        sliderGroupDist = findViewById(R.id.sliderDistanceGroups)
        checkboxOne = findViewById(R.id.checkboxClassOne)
        checkboxTwo = findViewById(R.id.checkboxClassTwo)

        // Map button
        findViewById<Button>(R.id.buttonSetMapArea).setOnClickListener {
            val frag = MapSelectFragment { center, radius ->
                eventCenter = center
                eventRadius = radius
                validateForm()
                Toast.makeText(this, "Location set with radius $radius m", Toast.LENGTH_SHORT).show()
            }
            supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, frag)
                .addToBackStack(null)
                .commit()
        }

        // Sliders and text sync for teacher distance (2–50m)
        sliderTeacherDist.max = 48
        sliderTeacherDist.progress = 0
        inputTeacherDist.setText("2")

        sliderTeacherDist.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, value: Int, fromUser: Boolean) {
                val real = value + 2
                inputTeacherDist.setText(real.toString())
                teacherDistance = real
                validateForm()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        inputTeacherDist.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val v = s.toString().toIntOrNull()
                if (v != null && v in 2..50) {
                    sliderTeacherDist.progress = v - 2
                    teacherDistance = v
                }
                validateForm()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Sliders and text sync for group distance (2–10m)
        sliderGroupDist.max = 8
        sliderGroupDist.progress = 0
        inputGroupDist.setText("2")

        sliderGroupDist.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, value: Int, fromUser: Boolean) {
                val real = value + 2
                inputGroupDist.setText(real.toString())
                groupDistance = real
                validateForm()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        inputGroupDist.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val v = s.toString().toIntOrNull()
                if (v != null && v in 2..10) {
                    sliderGroupDist.progress = v - 2
                    groupDistance = v
                }
                validateForm()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Checkbox tracking
        val checkboxes = listOf(checkboxOne, checkboxTwo)
        checkboxes.forEach { checkbox ->
            checkbox.setOnCheckedChangeListener { _, _ ->
                selectedClasses.clear()
                checkboxes.filter { it.isChecked }.mapTo(selectedClasses) { it.text.toString() }
                validateForm()
            }
        }

        // Final create event
        buttonCreate.setOnClickListener {
            Toast.makeText(this, "Event Created!", Toast.LENGTH_LONG).show()
            finish()
        }

        validateForm()
    }

    private fun validateForm() {
        val valid = eventName != null &&
                eventCenter != null &&
                eventRadius in 50..10000 &&
                teacherDistance in 2..50 &&
                groupDistance in 2..10 &&
                selectedClasses.isNotEmpty()
        buttonCreate.isEnabled = valid
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_event_create, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_create_event -> {
                eventFormVisible = !eventFormVisible
                creationContainer.visibility = if (eventFormVisible) View.VISIBLE else View.GONE
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}
