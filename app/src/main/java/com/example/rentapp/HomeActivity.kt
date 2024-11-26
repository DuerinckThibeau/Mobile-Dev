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

        findViewById<ImageButton>(R.id.notificationButton).setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
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
        if (accepted) {
            // Get owner's address first
            db.collection("users").document(auth.currentUser?.uid ?: "")
                .get()
                .addOnSuccessListener { userDoc ->
                    val address = userDoc.get("address") as? Map<String, Any>
                    val fullAddress = address?.let {
                        "${it["streetname"]} ${it["housenumber"]}, ${it["zipcode"]} ${it["city"]}"
                    } ?: "Address not available"

                    // Update rental status
                    db.collection("rentals").document(rental.id)
                        .update("status", "ACCEPTED")
                        .addOnSuccessListener {
                            // Update item status
                            db.collection("items").document(rental.itemId)
                                .update("isRented", true)

                            // Create notification with address
                            val notification = hashMapOf(
                                "userId" to rental.requestedById,
                                "title" to "Rental Request Accepted",
                                "message" to "Your request to rent ${rental.itemTitle} has been accepted. You can pick it up at $fullAddress",
                                "timestamp" to System.currentTimeMillis(),
                                "read" to false
                            )

                            db.collection("notifications").add(notification)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Notification sent", Toast.LENGTH_SHORT).show()
                                    loadRentalRequests()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Failed to send notification: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                }
        } else {
            // Handle rejection (same as before)
            db.collection("rentals").document(rental.id)
                .update("status", "REJECTED")
                .addOnSuccessListener {
                    val notification = hashMapOf(
                        "userId" to rental.requestedById,
                        "title" to "Rental Request Rejected",
                        "message" to "Your request to rent ${rental.itemTitle} has been rejected",
                        "timestamp" to System.currentTimeMillis(),
                        "read" to false
                    )

                    db.collection("notifications").add(notification)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Notification sent", Toast.LENGTH_SHORT).show()
                            loadRentalRequests()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Failed to send notification: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to update rental: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateNotificationBadge() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            db.collection("notifications")
                .whereEqualTo("userId", user.uid)
                .whereEqualTo("read", false)
                .get()
                .addOnSuccessListener { result ->
                    val unreadCount = result.size()
                    val badge = findViewById<TextView>(R.id.notificationBadge)
                    if (unreadCount > 0) {
                        badge.visibility = View.VISIBLE
                        badge.text = if (unreadCount > 9) "9+" else unreadCount.toString()
                    } else {
                        badge.visibility = View.GONE
                    }
                }
        }
    }

    override fun onResume() {
        super.onResume()
        updateNotificationBadge()
    }
} 