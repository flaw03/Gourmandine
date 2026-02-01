package com.assgui.gourmandine.data.repository

import com.assgui.gourmandine.data.model.Review
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class ReviewRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val reviewsCollection = firestore.collection("reviews")

    suspend fun getReviewsForRestaurant(restaurantId: String): Result<List<Review>> {
        return try {
            val snapshot = reviewsCollection
                .whereEqualTo("restaurantId", restaurantId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            val reviews = snapshot.toObjects(Review::class.java)
            Result.success(reviews)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}