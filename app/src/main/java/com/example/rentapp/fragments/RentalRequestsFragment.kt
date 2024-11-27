package com.example.rentapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.rentapp.R
import com.example.rentapp.adapters.RentalAdapter
import com.example.rentapp.models.Rental

class RentalRequestsFragment : BaseRentalFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_rentals, container, false)
    }

    override fun loadRentals() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            db.collection("rentals")
                .whereEqualTo("ownerId", user.uid)
                .whereEqualTo("status", "PENDING")
                .get()
                .addOnSuccessListener { result ->
                    val rentals = result.toObjects(Rental::class.java)
                    updateEmptyState(rentals)
                    recyclerView.adapter = RentalAdapter(
                        rentals = rentals,
                        showActions = true
                    ) { rental, accepted ->
                        handleRentalResponse(rental, accepted)
                    }
                }
        }
    }

    private fun handleRentalResponse(rental: Rental, accepted: Boolean) {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val status = if (accepted) "ACCEPTED" else "REJECTED"
            
            // Get owner's address first
            db.collection("users").document(user.uid)
                .get()
                .addOnSuccessListener { userDoc ->
                    val address = userDoc.get("address") as? Map<String, String>
                    val addressStr = if (address != null) {
                        "${address["streetname"]} ${address["housenumber"]}, ${address["zipcode"]} ${address["city"]}"
                    } else ""

                    // Update rental status
                    db.collection("rentals")
                        .whereEqualTo("itemId", rental.itemId)
                        .whereEqualTo("requestedById", rental.requestedById)
                        .whereEqualTo("status", "PENDING")
                        .get()
                        .addOnSuccessListener { documents ->
                            for (document in documents) {
                                document.reference.update("status", status)
                                    .addOnSuccessListener {
                                        // Create notification with address
                                        val notification = hashMapOf(
                                            "userId" to rental.requestedById,
                                            "title" to "Rental Request ${if (accepted) "Accepted" else "Rejected"}",
                                            "message" to if (accepted) 
                                                "Your request for ${rental.itemTitle} has been accepted. You can pick it up at: $addressStr"
                                            else 
                                                "Your request for ${rental.itemTitle} has been rejected",
                                            "timestamp" to System.currentTimeMillis(),
                                            "read" to false
                                        )

                                        db.collection("notifications")
                                            .add(notification)
                                            .addOnSuccessListener {
                                                Toast.makeText(context, 
                                                    "Request ${if (accepted) "accepted" else "rejected"}", 
                                                    Toast.LENGTH_SHORT).show()
                                                loadRentals()
                                            }
                                    }
                            }
                        }
                }
        }
    }

    override fun getEmptyStateMessage(): String {
        return "No new requests. You're all caught up!"
    }
} 