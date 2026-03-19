package com.assgui.gourmandine.ui.screens.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.assgui.gourmandine.data.ServiceLocator
import com.assgui.gourmandine.data.model.Favorite
import com.assgui.gourmandine.data.repository.FavoritesRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FavoritesUiState(
    val favorites: List<Favorite> = emptyList(),
    val isLoading: Boolean = false
)

class FavoritesViewModel(
    private val repository: FavoritesRepository = ServiceLocator.favoritesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    private var lastLoadedUid: String? = null

    init {
        val auth = FirebaseAuth.getInstance()
        auth.addAuthStateListener { firebaseAuth ->
            val uid = firebaseAuth.currentUser?.uid
            if (uid != null && uid != lastLoadedUid) {
                lastLoadedUid = uid
                loadFavorites()
            } else if (uid == null) {
                lastLoadedUid = null
                _uiState.update { FavoritesUiState() }
            }
        }
    }

    fun loadFavorites() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getFavorites()
                .onSuccess { favorites ->
                    _uiState.update { it.copy(favorites = favorites, isLoading = false) }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoading = false) }
                }
        }
    }

    fun removeFavorite(restaurantId: String) {
        viewModelScope.launch {
            repository.removeFavorite(restaurantId)
            _uiState.update { it.copy(favorites = it.favorites.filter { f -> f.restaurantId != restaurantId }) }
        }
    }
}
