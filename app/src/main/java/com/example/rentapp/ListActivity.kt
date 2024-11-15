package com.example.rentapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.example.rentapp.models.Item
import com.example.rentapp.adapters.ItemGridAdapter

class ListActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var db: FirebaseFirestore

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
    }

    override fun onResume() {
        super.onResume()
        loadItems()
    }

    private fun loadItems() {
        db.collection("items")
            .get()
            .addOnSuccessListener { result ->
                val items = result.toObjects(Item::class.java)
                recyclerView.adapter = ItemGridAdapter(items)
            }
            .addOnFailureListener { exception ->
                // Handle any errors
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
} 