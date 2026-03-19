package com.assgui.gourmandine.data.cache

import android.util.Log
import com.assgui.gourmandine.data.model.Favorite
import com.assgui.gourmandine.data.model.Reservation
import com.assgui.gourmandine.data.model.Restaurant
import com.assgui.gourmandine.data.model.Review
import com.assgui.gourmandine.data.model.User
import java.util.concurrent.ConcurrentHashMap

private const val TAG = "CacheManager"

object CacheManager {

    // ── TTL configuration (ms) ──────────────────────────────────────
    private const val RESTAURANT_TTL = 10 * 60 * 1000L   // 10 min
    private const val REVIEWS_TTL = 5 * 60 * 1000L       // 5 min
    private const val FAVORITES_TTL = 30 * 60 * 1000L    // 30 min
    private const val RESERVATIONS_TTL = 5 * 60 * 1000L  // 5 min
    private const val USER_TTL = 30 * 60 * 1000L         // 30 min
    private const val NEARBY_TTL = 3 * 60 * 1000L        // 3 min

    // ── Generic cache entry with timestamp ──────────────────────────
    private data class CacheEntry<T>(
        val data: T,
        val timestamp: Long = System.currentTimeMillis()
    ) {
        fun isExpired(ttl: Long): Boolean =
            System.currentTimeMillis() - timestamp > ttl
    }

    // ── Restaurant cache (by placeId) ───────────────────────────────
    private val restaurants = ConcurrentHashMap<String, CacheEntry<Restaurant>>()

    fun putRestaurant(restaurant: Restaurant) {
        if (restaurant.id.isBlank()) return
        restaurants[restaurant.id] = CacheEntry(restaurant)
    }

    fun putRestaurants(list: List<Restaurant>) {
        list.forEach { putRestaurant(it) }
    }

    fun getRestaurant(placeId: String): Restaurant? {
        val entry = restaurants[placeId] ?: return null
        if (entry.isExpired(RESTAURANT_TTL)) {
            restaurants.remove(placeId)
            return null
        }
        return entry.data
    }

    fun getAllCachedRestaurants(): List<Restaurant> =
        restaurants.values
            .filter { !it.isExpired(RESTAURANT_TTL) }
            .map { it.data }

    // ── Nearby search results cache (by area key) ───────────────────
    private data class NearbyKey(val latRounded: Double, val lngRounded: Double)

    private val nearbyResults = ConcurrentHashMap<NearbyKey, CacheEntry<List<Restaurant>>>()

    private fun nearbyKey(lat: Double, lng: Double): NearbyKey =
        NearbyKey(
            latRounded = Math.round(lat * 1000.0) / 1000.0,
            lngRounded = Math.round(lng * 1000.0) / 1000.0
        )

    fun putNearbyResults(lat: Double, lng: Double, list: List<Restaurant>) {
        val key = nearbyKey(lat, lng)
        nearbyResults[key] = CacheEntry(list)
        putRestaurants(list)
        Log.d(TAG, "putNearby: ${list.size} restaurants cached for ($lat, $lng)")
    }

    fun getNearbyResults(lat: Double, lng: Double): List<Restaurant>? {
        val key = nearbyKey(lat, lng)
        val entry = nearbyResults[key] ?: return null
        if (entry.isExpired(NEARBY_TTL)) {
            nearbyResults.remove(key)
            return null
        }
        Log.d(TAG, "getNearby: cache hit for ($lat, $lng) -> ${entry.data.size} restaurants")
        return entry.data
    }

    // ── Reviews cache (by restaurantId) ─────────────────────────────
    private val reviewsByRestaurant = ConcurrentHashMap<String, CacheEntry<List<Review>>>()
    private val googleReviewsByRestaurant = ConcurrentHashMap<String, CacheEntry<List<Review>>>()
    private val reviewsByUser = ConcurrentHashMap<String, CacheEntry<List<Review>>>()

    fun putReviews(restaurantId: String, reviews: List<Review>) {
        reviewsByRestaurant[restaurantId] = CacheEntry(reviews)
    }

    fun getReviews(restaurantId: String): List<Review>? {
        val entry = reviewsByRestaurant[restaurantId] ?: return null
        if (entry.isExpired(REVIEWS_TTL)) {
            reviewsByRestaurant.remove(restaurantId)
            return null
        }
        return entry.data
    }

    fun putGoogleReviews(restaurantId: String, reviews: List<Review>) {
        googleReviewsByRestaurant[restaurantId] = CacheEntry(reviews)
    }

    fun getGoogleReviews(restaurantId: String): List<Review>? {
        val entry = googleReviewsByRestaurant[restaurantId] ?: return null
        if (entry.isExpired(REVIEWS_TTL)) {
            googleReviewsByRestaurant.remove(restaurantId)
            return null
        }
        return entry.data
    }

    fun putUserReviews(userId: String, reviews: List<Review>) {
        reviewsByUser[userId] = CacheEntry(reviews)
    }

    fun getUserReviews(userId: String): List<Review>? {
        val entry = reviewsByUser[userId] ?: return null
        if (entry.isExpired(REVIEWS_TTL)) {
            reviewsByUser.remove(userId)
            return null
        }
        return entry.data
    }

