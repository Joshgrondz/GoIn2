package com.example.goin2.teacher.CreateClass

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.goin2.API_and_location.ApiClient
import com.example.goin2.R
import com.example.goin2.main.MainActivity

class AddStudentFragment(
    private val classId: Int,
    private val onRefresh: () -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_add_student, null)
        val firstInput = view.findViewById<EditText>(R.id.firstNameInput)
        val lastInput = view.findViewById<EditText>(R.id.lastNameInput)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Add Student")
            .setView(view)
            .setPositiveButton("Submit", null)  // Set null to prevent auto-dismiss
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener {
                val first = firstInput.text.toString().trim()
                val last = lastInput.text.toString().trim()

                if (first.isEmpty() || last.isEmpty()) {
                    Toast.makeText(context, "Please enter both names", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                ApiClient.createStudent(requireContext(), first, last) { newId ->
                    if (newId != null) {
                        ApiClient.addStudentToClass(classId, newId) { success ->
                            if (success) {
                                Toast.makeText(context, "Student added", Toast.LENGTH_SHORT).show()
                                onRefresh()
                                dialog.dismiss()  // ✅ Only dismiss if everything succeeded
                            } else {
                                Toast.makeText(context, "Failed to add to class", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(context, "Failed to create student", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        return dialog
    }
}
