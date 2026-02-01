package com.assgui.gourmandine.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.assgui.gourmandine.data.model.Restaurant
import com.assgui.gourmandine.data.repository.PlacesRepository
import com.assgui.gourmandine.data.repository.PlacesResult
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
    val cameraPosition: LatLng = LatLng(48.8566, 2.3522),
    val cameraZoom: Float = 14f,
    val detailRestaurant: Restaurant? = null,
    val clusterBounds: LatLngBounds? = null
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val placesRepository = PlacesRepository.create(application)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadNearbyRestaurants(48.8566, 2.3522)
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
            _uiState.update { it.copy(detailRestaurant = restaurant) }
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
        _uiState.update { it.copy(detailRestaurant = restaurant) }
    }

    fun onClusterClick(bounds: LatLngBounds) {
        _uiState.update { it.copy(clusterBounds = bounds) }
    }

    fun onDismissDetail() {
        _uiState.update { it.copy(detailRestaurant = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
