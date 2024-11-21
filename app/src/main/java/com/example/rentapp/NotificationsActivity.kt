package com.example.rentapp

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.example.rentapp.models.Notification
import com.example.rentapp.adapters.NotificationAdapter
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.core.content.ContextCompat

class NotificationsActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        recyclerView = findViewById(R.id.notificationsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Add swipe-to-delete functionality with background
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, 
                              target: RecyclerView.ViewHolder): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val notification = (recyclerView.adapter as NotificationAdapter).getNotificationAt(position)
                deleteNotification(notification.id)
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val deleteIcon = ContextCompat.getDrawable(this@NotificationsActivity, R.drawable.ic_delete)
                val background = ColorDrawable(Color.RED)
                
                // Draw red background
                background.setBounds(
                    itemView.right + dX.toInt(),
                    itemView.top,
                    itemView.right,
                    itemView.bottom
                )
                background.draw(c)

                // Only draw delete icon when enough space is available (e.g., swiped more than half the icon width)
                if (dX < -deleteIcon!!.intrinsicWidth * 2) { 
                    val iconMargin = (itemView.height - deleteIcon.intrinsicHeight) / 2
                    val iconLeft = itemView.right - iconMargin - deleteIcon.intrinsicWidth
                    val iconRight = itemView.right - iconMargin
                    val iconTop = itemView.top + (itemView.height - deleteIcon.intrinsicHeight) / 2
                    val iconBottom = iconTop + deleteIcon.intrinsicHeight
                    deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    deleteIcon.draw(c)
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }
        ItemTouchHelper(swipeHandler).attachToRecyclerView(recyclerView)

        loadNotifications()

        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }
    }

    private fun loadNotifications() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            db.collection("notifications")
                .whereEqualTo("userId", user.uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { result ->
                    val notifications = result.documents.mapNotNull { doc ->
                        try {
                            Notification(
                                id = doc.id,
                                userId = doc.getString("userId") ?: "",
                                title = doc.getString("title") ?: "",
                                message = doc.getString("message") ?: "",
                                timestamp = doc.getLong("timestamp") ?: 0,
                                read = doc.getBoolean("read") ?: false
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    recyclerView.adapter = NotificationAdapter(notifications) { notification ->
                        markAsRead(notification.id)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error loading notifications: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun markAsRead(notificationId: String) {
        db.collection("notifications").document(notificationId)
            .update("read", true)
            .addOnSuccessListener {
                loadNotifications()  // Reload to update the UI
            }
    }

    private fun deleteNotification(notificationId: String) {
        db.collection("notifications").document(notificationId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Notification deleted", Toast.LENGTH_SHORT).show()
                loadNotifications()  // Reload the list
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error deleting notification: ${e.message}", Toast.LENGTH_SHORT).show()
                loadNotifications()  // Reload to restore the item in case of failure
            }
    }
} 