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

    override fun onFilterChanged(search: String, category: String, location: String) {
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

        recyclerView.adapter = ItemGridAdapter(filteredItems)
    }
} 