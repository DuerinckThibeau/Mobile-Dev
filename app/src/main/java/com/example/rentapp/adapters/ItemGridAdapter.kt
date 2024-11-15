package com.example.rentapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rentapp.R
import com.example.rentapp.models.Item

class ItemGridAdapter(private val items: List<Item>) : 
    RecyclerView.Adapter<ItemGridAdapter.ItemViewHolder>() {

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemImage: ImageView = view.findViewById(R.id.itemImage)
        val itemTitle: TextView = view.findViewById(R.id.itemTitle)
        val itemDescription: TextView = view.findViewById(R.id.itemDescription)
        val userName: TextView = view.findViewById(R.id.userName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.grid_item, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.itemTitle.text = item.title
        holder.itemDescription.text = item.description
        holder.userName.text = item.createdBy

        if (item.imageUrl.isNotEmpty()) {
            Glide.with(holder.itemImage.context)
                .load(item.imageUrl)
                .placeholder(R.drawable.ic_image_placeholder)
                .into(holder.itemImage)
        } else {
            holder.itemImage.setImageResource(R.drawable.ic_image_placeholder)
        }
    }

    override fun getItemCount() = items.size
} 