package com.example.triphub.activities

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.triphub.R
import com.example.triphub.databinding.ActivityMapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*

class MapActivity : BaseActivity<ActivityMapBinding>(), OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener, GoogleMap.OnMapClickListener,
    GoogleMap.OnInfoWindowClickListener, GoogleMap.OnPolylineClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var marker: Marker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setOnButtonClickedListeners()

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onResume() {
        super.onResume()
        val mapInputTypes = resources.getStringArray(R.array.mapInputTypes)
        val arrayAdapter = ArrayAdapter(this, R.layout.dropdown_map_input_types, mapInputTypes)
        binding.actvMapInputType.setAdapter(arrayAdapter)
    }

    private fun setOnButtonClickedListeners() {
        binding.ibArrowUp.setOnClickListener {
            binding.ibArrowUp.visibility = View.GONE
            binding.llTools.visibility = View.VISIBLE
        }
        binding.ibArrowDown.setOnClickListener {
            binding.llTools.visibility = View.GONE
            binding.ibArrowUp.visibility = View.VISIBLE
        }
    }

    override fun getViewBinding() = ActivityMapBinding.inflate(layoutInflater)

    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap!!
        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
        val sydneyLoc = LatLng(-34.0, 151.0)
        marker = mMap.addMarker(
            MarkerOptions()
                .position(sydneyLoc)
                .draggable(true)
                .rotation(90f)
                .zIndex(1f)
                .snippet("Additional text that is displayed below title")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_user_placeholder_image))
                .title("Marker in Sydney")
        )

        val otherLoc = LatLng(-30.0, 130.0)
        mMap.addMarker(
            MarkerOptions()
                .position(otherLoc)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
        )

        mMap.setOnMarkerClickListener(this)
        mMap.setOnMarkerDragListener(this)
        mMap.setOnMapClickListener(this)
        mMap.setOnInfoWindowClickListener(this)
        mMap.setOnPolylineClickListener(this)

        // Polyline
        val pattern = mutableListOf(Dot(), Gap(10f))

        val m1 = LatLng(20.0, 40.0)
        val m2 = LatLng(65.0, 45.0)
        val m3 = LatLng(30.0, 50.0)
        val polylineOptions = PolylineOptions()
            .add(m1)
            .add(m2)
            .add(m3)
            .color(Color.CYAN)
            .pattern(pattern)
        val polyline = mMap.addPolyline(polylineOptions)
        polyline.isClickable = true

        // Polygon
        val m11 = LatLng(10.0, 10.0)
        val m22 = LatLng(20.0, 10.0)
        val m33 = LatLng(20.0, 20.0)
        val m44 = LatLng(10.0, 20.0)
        val polygonOptions = PolygonOptions()
            .addAll(mutableListOf(m11, m22, m33, m44))
            .fillColor(0x7F00FF00)
            .addHole(
                mutableListOf(
                    LatLng(14.0, 14.0),
                    LatLng(15.0, 14.0),
                    LatLng(15.0, 15.0),
                    LatLng(14.0, 15.0)
                )
            )
        val polygon = mMap.addPolygon(polygonOptions)

        // Circle
        val circleOptions = CircleOptions()
            .center(LatLng(50.0, 50.0))
            .radius(1000_000.0)  // in meters
        val circle = mMap.addCircle(circleOptions)

        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydneyLoc))
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        Toast.makeText(this, "My position ${marker.position}", Toast.LENGTH_SHORT).show()

        return false
    }

    override fun onMarkerDragStart(marker: Marker) {

    }

    override fun onMarkerDrag(marker: Marker) {

    }

    override fun onMarkerDragEnd(marker: Marker) {
        Toast.makeText(this, "New position ${marker.position}", Toast.LENGTH_SHORT).show()
    }

    override fun onMapClick(latLng: LatLng) {
        Toast.makeText(this, "Map clicked at $latLng", Toast.LENGTH_SHORT).show()
    }

    override fun onInfoWindowClick(marker: Marker) {
        Toast.makeText(this, "You clicked info window!", Toast.LENGTH_SHORT).show()
    }

    override fun onPolylineClick(polyline: Polyline) {
        Toast.makeText(this, "Polyline clicked", Toast.LENGTH_SHORT).show()
    }
}