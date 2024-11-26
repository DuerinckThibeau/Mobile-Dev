package com.example.rentapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.rentapp.models.Item
import com.example.rentapp.adapters.MyItemsAdapter

class MyItemsActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchInput: EditText
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var allItems = listOf<Item>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_items)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        recyclerView = findViewById(R.id.itemsRecyclerView)
        searchInput = findViewById(R.id.searchInput)
        recyclerView.layoutManager = LinearLayoutManager(this)

        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }

        setupSearchListener()
        loadUserItems()
    }

    private fun setupSearchListener() {
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterItems(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun loadUserItems() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collection("items")
                .whereEqualTo("createdById", currentUser.uid)
                .get()
                .addOnSuccessListener { result ->
                    allItems = result.documents.map { doc ->
                        doc.toObject(Item::class.java)?.copy(id = doc.id) ?: Item()
                    }
                    setupRecyclerView()
                }
        }
    }

    private fun setupRecyclerView() {
        val adapter = MyItemsAdapter(
            items = allItems,
            onEditClick = { item ->
                val intent = Intent(this, EditItemActivity::class.java).apply {
                    putExtra("itemId", item.id)
                }
                startActivity(intent)
            },
            onDeleteClick = { item ->
                AlertDialog.Builder(this, R.style.AlertDialogTheme)
                    .setTitle("Delete Item")
                    .setMessage("Are you sure you want to delete this item?")
                    .setPositiveButton("Yes") { _, _ ->
                        deleteItem(item)
                    }
                    .setNegativeButton("No", null)
                    .show()
            }
        )
        recyclerView.adapter = adapter
    }

    private fun deleteItem(item: Item) {
        FirebaseFirestore.getInstance().collection("items")
            .document(item.id)
            .delete()
            .addOnSuccessListener {
                loadUserItems()
                Toast.makeText(this, "Item deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete item", Toast.LENGTH_SHORT).show()
            }
    }

    private fun filterItems(query: String) {
        val filteredItems = if (query.isEmpty()) {
            allItems
        } else {
            allItems.filter { 
                it.title.contains(query, ignoreCase = true) 
            }
        }
        setupRecyclerView()
    }
} 