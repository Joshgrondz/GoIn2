package com.example.goin2.teacher.CreateEvent

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.goin2.R
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.maps.android.SphericalUtil
import android.location.Geocoder
import android.location.Address
import android.os.Build
import androidx.annotation.RequiresApi
import java.util.Locale

class MapSelectFragment(private val onSubmit: (LatLng, Int) -> Unit) : Fragment() {

    private lateinit var map: GoogleMap
    private var marker: Marker? = null
    private var circle: Circle? = null
    private var radius: Int = 500

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_map_select, container, false)

        val searchInput = view.findViewById<EditText>(R.id.mapSearchInput)
        val searchButton = view.findViewById<Button>(R.id.buttonSearchLocation)
        val cancelButton = view.findViewById<Button>(R.id.buttonCancelMap)
        val confirmButton = view.findViewById<Button>(R.id.buttonConfirmMap)
        val centerButton = view.findViewById<Button>(R.id.buttonDropPin)
        val seekBar = view.findViewById<SeekBar>(R.id.mapRadiusSeekBar)
        val radiusInput = view.findViewById<EditText>(R.id.mapRadiusInput)

        val mapFragment = childFragmentManager.findFragmentById(R.id.fullScreenMap) as SupportMapFragment
        mapFragment.getMapAsync { gMap ->
            map = gMap
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(40.0, -79.0), 14f))
        }

        searchButton.setOnClickListener {
            val query = searchInput.text.toString().trim()
            if (query.isNotEmpty()) {
                val geocoder = Geocoder(requireContext(), Locale.getDefault())
                geocoder.getFromLocationName(query, 1, object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) {
                        if (addresses.isNotEmpty()) {
                            val loc = addresses[0]
                            val latLng = LatLng(loc.latitude, loc.longitude)
                            requireActivity().runOnUiThread {
                                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                            }
                        }
                    }

                    override fun onError(errorMessage: String?) {}
                })
            }
        }

        centerButton.setOnClickListener {
            val center = map.cameraPosition.target
            marker?.remove()
            circle?.remove()

            marker = map.addMarker(MarkerOptions().position(center).title("Event Center"))
            drawCircle(center)
            zoomToCircle(center)
        }

        seekBar.max = 450
        seekBar.progress = radius - 50
        radiusInput.setText(radius.toString())

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, value: Int, fromUser: Boolean) {
                radius = value + 50
                radiusInput.setText(radius.toString())
                circle?.radius = radius.toDouble()
                marker?.position?.let { zoomToCircle(it) }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        radiusInput.setOnEditorActionListener { _, _, _ ->
            val r = radiusInput.text.toString().toIntOrNull()
            if (r != null && r in 10..10000) {
                radius = r
                seekBar.progress = (r - 50).coerceIn(0, seekBar.max)
                circle?.radius = radius.toDouble()
                marker?.position?.let { zoomToCircle(it) }
            }
            false
        }

        cancelButton.setOnClickListener {
            parentFragmentManager.popBackStack() // Close overlay
        }

        confirmButton.setOnClickListener {
            val pos = marker?.position
            if (pos != null) {
                onSubmit(pos, radius) // ✅ Send result back
                parentFragmentManager.popBackStack()
            } else {
                Toast.makeText(requireContext(), "No location selected", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun drawCircle(center: LatLng) {
        circle = map.addCircle(
            CircleOptions()
                .center(center)
                .radius(radius.toDouble())
                .strokeColor(Color.BLUE)
                .fillColor(0x220000FF)
                .strokeWidth(2f)
        )
    }

    private fun zoomToCircle(center: LatLng) {
        val bounds = LatLngBounds.builder()
            .include(SphericalUtil.computeOffset(center, radius.toDouble(), 0.0))
            .include(SphericalUtil.computeOffset(center, radius.toDouble(), 90.0))
            .include(SphericalUtil.computeOffset(center, radius.toDouble(), 180.0))
            .include(SphericalUtil.computeOffset(center, radius.toDouble(), 270.0))
            .build()

        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
    }
}
