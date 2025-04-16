package com.example.goin2.teacher.CreateClass

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.goin2.R

class CreateClassFragment(private val onSubmit: (String) -> Unit) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_create_class, null)
        val classNameInput = view.findViewById<EditText>(R.id.classNameInput)

        return AlertDialog.Builder(requireContext())
            .setTitle("Create Class")
            .setView(view)
            .setPositiveButton("Submit") { _, _ ->
                val name = classNameInput.text.toString().trim()
                if (name.isNotEmpty()) {
                    onSubmit(name)
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
    }
}
