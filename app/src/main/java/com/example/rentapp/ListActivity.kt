package com.example.rentapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        findViewById<FloatingActionButton>(R.id.addItemFab).setOnClickListener {
            startActivity(Intent(this, AddItemActivity::class.java))
        }

        setupBottomNavigation()
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
                    // Navigate to profile when implemented
                    true
                }
                else -> false
            }
        }
        navView.selectedItemId = R.id.navigation_list
    }
} 