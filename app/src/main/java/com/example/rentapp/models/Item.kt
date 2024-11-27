package com.example.rentapp.models

data class Item(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val price: String = "0",
    val createdBy: String = "",
    val createdByName: String = "",
    val createdByProfilePic: String = "",
    val category: String = "",
    val location: Map<String, Any> = mapOf(),
    val isRented: Boolean = false
) 