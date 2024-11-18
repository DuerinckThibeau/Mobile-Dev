package com.example.rentapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.PopupMenu
import android.view.ContextThemeWrapper
import android.graphics.Color
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class ProfileActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        Configuration.getInstance().userAgentValue = packageName
        mapView = findViewById(R.id.mapView)
        mapView.setTileSource(TileSourceFactory.MAPNIK)

        loadUserData()
        setupBottomNavigation()

        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }

        findViewById<ImageButton>(R.id.editButton).setOnClickListener { view ->
            val wrapper = ContextThemeWrapper(this, R.style.PopupMenuTheme)
            val popup = PopupMenu(wrapper, view)
            popup.menuInflater.inflate(R.menu.profile_menu, popup.menu)

            for (i in 0 until popup.menu.size()) {
                popup.menu.getItem(i).title = SpannableString(popup.menu.getItem(i).title).apply {
                    setSpan(ForegroundColorSpan(Color.WHITE), 0, length, 0)
                }
            }

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_edit_profile -> {
                        startActivityForResult(Intent(this, EditProfileActivity::class.java), EDIT_PROFILE_REQUEST)
                        true
                    }
                    R.id.action_logout -> {
                        auth.signOut()
                        val intent = Intent(this, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            db.collection("users").document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val firstName = document.getString("firstname") ?: ""
                        val lastName = document.getString("lastname") ?: ""
                        val profilePicture = document.getString("profilepicture") ?: ""
                        val address = document.get("address") as? Map<String, Any>
                        
                        findViewById<TextView>(R.id.userName).text = "$firstName $lastName"
                        
                        val street = address?.get("streetname") as? String ?: ""
                        val number = address?.get("housenumber") as? String ?: ""
                        val zipcode = address?.get("zipcode") as? String ?: ""
                        val city = address?.get("city") as? String ?: ""
                        
                        findViewById<TextView>(R.id.addressText).text = 
                            "$street $number\n$zipcode $city"
                        
                        findViewById<TextView>(R.id.phoneText).text = 
                            formatPhoneNumber(document.getString("phone") ?: "")
                        
                        val profileImageView = findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.profileImage)
                        if (profilePicture.isNotEmpty()) {
                            Glide.with(this)
                                .load(profilePicture)
                                .placeholder(R.drawable.ic_person)
                                .error(R.drawable.ic_person)
                                .into(profileImageView)
                        }

                        // Setup map with user's location
                        val geoPoint = address?.get("geopoint") as? com.google.firebase.firestore.GeoPoint
                        if (geoPoint != null) {
                            setupMap(geoPoint.latitude, geoPoint.longitude)
                        }
                    }
                }
        }
    }

    private fun setupBottomNavigation() {
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navView.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    true
                }
                R.id.navigation_list -> {
                    startActivity(Intent(this, ListActivity::class.java))
                    true
                }
                R.id.navigation_profile -> true
                else -> false
            }
        }
        navView.selectedItemId = R.id.navigation_profile
    }

    companion object {
        private const val EDIT_PROFILE_REQUEST = 1
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EDIT_PROFILE_REQUEST && resultCode == RESULT_OK) {
            loadUserData() 
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

    private fun formatPhoneNumber(phone: String): String {
        return phone.replace("\\s".toRegex(), "") 
            .let { 
                if (it.length >= 10) {
                    "${it.substring(0,4)} ${it.substring(4,6)} ${it.substring(6,8)} ${it.substring(8)}"
                } else {
                    it  
                }
            }
    }
} 