    fun addUserReview(userId: String, review: Review) {
        val current = reviewsByUser[userId]?.data ?: emptyList()
        reviewsByUser[userId] = CacheEntry(listOf(review) + current)
        // Also update restaurant reviews cache
        val restaurantReviews = reviewsByRestaurant[review.restaurantId]?.data ?: emptyList()
        reviewsByRestaurant[review.restaurantId] = CacheEntry(listOf(review) + restaurantReviews)
    }

    fun removeUserReview(userId: String, reviewId: String) {
        reviewsByUser[userId]?.let { entry ->
            reviewsByUser[userId] = CacheEntry(entry.data.filter { it.id != reviewId })
        }
    }

    // ── Favorites cache (for current user) ──────────────────────────
    private var favoritesEntry: CacheEntry<List<Favorite>>? = null
    private var favoriteIdsEntry: CacheEntry<Set<String>>? = null

    fun putFavorites(favorites: List<Favorite>) {
        favoritesEntry = CacheEntry(favorites)
        favoriteIdsEntry = CacheEntry(favorites.map { it.restaurantId }.toSet())
        Log.d(TAG, "putFavorites: ${favorites.size} cached")
    }

    fun getFavorites(): List<Favorite>? {
        val entry = favoritesEntry ?: return null
        if (entry.isExpired(FAVORITES_TTL)) {
            favoritesEntry = null
            return null
        }
        return entry.data
    }

    fun getFavoriteIds(): Set<String>? {
        val entry = favoriteIdsEntry ?: return null
        if (entry.isExpired(FAVORITES_TTL)) {
            favoriteIdsEntry = null
            return null
        }
        return entry.data
    }

    fun addFavorite(favorite: Favorite) {
        val current = favoritesEntry?.data ?: emptyList()
        putFavorites(current + favorite)
    }

    fun removeFavorite(restaurantId: String) {
        val current = favoritesEntry?.data ?: return
        putFavorites(current.filter { it.restaurantId != restaurantId })
    }

    // ── Reservations cache (for current user) ───────────────────────
    private var reservationsEntry: CacheEntry<List<Reservation>>? = null

    fun putReservations(reservations: List<Reservation>) {
        reservationsEntry = CacheEntry(reservations)
        Log.d(TAG, "putReservations: ${reservations.size} cached")
    }

    fun getReservations(): List<Reservation>? {
        val entry = reservationsEntry ?: return null
        if (entry.isExpired(RESERVATIONS_TTL)) {
            reservationsEntry = null
            return null
        }
        return entry.data
    }

    fun addReservation(reservation: Reservation) {
        val current = reservationsEntry?.data ?: emptyList()
        reservationsEntry = CacheEntry(current + reservation)
    }

    fun removeReservation(reservationId: String) {
        val current = reservationsEntry?.data ?: return
        reservationsEntry = CacheEntry(current.filter { it.id != reservationId })
    }

    fun updateReservation(reservationId: String, transform: (Reservation) -> Reservation) {
        val current = reservationsEntry?.data ?: return
        reservationsEntry = CacheEntry(current.map { if (it.id == reservationId) transform(it) else it })
    }

    // ── User profile cache ──────────────────────────────────────────
    private var userEntry: CacheEntry<User>? = null

    fun putUser(user: User) {
        userEntry = CacheEntry(user)
    }

    fun getUser(): User? {
        val entry = userEntry ?: return null
        if (entry.isExpired(USER_TTL)) {
            userEntry = null
            return null
        }
        return entry.data
    }

    // ── Review images cache (restaurantId -> first image URL) ───────
    private val reviewImages = ConcurrentHashMap<String, CacheEntry<String>>()

    fun putReviewImage(restaurantId: String, imageUrl: String) {
        reviewImages[restaurantId] = CacheEntry(imageUrl)
    }

    fun getReviewImage(restaurantId: String): String? {
        val entry = reviewImages[restaurantId] ?: return null
        if (entry.isExpired(REVIEWS_TTL)) {
            reviewImages.remove(restaurantId)
            return null
        }
        return entry.data
    }

    fun getCachedReviewImages(): Map<String, String> =
        reviewImages.entries
            .filter { !it.value.isExpired(REVIEWS_TTL) }
            .associate { it.key to it.value.data }

    // ── Clear all caches (on logout) ────────────────────────────────
    fun clearUserData() {
        favoritesEntry = null
        favoriteIdsEntry = null
        reservationsEntry = null
        userEntry = null
        reviewsByUser.clear()
        Log.d(TAG, "clearUserData: user-specific caches cleared")
    }

    fun clearAll() {
        restaurants.clear()
        nearbyResults.clear()
        reviewsByRestaurant.clear()
        googleReviewsByRestaurant.clear()
        reviewsByUser.clear()
        reviewImages.clear()
        favoritesEntry = null
        favoriteIdsEntry = null
        reservationsEntry = null
        userEntry = null
        Log.d(TAG, "clearAll: all caches cleared")
    }
}
