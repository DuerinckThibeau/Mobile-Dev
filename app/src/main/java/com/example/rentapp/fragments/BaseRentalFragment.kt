package com.example.rentapp.fragments

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.rentapp.R
import com.example.rentapp.models.Rental

abstract class BaseRentalFragment : Fragment() {
    protected lateinit var auth: FirebaseAuth
    protected lateinit var db: FirebaseFirestore
    protected lateinit var recyclerView: RecyclerView
    protected lateinit var emptyState: View
    protected lateinit var emptyStateText: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        
        emptyState = view.findViewById(R.id.emptyState)
        emptyStateText = emptyState.findViewById(R.id.emptyStateText)
        
        loadRentals()
    }

    protected fun updateEmptyState(rentals: List<Rental>) {
        if (rentals.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyState.visibility = View.VISIBLE
            emptyStateText.text = getEmptyStateMessage()
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyState.visibility = View.GONE
        }
    }

    abstract fun loadRentals()
    abstract fun getEmptyStateMessage(): String
}
