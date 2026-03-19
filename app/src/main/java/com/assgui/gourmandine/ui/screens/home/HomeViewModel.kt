package com.assgui.gourmandine.ui.screens.home

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.assgui.gourmandine.data.model.Restaurant
import com.assgui.gourmandine.data.model.Review
import com.assgui.gourmandine.data.repository.FavoritesRepository
import com.assgui.gourmandine.data.repository.PlacesRepository
import com.assgui.gourmandine.data.repository.PlacesResult
import com.assgui.gourmandine.data.repository.ReviewRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val restaurants: List<Restaurant> = emptyList(),
    val selectedRestaurantId: String? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isOffline: Boolean = false,
    val cameraPosition: LatLng = LatLng(48.8566, 2.3522),
    val cameraZoom: Float = 14f,
    val detailRestaurant: Restaurant? = null,
    val clusterBounds: LatLngBounds? = null,
    val detailReviews: List<Review> = emptyList(),
    val detailGoogleReviews: List<Review> = emptyList(),
    val userLocation: LatLng? = null,
    val restaurantReviewImages: Map<String, String> = emptyMap(),
    val favoriteIds: Set<String> = emptySet(),
    val activeFilters: Set<RestaurantFilter> = emptySet(),
    val filteredRestaurants: List<Restaurant> = emptyList(),
    val maxDistanceKm: Float? = null,
    val pendingReview: Boolean = false,
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val placesRepository = PlacesRepository.create(application)
    private val reviewRepository = ReviewRepository()
    private val favoritesRepository = FavoritesRepository()
    private val connectivityManager = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var initialLoadDone = false
    private var lastSearchLat = 48.8566
    private var lastSearchLng = 2.3522

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _uiState.update { it.copy(isOffline = false) }
            if (_uiState.value.restaurants.isEmpty() && initialLoadDone) {
                val pos = _uiState.value.cameraPosition
                loadNearbyRestaurants(pos.latitude, pos.longitude)
            }
        }

        override fun onLost(network: Network) {
            _uiState.update { it.copy(isOffline = true) }
        }
    }

    init {
        observeNetworkConnectivity()
        loadFavorites()
        listenAuthForFavorites()
        val initialLocation = getLastKnownLocationIfPermitted()
        if (initialLocation != null) {
            _uiState.update {
                it.copy(
                    userLocation = initialLocation,
                    cameraPosition = initialLocation,
                    cameraZoom = 15f
                )
            }
            loadNearbyRestaurants(initialLocation.latitude, initialLocation.longitude)
        } else {
            loadNearbyRestaurants(48.8566, 2.3522)
        }
        initialLoadDone = true
    }

    private var lastFavUid: String? = null

    private fun listenAuthForFavorites() {
        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            val uid = auth.currentUser?.uid
            if (uid != null && uid != lastFavUid) {
                lastFavUid = uid
                loadFavorites()
            } else if (uid == null) {
                lastFavUid = null
                _uiState.update { it.copy(favoriteIds = emptySet()) }
            }
        }
    }

    private fun getLastKnownLocationIfPermitted(): LatLng? {
        val context = getApplication<Application>()
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return null
        }
        return try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            location?.let { LatLng(it.latitude, it.longitude) }
        } catch (e: SecurityException) {
            null
        }
    }

    private fun observeNetworkConnectivity() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

        // Check initial connectivity state
        val isConnected = connectivityManager.activeNetwork?.let { network ->
            connectivityManager.getNetworkCapabilities(network)
                ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } ?: false
        _uiState.update { it.copy(isOffline = !isConnected) }
    }

    override fun onCleared() {
        super.onCleared()
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    fun loadNearbyRestaurants(lat: Double, lng: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = placesRepository.searchNearby(lat, lng)) {
                is PlacesResult.Success -> {
                    _uiState.update { s ->
                        val refLat = s.userLocation?.latitude ?: lat
                        val refLng = s.userLocation?.longitude ?: lng
                        s.copy(
                            restaurants = result.restaurants,
                            filteredRestaurants = applyFilters(result.restaurants, s.activeFilters, s.maxDistanceKm, refLat, refLng),
                            isLoading = false
                        )
                    }
                    loadReviewImagesForRestaurants(result.restaurants)
                }
                is PlacesResult.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onSearchSubmit(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val pos = _uiState.value.cameraPosition
            when (val result = placesRepository.searchByText(query, lat = pos.latitude, lng = pos.longitude)) {
                is PlacesResult.Success -> {
                    val restaurants = result.restaurants
                    val first = restaurants.firstOrNull()
                    _uiState.update { s ->
                        val refLat = s.userLocation?.latitude ?: s.cameraPosition.latitude
                        val refLng = s.userLocation?.longitude ?: s.cameraPosition.longitude
                        s.copy(
                            restaurants = restaurants,
                            filteredRestaurants = applyFilters(restaurants, s.activeFilters, s.maxDistanceKm, refLat, refLng),
                            isLoading = false,
                            selectedRestaurantId = first?.id,
                            cameraPosition = first?.let { r -> LatLng(r.latitude, r.longitude) }
                                ?: s.cameraPosition,
                            cameraZoom = 14f
                        )
                    }
                }
                is PlacesResult.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
            }
        }
    }

    fun onMarkerClick(id: String) {
        _uiState.update { it.copy(selectedRestaurantId = id) }
    }

    fun onCardClick(id: String) {
        val restaurant = _uiState.value.restaurants.find { it.id == id } ?: return
        _uiState.update {
            it.copy(
                selectedRestaurantId = id,
                detailRestaurant = restaurant,
                detailReviews = emptyList(),
                detailGoogleReviews = emptyList()
            )
        }
        loadReviews(id)
    }

    fun onMarkerDetailClick(id: String) {
        val restaurant = _uiState.value.restaurants.find { it.id == id } ?: return
        _uiState.update { it.copy(detailRestaurant = restaurant, detailReviews = emptyList(), detailGoogleReviews = emptyList()) }
        loadReviews(id)
    }

    fun onClusterClick(bounds: LatLngBounds) {
        _uiState.update { it.copy(clusterBounds = bounds) }
    }

    fun onDismissDetail() {
        _uiState.update { it.copy(detailRestaurant = null, detailReviews = emptyList(), detailGoogleReviews = emptyList()) }
    }

    fun refreshDetailReviews() {
        val restaurant = _uiState.value.detailRestaurant ?: return
        loadReviews(restaurant.id)
    }

    fun onMyLocationClick() {
        val context = getApplication<Application>()

        // Check permission
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        try {
            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            location?.let {
                val userPosition = LatLng(it.latitude, it.longitude)
                _uiState.update { state ->
                    state.copy(
                        cameraPosition = userPosition,
                        cameraZoom = 15f,
                        userLocation = userPosition
                    )
                }
                loadNearbyRestaurants(it.latitude, it.longitude)
            }
        } catch (e: SecurityException) {
            // Permission was revoked
        }
    }

    private fun loadReviews(restaurantId: String) {
        viewModelScope.launch {
            // Load Firebase and Google reviews in parallel
            val firebaseJob = launch {
                reviewRepository.getReviewsForRestaurant(restaurantId)
                    .onSuccess { reviews ->
                        _uiState.update { it.copy(detailReviews = reviews) }
                    }
            }
            val googleJob = launch {
                val googleReviews = placesRepository.fetchGoogleReviews(restaurantId)
                _uiState.update { it.copy(detailGoogleReviews = googleReviews) }
            }
            firebaseJob.join()
            googleJob.join()
        }
    }

    private fun loadReviewImagesForRestaurants(restaurants: List<Restaurant>) {
        viewModelScope.launch {
            val imageMap = mutableMapOf<String, String>()
            for (restaurant in restaurants) {
                reviewRepository.getReviewsForRestaurant(restaurant.id)
                    .onSuccess { reviews ->
                        val firstImage = reviews
                            .flatMap { it.imageUrls }
                            .firstOrNull()
                        if (firstImage != null) {
                            imageMap[restaurant.id] = firstImage
                        }
                    }
            }
            _uiState.update { it.copy(restaurantReviewImages = imageMap) }
        }
    }

    fun loadFavorites() {
        viewModelScope.launch {
            favoritesRepository.getFavorites()
                .onSuccess { favorites ->
                    _uiState.update { it.copy(favoriteIds = favorites.map { f -> f.restaurantId }.toSet()) }
                }
                .onFailure { e ->
                    android.util.Log.e("HomeViewModel", "loadFavorites failed: ${e::class.simpleName} - ${e.message}", e)
                }
        }
    }

    fun onToggleFavorite(restaurant: Restaurant) {
        // Mise à jour optimiste : l'icône bascule instantanément
        val wasAlreadyFavorite = restaurant.id in _uiState.value.favoriteIds
        val optimisticIds = _uiState.value.favoriteIds.toMutableSet().apply {
            if (wasAlreadyFavorite) remove(restaurant.id) else add(restaurant.id)
        }
        _uiState.update { it.copy(favoriteIds = optimisticIds) }
        viewModelScope.launch {
            favoritesRepository.toggleFavorite(restaurant)
                .onFailure { e ->
                    // Annule si Firebase échoue
                    val revertedIds = _uiState.value.favoriteIds.toMutableSet().apply {
                        if (wasAlreadyFavorite) add(restaurant.id) else remove(restaurant.id)
                    }
                    android.util.Log.e("HomeViewModel", "toggleFavorite failed: ${e::class.simpleName} - ${e.message}", e)
                    _uiState.update { it.copy(favoriteIds = revertedIds, errorMessage = "Favori: ${e.message}") }
                }
        }
    }

    fun onToggleFilter(filter: RestaurantFilter) {
        _uiState.update { state ->
            val updated = if (filter in state.activeFilters) state.activeFilters - filter else state.activeFilters + filter
            val refLat = state.userLocation?.latitude ?: state.cameraPosition.latitude
            val refLng = state.userLocation?.longitude ?: state.cameraPosition.longitude
            state.copy(
                activeFilters = updated,
                filteredRestaurants = applyFilters(state.restaurants, updated, state.maxDistanceKm, refLat, refLng)
            )
        }
    }

    fun onDistanceFilterChange(km: Float?) {
        _uiState.update { state ->
            val refLat = state.userLocation?.latitude ?: state.cameraPosition.latitude
            val refLng = state.userLocation?.longitude ?: state.cameraPosition.longitude
            state.copy(
                maxDistanceKm = km,
                filteredRestaurants = applyFilters(state.restaurants, state.activeFilters, km, refLat, refLng)
            )
        }
    }

    fun clearFilters() {
        _uiState.update { state ->
            state.copy(
                activeFilters = emptySet(),
                maxDistanceKm = null,
                filteredRestaurants = state.restaurants
            )
        }
    }

    private fun applyFilters(
        list: List<Restaurant>,
        filters: Set<RestaurantFilter>,
        maxDistanceKm: Float? = null,
        refLat: Double = 0.0,
        refLng: Double = 0.0
    ): List<Restaurant> {
        if (filters.isEmpty() && maxDistanceKm == null) return list
        val priceFilters = filters.filter {
            it == RestaurantFilter.PRICE_1 || it == RestaurantFilter.PRICE_2 || it == RestaurantFilter.PRICE_3
        }
        return list.filter { r ->
            val passesOpen = if (RestaurantFilter.OPEN_NOW in filters) r.isOpen else true
            val ratingFilters = filters.filter {
                it == RestaurantFilter.RATING_3_PLUS || it == RestaurantFilter.RATING_35_PLUS ||
                it == RestaurantFilter.RATING_4_PLUS || it == RestaurantFilter.RATING_45_PLUS
            }
            val passesRating = if (ratingFilters.isEmpty()) true else {
                val minRating = ratingFilters.maxOf { f -> when (f) {
                    RestaurantFilter.RATING_3_PLUS -> 3.0
                    RestaurantFilter.RATING_35_PLUS -> 3.5
                    RestaurantFilter.RATING_4_PLUS -> 4.0
                    RestaurantFilter.RATING_45_PLUS -> 4.5
                    else -> 0.0
                }}
                r.rating >= minRating
            }
            @Suppress("UNUSED_VARIABLE")
            val passesRating4 = true
            @Suppress("UNUSED_VARIABLE")
            val passesRating45 = true
            val passesPrice = if (priceFilters.isNotEmpty()) {
                priceFilters.any { f ->
                    when (f) {
                        RestaurantFilter.PRICE_1 -> r.priceLevel == 1
                        RestaurantFilter.PRICE_2 -> r.priceLevel == 2
                        RestaurantFilter.PRICE_3 -> r.priceLevel == 3
                        else -> false
                    }
                }
            } else true
            val passesDistance = if (maxDistanceKm != null) {
                haversineKm(refLat, refLng, r.latitude, r.longitude) <= maxDistanceKm
            } else true
            passesOpen && passesRating && passesPrice && passesDistance
        }
    }

    private fun haversineKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val sinLat = sin(dLat / 2)
        val sinLng = sin(dLng / 2)
        val a = sinLat * sinLat + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sinLng * sinLng
        return 2 * r * atan2(sqrt(a), sqrt(1 - a))
    }

    fun onCameraIdle(lat: Double, lng: Double) {
        if (haversineKm(lastSearchLat, lastSearchLng, lat, lng) > 0.8) {
            lastSearchLat = lat
            lastSearchLng = lng
            loadNearbyRestaurants(lat, lng)
        }
    }

    fun openRestaurantForReview(restaurantId: String) {
        openRestaurantById(restaurantId)
        _uiState.update { it.copy(pendingReview = true) }
    }

    fun consumePendingReview() {
        _uiState.update { it.copy(pendingReview = false) }
    }

    fun openRestaurantById(restaurantId: String) {
        // Cherche d'abord dans la liste déjà chargée
        val cached = _uiState.value.restaurants.find { it.id == restaurantId }
        if (cached != null) {
            _uiState.update {
                it.copy(
                    selectedRestaurantId = restaurantId,
                    detailRestaurant = cached,
                    detailReviews = emptyList(),
                    detailGoogleReviews = emptyList()
                )
            }
            loadReviews(restaurantId)
            return
        }
        // Sinon fetch depuis Places API
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val restaurant = placesRepository.fetchPlaceById(restaurantId)
            if (restaurant != null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        selectedRestaurantId = restaurantId,
                        detailRestaurant = restaurant,
                        detailReviews = emptyList(),
                        detailGoogleReviews = emptyList()
                    )
                }
                loadReviews(restaurantId)
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onRemoveFavoriteFromList(restaurantId: String) {
        val current = _uiState.value.favoriteIds.toMutableSet().apply { remove(restaurantId) }
        _uiState.update { it.copy(favoriteIds = current) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
