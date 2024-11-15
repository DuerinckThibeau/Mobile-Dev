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
import android.widget.Spinner
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.slider.Slider
import com.google.firebase.firestore.GeoPoint

class FilterBottomSheet : BottomSheetDialogFragment() {
    private lateinit var searchInput: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var locationSpinner: Spinner
    private lateinit var radiusSlider: Slider
    private lateinit var radiusLabel: TextView
    private var filterListener: FilterListener? = null
    private var userLocation: GeoPoint? = null

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

        setupSearchListener()
        setupSpinners()
        setupRadiusSlider()
        getUserLocation()

        return view
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
        radiusSlider.addOnChangeListener { _, value, _ ->
            radiusLabel.text = "Distance: ${value.toInt()} km"
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
                    userLocation = address?.get("geopoint") as? GeoPoint
                    // Enable slider only if we have a valid location
                    radiusSlider.isEnabled = (userLocation != null)
                    if (userLocation == null) {
                        radiusLabel.text = "Distance filtering unavailable"
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

    fun getUserGeoPoint(): GeoPoint? = userLocation
} 