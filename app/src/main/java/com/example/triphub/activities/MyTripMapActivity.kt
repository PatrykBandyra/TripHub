package com.example.triphub.activities

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.triphub.BuildConfig
import com.example.triphub.R
import com.example.triphub.databinding.ActivityMyTripMapBinding
import com.example.triphub.firebase.MyTripFireStore
import com.example.triphub.models.*
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
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.flag.BubbleFlag
import com.skydoves.colorpickerview.flag.FlagMode
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener


class MyTripMapActivity : BaseActivity<ActivityMyTripMapBinding>(), OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener, GoogleMap.OnMapClickListener,
    GoogleMap.OnInfoWindowClickListener, GoogleMap.OnPolylineClickListener,
    GoogleMap.OnCircleClickListener, GoogleMap.OnPolygonClickListener {

    private lateinit var mTrip: MyTrip
    private lateinit var mUser: User

    private lateinit var mMap: GoogleMap

    private var mMapInputType: String? = null
    private var mEditMode: Boolean = false
    private var mAddMode: Boolean = true
    private var mTempLocation: LatLng? = null
    private var mTempMarker: Marker? = null

    // Marker
    private enum class MarkerColor {
        ORANGE, RED, GREEN
    }

    private var mMarkerColor = MarkerColor.ORANGE
    private lateinit var mMarkerColorDescriptor: BitmapDescriptor

    // Circle
    private var mCircleFillColor: Int = Constants.Map.CIRCLE_DEFAULT_FILL_COLOR
    private var mCircleStrokeColor: Int = Constants.Map.CIRCLE_DEFAULT_STROKE_COLOR

    // Polygon
    private var mIsPolygonBeingCreated: Boolean = false
    private var mPolygonFillColor: Int = Constants.Map.POLYGON_DEFAULT_FILL_COLOR
    private var mPolygonStrokeColor: Int = Constants.Map.POLYGON_DEFAULT_STROKE_COLOR
    private var mPolygonLineType: PolylineType = PolylineType.SOLID
    private var mCurrentPolygonPoints: ArrayList<LatLng>? = null
    private var mTempPolygonMarkers: ArrayList<Marker>? = null

    private enum class PolylineType {
        SOLID, DASH, DOT
    }

    // Polyline
    private var mIsPolylineBeingCreated: Boolean = false
    private var mPolylineColor: Int = Constants.Map.POLYLINE_DEFAULT_COLOR
    private var mPolylineType: PolylineType = PolylineType.SOLID
    private var mCurrentPolyline: Polyline? = null
    private var mCurrentPolylinePoints: ArrayList<LatLng>? = null

    private val eventListener: EventListener<QuerySnapshot> =
        EventListener<QuerySnapshot> { value, error ->
            if (error != null) {
                return@EventListener
            }
            if (value != null) {
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.hasExtra(Constants.Intent.TRIP)) {
            mTrip = intent.getParcelableExtra(Constants.Intent.TRIP)!!
        } else {
            throw Exception("${this::class.java.simpleName}: No MyTrip object provided")
        }

        if (intent.hasExtra(Constants.Intent.USER_DATA)) {
            mUser = intent.getParcelableExtra(Constants.Intent.USER_DATA)!!
        } else {
            throw Exception("${this::class.java.simpleName}: No User object provided")
        }

        setOnButtonClickedListeners()
        setUpTaskBarNavigation()

        binding.ibFillColor.setBackgroundColor(mCircleFillColor)
        binding.ibStrokeColor.setBackgroundColor(mCircleStrokeColor)

        if (!Places.isInitialized()) {
            Places.initialize(this@MyTripMapActivity, BuildConfig.GOOGLE_API_KEY)
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun setUpTaskBarNavigation() {
        binding.ivBoard.setOnClickListener {
            val intent = Intent(this, MyTripBoardActivity::class.java)
            intent.putExtra(Constants.Intent.TRIP, mTrip)
            intent.putExtra(Constants.Intent.USER_DATA, mUser)
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
        }
        binding.ivEvent.setOnClickListener {
            val intent = Intent(this, MyTripEventActivity::class.java)
            intent.putExtra(Constants.Intent.TRIP, mTrip)
            intent.putExtra(Constants.Intent.USER_DATA, mUser)
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
        }
        binding.ivPeople.setOnClickListener {
            val intent = Intent(this, MyTripPeopleActivity::class.java)
            intent.putExtra(Constants.Intent.TRIP, mTrip)
            intent.putExtra(Constants.Intent.USER_DATA, mUser)
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
        }
        binding.ivChat.setOnClickListener {
            val intent = Intent(this, MyTripChatActivity::class.java)
            intent.putExtra(Constants.Intent.TRIP, mTrip)
            intent.putExtra(Constants.Intent.USER_DATA, mUser)
            startActivity(intent)
            overridePendingTransition(0, 0)
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
            val mapInputType = adapterView.getItemAtPosition(position).toString()
            if (mapInputType != mMapInputType) {
                mMapInputType = mapInputType
                changeUIBasedOnInputType()
            }
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
        binding.switchMapMode.setOnCheckedChangeListener { _, isChecked ->
            mEditMode = isChecked
        }
        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.radioAdd.id -> {
                    mAddMode = true
                }
                binding.radioRemove.id -> {
                    mAddMode = false
                }
            }
        }

        // Marker click listeners
        binding.radioGroupMarkerColor.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.radioMarkerColorOrange.id -> {
                    mMarkerColorDescriptor =
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                    mMarkerColor = MarkerColor.ORANGE
                }
                binding.radioMarkerColorRed.id -> {
                    mMarkerColorDescriptor =
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                    mMarkerColor = MarkerColor.RED
                }
                binding.radioMarkerColorGreen.id -> {
                    mMarkerColorDescriptor =
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                    mMarkerColor = MarkerColor.GREEN
                }
            }
        }

        // Circle click listeners
        binding.ibFillColor.setOnClickListener {
            showColorPickerDialog("Select Circle Fill Color") { color: Int ->
                mCircleFillColor = color
                binding.ibFillColor.setBackgroundColor(mCircleFillColor)
            }
        }
        binding.ibStrokeColor.setOnClickListener {
            showColorPickerDialog("Select Circle Stroke Color") { color: Int ->
                mCircleStrokeColor = color
                binding.ibStrokeColor.setBackgroundColor(mCircleStrokeColor)
            }
        }

        // Polyline click listeners
        binding.ibPolylineColor.setOnClickListener {
            showColorPickerDialog("Select Polyline Color") { color: Int ->
                mPolylineColor = color
                binding.ibPolylineColor.setBackgroundColor(mPolylineColor)
            }
        }
        binding.radioGroupPolylineType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.radioPolylineTypeSolid.id -> {
                    mPolylineType = PolylineType.SOLID
                }
                binding.radioPolylineTypeDash.id -> {
                    mPolylineType = PolylineType.DASH
                }
                binding.radioPolylineTypeDot.id -> {
                    mPolylineType = PolylineType.DOT
                }
            }
        }
        binding.btnPolylineStartStop.setOnClickListener {
            if (!mIsPolylineBeingCreated) {
                mIsPolylineBeingCreated = true
                binding.btnPolylineStartStop.text = getString(R.string.stop)
            } else {
                if (mCurrentPolylinePoints != null && mCurrentPolylinePoints!!.size >= 2) {
                    val points: ArrayList<MyPoint> =
                        ArrayList(mCurrentPolylinePoints!!.map { latLng ->
                            MyPoint(latitude = latLng.latitude, longitude = latLng.longitude)
                        })
                    val polyline = MyPolyline(
                        points = points,
                        color = mPolylineColor,
                        pattern = mPolylineType.toString()
                    )
                    mTrip.polylines.add(polyline)
                    MyTripFireStore().updatePolylines(mTrip)
                }
                mIsPolylineBeingCreated = false
                mCurrentPolyline = null
                mCurrentPolylinePoints = null
                binding.btnPolylineStartStop.text = getString(R.string.start)
            }
        }

        // Polygon click listeners
        binding.ibPolygonFillColor.setOnClickListener {
            showColorPickerDialog("Select Polygon Fill Color") { color: Int ->
                mPolygonFillColor = color
                binding.ibPolygonFillColor.setBackgroundColor(mPolygonFillColor)
            }
        }
        binding.ibPolygonStrokeColor.setOnClickListener {
            showColorPickerDialog("Select Polygon Stroke Color") { color: Int ->
                mPolygonStrokeColor = color
                binding.ibPolygonStrokeColor.setBackgroundColor(mPolygonStrokeColor)
            }
        }
        binding.radioGroupPolygonType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.radioPolygonTypeSolid.id -> {
                    mPolygonLineType = PolylineType.SOLID
                }
                binding.radioPolygonTypeDash.id -> {
                    mPolygonLineType = PolylineType.DASH
                }
                binding.radioPolygonTypeDot.id -> {
                    mPolygonLineType = PolylineType.DOT
                }
            }
        }
        binding.btnPolygonStartStop.setOnClickListener {
            if (!mIsPolygonBeingCreated) {
                mIsPolygonBeingCreated = true
                binding.btnPolygonStartStop.text = getString(R.string.stop)
            } else {
                if (mCurrentPolygonPoints != null && mCurrentPolygonPoints!!.size >= 3) {
                    var pattern: List<PatternItem>? = null
                    if (mPolygonLineType == PolylineType.DASH) {
                        pattern = mutableListOf(Dash(30f), Gap(10f))
                    } else if (mPolygonLineType == PolylineType.DOT) {
                        pattern = mutableListOf(Dot(), Gap(10f))
                    }

                    val polygonOptions = PolygonOptions()
                        .fillColor(mPolygonFillColor)
                        .strokeColor(mPolygonStrokeColor)
                        .clickable(true)
                        .addAll(mCurrentPolygonPoints!!.toMutableList())
                    if (pattern != null) {
                        polygonOptions.strokePattern(pattern)
                    }

                    mMap.addPolygon(polygonOptions)

                    val points: ArrayList<MyPoint> =
                        ArrayList(mCurrentPolygonPoints!!.map { latLng ->
                            MyPoint(latitude = latLng.latitude, longitude = latLng.longitude)
                        })
                    val polygon = MyPolygon(
                        points = points,
                        fillColor = mPolygonFillColor,
                        strokeColor = mPolygonStrokeColor,
                        strokePattern = mPolygonLineType.toString()
                    )
                    mTrip.polygons.add(polygon)
                    MyTripFireStore().updatePolygons(mTrip)
                } else {
                    showErrorSnackBar(R.string.polygons_at_least_3_points)
                }

                mTempPolygonMarkers?.forEach { marker ->
                    marker.remove()
                }
                mIsPolygonBeingCreated = false
                mCurrentPolygonPoints = null
                mTempPolygonMarkers = null
                binding.btnPolygonStartStop.text = getString(R.string.start)
            }
        }
    }

    private fun showColorPickerDialog(title: String, onColorSelected: (color: Int) -> Unit) {
        val colorDialog = ColorPickerDialog.Builder(this)
            .setTitle(title)
            .setPositiveButton(R.string.confirm, object : ColorEnvelopeListener {
                override fun onColorSelected(envelope: ColorEnvelope, fromUser: Boolean) {
                    onColorSelected(envelope.color)
                }
            })
            .setNegativeButton(R.string.cancel) { dialogInterface: DialogInterface, _: Int ->
                dialogInterface.dismiss()
            }
            .attachAlphaSlideBar(true)
            .attachBrightnessSlideBar(true)

        val bubbleFlag = BubbleFlag(this)
        bubbleFlag.flagMode = FlagMode.ALWAYS
        colorDialog.colorPickerView.flagView = bubbleFlag

        colorDialog.show()
    }

    private fun changeUIBasedOnInputType() {
        when (mMapInputType) {
            Constants.Navigation.MARKER -> {
                binding.llMarkerType.visibility = View.VISIBLE
                binding.llPolygonType.visibility = View.GONE
                binding.llPolylineType.visibility = View.GONE
                binding.llCircleType.visibility = View.GONE
            }
            Constants.Navigation.POLYLINE -> {
                binding.llMarkerType.visibility = View.GONE
                binding.llPolygonType.visibility = View.GONE
                binding.llPolylineType.visibility = View.VISIBLE
                binding.llCircleType.visibility = View.GONE
            }
            Constants.Navigation.POLYGON -> {
                binding.llMarkerType.visibility = View.GONE
                binding.llPolygonType.visibility = View.VISIBLE
                binding.llPolylineType.visibility = View.GONE
                binding.llCircleType.visibility = View.GONE
            }
            Constants.Navigation.CIRCLE -> {
                binding.llMarkerType.visibility = View.GONE
                binding.llPolygonType.visibility = View.GONE
                binding.llPolylineType.visibility = View.GONE
                binding.llCircleType.visibility = View.VISIBLE
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

    override fun getViewBinding() = ActivityMyTripMapBinding.inflate(layoutInflater)

    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap!!
        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
        mMarkerColorDescriptor =
            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)

        mTrip.places.forEach { place ->
            val markerColorDescriptor = when (place.markerColor) {
                MarkerColor.ORANGE.toString() -> BitmapDescriptorFactory.defaultMarker(
                    BitmapDescriptorFactory.HUE_ORANGE
                )
                MarkerColor.RED.toString() -> BitmapDescriptorFactory.defaultMarker(
                    BitmapDescriptorFactory.HUE_RED
                )
                else -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
            }
            mMap.addMarker(
                MarkerOptions()
                    .position(LatLng(place.latitude, place.longitude))
                    .draggable(true)
                    .title(place.title)
                    .snippet(place.snippet)
                    .icon(markerColorDescriptor)
            )
        }

        mTrip.circles.forEach { circle ->
            val latLng = LatLng(circle.latitude, circle.longitude)
            mMap.addCircle(
                CircleOptions()
                    .center(latLng)
                    .radius(circle.radius)
                    .fillColor(circle.fillColor)
                    .strokeColor(circle.strokeColor)
                    .clickable(true)
            )
        }

        mTrip.polylines.forEach { polyline ->
            val pattern: List<PatternItem>? = when (polyline.pattern) {
                PolylineType.DASH.toString() -> mutableListOf(Dash(30f), Gap(10f))
                PolylineType.DOT.toString() -> mutableListOf(Dot(), Gap(10f))
                else -> null
            }

            val polylineOptions = PolylineOptions()
                .addAll(ArrayList<LatLng>(
                    polyline.points.map { myPoint ->
                        LatLng(myPoint.latitude, myPoint.longitude)
                    }
                ))
                .color(polyline.color)
                .clickable(true)
            if (pattern != null) {
                polylineOptions.pattern(pattern)
            }

            mMap.addPolyline(polylineOptions)
        }

        mTrip.polygons.forEach { polygon ->
            val pattern: List<PatternItem>? = when (polygon.strokePattern) {
                PolylineType.DASH.toString() -> mutableListOf(Dash(30f), Gap(10f))
                PolylineType.DOT.toString() -> mutableListOf(Dot(), Gap(10f))
                else -> null
            }

            val polygonOptions = PolygonOptions()
                .addAll(ArrayList<LatLng>(
                    polygon.points.map { myPoint ->
                        LatLng(myPoint.latitude, myPoint.longitude)
                    }
                ))
                .fillColor(polygon.fillColor)
                .strokeColor(polygon.strokeColor)
                .clickable(true)
            if (pattern != null) {
                polygonOptions.strokePattern(pattern)
            }

            mMap.addPolygon(polygonOptions)
        }

        mMap.setOnMarkerClickListener(this)
        mMap.setOnMarkerDragListener(this)
        mMap.setOnMapClickListener(this)
        mMap.setOnInfoWindowClickListener(this)
        mMap.setOnPolylineClickListener(this)
        mMap.setOnCircleClickListener(this)
        mMap.setOnPolygonClickListener(this)

        if (mTrip.places.size > 0) {
            val latLng = LatLng(mTrip.places.last().latitude, mTrip.places.last().longitude)
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        }
    }

    override fun onMapClick(latLng: LatLng) {
        mTempMarker?.remove()
        if (mEditMode) {
            performActionDependingOnMapInputType(latLng)
        }
    }

    private fun performActionDependingOnMapInputType(latLng: LatLng) {
        when (mMapInputType) {
            Constants.Navigation.MARKER -> {
                if (mAddMode) {
                    hideKeyboard()
                    if (binding.etMarkerTitle.text.toString().isNotEmpty()) {
                        mMap.addMarker(
                            MarkerOptions()
                                .position(latLng)
                                .draggable(true)
                                .title(binding.etMarkerTitle.text.toString())
                                .snippet(binding.etMarkerSnippet.text.toString())
                                .icon(mMarkerColorDescriptor)
                        )

                        val place = MyPlace(
                            title = binding.etMarkerTitle.text.toString(),
                            snippet = binding.etMarkerSnippet.text.toString(),
                            latitude = latLng.latitude,
                            longitude = latLng.longitude,
                            markerColor = mMarkerColor.toString()
                        )
                        mTrip.places.add(place)
                        MyTripFireStore().updatePlaces(mTrip)
                        binding.etMarkerTitle.text?.clear()
                        binding.etMarkerSnippet.text?.clear()
                    } else {
                        showErrorSnackBar(R.string.marker_title_empty_error_msg)
                    }
                }
            }
            Constants.Navigation.POLYLINE -> {
                if (mAddMode) {
                    if (mIsPolylineBeingCreated && mCurrentPolyline == null) {
                        var pattern: List<PatternItem>? = null
                        if (mPolylineType == PolylineType.DASH) {
                            pattern = mutableListOf(Dash(30f), Gap(10f))
                        } else if (mPolylineType == PolylineType.DOT) {
                            pattern = mutableListOf(Dot(), Gap(10f))
                        }

                        val polylineOptions = PolylineOptions()
                            .add(latLng)
                            .color(mPolylineColor)
                            .clickable(true)
                        if (pattern != null) {
                            polylineOptions.pattern(pattern)
                        }

                        mCurrentPolyline = mMap.addPolyline(polylineOptions)
                        mCurrentPolylinePoints = arrayListOf(latLng)
                    } else if (mIsPolylineBeingCreated && mCurrentPolyline != null) {
                        mCurrentPolylinePoints!!.add(latLng)
                        mCurrentPolyline!!.points = mCurrentPolylinePoints
                    }
                }
            }
            Constants.Navigation.POLYGON -> {
                if (mAddMode) {
                    if (mIsPolygonBeingCreated) {
                        if (mCurrentPolygonPoints == null) {
                            mCurrentPolygonPoints = arrayListOf(latLng)
                        } else {
                            mCurrentPolygonPoints!!.add(latLng)
                        }
                        val tempMarker = mMap.addMarker(MarkerOptions().position(latLng))
                        if (mTempPolygonMarkers == null) {
                            mTempPolygonMarkers = arrayListOf(tempMarker)
                        } else {
                            mTempPolygonMarkers!!.add(tempMarker)
                        }
                    }
                }
            }
            Constants.Navigation.CIRCLE -> {
                if (mAddMode) {
                    if (validateCircleOptions()) {
                        mMap.addCircle(
                            CircleOptions()
                                .center(latLng)
                                .radius(binding.etCircleRadius.text.toString().toDouble())
                                .fillColor(mCircleFillColor)
                                .strokeColor(mCircleStrokeColor)
                                .clickable(true)
                        )
                        val circle = MyCircle(
                            latitude = latLng.latitude,
                            longitude = latLng.longitude,
                            radius = binding.etCircleRadius.text.toString().toDouble(),
                            fillColor = mCircleFillColor,
                            strokeColor = mCircleStrokeColor
                        )
                        mTrip.circles.add(circle)
                        MyTripFireStore().updateCircles(mTrip)
                    }
                }
            }
            else -> {
                val toast = Toast.makeText(this, "Select Map Input Type", Toast.LENGTH_LONG)
                toast.setGravity(Gravity.TOP, 0, 250)
                toast.show()
            }
        }
    }

    private fun validateCircleOptions(): Boolean {
        if (binding.etCircleRadius.text!!.isEmpty() || !binding.etCircleRadius.text!!.all { char -> char.isDigit() }) {
            showErrorSnackBar(R.string.enter_circle_radius)
            return false
        }
        return true
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        if (marker == mTempMarker) {
            mTempMarker?.remove()
        } else {
            if (mEditMode && !mAddMode) {
                mTrip.places.removeIf { place ->
                    place.latitude == marker.position.latitude && place.longitude == marker.position.longitude
                            && place.title == marker.title
                }
                MyTripFireStore().updatePlaces(mTrip)
                marker.remove()
            }
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
        if (mEditMode && !mAddMode) {
            mTrip.polylines.removeIf { myPolyline ->
                myPolyline.points.containsAll(ArrayList<MyPoint>(polyline.points.map { latLng ->
                    MyPoint(latitude = latLng.latitude, longitude = latLng.longitude)
                }))
            }
            MyTripFireStore().updatePolylines(mTrip)
            polyline.remove()
            mCurrentPolyline = null
            mCurrentPolylinePoints = null
            mIsPolylineBeingCreated = false
            binding.btnPolylineStartStop.text = getString(R.string.start)
        }
    }

    override fun onCircleClick(circle: Circle) {
        if (mEditMode && !mAddMode) {
            mTrip.circles.removeIf { myCircle ->
                myCircle.latitude == circle.center.latitude && myCircle.longitude == circle.center.longitude
                        && myCircle.radius == circle.radius
            }
            MyTripFireStore().updateCircles(mTrip)
            circle.remove()
        }
    }

    override fun onPolygonClick(polygon: Polygon) {
        if (mEditMode && !mAddMode) {
            mTrip.polygons.removeIf { myPolygon ->
                myPolygon.points.containsAll(ArrayList<MyPoint>(polygon.points.map { latLng ->
                    MyPoint(latitude = latLng.latitude, longitude = latLng.longitude)
                }))
            }
            MyTripFireStore().updatePolygons(mTrip)
            polygon.remove()
        }
    }
}