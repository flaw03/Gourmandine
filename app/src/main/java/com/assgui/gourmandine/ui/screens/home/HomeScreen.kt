package com.assgui.gourmandine.ui.screens.home

import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.assgui.gourmandine.ui.components.RestaurantDetailSheet
import com.assgui.gourmandine.ui.screens.home.components.RestaurantMapSection
import com.assgui.gourmandine.ui.screens.home.components.SearchBarRow
import com.assgui.gourmandine.ui.screens.home.components.SheetDragHandle
import com.assgui.gourmandine.ui.screens.home.components.SheetScrollableContent
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

enum class SheetPosition { Down, Middle, Up }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onProfileClick: () -> Unit = {},
    onReservationClick: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(uiState.cameraPosition, uiState.cameraZoom)
    }

    val imeHeightPx = WindowInsets.ime.getBottom(density)
    val isKeyboardOpen by remember { derivedStateOf { imeHeightPx > 0 } }

    var containerHeightPx by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { containerHeightPx = it.size.height }
    ) {
        val fullHeightPx = containerHeightPx.toFloat()
        val downHeightPx = with(density) { 120.dp.toPx() }
        val middleHeightPx = fullHeightPx * 0.5f
        val upHeightPx = fullHeightPx * 0.9f

        val anchors = remember(fullHeightPx) {
            DraggableAnchors {
                SheetPosition.Down at (fullHeightPx - downHeightPx)
                SheetPosition.Middle at (fullHeightPx - middleHeightPx)
                SheetPosition.Up at (fullHeightPx - upHeightPx)
            }
        }

        val sheetState = remember {
            AnchoredDraggableState(
                initialValue = SheetPosition.Middle,
                anchors = anchors,
                positionalThreshold = { distance: Float -> distance * 0.5f },
                velocityThreshold = { with(density) { 125.dp.toPx() } },
                snapAnimationSpec = tween(300),
                decayAnimationSpec = exponentialDecay()
            )
        }

        LaunchedEffect(anchors) {
            sheetState.updateAnchors(anchors)
        }

        val currentPosition by remember {
            derivedStateOf { sheetState.currentValue }
        }

        val middleHeightDp = with(density) { middleHeightPx.toDp() }
        val mapBottomPaddingDp = when (currentPosition) {
            SheetPosition.Down -> 120.dp
            SheetPosition.Middle -> middleHeightDp
            SheetPosition.Up -> middleHeightDp
        }

        val isLocationButtonVisible = currentPosition != SheetPosition.Up

        // --- Événements ---

        LaunchedEffect(isKeyboardOpen) {
            if (isKeyboardOpen) sheetState.animateTo(SheetPosition.Up)
        }

        LaunchedEffect(currentPosition) {
            if (currentPosition == SheetPosition.Down || currentPosition == SheetPosition.Middle) {
                focusManager.clearFocus()
            }
        }

        LaunchedEffect(currentPosition) {
            if (currentPosition == SheetPosition.Up && !isKeyboardOpen) {
                focusRequester.requestFocus()
            }
        }

        LaunchedEffect(uiState.cameraPosition, uiState.cameraZoom) {
            cameraPositionState.animate(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.fromLatLngZoom(uiState.cameraPosition, uiState.cameraZoom)
                )
            )
        }

        LaunchedEffect(uiState.selectedRestaurantId) {
            if (uiState.selectedRestaurantId != null && uiState.restaurants.isNotEmpty()) {
                sheetState.animateTo(SheetPosition.Middle)
                val index = uiState.restaurants.indexOfFirst { it.id == uiState.selectedRestaurantId }
                if (index >= 0) listState.animateScrollToItem(index)
            }
        }

        LaunchedEffect(uiState.clusterBounds) {
            uiState.clusterBounds?.let { bounds ->
                cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(bounds, 100))
            }
        }

        LaunchedEffect(uiState.restaurants.isEmpty()) {
            if (uiState.restaurants.isEmpty()) sheetState.animateTo(SheetPosition.Down)
        }

        // --- Layout ---

        RestaurantMapSection(
            restaurants = uiState.restaurants,
            selectedRestaurantId = uiState.selectedRestaurantId,
            cameraPositionState = cameraPositionState,
            mapBottomPadding = mapBottomPaddingDp,
            onMarkerClick = viewModel::onMarkerClick,
            onMarkerDetailClick = viewModel::onMarkerDetailClick,
            onClusterClick = viewModel::onClusterClick,
            onProfileClick = onProfileClick,
            onReservationClick = onReservationClick,
            onMyLocationClick = viewModel::onMyLocationClick,
            userLocation = uiState.userLocation,
            isLocationButtonVisible = isLocationButtonVisible
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .offset { IntOffset(0, sheetState.requireOffset().toInt()) }
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Color.White)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .anchoredDraggable(sheetState, Orientation.Vertical)
                ) {
                    SheetDragHandle()

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        SearchBarRow(
                            query = uiState.searchQuery,
                            onQueryChange = viewModel::onSearchQueryChange,
                            onSearch = {
                                viewModel.onSearchSubmit(uiState.searchQuery)
                                focusManager.clearFocus()
                            },
                            focusRequester = focusRequester,
                            onFocusChanged = { isFocused ->
                                if (isFocused) {
                                    coroutineScope.launch {
                                        sheetState.animateTo(SheetPosition.Up)
                                    }
                                }
                            }
                        )
                    }
                }

                SheetScrollableContent(
                    uiState = uiState,
                    listState = listState,
                    onCardClick = viewModel::onCardClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        RestaurantDetailSheet(
            restaurant = uiState.detailRestaurant,
            visible = uiState.detailRestaurant != null,
            reviews = uiState.detailReviews,
            onDismiss = viewModel::onDismissDetail,
            modifier = Modifier.fillMaxSize()
        )
    }
}