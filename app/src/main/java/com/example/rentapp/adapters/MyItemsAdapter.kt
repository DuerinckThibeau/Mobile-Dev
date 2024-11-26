package com.example.rentapp.adapters

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.view.ContextThemeWrapper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rentapp.ItemDetailActivity
import com.example.rentapp.R
import com.example.rentapp.models.Item

class MyItemsAdapter(
    private val items: List<Item>,
    private val onEditClick: (Item) -> Unit,
    private val onDeleteClick: (Item) -> Unit
) : RecyclerView.Adapter<MyItemsAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.itemImage)
        val title: TextView = view.findViewById(R.id.itemTitle)
        val description: TextView = view.findViewById(R.id.itemDescription)
        val status: TextView = view.findViewById(R.id.statusText)
        val menuButton: ImageButton = view.findViewById(R.id.itemMenuButton)

        init {
            itemView.setOnClickListener(null)
            
            menuButton.setOnClickListener { button ->
                val wrapper = ContextThemeWrapper(button.context, R.style.PopupMenuStyle)
                val popupMenu = PopupMenu(wrapper, button)
                popupMenu.inflate(R.menu.item_options_menu)
                
                popupMenu.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.action_edit_item -> {
                            onEditClick(items[adapterPosition])
                            true
                        }
                        R.id.action_delete_item -> {
                            onDeleteClick(items[adapterPosition])
                            true
                        }
                        else -> false
                    }
                }
                popupMenu.show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.my_item_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        
        holder.title.text = item.title
        holder.description.text = item.description
        holder.status.text = if(item.isRented) "RENTED" else "AVAILABLE"
        holder.status.setTextColor(if(item.isRented) Color.RED else Color.GREEN)
        
        Glide.with(holder.image)
            .load(item.imageUrl)
            .centerCrop()
            .into(holder.image)
    }

    override fun getItemCount() = items.size
} 