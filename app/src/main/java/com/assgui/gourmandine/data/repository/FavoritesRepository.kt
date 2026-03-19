package com.assgui.gourmandine.data.repository

import android.util.Log
import com.assgui.gourmandine.data.cache.CacheManager
import com.assgui.gourmandine.data.model.Favorite
import com.assgui.gourmandine.data.model.Restaurant
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

private const val TAG = "Firestore/Favorites"

class FavoritesRepository {

    private val firestore = FirebaseFirestore.getInstance()

    private fun itemsCollection(uid: String) =
        firestore.collection("favorites").document(uid).collection("items")

    suspend fun toggleFavorite(restaurant: Restaurant): Result<Boolean> {
        val uid = requireUserId().getOrElse { return Result.failure(it) }
        return try {
            val docRef = itemsCollection(uid).document(restaurant.id)
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                docRef.delete().await()
                CacheManager.removeFavorite(restaurant.id)
                Log.d(TAG, "toggleFavorite: supprimé ${restaurant.id}")
                Result.success(false)
            } else {
                val favorite = Favorite(
                    restaurantId = restaurant.id,
                    restaurantName = restaurant.name,
                    restaurantImageUrl = restaurant.imageUrls.firstOrNull() ?: "",
                    restaurantAddress = restaurant.address,
                    restaurantRating = restaurant.rating
                )
                docRef.set(favorite).await()
                CacheManager.addFavorite(favorite)
                Log.d(TAG, "toggleFavorite: ajouté ${restaurant.id}")
                Result.success(true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "toggleFavorite: échec pour ${restaurant.id}", e)
            Result.failure(e)
        }
    }

    suspend fun getFavorites(): Result<List<Favorite>> {
        val uid = requireUserId().getOrElse { return Result.failure(it) }
        CacheManager.getFavorites()?.let {
            Log.d(TAG, "getFavorites: ${it.size} favoris depuis cache")
            return Result.success(it)
        }
        return try {
            Log.d(TAG, "getFavorites: chargement pour uid=$uid")
            val snapshot = itemsCollection(uid).get().await()
            val favorites = snapshot.documents.mapNotNull { it.toObject(Favorite::class.java) }
            CacheManager.putFavorites(favorites)
            Log.d(TAG, "getFavorites: ${favorites.size} favoris chargés")
            Result.success(favorites)
        } catch (e: Exception) {
            Log.e(TAG, "getFavorites: échec", e)
            Result.failure(e)
        }
    }

    suspend fun removeFavorite(restaurantId: String): Result<Unit> {
        val uid = requireUserId().getOrElse { return Result.failure(it) }
        return try {
            itemsCollection(uid).document(restaurantId).delete().await()
            CacheManager.removeFavorite(restaurantId)
            Log.d(TAG, "removeFavorite: supprimé $restaurantId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "removeFavorite: échec pour $restaurantId", e)
            Result.failure(e)
        }
    }

    suspend fun isFavorite(restaurantId: String): Result<Boolean> {
        val uid = requireUserId().getOrElse { return Result.success(false) }
        CacheManager.getFavoriteIds()?.let {
            return Result.success(restaurantId in it)
        }
        return try {
            val snapshot = itemsCollection(uid).document(restaurantId).get().await()
            Result.success(snapshot.exists())
        } catch (e: Exception) {
            Log.e(TAG, "isFavorite: échec pour $restaurantId", e)
            Result.failure(e)
        }
    }
}
