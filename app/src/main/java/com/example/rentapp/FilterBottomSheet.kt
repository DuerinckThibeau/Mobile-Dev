package com.example.rentapp

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.slider.Slider
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import com.example.rentapp.utils.CircleOverlay
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class FilterBottomSheet : BottomSheetDialogFragment() {
    private lateinit var searchInput: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var locationSpinner: Spinner
    private lateinit var radiusSlider: Slider
    private lateinit var radiusLabel: TextView
    private var filterListener: FilterListener? = null
    private var userLocation: com.google.firebase.firestore.GeoPoint? = null
    private lateinit var mapView: MapView
    private var circleOverlay: CircleOverlay? = null
    private var currentZoom = 15.0
    private var marker: Marker? = null
    private var savedRadius: Int = 0

    interface FilterListener {
        fun onFilterChanged(search: String, category: String, location: String, radiusKm: Int)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_filter, container, false)
        
        searchInput = view.findViewById(R.id.searchInput)
        categorySpinner = view.findViewById(R.id.categoryFilter)
        locationSpinner = view.findViewById(R.id.locationFilter)
        radiusSlider = view.findViewById(R.id.radiusSlider)
        radiusLabel = view.findViewById(R.id.radiusLabel)
        mapView = view.findViewById(R.id.mapView)

        setupSearchListener()
        setupSpinners()
        setupRadiusSlider()
        getUserLocation()
        setupMap()

        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedRadius = arguments?.getInt("lastRadius", 0) ?: 0
    }

    private fun setupSearchListener() {
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                notifyFilterChanged()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupSpinners() {
        // Setup Category Spinner
        val categories = mutableListOf("Filter on category")
        categories.addAll(resources.getStringArray(R.array.categories_array))
        
        val categoryAdapter = object : ArrayAdapter<String>(
            requireContext(),
            R.layout.spinner_item,
            categories
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                if (position == 0) {
                    view.setTextColor(Color.parseColor("#80FFFFFF"))
                } else {
                    view.setTextColor(Color.WHITE)
                }
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent) as TextView
                if (position == 0) {
                    view.setTextColor(Color.parseColor("#80FFFFFF"))
                } else {
                    view.setTextColor(Color.WHITE)
                }
                return view
            }
        }
        
        categoryAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        categorySpinner.adapter = categoryAdapter

        // Setup Location Spinner with same styling
        val db = FirebaseFirestore.getInstance()
        db.collection("items")
            .get()
            .addOnSuccessListener { result ->
                val locations = result.documents
                    .mapNotNull { it.get("location.city") as? String }
                    .distinct()
                    .toMutableList()
                locations.add(0, "Filter on location")

                val locationAdapter = object : ArrayAdapter<String>(
                    requireContext(),
                    R.layout.spinner_item,
                    locations
                ) {
                    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                        val view = super.getView(position, convertView, parent) as TextView
                        if (position == 0) {
                            view.setTextColor(Color.parseColor("#80FFFFFF"))
                        } else {
                            view.setTextColor(Color.WHITE)
                        }
                        return view
                    }

                    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                        val view = super.getDropDownView(position, convertView, parent) as TextView
                        if (position == 0) {
                            view.setTextColor(Color.parseColor("#80FFFFFF"))
                        } else {
                            view.setTextColor(Color.WHITE)
                        }
                        return view
                    }
                }
                locationAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
                locationSpinner.adapter = locationAdapter
            }

        // Add listeners for both spinners
        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                view?.let {
                    (it as TextView).setTextColor(
                        if (position == 0) Color.parseColor("#80FFFFFF")
                        else Color.WHITE
                    )
                }
                if (position > 0) {
                    notifyFilterChanged()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        locationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                view?.let {
                    (it as TextView).setTextColor(
                        if (position == 0) Color.parseColor("#80FFFFFF")
                        else Color.WHITE
                    )
                }
                if (position > 0) {
                    notifyFilterChanged()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupRadiusSlider() {
        radiusSlider.isEnabled = false  // Disable initially
        radiusSlider.value = savedRadius.toFloat()  // Set the initial value from arguments
        radiusLabel.text = "Distance: $savedRadius km"
        
        radiusSlider.addOnChangeListener { _, value, _ ->
            savedRadius = value.toInt()
            radiusLabel.text = "Distance: ${value.toInt()} km"
            updateMapRadius(value.toInt())
            notifyFilterChanged()
        }
    }

    private fun getUserLocation() {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        
        auth.currentUser?.let { user ->
            db.collection("users").document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    val address = document.get("address") as? Map<String, Any>
                    userLocation = address?.get("geopoint") as? com.google.firebase.firestore.GeoPoint
                    // Enable slider only if we have a valid location
                    radiusSlider.isEnabled = (userLocation != null)
                    if (userLocation == null) {
                        radiusLabel.text = "Distance filtering unavailable"
                    } else {
                        // Update map with initial user location
                        updateMapRadius(radiusSlider.value.toInt())
                    }
                }
        }
    }

    private fun notifyFilterChanged() {
        filterListener?.onFilterChanged(
            searchInput.text.toString(),
            if (categorySpinner.selectedItemPosition > 0) categorySpinner.selectedItem.toString() else "",
            if (locationSpinner.selectedItemPosition > 0) locationSpinner.selectedItem.toString() else "",
            radiusSlider.value.toInt()
        )
    }

    fun setFilterListener(listener: FilterListener) {
        filterListener = listener
    }

    fun getUserGeoPoint(): com.google.firebase.firestore.GeoPoint? = userLocation

    private fun setupMap() {
        Configuration.getInstance().userAgentValue = requireActivity().packageName
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        
        // Initialize with default settings
        val mapController = mapView.controller
        mapController.setZoom(currentZoom)
        
        // If we already have the user location, update the map
        userLocation?.let { location ->
            updateMapRadius(radiusSlider.value.toInt())
        }
    }

    private fun updateMapRadius(radiusKm: Int) {
        userLocation?.let { location ->
            // Remove existing overlays
            circleOverlay?.let { mapView.overlays.remove(it) }
            marker?.let { mapView.overlays.remove(it) }
            
            val userGeoPoint = org.osmdroid.util.GeoPoint(location.latitude, location.longitude)
            
            if (radiusKm == 0) {
                // Show marker for user location
                marker = org.osmdroid.views.overlay.Marker(mapView).apply {
                    position = userGeoPoint
                    setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_BOTTOM)
                    icon = resources.getDrawable(R.drawable.ic_location, null)
                }
                mapView.overlays.add(marker)
                
                // Set zoom for marker view
                currentZoom = 15.0
            } else {
                // Show radius circle
                val radiusMeters = radiusKm * 1000.0
                
                circleOverlay = CircleOverlay().apply {
                    position = userGeoPoint
                    radius = radiusMeters
                    fillColor = Color.argb(50, 0, 120, 255)
                    strokeColor = Color.argb(100, 0, 150, 255)
                    strokeWidth = 2f
                }
                
                mapView.overlays.add(circleOverlay)
                
                // Calculate appropriate zoom level
                currentZoom = calculateZoomLevel(radiusKm)
            }
            
            // Update map view
            mapView.controller.setZoom(currentZoom)
            mapView.controller.setCenter(userGeoPoint)
            mapView.invalidate()
        }
    }

    private fun calculateZoomLevel(radiusKm: Int): Double {
        // This formula provides a rough estimate for appropriate zoom level
        return when {
            radiusKm <= 1 -> 15.0
            radiusKm <= 5 -> 13.0
            radiusKm <= 10 -> 12.0
            radiusKm <= 20 -> 11.0
            else -> 10.0
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("radius", radiusSlider.value.toInt())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        savedInstanceState?.let {
            savedRadius = it.getInt("radius", 0)
            radiusSlider.value = savedRadius.toFloat()
        }
    }
} 