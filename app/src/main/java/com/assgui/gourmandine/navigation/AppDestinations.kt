package com.assgui.gourmandine.navigation

object AppRoutes {
    const val HOME = "home"
    const val RESERVATION = "reservation"
    const val PROFILE = "profile"
    const val RESTAURANT_DETAIL = "restaurant/{restaurantId}"
    const val ADD_REVIEW = "restaurant/{restaurantId}/addReview"
    const val LOGIN_FOR_REVIEW = "restaurant/{restaurantId}/loginForReview"

    fun restaurantDetail(restaurantId: String) = "restaurant/$restaurantId"
    fun addReview(restaurantId: String) = "restaurant/$restaurantId/addReview"
    fun loginForReview(restaurantId: String) = "restaurant/$restaurantId/loginForReview"
}
