package com.example.rentapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.example.rentapp.models.Item
import com.example.rentapp.adapters.ItemGridAdapter
import com.example.rentapp.FilterBottomSheet
import com.google.firebase.firestore.GeoPoint

class ListActivity : AppCompatActivity(), FilterBottomSheet.FilterListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var db: FirebaseFirestore
    private var allItems = listOf<Item>()
    private var filterSheet: FilterBottomSheet? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        db = FirebaseFirestore.getInstance()
        recyclerView = findViewById(R.id.itemsRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        loadItems()

        findViewById<FloatingActionButton>(R.id.addItemFab).setOnClickListener {
            startActivity(Intent(this, AddItemActivity::class.java))
        }

        setupBottomNavigation()

        findViewById<ImageButton>(R.id.filterButton).setOnClickListener {
            showFilterSheet()
        }
    }

    override fun onResume() {
        super.onResume()
        loadItems()
    }

    private fun loadItems() {
        db.collection("items")
            .get()
            .addOnSuccessListener { result ->
                allItems = result.toObjects(Item::class.java)
                recyclerView.adapter = ItemGridAdapter(allItems)
            }
            .addOnFailureListener { exception ->
                
            }
    }

    private fun setupBottomNavigation() {
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navView.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    true
                }
                R.id.navigation_list -> true
                R.id.navigation_profile -> {
                    true
                }
                else -> false
            }
        }
        navView.selectedItemId = R.id.navigation_list
    }

    private fun showFilterSheet() {
        filterSheet = FilterBottomSheet().apply {
            setFilterListener(this@ListActivity)
        }
        filterSheet?.show(supportFragmentManager, "FilterBottomSheet")
    }

    override fun onFilterChanged(search: String, category: String, location: String, radiusKm: Int) {
        var filteredItems = allItems

        if (search.isNotEmpty()) {
            filteredItems = filteredItems.filter { 
                it.title.contains(search, ignoreCase = true) 
            }
        }

        if (category.isNotEmpty()) {
            filteredItems = filteredItems.filter { 
                it.category == category 
            }
        }

        if (location.isNotEmpty()) {
            filteredItems = filteredItems.filter { 
                it.location["city"] == location 
            }
        }

        if (radiusKm > 0) {
            filteredItems = filteredItems.filter { item ->
                val itemLocation = item.location
                val geoPoint = itemLocation["geopoint"] as? GeoPoint
                
                if (geoPoint != null) {
                    val distance = calculateDistance(
                        userLat = filterSheet?.getUserGeoPoint()?.latitude ?: 0.0,
                        userLng = filterSheet?.getUserGeoPoint()?.longitude ?: 0.0,
                        itemLat = geoPoint.latitude,
                        itemLng = geoPoint.longitude
                    )
                    distance <= radiusKm
                } else false
            }
        }

        recyclerView.adapter = ItemGridAdapter(filteredItems)
    }

    private fun calculateDistance(userLat: Double, userLng: Double, itemLat: Double, itemLng: Double): Int {
        val r = 6371 // Earth's radius in km
        val dLat = Math.toRadians(itemLat - userLat)
        val dLng = Math.toRadians(itemLng - userLng)
        val a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(userLat)) * Math.cos(Math.toRadians(itemLat)) *
                Math.sin(dLng/2) * Math.sin(dLng/2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a))
        return (r * c).toInt()
    }
} 