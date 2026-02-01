package com.assgui.gourmandine.data.model

data class Review(
    val id: String = "",
    val restaurantId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userReviewCount: Int = 0,
    val userCreatedAt: Long = 0L,
    val imageUrls: List<String> = emptyList(),
    val text: String = "",
    val rating: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)
