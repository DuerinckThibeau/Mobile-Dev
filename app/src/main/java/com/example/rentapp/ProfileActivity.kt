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

class ProfileActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        loadUserData()
        setupBottomNavigation()

        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }

        findViewById<ImageButton>(R.id.editButton).setOnClickListener { view ->
            val wrapper = ContextThemeWrapper(this, R.style.PopupMenuTheme)
            val popup = PopupMenu(wrapper, view)
            popup.menuInflater.inflate(R.menu.profile_menu, popup.menu)

            // Make menu items white
            for (i in 0 until popup.menu.size()) {
                popup.menu.getItem(i).title = SpannableString(popup.menu.getItem(i).title).apply {
                    setSpan(ForegroundColorSpan(Color.WHITE), 0, length, 0)
                }
            }

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_edit_profile -> {
                        // TODO
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
                            document.getString("phone") ?: ""
                        
                        val profileImageView = findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.profileImage)
                        if (profilePicture.isNotEmpty()) {
                            Glide.with(this)
                                .load(profilePicture)
                                .placeholder(R.drawable.ic_person)
                                .error(R.drawable.ic_person)
                                .into(profileImageView)
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
} 