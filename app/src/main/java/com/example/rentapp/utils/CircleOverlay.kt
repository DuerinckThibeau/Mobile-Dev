package com.example.rentapp.utils

import android.graphics.Canvas
import android.graphics.Paint
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.Projection
import kotlin.math.*

class CircleOverlay : Overlay() {
    var position: GeoPoint? = null
    var fillColor: Int = 0
    var strokeColor: Int = 0
    var strokeWidth: Float = 0f
    var radius: Double = 0.0  // Radius in meters

    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val strokePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
    }

    override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
        if (shadow) return

        position?.let { pos ->
            val proj = mapView.projection
            val point = proj.toPixels(pos, null)
            
            // Convert radius from meters to pixels
            val radiusInPixels = getRadiusInPixels(proj, pos, radius)
            
            // Draw fill
            paint.color = fillColor
            canvas.drawCircle(point.x.toFloat(), point.y.toFloat(), radiusInPixels, paint)
            
            // Draw stroke
            strokePaint.color = strokeColor
            strokePaint.strokeWidth = strokeWidth
            canvas.drawCircle(point.x.toFloat(), point.y.toFloat(), radiusInPixels, strokePaint)
        }
    }

    private fun getRadiusInPixels(proj: Projection, center: GeoPoint, radiusInMeters: Double): Float {
        // Calculate meters per pixel at current zoom level and latitude
        val metersPerPixel = calculateMetersPerPixel(center.latitude, proj.zoomLevel)
        return (radiusInMeters / metersPerPixel).toFloat()
    }

    private fun calculateMetersPerPixel(latitude: Double, zoom: Double): Double {
        val earthRadius = 6378137.0 // Earth's radius in meters
        val latRad = Math.toRadians(latitude)
        return cos(latRad) * 2 * PI * earthRadius / (256 * 2.0.pow(zoom))
    }
} 