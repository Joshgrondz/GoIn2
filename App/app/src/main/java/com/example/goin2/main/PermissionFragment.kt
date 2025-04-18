package com.example.goin2.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.goin2.R

class PermissionFragment : Fragment() {

    // 1. Request POST_NOTIFICATIONS (Android 13+)
    private val notificationRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { requestFineLocation() }

    // 2. Request ACCESS_FINE_LOCATION
    private val fineRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            requestBackgroundLocation()
        } else {
            showSettings()
        }
    }

    // 3. Request ACCESS_BACKGROUND_LOCATION
    private val backgroundRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            parentFragmentManager.beginTransaction().remove(this).commit()
        } else {
            showSettings()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.permission_fragment, container, false)

        view.findViewById<Button>(R.id.permission_ok_button).setOnClickListener {
            requestNotificationPermission()
        }

        return view
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationRequest.launch(Manifest.permission.POST_NOTIFICATIONS)
                return
            }
        }
        requestFineLocation()
    }

    private fun requestFineLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            fineRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            requestBackgroundLocation()
        }
    }

    private fun requestBackgroundLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            backgroundRequest.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        } else {
            parentFragmentManager.beginTransaction().remove(this).commit()
        }
    }

    private fun showSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.fromParts("package", requireActivity().packageName, null)
        startActivity(intent)
    }
}
