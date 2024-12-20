package com.example.rentapp.models

data class Notification(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val timestamp: Long = 0,
    val read: Boolean = false
) {
    constructor() : this("", "", "", "", 0, false)
} 