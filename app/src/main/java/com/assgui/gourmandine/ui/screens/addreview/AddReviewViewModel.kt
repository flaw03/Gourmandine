package com.assgui.gourmandine.ui.screens.addreview

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.assgui.gourmandine.data.model.Review
import com.assgui.gourmandine.data.repository.ImageStorageRepository
import com.assgui.gourmandine.data.repository.ReviewRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddReviewUiState(
    val rating: Int = 0,
    val comment: String = "",
    val selectedImageUris: List<Uri> = emptyList(),
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val isSubmitted: Boolean = false,
    val visitDate: Long? = null,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

class AddReviewViewModel(
    private val reviewRepository: ReviewRepository = ReviewRepository(),
    private val imageStorageRepository: ImageStorageRepository = ImageStorageRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddReviewUiState())
    val uiState: StateFlow<AddReviewUiState> = _uiState.asStateFlow()

    fun onRatingChange(rating: Int) {
        _uiState.update { it.copy(rating = rating, errorMessage = null) }
    }

    fun onCommentChange(comment: String) {
        _uiState.update { it.copy(comment = comment, errorMessage = null) }
    }

    fun onVisitDateChange(date: Long) {
        _uiState.update { it.copy(visitDate = date) }
    }

    fun addImage(uri: Uri) {
        _uiState.update { state ->
            if (state.selectedImageUris.size < 5) {
                state.copy(selectedImageUris = state.selectedImageUris + uri)
            } else {
                state.copy(errorMessage = "Maximum 5 photos autorisées")
            }
        }
    }

    fun removeImage(index: Int) {
        _uiState.update { state ->
            state.copy(
                selectedImageUris = state.selectedImageUris.toMutableList().apply {
                    removeAt(index)
                }
            )
        }
    }

    fun replaceImage(index: Int, uri: Uri) {
        _uiState.update { state ->
            state.copy(
                selectedImageUris = state.selectedImageUris.toMutableList().apply {
                    set(index, uri)
                }
            )
        }
    }

    fun setLocation(latitude: Double, longitude: Double) {
        _uiState.update { it.copy(latitude = latitude, longitude = longitude) }
    }

    fun submitReview(restaurantId: String) {
        val state = _uiState.value
        if (state.visitDate == null) {
            _uiState.update { it.copy(errorMessage = "Veuillez sélectionner une date de visite") }
            return
        }
        if (state.visitDate > System.currentTimeMillis()) {
            _uiState.update { it.copy(errorMessage = "La date de visite ne peut pas être dans le futur") }
            return
        }
        if (state.rating == 0) {
            _uiState.update { it.copy(errorMessage = "Veuillez donner une note") }
            return
        }
        if (state.comment.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Veuillez écrire un commentaire") }
            return
        }

        val currentUser = FirebaseAuth.getInstance().currentUser ?: return

        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val imageUrls = if (state.selectedImageUris.isNotEmpty()) {
                    imageStorageRepository.uploadImages(state.selectedImageUris, currentUser.uid)
                        .getOrThrow()
                } else {
                    emptyList()
                }

                val review = Review(
                    restaurantId = restaurantId,
                    userId = currentUser.uid,
                    userName = currentUser.displayName ?: "Utilisateur",
                    imageUrls = imageUrls,
                    text = state.comment,
                    rating = state.rating.toDouble(),
                    createdAt = System.currentTimeMillis(),
                    visitDate = state.visitDate ?: System.currentTimeMillis(),
                    latitude = state.latitude,
                    longitude = state.longitude
                )

                reviewRepository.addReview(review).getOrThrow()

                _uiState.update {
                    it.copy(isSubmitting = false, isSubmitted = true)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = "Erreur: ${e.message}"
                    )
                }
            }
        }
    }
}
