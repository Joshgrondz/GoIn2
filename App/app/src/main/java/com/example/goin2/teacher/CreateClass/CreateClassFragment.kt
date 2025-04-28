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

class CreateClassFragment(private val onSuccess: () -> Unit) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_create_class, null)
        val classNameInput = view.findViewById<EditText>(R.id.classNameInput)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Create Class")
            .setView(view)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            button?.setOnClickListener {
                val name = classNameInput.text.toString().trim()
                if (name.isEmpty()) {
                    Toast.makeText(context, "Enter class name", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                ApiClient.createClass(MainActivity.currentTeacherId, name) { success ->
                    if (success) {
                        Toast.makeText(context, "Class created", Toast.LENGTH_SHORT).show()
                        onSuccess()
                        dialog.dismiss()
                    } else {
                        Toast.makeText(context, "Failed to create class", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Submit") { _, _ -> }
        return dialog
    }
}
