package com.example.goin2.teacher.ActiveEvent

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.goin2.API_and_location.ApiClient
import com.example.goin2.R
import org.json.JSONArray

class GoIn2GroupFragment(
    private val eventId: Int,
    private val onSuccess: () -> Unit
) : Fragment() {

    private val studentMap = mutableMapOf<Int, String>()
    private val selectedIds = mutableSetOf<Int>()
    private val checkboxRefs = mutableMapOf<Int, CheckBox>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = FrameLayout(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(
                (resources.displayMetrics.widthPixels * 0.9).toInt(),
                (resources.displayMetrics.heightPixels * 0.7).toInt(),
                Gravity.CENTER
            )
            setBackgroundColor(Color.BLACK)
        }

        val scrollView = ScrollView(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            ).apply { bottomMargin = 120 }
        }

        val listLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        scrollView.addView(listLayout)
        root.addView(scrollView)

        val submitButton = Button(requireContext()).apply {
            text = "Submit"
            isEnabled = false
            setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.purple_500))
            setTextColor(Color.WHITE)
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM or Gravity.END
            ).apply {
                setMargins(0, 0, 48, 48)
            }
        }

        root.addView(submitButton)

        submitButton.setOnClickListener {
            if (selectedIds.size == 2) {
                val (id1, id2) = selectedIds.toList()

                // Before creating, validate against current active pairs
                ApiClient.getActivePairsForEvent(eventId) { response ->
                    val pairs = JSONArray(response)

                    var conflict = false

                    for (i in 0 until pairs.length()) {
                        val obj = pairs.getJSONObject(i)
                        val existing1 = obj.getInt("student1id")
                        val existing2 = obj.getInt("student2id")

                        if (existing1 == id1 || existing2 == id1 || existing1 == id2 || existing2 == id2) {
                            conflict = true
                            break
                        }
                    }

                    requireActivity().runOnUiThread {
                        if (conflict) {
                            Toast.makeText(requireContext(), "❌ One or both students are already in a group", Toast.LENGTH_SHORT).show()
                        } else {
                            // No conflict, safe to create
                            ApiClient.createGoIn2Pair(id1, id2, eventId) { success ->
                                requireActivity().runOnUiThread {
                                    if (success) {
                                        Toast.makeText(requireContext(), "Pair created!", Toast.LENGTH_SHORT).show()
                                        onSuccess()
                                        parentFragmentManager.popBackStack()
                                    } else {
                                        Toast.makeText(requireContext(), "❌ Pairing failed", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }


        loadStudents(listLayout, submitButton)

        return root
    }

    private fun loadStudents(listLayout: LinearLayout, submitButton: Button) {
        ApiClient.getAllClassEvents { classEventBody ->
            val matchingClassIds = mutableSetOf<Int>()
            val entries = JSONArray(classEventBody)

            for (i in 0 until entries.length()) {
                val obj = entries.getJSONObject(i)
                if (obj.getInt("eventid") == eventId) {
                    matchingClassIds.add(obj.getInt("classid"))
                }
            }

            if (matchingClassIds.isEmpty()) return@getAllClassEvents

            ApiClient.getAllClasses { allClassBody ->
                val allClasses = JSONArray(allClassBody)

                for (i in 0 until allClasses.length()) {
                    val classObj = allClasses.getJSONObject(i)
                    val classId = classObj.getInt("id")

                    if (matchingClassIds.contains(classId)) {
                        ApiClient.getClassRoster(classId) { studentIds ->
                            studentIds.forEach { studentId ->
                                ApiClient.getUserById(studentId) { namePair ->
                                    if (namePair != null) {
                                        val fullName = "${namePair.first} ${namePair.second}"
                                        studentMap[studentId] = fullName

                                        requireActivity().runOnUiThread {
                                            val checkbox = CheckBox(requireContext()).apply {
                                                text = fullName
                                                setTextColor(Color.BLACK)
                                                setBackgroundColor(
                                                    ContextCompat.getColor(
                                                        requireContext(),
                                                        R.color.purple_200
                                                    )
                                                )

                                                setOnCheckedChangeListener { cb, isChecked ->
                                                    if (isChecked) {
                                                        if (selectedIds.size < 2) {
                                                            selectedIds.add(studentId)
                                                        } else {
                                                            cb.post {
                                                                cb.isChecked = false
                                                                Toast.makeText(
                                                                    requireContext(),
                                                                    "Only 2 students can be selected",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                            }
                                                        }
                                                    } else {
                                                        selectedIds.remove(studentId)
                                                    }

                                                    submitButton.isEnabled = (selectedIds.size == 2)
                                                    updateCheckboxStates()
                                                }
                                            }

                                            checkboxRefs[studentId] = checkbox
                                            listLayout.addView(checkbox)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    private fun updateCheckboxStates() {
        val disableOthers = selectedIds.size >= 2
        for ((id, box) in checkboxRefs) {
            box.isEnabled = !disableOthers || selectedIds.contains(id)
        }
    }

}
