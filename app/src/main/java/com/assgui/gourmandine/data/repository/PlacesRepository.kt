package com.assgui.gourmandine.data.repository

import android.content.Context

import com.assgui.gourmandine.data.model.Restaurant
import com.assgui.gourmandine.data.model.Review
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchResolvedPhotoUriRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchByTextRequest
import com.google.android.libraries.places.api.net.SearchNearbyRequest
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await

sealed class PlacesResult {
    data class Success(val restaurants: List<Restaurant>) : PlacesResult()
    data class Error(val message: String) : PlacesResult()
}

class PlacesRepository(private val placesClient: PlacesClient) {

    companion object {
        fun create(context: Context): PlacesRepository {
            return PlacesRepository(Places.createClient(context))
        }
    }

    private val placeFields = listOf(
        Place.Field.ID,
        Place.Field.DISPLAY_NAME,
        Place.Field.LOCATION,
        Place.Field.RATING,
        Place.Field.USER_RATING_COUNT,
        Place.Field.PRICE_LEVEL,
        Place.Field.CURRENT_OPENING_HOURS,
        Place.Field.FORMATTED_ADDRESS,
        Place.Field.PHOTO_METADATAS,
        Place.Field.EDITORIAL_SUMMARY,
        Place.Field.NATIONAL_PHONE_NUMBER,
        Place.Field.REVIEWS
    )

    suspend fun fetchGoogleReviews(placeId: String): List<Review> {
        return try {
            val request = FetchPlaceRequest.builder(placeId, listOf(Place.Field.REVIEWS)).build()
            val response = placesClient.fetchPlace(request).await()
            val place = response.place
            place.reviews?.map { review ->
                Review(
                    id = "google_${placeId}_${parsePublishTime(review.publishTime)}",
                    restaurantId = placeId,
                    userName = review.authorAttribution?.name ?: "Utilisateur Google",
                    userPhotoUrl = review.authorAttribution?.photoUri?.toString() ?: "",
                    text = review.text ?: "",
                    rating = review.rating,
                    createdAt = parsePublishTime(review.publishTime),
                    isGoogleReview = true
                )
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parsePublishTime(publishTime: String?): Long {
        if (publishTime.isNullOrBlank()) return 0L
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            sdf.parse(publishTime)?.time ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    suspend fun searchNearby(lat: Double, lng: Double, radiusMeters: Double = 1500.0): PlacesResult {
        return try {
            val center = LatLng(lat, lng)
            val bounds = CircularBounds.newInstance(center, radiusMeters)
            val request = SearchNearbyRequest.builder(bounds, placeFields)
                .setIncludedTypes(listOf("restaurant"))
                .setMaxResultCount(20)
                .build()
            val response = placesClient.searchNearby(request).await()
            val restaurants = response.places.map { it.toRestaurant() }
            PlacesResult.Success(restaurants)
        } catch (e: Exception) {
            PlacesResult.Error(e.message ?: "Erreur recherche à proximité")
        }
    }

    suspend fun searchByText(query: String): PlacesResult {
        return try {
            val request = SearchByTextRequest.builder(query, placeFields)
                .setIncludedType("restaurant")
                .setMaxResultCount(20)
                .build()
            val response = placesClient.searchByText(request).await()
            val restaurants = response.places.map { it.toRestaurant() }
            PlacesResult.Success(restaurants)
        } catch (e: Exception) {
            PlacesResult.Error(e.message ?: "Erreur recherche textuelle")
        }
    }

    private suspend fun resolvePhotoUris(place: Place): List<String> = coroutineScope {
        val metadatas = place.photoMetadatas ?: return@coroutineScope emptyList()
        metadatas.take(5).map { metadata ->
            async {
                try {
                    val request = FetchResolvedPhotoUriRequest.builder(metadata)
                        .setMaxWidth(400)
                        .build()
                    val response = placesClient.fetchResolvedPhotoUri(request).await()
                    response.uri?.toString()
                } catch (_: Exception) {
                    null
                }
            }
        }.awaitAll().filterNotNull()
    }

    private suspend fun Place.toRestaurant(): Restaurant {
        val photoUris = resolvePhotoUris(this)
        val country = extractCountry(formattedAddress)

        return Restaurant(
            id = id ?: "",
            name = displayName ?: "",
            imageUrls = photoUris,
            rating = rating ?: 0.0,
            reviewCount = userRatingCount ?: 0,
            country = country,
            priceLevel = priceLevel ?: 1,
            isOpen = currentOpeningHours?.weekdayText?.isNotEmpty() == true,
            latitude = location?.latitude ?: 0.0,
            longitude = location?.longitude ?: 0.0,
            address = formattedAddress ?: "",
            description = editorialSummary ?: "",
            phoneNumber = nationalPhoneNumber ?: ""
        )
    }

    private fun extractCountry(address: String?): String {
        if (address.isNullOrBlank()) return ""
        return address.split(",").lastOrNull()?.trim() ?: ""
    }
}
