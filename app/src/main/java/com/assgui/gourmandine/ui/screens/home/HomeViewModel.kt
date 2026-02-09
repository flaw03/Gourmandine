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
import com.assgui.gourmandine.data.repository.PlacesRepository
import com.assgui.gourmandine.data.repository.PlacesResult
import com.assgui.gourmandine.data.repository.ReviewRepository
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
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
    val restaurantReviewImages: Map<String, String> = emptyMap()
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val placesRepository = PlacesRepository.create(application)
    private val reviewRepository = ReviewRepository()
    private val connectivityManager = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var initialLoadDone = false

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
                    _uiState.update {
                        it.copy(
                            restaurants = result.restaurants,
                            isLoading = false,
                            cameraPosition = LatLng(lat, lng)
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
            when (val result = placesRepository.searchByText(query)) {
                is PlacesResult.Success -> {
                    val restaurants = result.restaurants
                    val first = restaurants.firstOrNull()
                    _uiState.update {
                        it.copy(
                            restaurants = restaurants,
                            isLoading = false,
                            selectedRestaurantId = first?.id,
                            cameraPosition = first?.let { r -> LatLng(r.latitude, r.longitude) }
                                ?: it.cameraPosition,
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
        val alreadySelected = _uiState.value.selectedRestaurantId == id
        if (alreadySelected) {
            // Already selected → open detail
            _uiState.update { it.copy(detailRestaurant = restaurant, detailReviews = emptyList(), detailGoogleReviews = emptyList()) }
            loadReviews(id)
        } else {
            // Not selected → select + move camera
            _uiState.update {
                it.copy(
                    selectedRestaurantId = id,
                    cameraPosition = LatLng(restaurant.latitude, restaurant.longitude),
                    cameraZoom = 16f
                )
            }
        }
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

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
