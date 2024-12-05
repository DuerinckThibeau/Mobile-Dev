package com.example.rentapp

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import de.hdodenhof.circleimageview.CircleImageView

class RentalDetailActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rental_detail)

        db = FirebaseFirestore.getInstance()
        
        Configuration.getInstance().userAgentValue = packageName
        mapView = findViewById(R.id.mapView)
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        
        val rentalId = intent.getStringExtra("rentalId") ?: return
        val isOwner = intent.getBooleanExtra("isOwner", false)
        
        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }

        loadRentalDetails(rentalId, isOwner)
    }

    private fun loadRentalDetails(rentalId: String, isOwner: Boolean) {
        val confirmReturnButton = findViewById<Button>(R.id.confirmReturnButton)
        
        db.collection("rentals").document(rentalId)
            .get()
            .addOnSuccessListener { rentalDoc ->
                if (rentalDoc != null) {
                    findViewById<TextView>(R.id.itemTitle).text = rentalDoc.getString("itemTitle")
                    findViewById<TextView>(R.id.dateRange).text = 
                        "${rentalDoc.getString("startDate")} - ${rentalDoc.getString("endDate")}"
                    findViewById<TextView>(R.id.message).text = rentalDoc.getString("message")
                    
                    val imageUrl = rentalDoc.getString("itemImage")
                    if (!imageUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(imageUrl)
                            .into(findViewById(R.id.itemImage))
                    }

                    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                    val ownerId = rentalDoc.getString("ownerId")
                    
                    if (currentUserId == ownerId) {
                        val renterId = rentalDoc.getString("requestedById")
                        loadUserDetails(renterId ?: "", true)
                    } else {
                        loadUserDetails(ownerId ?: "", false)
                    }

                    if (currentUserId == ownerId && rentalDoc.getString("status") == "ACCEPTED") {
                        confirmReturnButton.visibility = View.VISIBLE
                        confirmReturnButton.setOnClickListener {
                            showConfirmReturnDialog(rentalId)
                        }
                    } else {
                        confirmReturnButton.visibility = View.GONE
                    }
                }
            }
    }

    private fun loadUserDetails(userId: String, isOwner: Boolean) {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val firstName = document.getString("firstname") ?: ""
                    val lastName = document.getString("lastname") ?: ""
                    val profilePic = document.getString("profilepicture") ?: ""
                    val address = document.get("address") as? Map<*, *>
                    
                    findViewById<TextView>(R.id.userName).text = "$firstName $lastName"
                    findViewById<TextView>(R.id.userRole).text = if (isOwner) "Renter" else "Owner"
                    
                    if (profilePic.isNotEmpty()) {
                        Glide.with(this)
                            .load(profilePic)
                            .placeholder(R.drawable.ic_person)
                            .into(findViewById<CircleImageView>(R.id.userImage))
                    }

                    val geoPoint = address?.get("geopoint") as? com.google.firebase.firestore.GeoPoint
                    if (geoPoint != null) {
                        setupMap(geoPoint.latitude, geoPoint.longitude)
                        
                        val street = address["streetname"] as? String ?: ""
                        val number = address["housenumber"] as? String ?: ""
                        val zipcode = address["zipcode"] as? String ?: ""
                        val city = address["city"] as? String ?: ""
                        
                        findViewById<TextView>(R.id.addressText).text = 
                            "$street $number\n$zipcode $city"
                    }
                }
            }
    }

    private fun setupMap(latitude: Double, longitude: Double) {
        val mapController = mapView.controller
        mapController.setZoom(15.0)
        val startPoint = GeoPoint(latitude, longitude)
        mapController.setCenter(startPoint)

        val marker = Marker(mapView)
        marker.position = startPoint
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        mapView.overlays.add(marker)
        mapView.invalidate()
    }

    private fun showConfirmReturnDialog(rentalId: String) {
        val dialog = AlertDialog.Builder(this, R.style.DarkAlertDialog)
            .setTitle("Confirm Return")
            .setMessage("You're about to confirm the item has been returned to you.")
            .setPositiveButton("CONFIRM") { _, _ ->
                updateRentalStatus(rentalId)
            }
            .setNegativeButton("CANCEL", null)
            .show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE)
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.WHITE)
    }

    private fun updateRentalStatus(rentalId: String) {
        db.collection("rentals").document(rentalId)
            .get()
            .addOnSuccessListener { rentalDoc ->
                val renterId = rentalDoc.getString("requestedById")
                val itemTitle = rentalDoc.getString("itemTitle")
                
                rentalDoc.reference.update("status", "COMPLETED")
                    .addOnSuccessListener {
                        val notification = hashMapOf(
                            "userId" to renterId,
                            "title" to "Item Return Confirmed",
                            "message" to "The owner has confirmed the return of $itemTitle",
                            "timestamp" to System.currentTimeMillis(),
                            "read" to false
                        )

                        db.collection("notifications")
                            .add(notification)
                            .addOnSuccessListener {
                                showReviewDialog(renterId ?: "", itemTitle ?: "")
                            }
                    }
            }
    }

    private fun showReviewDialog(renterId: String, itemTitle: String) {
        AlertDialog.Builder(this, R.style.DarkAlertDialog)
            .setTitle("Rate Renter")
            .setMessage("Would you like to rate this renter?")
            .setPositiveButton("YES") { _, _ ->
                val intent = Intent(this, AddReviewActivity::class.java)
                intent.putExtra("userId", renterId)
                intent.putExtra("itemTitle", itemTitle)
                startActivity(intent)
                finish()
            }
            .setNegativeButton("NO") { _, _ ->
                finish()
            }
            .show()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
} 