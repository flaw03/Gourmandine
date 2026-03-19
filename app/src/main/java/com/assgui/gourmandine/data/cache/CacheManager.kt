package com.assgui.gourmandine.data.cache

import android.util.Log
import com.assgui.gourmandine.data.model.Favorite
import com.assgui.gourmandine.data.model.Reservation
import com.assgui.gourmandine.data.model.Restaurant
import com.assgui.gourmandine.data.model.Review
import com.assgui.gourmandine.data.model.User

private const val TAG = "CacheManager"

object CacheManager {

    // ── TTL configuration (ms) ──────────────────────────────────────
    private const val RESTAURANT_TTL = 10 * 60 * 1000L   // 10 min
    private const val REVIEWS_TTL = 5 * 60 * 1000L       // 5 min
    private const val FAVORITES_TTL = 30 * 60 * 1000L    // 30 min
    private const val RESERVATIONS_TTL = 5 * 60 * 1000L  // 5 min
    private const val USER_TTL = 30 * 60 * 1000L         // 30 min
    private const val NEARBY_TTL = 3 * 60 * 1000L        // 3 min

    // ── Caches ──────────────────────────────────────────────────────
    private val restaurants = TypedCache<String, Restaurant>(RESTAURANT_TTL)
    private val nearbyResults = TypedCache<String, List<Restaurant>>(NEARBY_TTL)
    private val reviewsByRestaurant = TypedCache<String, List<Review>>(REVIEWS_TTL)
    private val googleReviewsByRestaurant = TypedCache<String, List<Review>>(REVIEWS_TTL)
    private val reviewsByUser = TypedCache<String, List<Review>>(REVIEWS_TTL)
    private val reviewImages = TypedCache<String, String>(REVIEWS_TTL)
    private val favoritesCache = SingleCache<List<Favorite>>(FAVORITES_TTL)
    private val favoriteIdsCache = SingleCache<Set<String>>(FAVORITES_TTL)
    private val reservationsCache = SingleCache<List<Reservation>>(RESERVATIONS_TTL)
    private val userCache = SingleCache<User>(USER_TTL)

    // ── Nearby key helper ───────────────────────────────────────────
    private fun nearbyKey(lat: Double, lng: Double): String {
        val latR = Math.round(lat * 1000.0) / 1000.0
        val lngR = Math.round(lng * 1000.0) / 1000.0
        return "$latR,$lngR"
    }

    // ── Restaurant ──────────────────────────────────────────────────
    fun putRestaurant(restaurant: Restaurant) {
        if (restaurant.id.isBlank()) return
        restaurants.put(restaurant.id, restaurant)
    }

    fun putRestaurants(list: List<Restaurant>) {
        list.forEach { putRestaurant(it) }
    }

    fun getRestaurant(placeId: String): Restaurant? = restaurants.get(placeId)

    fun getAllCachedRestaurants(): List<Restaurant> = restaurants.getAll()

    // ── Nearby search results ───────────────────────────────────────
    fun putNearbyResults(lat: Double, lng: Double, list: List<Restaurant>) {
        nearbyResults.put(nearbyKey(lat, lng), list)
        putRestaurants(list)
        Log.d(TAG, "putNearby: ${list.size} restaurants cached for ($lat, $lng)")
    }

    fun getNearbyResults(lat: Double, lng: Double): List<Restaurant>? {
        val result = nearbyResults.get(nearbyKey(lat, lng))
        if (result != null) {
            Log.d(TAG, "getNearby: cache hit for ($lat, $lng) -> ${result.size} restaurants")
        }
        return result
    }

    // ── Reviews ─────────────────────────────────────────────────────
    fun putReviews(restaurantId: String, reviews: List<Review>) {
        reviewsByRestaurant.put(restaurantId, reviews)
    }

    fun getReviews(restaurantId: String): List<Review>? = reviewsByRestaurant.get(restaurantId)

    fun putGoogleReviews(restaurantId: String, reviews: List<Review>) {
        googleReviewsByRestaurant.put(restaurantId, reviews)
    }

    fun getGoogleReviews(restaurantId: String): List<Review>? = googleReviewsByRestaurant.get(restaurantId)

    fun putUserReviews(userId: String, reviews: List<Review>) {
        reviewsByUser.put(userId, reviews)
    }

    fun getUserReviews(userId: String): List<Review>? = reviewsByUser.get(userId)

    fun addUserReview(userId: String, review: Review) {
        val current = reviewsByUser.get(userId) ?: emptyList()
        reviewsByUser.put(userId, listOf(review) + current)
        val restaurantReviews = reviewsByRestaurant.get(review.restaurantId) ?: emptyList()
        reviewsByRestaurant.put(review.restaurantId, listOf(review) + restaurantReviews)
    }

    fun removeUserReview(userId: String, reviewId: String) {
        reviewsByUser.get(userId)?.let { reviews ->
            reviewsByUser.put(userId, reviews.filter { it.id != reviewId })
        }
    }

    // ── Favorites ───────────────────────────────────────────────────
    fun putFavorites(favorites: List<Favorite>) {
        favoritesCache.put(favorites)
        favoriteIdsCache.put(favorites.map { it.restaurantId }.toSet())
        Log.d(TAG, "putFavorites: ${favorites.size} cached")
    }

    fun getFavorites(): List<Favorite>? = favoritesCache.get()

    fun getFavoriteIds(): Set<String>? = favoriteIdsCache.get()

    fun addFavorite(favorite: Favorite) {
        val current = favoritesCache.get() ?: emptyList()
        putFavorites(current + favorite)
    }

    fun removeFavorite(restaurantId: String) {
        val current = favoritesCache.get() ?: return
        putFavorites(current.filter { it.restaurantId != restaurantId })
    }

    // ── Reservations ────────────────────────────────────────────────
    fun putReservations(reservations: List<Reservation>) {
        reservationsCache.put(reservations)
        Log.d(TAG, "putReservations: ${reservations.size} cached")
    }

    fun getReservations(): List<Reservation>? = reservationsCache.get()

    fun addReservation(reservation: Reservation) {
        val current = reservationsCache.get() ?: emptyList()
        reservationsCache.put(current + reservation)
    }

    fun removeReservation(reservationId: String) {
        val current = reservationsCache.get() ?: return
        reservationsCache.put(current.filter { it.id != reservationId })
    }

    fun updateReservation(reservationId: String, transform: (Reservation) -> Reservation) {
        val current = reservationsCache.get() ?: return
        reservationsCache.put(current.map { if (it.id == reservationId) transform(it) else it })
    }

    // ── User profile ────────────────────────────────────────────────
    fun putUser(user: User) {
        userCache.put(user)
    }

    fun getUser(): User? = userCache.get()

    // ── Review images ───────────────────────────────────────────────
    fun putReviewImage(restaurantId: String, imageUrl: String) {
        reviewImages.put(restaurantId, imageUrl)
    }

    fun getReviewImage(restaurantId: String): String? = reviewImages.get(restaurantId)

    fun getCachedReviewImages(): Map<String, String> = reviewImages.getAllEntries()

    // ── Clear ───────────────────────────────────────────────────────
    fun clearUserData() {
        favoritesCache.clear()
        favoriteIdsCache.clear()
        reservationsCache.clear()
        userCache.clear()
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
        favoritesCache.clear()
        favoriteIdsCache.clear()
        reservationsCache.clear()
        userCache.clear()
        Log.d(TAG, "clearAll: all caches cleared")
    }
}
