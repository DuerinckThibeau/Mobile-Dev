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

class HomeActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var rentalsRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        
        rentalsRecyclerView = findViewById(R.id.rentalsRecyclerView)
        rentalsRecyclerView.layoutManager = LinearLayoutManager(this)

        loadRentalRequests()

        val welcomeText = findViewById<TextView>(R.id.welcomeText)
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            db.collection("users").document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val firstname = document.getString("firstname")
                        val lastname = document.getString("lastname")
                        val address = document.get("address") as? Map<String, String>
                        val city = address?.get("city")
                        val streetname = address?.get("streetname")
                        val housenumber = address?.get("housenumber")
                        val zipcode = address?.get("zipcode")

                        welcomeText.text = "Welcome, $firstname!"
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error loading user data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        
        navView.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.navigation_home -> {
                    true
                }
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
    }

    private fun loadRentalRequests() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            db.collection("rentals")
                .whereEqualTo("ownerId", user.uid)
                .whereEqualTo("status", "PENDING")
                .get()
                .addOnSuccessListener { result ->
                    val rentals = result.documents.map { doc ->
                        doc.toObject(Rental::class.java)?.copy(id = doc.id) ?: Rental()
                    }
                    rentalsRecyclerView.adapter = RentalAdapter(rentals) { rental, accepted ->
                        handleRentalResponse(rental, accepted)
                    }
                }
        }
    }

    private fun handleRentalResponse(rental: Rental, accepted: Boolean) {
        db.collection("rentals").document(rental.id)
            .update("status", if (accepted) "ACCEPTED" else "REJECTED")
            .addOnSuccessListener {
                if (accepted) {
                    // Update item status to rented
                    db.collection("items").document(rental.itemId)
                        .update("isRented", true)
                }
                loadRentalRequests()
            }
    }
} 