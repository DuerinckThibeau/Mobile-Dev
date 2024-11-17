package com.example.rentapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.rentapp.utils.GeocodingUtil

class EditProfileActivity : AppCompatActivity() {
    private lateinit var firstNameInput: EditText
    private lateinit var lastNameInput: EditText
    private lateinit var phoneInput: EditText
    private lateinit var streetInput: EditText
    private lateinit var numberInput: EditText
    private lateinit var zipCodeInput: EditText
    private lateinit var cityInput: EditText
    private lateinit var confirmButton: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        initializeViews()
        loadUserData()
    }

    private fun initializeViews() {
        firstNameInput = findViewById(R.id.firstNameInput)
        lastNameInput = findViewById(R.id.lastNameInput)
        phoneInput = findViewById(R.id.phoneInput)
        streetInput = findViewById(R.id.streetInput)
        numberInput = findViewById(R.id.numberInput)
        zipCodeInput = findViewById(R.id.zipCodeInput)
        cityInput = findViewById(R.id.cityInput)
        confirmButton = findViewById(R.id.confirmButton)

        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }

        confirmButton.setOnClickListener {
            saveUserProfile()
        }
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            db.collection("users").document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        firstNameInput.setText(document.getString("firstname"))
                        lastNameInput.setText(document.getString("lastname"))
                        phoneInput.setText(document.getString("phone"))
                        
                        val address = document.get("address") as? Map<String, Any>
                        address?.let {
                            streetInput.setText(it["streetname"] as? String)
                            numberInput.setText(it["housenumber"] as? String)
                            zipCodeInput.setText(it["zipcode"] as? String)
                            cityInput.setText(it["city"] as? String)
                        }
                    }
                }
        }
    }

    private fun saveUserProfile() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val street = streetInput.text.toString()
            val city = cityInput.text.toString()
            
            val geoPoint = GeocodingUtil.getGeoPointFromAddress(
                this,
                street,
                city
            )

            val address = hashMapOf(
                "city" to city,
                "housenumber" to numberInput.text.toString(),
                "streetname" to street,
                "zipcode" to zipCodeInput.text.toString(),
                "geopoint" to geoPoint
            )

            val userProfile = hashMapOf(
                "address" to address,
                "firstname" to firstNameInput.text.toString(),
                "lastname" to lastNameInput.text.toString(),
                "phone" to phoneInput.text.toString()
            )

            db.collection("users").document(user.uid)
                .update(userProfile as Map<String, Any>)
                .addOnSuccessListener {
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error updating profile: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
} 