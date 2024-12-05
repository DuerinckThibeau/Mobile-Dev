package com.example.rentapp

import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.storage.FirebaseStorage
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.textfield.TextInputEditText
import java.util.UUID

class EditItemActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var selectedImageUri: Uri? = null
    private var itemId: String = ""
    private var currentImageUrl: String = ""
    private lateinit var priceInput: TextInputEditText

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            findViewById<ImageView>(R.id.itemImage).setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_item)

        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        
        itemId = intent.getStringExtra("itemId") ?: return finish()

        setupUI()
        loadItemData()
    }

    private fun setupUI() {
        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.changeImageButton).setOnClickListener {
            getContent.launch("image/*")
        }

        findViewById<Button>(R.id.saveButton).setOnClickListener {
            saveChanges()
        }
    }

    private fun loadItemData() {
        db.collection("items").document(itemId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    findViewById<TextInputEditText>(R.id.titleInput).setText(document.getString("title"))
                    findViewById<TextInputEditText>(R.id.descriptionInput).setText(document.getString("description"))
                    findViewById<TextInputEditText>(R.id.priceInput).setText(document.getString("price"))
                    currentImageUrl = document.getString("imageUrl") ?: ""
                    
                    Glide.with(this)
                        .load(currentImageUrl)
                        .into(findViewById(R.id.itemImage))
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load item data", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun saveChanges() {
        val title = findViewById<TextInputEditText>(R.id.titleInput).text.toString()
        val description = findViewById<TextInputEditText>(R.id.descriptionInput).text.toString()
        val price = findViewById<TextInputEditText>(R.id.priceInput).text.toString()

        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show()
            return
        }

        if (price.isEmpty()) {
            Toast.makeText(this, "Please enter a price", Toast.LENGTH_SHORT).show()
            return
        }

        val loadingDialog = AlertDialog.Builder(this)
            .setMessage("Saving changes...")
            .setCancelable(false)
            .create()
        loadingDialog.show()

        if (selectedImageUri != null) {
            val imageRef = storage.reference.child("items/${UUID.randomUUID()}")
            imageRef.putFile(selectedImageUri!!)
                .continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let { throw it }
                    }
                    imageRef.downloadUrl
                }
                .addOnSuccessListener { uri ->
                    updateItem(title, description, uri.toString(), price, loadingDialog)
                }
                .addOnFailureListener {
                    loadingDialog.dismiss()
                    Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
                }
        } else {
            updateItem(title, description, currentImageUrl, price, loadingDialog)
        }
    }

    private fun updateItem(title: String, description: String, imageUrl: String, price: String, loadingDialog: AlertDialog) {
        val updates = hashMapOf<String, Any>(
            "title" to title,
            "description" to description,
            "imageUrl" to imageUrl,
            "price" to price,
            "lastModified" to FieldValue.serverTimestamp()
        )

        db.collection("items").document(itemId)
            .update(updates)
            .addOnSuccessListener {
                loadingDialog.dismiss()
                Toast.makeText(this, "Changes saved successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                loadingDialog.dismiss()
                Toast.makeText(this, "Failed to save changes", Toast.LENGTH_SHORT).show()
            }
    }
} 