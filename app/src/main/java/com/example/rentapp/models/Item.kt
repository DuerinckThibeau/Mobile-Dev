package com.example.rentapp.models

data class Item(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val createdBy: String = "",
    val createdByProfilePic: String = "",
    val imageUrl: String = "",
    val price: Double = 0.0,
    val category: String = ""
) 