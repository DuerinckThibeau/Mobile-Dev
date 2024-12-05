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
import de.hdodenhof.circleimageview.CircleImageView

class ItemGridAdapter(private val items: List<Item>) : 
    RecyclerView.Adapter<ItemGridAdapter.ItemViewHolder>() {

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.itemTitle)
        val description: TextView = view.findViewById(R.id.itemDescription)
        val image: ImageView = view.findViewById(R.id.itemImage)
        val userImage: CircleImageView = view.findViewById(R.id.userImage)
        val userName: TextView = view.findViewById(R.id.userName)
        val price: TextView = view.findViewById(R.id.itemPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.grid_item, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        
        holder.title.text = item.title
        holder.description.text = item.description
        holder.userName.text = item.createdBy
        
        holder.price.text = if (item.price == "0") "FREE" else "€${item.price}"

        if (item.imageUrl.isNotEmpty()) {
            Glide.with(holder.image.context)
                .load(item.imageUrl)
                .into(holder.image)
        }

        if (item.createdByProfilePic.isNotEmpty()) {
            Glide.with(holder.userImage.context)
                .load(item.createdByProfilePic)
                .into(holder.userImage)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, ItemDetailActivity::class.java)
            intent.putExtra("itemId", item.id)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount() = items.size
} 