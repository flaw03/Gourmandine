package com.assgui.gourmandine.data.model

data class Reservation(
    val id: String = "",
    val userId: String = "",
    val restaurantId: String = "",
    val restaurantName: String = "",
    val restaurantAddress: String = "",
    val restaurantImageUrl: String = "",
    val dateMs: Long = 0L,
    val partySize: Int = 2,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val isPast: Boolean get() = dateMs < System.currentTimeMillis()
}
