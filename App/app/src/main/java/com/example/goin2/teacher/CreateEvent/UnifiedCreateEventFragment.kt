package com.example.goin2.teacher.CreateEvent

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import com.example.goin2.R
import com.example.goin2.API_and_location.ApiClient
import com.example.goin2.main.MainActivity
import com.google.android.gms.maps.model.LatLng
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*

class UnifiedCreateEventFragment(private val onComplete: () -> Unit) : Fragment() {

    private lateinit var root: FrameLayout
    private lateinit var scrollView: ScrollView
    private lateinit var content: LinearLayout

    private var eventName = ""
    private var eventLocationText = ""
    private var center: LatLng? = null
    private var radius: Int = 0
    private var teacherRadius: Int = 10
    private var pairDistance: Int = 10
    private val selectedClassIds = mutableSetOf<Int>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_unified_create_event, container, false)
        root = view.findViewById(R.id.eventFlowRoot)
        scrollView = view.findViewById(R.id.scrollView)
        content = view.findViewById(R.id.flowMainContainer)

        loadStepOne()
        return view
    }

    private fun clearContent() {
        content.removeAllViews()
    }

    private fun loadStepOne() {
        clearContent()

        val nameInput = EditText(requireContext()).apply {
            hint = "Event Name"
            setTextColor(resources.getColor(android.R.color.black))
            setHintTextColor(resources.getColor(android.R.color.darker_gray))
            setBackgroundColor(resources.getColor(android.R.color.white))
            setPadding(24)
        }

        val locInput = EditText(requireContext()).apply {
            hint = "Location (e.g., UPJ)"
            setTextColor(resources.getColor(android.R.color.black))
            setHintTextColor(resources.getColor(android.R.color.darker_gray))
            setBackgroundColor(resources.getColor(android.R.color.white))
            setPadding(24)
        }

        val nextBtn = Button(requireContext()).apply {
            text = "Enter Names"
            textSize = 18f
            setBackgroundColor(resources.getColor(R.color.purple_500))
            setTextColor(resources.getColor(android.R.color.white))
        }

        content.addView(nameInput)
        content.addView(locInput)
        content.addView(nextBtn)

        nextBtn.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val location = locInput.text.toString().trim()
            if (name.isEmpty() || location.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill out both fields", Toast.LENGTH_SHORT).show()
            } else {
                eventName = name
                eventLocationText = location
                openMapOverlay()
            }
        }
    }

    private fun openMapOverlay() {
        val mapFragment = MapSelectFragment { pos, r ->
            center = pos
            radius = r
            loadStepTwo()
        }

        requireActivity().supportFragmentManager.beginTransaction()
            .add(android.R.id.content, mapFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun loadStepTwo() {
        clearContent()

        val teacherLabel = TextView(requireContext()).apply {
            text = "Max Distance from Teacher (10–50m)"
            textSize = 20f
            setTextColor(resources.getColor(android.R.color.white))
        }

        val teacherSeek = SeekBar(requireContext()).apply {
            max = 40 // 10–50
            progress = teacherRadius - 10
        }

        val teacherInput = EditText(requireContext()).apply {
            setText(teacherRadius.toString())
            setBackgroundColor(resources.getColor(android.R.color.white))
            setTextColor(resources.getColor(android.R.color.black))
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setPadding(20)
        }

        val pairLabel = TextView(requireContext()).apply {
            text = "Max Distance Between Groups (10–20m)"
            textSize = 20f
            setTextColor(resources.getColor(android.R.color.white))
        }

        val pairSeek = SeekBar(requireContext()).apply {
            max = 10 // 10–20
            progress = pairDistance - 10
        }

        val pairInput = EditText(requireContext()).apply {
            setText(pairDistance.toString())
            setBackgroundColor(resources.getColor(android.R.color.white))
            setTextColor(resources.getColor(android.R.color.black))
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setPadding(20)
        }

        val nextBtn = Button(requireContext()).apply {
            text = "Submit"
            textSize = 20f
            setBackgroundColor(resources.getColor(R.color.purple_500))
            setTextColor(resources.getColor(android.R.color.white))
        }

        teacherSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, value: Int, fromUser: Boolean) {
                teacherRadius = value + 10
                teacherInput.setText(teacherRadius.toString())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        pairSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, value: Int, fromUser: Boolean) {
                pairDistance = value + 10
                pairInput.setText(pairDistance.toString())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        nextBtn.setOnClickListener {
            loadStepThree()
        }

        content.addView(teacherLabel)
        content.addView(teacherSeek)
        content.addView(teacherInput)
        content.addView(pairLabel)
        content.addView(pairSeek)
        content.addView(pairInput)
        content.addView(nextBtn)
    }

    private fun loadStepThree() {
        clearContent()

        val title = TextView(requireContext()).apply {
            text = "Select Classes for this Event:"
            setTextColor(resources.getColor(android.R.color.white))
            textSize = 18f
            setPadding(0, 0, 0, 16)
        }

        val checkboxContainer = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
        }

        val submitButton = Button(requireContext()).apply {
            text = "Submit Event"
            setBackgroundColor(resources.getColor(R.color.teal_700))
            setTextColor(resources.getColor(android.R.color.white))
        }

        content.addView(title)
        content.addView(checkboxContainer)
        content.addView(submitButton)

        ApiClient.getClassesByTeacher(MainActivity.currentTeacherId) { classList ->
            classList.forEach { (classId, className) ->
                requireActivity().runOnUiThread {
                    val box = CheckBox(requireContext()).apply {
                        text = className
                        setTextColor(resources.getColor(android.R.color.white))
                        setOnCheckedChangeListener { _, checked ->
                            if (checked) selectedClassIds.add(classId)
                            else selectedClassIds.remove(classId)
                        }
                    }
                    checkboxContainer.addView(box)
                }
            }
        }

        submitButton.setOnClickListener {
            submitEventCreation()
        }
    }

    private fun submitEventCreation() {
        val pos = center
        if (eventName.isEmpty() || eventLocationText.isEmpty() || pos == null || selectedClassIds.isEmpty()) {
            Toast.makeText(requireContext(), "Missing fields", Toast.LENGTH_SHORT).show()
            return
        }

        ApiClient.createGeoFence(
            radius, teacherRadius, pairDistance, pos.latitude, pos.longitude
        ) { geoId ->
            if (geoId == null) {
                Toast.makeText(requireContext(), "Geofence creation failed", Toast.LENGTH_SHORT).show()
                return@createGeoFence
            }

            val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            ApiClient.createEvent(
                eventName, today, eventLocationText, MainActivity.currentTeacherId, geoId
            ) { eventId ->
                if (eventId == null) {
                    Toast.makeText(requireContext(), "Event creation failed", Toast.LENGTH_SHORT).show()
                    return@createEvent
                }

                var completed = 0
                var failed = false
                for (cid in selectedClassIds) {
                    ApiClient.linkClassToEvent(cid, eventId) { success ->
                        if (!success) failed = true
                        completed++
                        if (completed == selectedClassIds.size) {
                            val msg = if (failed) "Some classes failed" else "Event created!"
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                            onComplete()
                            requireActivity().supportFragmentManager.popBackStack()
                        }
                    }
                }
            }
        }
    }
}
