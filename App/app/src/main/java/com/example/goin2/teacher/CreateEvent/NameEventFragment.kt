package com.example.goin2.teacher.CreateEvent

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.goin2.R

class NameEventFragment(private val onSubmit: (String) -> Unit) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_name_event, null)
        val input = view.findViewById<EditText>(R.id.eventNameInput)

        return AlertDialog.Builder(requireContext())
            .setTitle("Name Your Event")
            .setView(view)
            .setPositiveButton("Submit") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    onSubmit(name)
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
    }
}
