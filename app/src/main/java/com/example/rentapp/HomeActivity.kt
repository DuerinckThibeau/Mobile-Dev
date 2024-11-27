package com.example.rentapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.Toast
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rentapp.models.Rental
import com.example.rentapp.adapters.RentalAdapter
import android.widget.ImageButton
import android.view.View
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.rentapp.fragments.RentalRequestsFragment
import com.example.rentapp.fragments.RentedItemsFragment
import com.example.rentapp.fragments.RentedOutItemsFragment

class HomeActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupViews()
        setupNavigation()
        loadUserData()
    }

    private fun setupViews() {
        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)
        val notificationBadge = findViewById<TextView>(R.id.notificationBadge)

        findViewById<ImageButton>(R.id.notificationButton).setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        // Load unread notifications count
        auth.currentUser?.let { user ->
            db.collection("notifications")
                .whereEqualTo("userId", user.uid)
                .whereEqualTo("read", false)
                .get()
                .addOnSuccessListener { result ->
                    val unreadCount = result.size()
                    if (unreadCount > 0) {
                        notificationBadge.visibility = View.VISIBLE
                        notificationBadge.text = if (unreadCount > 9) "9+" else unreadCount.toString()
                    } else {
                        notificationBadge.visibility = View.GONE
                    }
                }
        }

        viewPager.adapter = RentalsPagerAdapter(this)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "REQUESTS"
                1 -> "RENTING"
                2 -> "RENTED OUT"
                else -> ""
            }
        }.attach()
    }

    private fun setupNavigation() {
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navView.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.navigation_home -> true
                R.id.navigation_list -> {
                    startActivity(Intent(this, ListActivity::class.java))
                    true
                }
                R.id.navigation_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
        navView.selectedItemId = R.id.navigation_home
    }

    private fun loadUserData() {
        val welcomeText = findViewById<TextView>(R.id.welcomeText)
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            db.collection("users").document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val firstname = document.getString("firstname")
                        welcomeText.text = "Welcome, $firstname!"
                    }
                }
        }
    }

    private inner class RentalsPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> RentalRequestsFragment()
                1 -> RentedItemsFragment()
                2 -> RentedOutItemsFragment()
                else -> throw IllegalStateException("Invalid position $position")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setupViews() // Refresh notification count when returning to the activity
    }
} 