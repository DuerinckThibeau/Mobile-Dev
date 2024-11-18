package com.example.rentapp.models

data class Item(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val createdBy: String = "",
    val createdByProfilePic: String = "",
    val category: String = "",
    val location: Map<String, Any> = mapOf()
) 