package com.example.rentapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddReviewActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_review)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val userId = intent.getStringExtra("userId") ?: return finish()
        val itemTitle = intent.getStringExtra("itemTitle") ?: ""

        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.submitButton).setOnClickListener {
            val rating = findViewById<RatingBar>(R.id.ratingBar).rating
            val comment = findViewById<EditText>(R.id.commentInput).text.toString()
            
            submitReview(userId, itemTitle, rating, comment)
        }
    }

    private fun submitReview(userId: String, itemTitle: String, rating: Float, comment: String) {
        if (comment.isEmpty()) {
            Toast.makeText(this, "Please write a comment", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Please log in again", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("users").document(currentUser.uid)
            .get()
            .addOnSuccessListener { userDoc ->
                if (!userDoc.exists()) {
                    Toast.makeText(this, "Error: User document not found", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val reviewerName = "${userDoc.getString("firstname")} ${userDoc.getString("lastname")}"
                
                val review = hashMapOf(
                    "userId" to userId,
                    "reviewerId" to currentUser.uid,
                    "reviewerName" to reviewerName,
                    "rating" to rating,
                    "comment" to comment,
                    "itemTitle" to itemTitle,
                    "timestamp" to System.currentTimeMillis()
                )

                android.util.Log.d("AddReview", "Attempting to submit review: $review")

                db.collection("reviews")
                    .add(review)
                    .addOnSuccessListener {
                        android.util.Log.d("AddReview", "Review submitted successfully")
                        Toast.makeText(this, "Review submitted successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        android.util.Log.e("AddReview", "Error submitting review", e)
                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                android.util.Log.e("AddReview", "Error getting user data", e)
                Toast.makeText(this, "Error getting user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
} 