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

class HomeActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

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

        findViewById<Button>(R.id.logoutButton).setOnClickListener {
            auth.signOut()
            // Navigate back to login screen
            val intent = Intent(this, LoginActivity::class.java)
            // Clear the back stack so user can't go back after logging out
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
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
} 