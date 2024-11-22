package com.example.rentapp

import android.content.Context
import android.view.ContextThemeWrapper
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import de.hdodenhof.circleimageview.CircleImageView
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.graphics.Color

class ItemDetailActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_detail)

        db = FirebaseFirestore.getInstance()
        
        // Initialize OSMDroid
        Configuration.getInstance().userAgentValue = packageName
        mapView = findViewById(R.id.mapView)
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        
        val itemId = intent.getStringExtra("itemId") ?: return
        
        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }

        findViewById<ImageButton>(R.id.settingsButton).setOnClickListener { view ->
            val wrapper = ContextThemeWrapper(this, R.style.PopupMenuTheme)
            val popup = PopupMenu(wrapper, view)
            popup.menuInflater.inflate(R.menu.item_menu, popup.menu)

            // Make all menu items white
            for (i in 0 until popup.menu.size()) {
                val item = popup.menu.getItem(i)
                val s = SpannableString(item.title)
                s.setSpan(ForegroundColorSpan(Color.WHITE), 0, s.length, 0)
                item.title = s
            }

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_edit_item -> {
                        // Handle edit
                        true
                    }
                    R.id.action_delete_item -> {
                        // Handle delete
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

        loadItemDetails(itemId)
    }

    private fun loadItemDetails(itemId: String) {
        db.collection("items").document(itemId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    // Load item details
                    findViewById<TextView>(R.id.itemTitle).text = document.getString("title")
                    findViewById<TextView>(R.id.descriptionText).text = document.getString("description")
                    findViewById<TextView>(R.id.categoryLabel).text = document.getString("category")
                    
                    val location = document.get("location") as? Map<*, *>
                    val city = location?.get("city") as? String
                    findViewById<TextView>(R.id.locationText).text = "$city"

                    // Load item image
                    val imageUrl = document.getString("imageUrl")
                    if (!imageUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(imageUrl)
                            .into(findViewById(R.id.itemImage))
                    }

                    // Load user details
                    val createdBy = document.getString("createdBy") ?: ""
                    val createdByProfilePic = document.getString("createdByProfilePic") ?: ""
                    
                    findViewById<TextView>(R.id.userName).text = createdBy
                    findViewById<TextView>(R.id.userLocation).text = city
                    
                    if (createdByProfilePic.isNotEmpty()) {
                        Glide.with(this)
                            .load(createdByProfilePic)
                            .into(findViewById<CircleImageView>(R.id.userImage))
                    }

                    // Setup contact button
                    findViewById<Button>(R.id.contactButton).setOnClickListener {
                        ContactBottomSheet.newInstance(createdBy, document.id)
                            .show(supportFragmentManager, "ContactBottomSheet")
                    }

                    // Setup map
                    val geoPoint = location?.get("geopoint") as? com.google.firebase.firestore.GeoPoint
                    if (geoPoint != null) {
                        setupMap(geoPoint.latitude, geoPoint.longitude)
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