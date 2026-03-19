package com.assgui.gourmandine.ui.screens.reservation

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.assgui.gourmandine.data.model.Reservation
import com.assgui.gourmandine.data.model.Review
import com.assgui.gourmandine.data.repository.ReservationRepository
import com.assgui.gourmandine.data.repository.ReviewRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReservationUiState(
    val upcoming: List<Reservation> = emptyList(),
    val past: List<Reservation> = emptyList(),
    val myReviews: List<Review> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingReviews: Boolean = false,
    val error: String? = null
)

private const val TAG = "ReservationVM"

class ReservationViewModel : ViewModel() {

    private val repository = ReservationRepository()
    private val reviewRepository = ReviewRepository()

    private val _uiState = MutableStateFlow(ReservationUiState())
    val uiState: StateFlow<ReservationUiState> = _uiState.asStateFlow()

    private var lastLoadedUid: String? = null

    init {
        listenAuthChanges()
    }

    private fun listenAuthChanges() {
        val auth = FirebaseAuth.getInstance()
        // Charge immédiatement si déjà connecté
        auth.currentUser?.uid?.let { uid ->
            lastLoadedUid = uid
            loadReservations()
            loadMyReviews()
        }
        auth.addAuthStateListener { firebaseAuth ->
            val uid = firebaseAuth.currentUser?.uid
            Log.d(TAG, "authStateChanged: uid=$uid lastLoaded=$lastLoadedUid")
            if (uid != null && uid != lastLoadedUid) {
                lastLoadedUid = uid
                loadReservations()
                loadMyReviews()
            } else if (uid == null) {
                lastLoadedUid = null
                _uiState.update { ReservationUiState() }
            }
        }
    }

    fun loadMyReviews() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        _uiState.update { it.copy(isLoadingReviews = true) }
        viewModelScope.launch {
            Log.d(TAG, "loadMyReviews: début pour uid=$userId")
            reviewRepository.getReviewsByUser(userId, fromCache = false)
                .onSuccess { fresh ->
                    Log.d(TAG, "loadMyReviews: ${fresh.size} avis chargés")
                    _uiState.update { it.copy(myReviews = fresh, isLoadingReviews = false) }
                }
                .onFailure { e ->
                    Log.e(TAG, "loadMyReviews: échec", e)
                    _uiState.update { it.copy(isLoadingReviews = false) }
                }
        }
    }

    fun deleteMyReview(reviewId: String) {
        val removed = _uiState.value.myReviews.find { it.id == reviewId }
        _uiState.update { it.copy(myReviews = it.myReviews.filter { r -> r.id != reviewId }) }
        viewModelScope.launch {
            reviewRepository.deleteReview(reviewId).onFailure {
                if (removed != null) {
                    _uiState.update { state -> state.copy(myReviews = (state.myReviews + removed).sortedByDescending { r -> r.createdAt }) }
                }
            }
        }
    }

    fun loadReservations() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            Log.d(TAG, "loadReservations: début pour uid=${FirebaseAuth.getInstance().currentUser?.uid}")

            repository.getReservations(fromCache = false)
                .onSuccess { list ->
                    Log.d(TAG, "loadReservations: ${list.size} réservations chargées")
                    _uiState.update {
                        it.copy(
                            upcoming = list.filter { r -> r.dateMs >= now }.sortedBy { r -> r.dateMs },
                            past = list.filter { r -> r.dateMs < now }.sortedByDescending { r -> r.dateMs },
                            isLoading = false
                        )
                    }
                }
                .onFailure { e ->
                    Log.e(TAG, "loadReservations: échec", e)
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun addReservation(reservation: Reservation) {
        // Mise à jour optimiste : apparaît immédiatement dans la liste
        val tempId = java.util.UUID.randomUUID().toString()
        val optimistic = reservation.copy(id = tempId)
        _uiState.update { state ->
            state.copy(upcoming = (state.upcoming + optimistic).sortedBy { it.dateMs })
        }
        viewModelScope.launch {
            repository.addReservation(reservation)
                .onSuccess { realId ->
                    // Remplace l'ID temporaire par l'ID Firebase réel
                    _uiState.update { state ->
                        state.copy(upcoming = state.upcoming.map { r ->
                            if (r.id == tempId) r.copy(id = realId) else r
                        })
                    }
                }
                .onFailure { e ->
                    // Annule l'ajout optimiste
                    _uiState.update { state ->
                        state.copy(
                            upcoming = state.upcoming.filter { it.id != tempId },
                            error = e.message
                        )
                    }
                }
        }
    }

    fun deleteReservation(id: String) {
        // Mise à jour optimiste : retire immédiatement
        val removedUpcoming = _uiState.value.upcoming.find { it.id == id }
        val removedPast = _uiState.value.past.find { it.id == id }
        _uiState.update { state ->
            state.copy(
                upcoming = state.upcoming.filter { it.id != id },
                past = state.past.filter { it.id != id }
            )
        }
        viewModelScope.launch {
            repository.deleteReservation(id)
                .onFailure { e ->
                    // Remet la réservation en cas d'échec
                    _uiState.update { state ->
                        state.copy(
                            upcoming = if (removedUpcoming != null)
                                (state.upcoming + removedUpcoming).sortedBy { it.dateMs }
                            else state.upcoming,
                            past = if (removedPast != null)
                                (state.past + removedPast).sortedByDescending { it.dateMs }
                            else state.past,
                            error = e.message
                        )
                    }
                }
        }
    }

    fun updateDate(id: String, newDateMs: Long) {
        val prevUpcoming = _uiState.value.upcoming
        val prevPast = _uiState.value.past
        val reservation = (prevUpcoming + prevPast).find { it.id == id } ?: return
        val updated = reservation.copy(dateMs = newDateMs)
        val now = System.currentTimeMillis()
        val allUpdated = (prevUpcoming + prevPast).map { if (it.id == id) updated else it }
        // Mise à jour optimiste
        _uiState.update { state ->
            state.copy(
                upcoming = allUpdated.filter { it.dateMs >= now }.sortedBy { it.dateMs },
                past = allUpdated.filter { it.dateMs < now }.sortedByDescending { it.dateMs }
            )
        }
        viewModelScope.launch {
            repository.updateReservationDate(id, newDateMs)
                .onFailure { e ->
                    // Remet l'ancienne date en cas d'échec
                    _uiState.update { state ->
                        state.copy(upcoming = prevUpcoming, past = prevPast, error = e.message)
                    }
                }
        }
    }

    fun addToCalendar(context: Context, reservation: Reservation) {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, "Réservation ${reservation.restaurantName}")
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, reservation.dateMs)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, reservation.dateMs + 2 * 3600_000L)
            putExtra(CalendarContract.Events.EVENT_LOCATION, reservation.restaurantAddress)
        }
        context.startActivity(intent)
    }
}
