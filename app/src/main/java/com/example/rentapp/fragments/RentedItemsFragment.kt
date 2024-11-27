package com.example.rentapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.rentapp.R
import com.example.rentapp.adapters.RentalAdapter
import com.example.rentapp.models.Rental

class RentedItemsFragment : BaseRentalFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_rentals, container, false)
    }

    override fun loadRentals() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            db.collection("rentals")
                .whereEqualTo("requestedById", user.uid)
                .whereEqualTo("status", "ACCEPTED")
                .get()
                .addOnSuccessListener { result ->
                    val rentals = result.toObjects(Rental::class.java)
                    updateEmptyState(rentals)
                    recyclerView.adapter = RentalAdapter(
                        rentals = rentals,
                        showActions = false
                    ) { _, _ -> }
                }
        }
    }

    override fun getEmptyStateMessage(): String {
        return "Not currently renting anything."
    }
}
