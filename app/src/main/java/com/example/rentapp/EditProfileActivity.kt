package com.example.rentapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.example.rentapp.utils.GeocodingUtil
import de.hdodenhof.circleimageview.CircleImageView

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
    private lateinit var profileImage: CircleImageView

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { selectedImageUri ->
            val storageRef = FirebaseStorage.getInstance().reference
            val imageRef = storageRef.child("profile_pictures/${auth.currentUser?.uid}")
            
            imageRef.putFile(selectedImageUri)
                .addOnSuccessListener { taskSnapshot ->
                    imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        auth.currentUser?.let { user ->
                            db.collection("users").document(user.uid)
                                .update("profilepicture", downloadUri.toString())
                                .addOnSuccessListener {
                                    Glide.with(this)
                                        .load(downloadUri)
                                        .placeholder(R.drawable.ic_person)
                                        .into(profileImage)
                                }
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
                }
        }
    }

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
        profileImage = findViewById(R.id.profileImage)

        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }

        confirmButton.setOnClickListener {
            saveUserProfile()
        }

        findViewById<ImageView>(R.id.changeProfilePicButton).setOnClickListener {
            getContent.launch("image/*")
        }
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            db.collection("users").document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val profilePicture = document.getString("profilepicture")
                        if (!profilePicture.isNullOrEmpty()) {
                            Glide.with(this)
                                .load(profilePicture)
                                .placeholder(R.drawable.ic_person)
                                .into(profileImage)
                        }
                        
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

            // Update Firestore in background
            db.collection("users").document(user.uid)
                .update(userProfile as Map<String, Any>)
                .addOnSuccessListener {
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error updating profile: ${e.message}", Toast.LENGTH_SHORT).show()
                }

            // Update ProfileActivity immediately
            val intent = Intent(this, ProfileActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }
} 