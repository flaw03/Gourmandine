package com.assgui.gourmandine.data.model

data class Restaurant(
    val id: String = "",
    val name: String = "",
    val imageUrls: List<String> = emptyList(),
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val country: String = "",
    val priceLevel: Int = 1,
    val isOpen: Boolean = false,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = "",
    val description: String = "",
    val phoneNumber: String = ""
)
