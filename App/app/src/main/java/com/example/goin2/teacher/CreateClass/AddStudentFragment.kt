package com.example.goin2.teacher.CreateClass

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.goin2.R

class AddStudentFragment(private val onSubmit: (TeacherClassManagementActivity.Student) -> Unit) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_add_student, null)
        val firstInput = view.findViewById<EditText>(R.id.firstNameInput)
        val lastInput = view.findViewById<EditText>(R.id.lastNameInput)

        return AlertDialog.Builder(requireContext())
            .setTitle("Add Student")
            .setView(view)
            .setPositiveButton("Submit") { _, _ ->
                val first = firstInput.text.toString().trim()
                val last = lastInput.text.toString().trim()
                if (first.isNotEmpty() && last.isNotEmpty()) {
                    onSubmit(TeacherClassManagementActivity.Student(first, last))
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
    }
}
