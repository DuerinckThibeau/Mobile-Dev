package com.example.rentapp.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rentapp.ItemDetailActivity
import com.example.rentapp.R
import com.example.rentapp.models.Item

class MyItemsAdapter(private val items: List<Item>) : 
    RecyclerView.Adapter<MyItemsAdapter.ItemViewHolder>() {

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.itemTitle)
        val description: TextView = view.findViewById(R.id.itemDescription)
        val image: ImageView = view.findViewById(R.id.itemImage)
        val status: TextView = view.findViewById(R.id.statusText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.my_item_row, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        
        holder.title.text = item.title
        holder.description.text = item.description
        holder.status.text = if (item.isRented) "HIRED" else "AVAILABLE"
        holder.status.setTextColor(if (item.isRented) 
            holder.itemView.context.getColor(R.color.hired_color)
            else holder.itemView.context.getColor(R.color.available_color))

        if (item.imageUrl.isNotEmpty()) {
            Glide.with(holder.image.context)
                .load(item.imageUrl)
                .into(holder.image)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, ItemDetailActivity::class.java)
            intent.putExtra("itemId", item.id)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount() = items.size
} 