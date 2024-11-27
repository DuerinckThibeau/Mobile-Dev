package com.example.rentapp

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
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
        
        // Initialize OSMDroid
        Configuration.getInstance().userAgentValue = packageName
        mapView = findViewById(R.id.mapView)
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        
        val rentalId = intent.getStringExtra("rentalId") ?: return
        val isOwner = intent.getBooleanExtra("isOwner", false)
        
        // Add back button functionality
        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }

        loadRentalDetails(rentalId, isOwner)
    }

    private fun loadRentalDetails(rentalId: String, isOwner: Boolean) {
        db.collection("rentals").document(rentalId)
            .get()
            .addOnSuccessListener { rentalDoc ->
                if (rentalDoc != null) {
                    // Load basic rental info
                    findViewById<TextView>(R.id.itemTitle).text = rentalDoc.getString("itemTitle")
                    findViewById<TextView>(R.id.dateRange).text = 
                        "${rentalDoc.getString("startDate")} - ${rentalDoc.getString("endDate")}"
                    findViewById<TextView>(R.id.message).text = rentalDoc.getString("message")
                    
                    // Load item image
                    val imageUrl = rentalDoc.getString("itemImage")
                    if (!imageUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(imageUrl)
                            .into(findViewById(R.id.itemImage))
                    }

                    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                    val ownerId = rentalDoc.getString("ownerId")
                    
                    if (currentUserId == ownerId) {
                        // Current user is the owner, show renter's info
                        val renterId = rentalDoc.getString("requestedById")
                        loadUserDetails(renterId ?: "", true)
                    } else {
                        // Current user is the renter, show owner's info
                        loadUserDetails(ownerId ?: "", false)
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
                    
                    // Set user info
                    findViewById<TextView>(R.id.userName).text = "$firstName $lastName"
                    findViewById<TextView>(R.id.userRole).text = if (isOwner) "Renter" else "Owner"
                    
                    if (profilePic.isNotEmpty()) {
                        Glide.with(this)
                            .load(profilePic)
                            .placeholder(R.drawable.ic_person)
                            .into(findViewById<CircleImageView>(R.id.userImage))
                    }

                    // Setup address and map
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

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
} 