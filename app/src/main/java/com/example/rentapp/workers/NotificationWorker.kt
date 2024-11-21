package com.example.rentapp.workers

import android.content.Context
import androidx.work.*
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

class NotificationWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val db = FirebaseFirestore.getInstance()
        val currentTime = System.currentTimeMillis()

        // Get all scheduled notifications that should be sent
        db.collection("scheduledNotifications")
            .whereLessThanOrEqualTo("scheduledFor", currentTime)
            .get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    // Move to regular notifications collection
                    val notification = hashMapOf(
                        "userId" to doc.getString("userId"),
                        "title" to doc.getString("title"),
                        "message" to doc.getString("message"),
                        "timestamp" to doc.getLong("scheduledFor"),
                        "read" to false
                    )

                    db.collection("notifications").add(notification)
                        .addOnSuccessListener {
                            // Delete the scheduled notification
                            doc.reference.delete()
                        }
                }
            }

        return Result.success()
    }

    companion object {
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<NotificationWorker>(
                1, TimeUnit.HOURS
            ).setConstraints(constraints)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    "notification_worker",
                    ExistingPeriodicWorkPolicy.KEEP,
                    request
                )
        }
    }
} 