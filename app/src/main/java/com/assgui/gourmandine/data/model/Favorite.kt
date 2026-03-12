package com.assgui.gourmandine.data.model

data class Favorite(
    val restaurantId: String = "",
    val restaurantName: String = "",
    val restaurantImageUrl: String = "",
    val restaurantAddress: String = "",
    val restaurantRating: Double = 0.0,
    val addedAt: Long = System.currentTimeMillis()
)
