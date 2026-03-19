package com.assgui.gourmandine.data.repository

import com.assgui.gourmandine.data.cache.CacheManager
import com.assgui.gourmandine.data.model.Review
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await

class ReviewRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val reviewsCollection = firestore.collection("reviews")

    suspend fun getReviewsForRestaurant(restaurantId: String): Result<List<Review>> {
        CacheManager.getReviews(restaurantId)?.let { return Result.success(it) }
        return try {
            val snapshot = reviewsCollection
                .whereEqualTo("restaurantId", restaurantId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            val reviews = snapshot.toObjects(Review::class.java)
            CacheManager.putReviews(restaurantId, reviews)
            Result.success(reviews)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getReviewsByUser(userId: String, fromCache: Boolean = false): Result<List<Review>> {
        if (fromCache) {
            CacheManager.getUserReviews(userId)?.let { return Result.success(it) }
        }
        return try {
            val source = if (fromCache) Source.CACHE else Source.DEFAULT
            val snapshot = reviewsCollection
                .whereEqualTo("userId", userId)
                .get(source)
                .await()
            val reviews = snapshot.toObjects(Review::class.java)
                .sortedByDescending { it.createdAt }
            CacheManager.putUserReviews(userId, reviews)
            Result.success(reviews)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteReview(reviewId: String): Result<Unit> {
        return try {
            reviewsCollection.document(reviewId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addReview(review: Review): Result<String> {
        return try {
            val docRef = reviewsCollection.document()
            val reviewWithId = review.copy(id = docRef.id)
            docRef.set(reviewWithId).await()
            CacheManager.addUserReview(review.userId, reviewWithId)
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
