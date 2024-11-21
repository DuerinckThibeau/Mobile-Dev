package com.example.rentapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rentapp.R
import com.example.rentapp.models.Rental
import de.hdodenhof.circleimageview.CircleImageView

class RentalAdapter(
    private val rentals: List<Rental>,
    private val onResponseClick: (Rental, Boolean) -> Unit
) : RecyclerView.Adapter<RentalAdapter.RentalViewHolder>() {

    class RentalViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemImage: ImageView = view.findViewById(R.id.itemImage)
        val itemTitle: TextView = view.findViewById(R.id.itemTitle)
        val userImage: CircleImageView = view.findViewById(R.id.userImage)
        val userName: TextView = view.findViewById(R.id.userName)
        val dateRange: TextView = view.findViewById(R.id.dateRange)
        val message: TextView = view.findViewById(R.id.message)
        val acceptButton: Button = view.findViewById(R.id.acceptButton)
        val rejectButton: Button = view.findViewById(R.id.rejectButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RentalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rental_request_item, parent, false)
        return RentalViewHolder(view)
    }

    override fun onBindViewHolder(holder: RentalViewHolder, position: Int) {
        val rental = rentals[position]
        
        holder.itemTitle.text = rental.itemTitle
        holder.userName.text = rental.requestedByName
        holder.dateRange.text = "${rental.startDate} - ${rental.endDate}"
        holder.message.text = rental.message

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

        holder.acceptButton.setOnClickListener { onResponseClick(rental, true) }
        holder.rejectButton.setOnClickListener { onResponseClick(rental, false) }
    }

    override fun getItemCount() = rentals.size
} 