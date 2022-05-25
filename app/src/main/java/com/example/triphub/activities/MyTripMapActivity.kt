package com.example.triphub.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.triphub.BuildConfig
import com.example.triphub.R
import com.example.triphub.databinding.ActivityMyTripMapBinding
import com.example.triphub.utils.Constants
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode

class MyTripMapActivity : BaseActivity<ActivityMyTripMapBinding>(), OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener, GoogleMap.OnMapClickListener,
    GoogleMap.OnInfoWindowClickListener, GoogleMap.OnPolylineClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var marker: Marker

    private var mMapInputType: String? = null
    private var mTempLocation: LatLng? = null
    private var mTempMarker: Marker? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setOnButtonClickedListeners()
        setUpTaskBarNavigation()

        if (!Places.isInitialized()) {
            Places.initialize(this@MyTripMapActivity, BuildConfig.GOOGLE_API_KEY)
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun setUpTaskBarNavigation() {
        binding.ivPeople.setOnClickListener {
            startActivity(Intent(this, MyTripPeopleActivity::class.java))
            finish()
        }
        binding.ivBoard.setOnClickListener {
            startActivity(Intent(this, MyTripBoardActivity::class.java))
            finish()
        }
        binding.ivChat.setOnClickListener {
            startActivity(Intent(this, MyTripChatActivity::class.java))
            finish()
        }
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
        binding.actvMapInputType.setOnItemClickListener { adapterView, _, position, _ ->
            mMapInputType = adapterView.getItemAtPosition(position).toString()
            setUpToolsDependingOnMapInputType()
        }
        binding.etFindPlace.setOnClickListener {
            try {
                val fields = listOf(
                    Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG,
                    Place.Field.ADDRESS
                )
                val intent =
                    Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                        .build(this@MyTripMapActivity)
                placesResultLauncher.launch(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        binding.btnGoToPlace.setOnClickListener {
            if (mTempLocation != null) {
                mTempMarker?.remove()
                mTempMarker = mMap.addMarker(
                    MarkerOptions()
                        .position(mTempLocation!!)
                )
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mTempLocation, 16f))
            }
        }
    }

    private val placesResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val place: Place = Autocomplete.getPlaceFromIntent(result.data!!)
                mTempLocation = place.latLng!!
                binding.etFindPlace.setText(place.address)

                mTempMarker?.remove()
                mTempMarker = mMap.addMarker(
                    MarkerOptions()
                        .position(mTempLocation!!)
                )
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mTempLocation, 16f))
            }
        }

    private fun setUpToolsDependingOnMapInputType() {
        when (mMapInputType) {
            Constants.Navigation.MARKER -> {

            }
            Constants.Navigation.POLYLINE -> {

            }
            Constants.Navigation.POLYGON -> {

            }
            Constants.Navigation.CIRCLE -> {

            }
            else -> {
                throw Exception("setUpToolsDependingOnMapInputType() called when mMapInputType is NULL")
            }
        }
    }


    override fun getViewBinding() = ActivityMyTripMapBinding.inflate(layoutInflater)

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
            .strokePattern(pattern)
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

    override fun onMapClick(latLng: LatLng) {
        mTempMarker?.remove()
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        if (marker == mTempMarker) {
            mTempMarker?.remove()
        }

        return false
    }

    override fun onMarkerDragStart(marker: Marker) {

    }

    override fun onMarkerDrag(marker: Marker) {

    }

    override fun onMarkerDragEnd(marker: Marker) {
        Toast.makeText(this, "New position ${marker.position}", Toast.LENGTH_SHORT).show()
    }

    override fun onInfoWindowClick(marker: Marker) {
        Toast.makeText(this, "You clicked info window!", Toast.LENGTH_SHORT).show()
    }

    override fun onPolylineClick(polyline: Polyline) {
        Toast.makeText(this, "Polyline clicked", Toast.LENGTH_SHORT).show()
    }
}