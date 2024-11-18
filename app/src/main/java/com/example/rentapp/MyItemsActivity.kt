package com.example.rentapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
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
                .whereEqualTo("createdBy", currentUser.uid)
                .get()
                .addOnSuccessListener { result ->
                    allItems = result.documents.map { doc ->
                        doc.toObject(Item::class.java)?.copy(id = doc.id) ?: Item()
                    }
                    recyclerView.adapter = MyItemsAdapter(allItems)
                }
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
        recyclerView.adapter = MyItemsAdapter(filteredItems)
    }
} 