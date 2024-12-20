package com.example.rentapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.rentapp.utils.GeocodingUtil

class ProfileSetupActivity : AppCompatActivity() {
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
        setContentView(R.layout.activity_profile_setup)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        firstNameInput = findViewById(R.id.firstNameInput)
        lastNameInput = findViewById(R.id.lastNameInput)
        phoneInput = findViewById(R.id.phoneInput)
        streetInput = findViewById(R.id.streetInput)
        numberInput = findViewById(R.id.numberInput)
        zipCodeInput = findViewById(R.id.zipCodeInput)
        cityInput = findViewById(R.id.cityInput)
        confirmButton = findViewById(R.id.confirmButton)

        confirmButton.setOnClickListener {
            saveUserProfile()
        }
    }

    private fun saveUserProfile() {
        val currentUser = FirebaseAuth.getInstance().currentUser
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
                "email" to user.email, 
                "firstname" to firstNameInput.text.toString(),
                "lastname" to lastNameInput.text.toString(),
                "phone" to phoneInput.text.toString(),
                "profilepicture" to ""
            )

            db.collection("users").document(user.uid)
                .set(userProfile)
                .addOnSuccessListener {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
} 