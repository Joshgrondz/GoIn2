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
import org.json.JSONObject

class EndGoIn2GroupFragment(
    private val eventId: Int,
    private val onSuccess: () -> Unit
) : Fragment() {

    private val selectedPairId = mutableListOf<Int>()
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
            if (selectedPairId.size == 1) {
                val pairId = selectedPairId.first()
                val pair = pairData[pairId] ?: return@setOnClickListener

                val payload = JSONObject().apply {
                    put("student1id", pair.first)
                    put("student2id", pair.second)
                    put("eventid", eventId)
                    put("status", false)
                }

                ApiClient.updateGoIn2Pair(pairId, payload) { success ->
                    requireActivity().runOnUiThread {
                        if (success) {
                            Toast.makeText(requireContext(), "Pair ended.", Toast.LENGTH_SHORT).show()
                            onSuccess()
                            parentFragmentManager.popBackStack()
                        } else {
                            Toast.makeText(requireContext(), "Failed to end pair.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        loadPairs(listLayout, submitButton)
        return root
    }

    private val pairData = mutableMapOf<Int, Pair<Int, Int>>() // pairId -> (student1id, student2id)

    private fun loadPairs(listLayout: LinearLayout, submitButton: Button) {
        ApiClient.getActivePairsForEvent(eventId) { response ->
            val pairs = JSONArray(response)

            for (i in 0 until pairs.length()) {
                val obj = pairs.getJSONObject(i)
                val pairId = obj.getInt("id")
                val id1 = obj.getInt("student1id")
                val id2 = obj.getInt("student2id")
                pairData[pairId] = id1 to id2

                ApiClient.getUserById(id1) { name1 ->
                    ApiClient.getUserById(id2) { name2 ->
                        if (name1 != null && name2 != null) {
                            val display = "${name1.first} ${name1.second} + ${name2.first} ${name2.second}"

                            requireActivity().runOnUiThread {
                                val checkbox = CheckBox(requireContext()).apply {
                                    text = display
                                    setTextColor(Color.BLACK)
                                    setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.purple_200))

                                    setOnCheckedChangeListener { cb, isChecked ->
                                        if (isChecked) {
                                            if (selectedPairId.isEmpty()) {
                                                selectedPairId.add(pairId)
                                            } else {
                                                cb.post {
                                                    cb.isChecked = false
                                                    Toast.makeText(context, "Select only one pair", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        } else {
                                            selectedPairId.remove(pairId)
                                        }

                                        submitButton.isEnabled = (selectedPairId.size == 1)
                                        updateCheckboxStates()
                                    }
                                }

                                checkboxRefs[pairId] = checkbox
                                listLayout.addView(checkbox)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateCheckboxStates() {
        val disableOthers = selectedPairId.size >= 1
        for ((id, box) in checkboxRefs) {
            box.isEnabled = !disableOthers || selectedPairId.contains(id)
        }
    }
}
