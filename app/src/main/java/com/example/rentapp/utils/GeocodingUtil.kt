package com.example.rentapp.utils

import android.content.Context
import android.location.Geocoder
import com.google.firebase.firestore.GeoPoint
import java.io.IOException
import java.util.Locale

object GeocodingUtil {
    fun getGeoPointFromAddress(context: Context, street: String, city: String): GeoPoint? {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val fullAddress = "$street, $city, Belgium"
            
            val addresses = geocoder.getFromLocationName(fullAddress, 1)
            
            if (!addresses.isNullOrEmpty()) {
                val location = addresses[0]
                return GeoPoint(location.latitude, location.longitude)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }
} 