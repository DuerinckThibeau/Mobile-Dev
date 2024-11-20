package com.example.rentapp

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.net.Uri
import android.content.Intent
import android.provider.MediaStore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import android.widget.FrameLayout
import android.widget.ImageView
import com.example.rentapp.utils.GeocodingUtil

class AddItemActivity : AppCompatActivity() {
    private lateinit var priceInput: EditText
    private lateinit var titleInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var imageContainer: FrameLayout
    private lateinit var selectedImageView: ImageView
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var selectedImageUri: Uri? = null
    
    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        priceInput = findViewById(R.id.priceInput)
        titleInput = findViewById(R.id.titleInput)
        descriptionInput = findViewById(R.id.descriptionInput)
        imageContainer = findViewById(R.id.imageContainer)
        selectedImageView = findViewById(R.id.selectedImageView)

        imageContainer.setOnClickListener {
            openImagePicker()
        }

        setupCategorySpinner()
        setupConfirmButton()

        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.data
            selectedImageView.setImageURI(selectedImageUri)
            selectedImageView.visibility = View.VISIBLE
        }
    }

    private fun setupCategorySpinner() {
        val spinner: Spinner = findViewById(R.id.categorySpinner)
        
        val adapter = object : ArrayAdapter<String>(
            this,
            R.layout.spinner_item,
            resources.getStringArray(R.array.categories_array)
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                if (position == 0) {
                    view.setTextColor(Color.parseColor("#80FFFFFF"))
                } else {
                    view.setTextColor(Color.WHITE)
                }
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent) as TextView
                if (position == 0) {
                    view.setTextColor(Color.parseColor("#80FFFFFF"))
                } else {
                    view.setTextColor(Color.WHITE)
                }
                return view
            }
        }

        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                view?.let {
                    (it as TextView).setTextColor(
                        if (position == 0) Color.parseColor("#80FFFFFF")
                        else Color.WHITE
                    )
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupConfirmButton() {
        findViewById<Button>(R.id.confirmButton).setOnClickListener {
            val title = titleInput.text.toString()
            val price = priceInput.text.toString()
            val description = descriptionInput.text.toString()
            val category = (findViewById<Spinner>(R.id.categorySpinner)).selectedItem.toString()

            if (title.isEmpty() || price.isEmpty() || description.isEmpty() || 
                category == "Category") {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            createItem(title, price, description, category)
        }
    }

    private fun createItem(title: String, price: String, description: String, category: String) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Please log in again", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("users").document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val firstName = document.getString("firstname") ?: ""
                    val lastName = document.getString("lastname") ?: ""
                    val profilePicture = document.getString("profilepicture") ?: ""
                    
                    val address = document.get("address") as? Map<String, Any>
                    val street = address?.get("streetname") as? String ?: ""
                    val city = address?.get("city") as? String ?: ""
                    
                    val geoPoint = GeocodingUtil.getGeoPointFromAddress(
                        this,
                        street,
                        city
                    )

                    val item = hashMapOf(
                        "title" to title,
                        "price" to price,
                        "description" to description,
                        "category" to category,
                        "imageUrl" to "",
                        "createdById" to currentUser.uid,
                        "createdBy" to "$firstName $lastName",
                        "createdByProfilePic" to profilePicture,
                        "location" to mapOf(
                            "streetname" to street,
                            "city" to city,
                            "geopoint" to geoPoint
                        ),
                        "isRented" to false
                    )

                    // Create item in database
                    db.collection("items")
                        .add(item)
                        .addOnSuccessListener {
                            if (selectedImageUri != null) {
                                uploadImage(it.id, selectedImageUri!!)
                            } else {
                                Toast.makeText(this, "Item added successfully", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error adding item: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error getting user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadImage(itemId: String, imageUri: Uri) {
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("items/$itemId.jpg")

        imageRef.putFile(imageUri)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    // Update the item with the image URL
                    db.collection("items").document(itemId)
                        .update("imageUrl", uri.toString())
                        .addOnSuccessListener {
                            Toast.makeText(this, "Item added successfully", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error updating image URL: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error uploading image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
} 