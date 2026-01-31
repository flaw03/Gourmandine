package com.assgui.gourmandine.ui.screens.home.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.assgui.gourmandine.data.model.Restaurant
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState

@Composable
fun RestaurantMapSection(
    restaurants: List<Restaurant>,
    selectedRestaurantId: String?,
    cameraPositionState: CameraPositionState,
    mapBottomPadding: Dp,
    onMarkerClick: (String) -> Unit,
    onProfileClick: () -> Unit,
    onReservationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            contentPadding = PaddingValues(bottom = mapBottomPadding)
        ) {
            restaurants.forEach { restaurant ->
                val isSelected = restaurant.id == selectedRestaurantId
                Marker(
                    state = MarkerState(
                        position = LatLng(restaurant.latitude, restaurant.longitude)
                    ),
                    title = restaurant.name,
                    snippet = if (isSelected) {
                        "${restaurant.rating} · ${if (restaurant.isOpen) "Open" else "Closed"}"
                    } else null,
                    icon = BitmapDescriptorFactory.defaultMarker(
                        if (isSelected) BitmapDescriptorFactory.HUE_ORANGE
                        else BitmapDescriptorFactory.HUE_RED
                    ),
                    onClick = {
                        onMarkerClick(restaurant.id)
                        true
                    }
                )
            }
        }

        MapHeaderOverlay(
            onProfileClick = onProfileClick,
            onReservationClick = onReservationClick,
            modifier = Modifier.align(Alignment.TopStart)
        )
    }
}