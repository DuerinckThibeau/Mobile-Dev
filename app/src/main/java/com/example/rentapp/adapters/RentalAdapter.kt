package com.example.rentapp.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rentapp.R
import com.example.rentapp.models.Rental
import com.example.rentapp.RentalDetailActivity
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView

class RentalAdapter(
    private val rentals: List<Rental>,
    private val showActions: Boolean = false,
    private val onResponseClick: (Rental, Boolean) -> Unit
) : RecyclerView.Adapter<RentalAdapter.RentalViewHolder>() {

    class RentalViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemImage: ImageView = view.findViewById(R.id.itemImage)
        val itemTitle: TextView = view.findViewById(R.id.itemTitle)
        val userImage: CircleImageView = view.findViewById(R.id.userImage)
        val userName: TextView = view.findViewById(R.id.userName)
        val dateRange: TextView = view.findViewById(R.id.dateRange)
        val message: TextView? = view.findViewById(R.id.message)
        val acceptButton: Button? = view.findViewById(R.id.acceptButton)
        val rejectButton: Button? = view.findViewById(R.id.rejectButton)
        val userRating: RatingBar? = view.findViewById(R.id.userRating)
        val ratingText: TextView? = view.findViewById(R.id.ratingText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RentalViewHolder {
        val layoutId = if (showActions) R.layout.rental_request_item else R.layout.rental_item
        val view = LayoutInflater.from(parent.context)
            .inflate(layoutId, parent, false)
        return RentalViewHolder(view)
    }

    override fun onBindViewHolder(holder: RentalViewHolder, position: Int) {
        val rental = rentals[position]
        
        holder.itemTitle.text = rental.itemTitle
        holder.userName.text = rental.requestedByName
        holder.dateRange.text = "${rental.startDate} - ${rental.endDate}"
        
        if (showActions) {
            holder.message?.text = rental.message
            holder.acceptButton?.setOnClickListener { onResponseClick(rental, true) }
            holder.rejectButton?.setOnClickListener { onResponseClick(rental, false) }
        }

        if (rental.itemImage.isNotEmpty()) {
            Glide.with(holder.itemImage.context)
                .load(rental.itemImage)
                .into(holder.itemImage)
        }

        if (rental.requestedByProfilePic.isNotEmpty()) {
            Glide.with(holder.userImage.context)
                .load(rental.requestedByProfilePic)
                .into(holder.userImage)
        }

        holder.userRating?.let { ratingBar ->
            holder.ratingText?.let { ratingText ->
                loadUserRating(rental.requestedById, ratingBar, ratingText)
            }
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, RentalDetailActivity::class.java)
            intent.putExtra("rentalId", rental.id)
            intent.putExtra("isOwner", true)
            holder.itemView.context.startActivity(intent)
        }
    }

    private fun loadUserRating(userId: String, ratingBar: RatingBar, ratingText: TextView) {
        FirebaseFirestore.getInstance()
            .collection("reviews")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val totalRating = documents.sumOf { it.getDouble("rating")?.toDouble() ?: 0.0 }
                    val averageRating = totalRating / documents.size()
                    ratingBar.rating = averageRating.toFloat()
                    ratingText.text = String.format("%.1f (%d reviews)", averageRating, documents.size())
                    ratingBar.visibility = View.VISIBLE
                    ratingText.visibility = View.VISIBLE
                } else {
                    ratingBar.visibility = View.GONE
                    ratingText.visibility = View.GONE
                }
            }
    }

    override fun getItemCount() = rentals.size
} 