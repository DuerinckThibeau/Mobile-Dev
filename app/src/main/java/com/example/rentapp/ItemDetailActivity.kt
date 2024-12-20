package com.example.rentapp

import android.content.Context

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView

import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

import de.hdodenhof.circleimageview.CircleImageView

import android.graphics.Color
import com.example.rentapp.utils.CircleOverlay

class ItemDetailActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_detail)

        db = FirebaseFirestore.getInstance()
        
        Configuration.getInstance().userAgentValue = packageName
        mapView = findViewById(R.id.mapView)
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        
        val itemId = intent.getStringExtra("itemId") ?: return
        
        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }

        loadItemDetails(itemId)
    }

    private fun loadItemDetails(itemId: String) {
        db.collection("items").document(itemId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    findViewById<TextView>(R.id.itemTitle).text = document.getString("title")
                    findViewById<TextView>(R.id.descriptionText).text = document.getString("description")
                    findViewById<TextView>(R.id.categoryLabel).text = document.getString("category")
                    
                    val location = document.get("location") as? Map<*, *>
                    val city = location?.get("city") as? String
                    findViewById<TextView>(R.id.locationText).text = "$city"

                    val imageUrl = document.getString("imageUrl")
                    if (!imageUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(imageUrl)
                            .into(findViewById(R.id.itemImage))
                    }

                    val createdBy = document.getString("createdBy") ?: ""
                    val createdByProfilePic = document.getString("createdByProfilePic") ?: ""
                    
                    findViewById<TextView>(R.id.userName).text = createdBy
                    findViewById<TextView>(R.id.userLocation).text = city
                    
                    if (createdByProfilePic.isNotEmpty()) {
                        Glide.with(this)
                            .load(createdByProfilePic)
                            .into(findViewById<CircleImageView>(R.id.userImage))
                    }

                    findViewById<Button>(R.id.contactButton).setOnClickListener {
                        ContactBottomSheet.newInstance(createdBy, document.id)
                            .show(supportFragmentManager, "ContactBottomSheet")
                    }

                    val geoPoint = location?.get("geopoint") as? com.google.firebase.firestore.GeoPoint
                    if (geoPoint != null) {
                        setupMap(geoPoint.latitude, geoPoint.longitude)
                    }

                    val price = document.getString("price") ?: "0"
                    findViewById<TextView>(R.id.itemPrice).text = "€${price}/day"

                    val ownerId = document.getString("createdById") ?: ""
                    if (ownerId.isNotEmpty()) {
                        loadUserRating(ownerId)
                    }
                }
            }
    }

    private fun setupMap(latitude: Double, longitude: Double) {
        val mapController = mapView.controller
        mapController.setZoom(15.0)
        val startPoint = GeoPoint(latitude, longitude)
        mapController.setCenter(startPoint)

      val circle = CircleOverlay().apply {
    position = startPoint
    fillColor = Color.argb(80, 0, 50, 150)
    strokeColor = Color.argb(130, 0, 70, 170)
    radius = 300.0 
}
        
        mapView.overlays.add(circle)
        mapView.invalidate()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    private fun loadUserRating(userId: String) {
        val ratingBar = findViewById<RatingBar>(R.id.userRating)
        val ratingText = findViewById<TextView>(R.id.ratingText)
        
        db.collection("reviews")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val totalRating = documents.sumOf { it.getDouble("rating")?.toDouble() ?: 0.0 }
                    val averageRating = totalRating / documents.size()
                    ratingBar.rating = averageRating.toFloat()
                    ratingBar.stepSize = 0.5f
                    ratingText.text = String.format("%.1f (%d reviews)", averageRating, documents.size())
                    ratingBar.visibility = View.VISIBLE
                    ratingText.visibility = View.VISIBLE
                } else {
                    ratingBar.visibility = View.GONE
                    ratingText.visibility = View.GONE
                }
            }
            .addOnFailureListener {
                ratingBar.visibility = View.GONE
                ratingText.visibility = View.GONE
            }
    }
} 