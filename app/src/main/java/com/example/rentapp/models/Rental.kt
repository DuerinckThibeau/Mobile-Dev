package com.example.rentapp.models

data class Rental(
    val id: String = "",
    val itemId: String = "",
    val itemTitle: String = "",
    val itemImage: String = "",
    val requestedBy: String = "",
    val requestedById: String = "",
    val requestedByName: String = "",
    val requestedByProfilePic: String = "",
    val ownerId: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val message: String = "",
    val status: String = "PENDING" // PENDING, ACCEPTED, REJECTED
) 