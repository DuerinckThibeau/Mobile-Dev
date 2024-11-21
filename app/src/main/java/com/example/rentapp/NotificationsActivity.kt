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
} 