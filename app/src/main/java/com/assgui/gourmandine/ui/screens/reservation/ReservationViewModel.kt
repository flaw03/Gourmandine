package com.assgui.gourmandine.ui.screens.reservation

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.assgui.gourmandine.data.model.Reservation
import com.assgui.gourmandine.data.repository.ReservationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReservationUiState(
    val upcoming: List<Reservation> = emptyList(),
    val past: List<Reservation> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class ReservationViewModel : ViewModel() {

    private val repository = ReservationRepository()

    private val _uiState = MutableStateFlow(ReservationUiState())
    val uiState: StateFlow<ReservationUiState> = _uiState.asStateFlow()

    init {
        loadReservations()
    }

    fun loadReservations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.getReservations()
                .onSuccess { list ->
                    val now = System.currentTimeMillis()
                    _uiState.update {
                        it.copy(
                            upcoming = list.filter { r -> r.dateMs >= now }.sortedBy { r -> r.dateMs },
                            past = list.filter { r -> r.dateMs < now }.sortedByDescending { r -> r.dateMs },
                            isLoading = false
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun addReservation(reservation: Reservation) {
        viewModelScope.launch {
            repository.addReservation(reservation)
                .onSuccess { loadReservations() }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }

    fun deleteReservation(id: String) {
        viewModelScope.launch {
            repository.deleteReservation(id)
                .onSuccess { loadReservations() }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }

    fun updateDate(id: String, newDateMs: Long) {
        viewModelScope.launch {
            repository.updateReservationDate(id, newDateMs)
                .onSuccess { loadReservations() }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
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
