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
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.abs
import com.assgui.gourmandine.ui.theme.AppColors
import com.assgui.gourmandine.ui.theme.AppShapes
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.assgui.gourmandine.data.model.Restaurant
import com.assgui.gourmandine.navigation.AppRoutes
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import com.assgui.gourmandine.ui.components.NavTab
import com.assgui.gourmandine.ui.components.RestaurantDetailSheet
import com.assgui.gourmandine.ui.screens.addreview.AddReviewScreen
import com.assgui.gourmandine.ui.screens.favorites.FavoritesScreen
import com.assgui.gourmandine.ui.screens.profile.ProfileScreen
import com.assgui.gourmandine.ui.screens.reservation.ReservationScreen
import com.assgui.gourmandine.ui.screens.reservation.ReservationViewModel
import com.assgui.gourmandine.ui.screens.reservation.components.ReservationBookingDialog
import com.google.firebase.auth.FirebaseAuth
import com.assgui.gourmandine.ui.screens.home.components.FilterBottomSheet
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
    navController: NavController,
    viewModel: HomeViewModel = viewModel(),
    onOpenProfile: () -> Unit = {},
    onOpenReservation: () -> Unit = {},
    onOpenFavorites: () -> Unit = {}
) {
    val reservationViewModel: ReservationViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    var restaurantToBook by remember { mutableStateOf<Restaurant?>(null) }
    var reviewRestaurant by remember { mutableStateOf<Restaurant?>(null) }
    var pendingLoginAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var showLoginOverlay by remember { mutableStateOf(false) }

    val isLoggedIn by produceState(initialValue = FirebaseAuth.getInstance().currentUser != null) {
        val auth = FirebaseAuth.getInstance()
        val listener = com.google.firebase.auth.FirebaseAuth.AuthStateListener { firebaseAuth ->
            value = firebaseAuth.currentUser != null
        }
        auth.addAuthStateListener(listener)
        awaitDispose { auth.removeAuthStateListener(listener) }
    }

    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    var showFilterSheet by remember { mutableStateOf(false) }
    // Onglet actif → icône surlignée dans le header + ModalBottomSheet ouvert
    var activeTab by remember { mutableStateOf(NavTab.HOME) }
    val topInsets = WindowInsets.statusBars.add(WindowInsets(top = 72.dp))

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
        val statusBarHeightPx = WindowInsets.statusBars.getTop(density).toFloat()
        val headerGapPx = statusBarHeightPx + with(density) { 72.dp.toPx() }
        val downHeightPx = with(density) { 140.dp.toPx() }
        val middleHeightPx = fullHeightPx * 0.5f
        // Up : même top gap que la carte détail → le header reste toujours visible
        val upHeightPx = fullHeightPx - headerGapPx

        val anchors = remember(fullHeightPx) {
            DraggableAnchors {
                SheetPosition.Down at (fullHeightPx - downHeightPx)
                SheetPosition.Middle at (fullHeightPx - middleHeightPx)
                SheetPosition.Up at (fullHeightPx - upHeightPx)
            }
        }

        val sheetState = remember {
            AnchoredDraggableState(
                initialValue = SheetPosition.Down,
                anchors = anchors,
                positionalThreshold = { distance: Float -> distance * 0.5f },
                velocityThreshold = { with(density) { 125.dp.toPx() } },
                snapAnimationSpec = tween(300),
                decayAnimationSpec = exponentialDecay()
            )
        }

        LaunchedEffect(anchors) {
            sheetState.updateAnchors(anchors, sheetState.currentValue)
        }

        val currentPosition by remember {
            derivedStateOf { sheetState.currentValue }
        }

        val middleHeightDp = with(density) { middleHeightPx.toDp() }
        val mapBottomPaddingDp = when (currentPosition) {
            SheetPosition.Down -> 140.dp
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

        // Ouvre automatiquement le review quand demandé depuis Réservations
        LaunchedEffect(uiState.pendingReview, uiState.detailRestaurant) {
            if (uiState.pendingReview && uiState.detailRestaurant != null) {
                reviewRestaurant = uiState.detailRestaurant
                viewModel.consumePendingReview()
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

        LaunchedEffect(uiState.restaurants.isEmpty(), uiState.isLoading) {
            when {
                uiState.isLoading -> Unit // ne pas bouger pendant le chargement
                uiState.restaurants.isEmpty() && uiState.searchQuery.isBlank() -> sheetState.animateTo(SheetPosition.Down)
                uiState.restaurants.isNotEmpty() -> sheetState.animateTo(SheetPosition.Middle)
            }
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
            onProfileClick = { activeTab = NavTab.PROFILE },
            onReservationClick = { activeTab = NavTab.RESERVATIONS },
            onFavoritesClick = { activeTab = NavTab.FAVORITES },
            activeTab = activeTab,
            isLoggedIn = isLoggedIn,
            onCameraIdle = viewModel::onCameraIdle,
            onMyLocationClick = viewModel::onMyLocationClick,
            userLocation = uiState.userLocation,
            isLocationButtonVisible = isLocationButtonVisible,
            reviewImages = uiState.restaurantReviewImages
        )

        val visibleSheetHeightDp = with(density) {
            val offset = try { sheetState.requireOffset() } catch (_: Exception) { fullHeightPx - middleHeightPx }
            (fullHeightPx - offset).coerceAtLeast(0f).toDp()
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(visibleSheetHeightDp)
                .offset { IntOffset(0, sheetState.requireOffset().toInt()) }
                .clip(AppShapes.Sheet)
                .background(AppColors.SurfaceSheet)
                .pointerInput(Unit) {
                    var totalX = 0f
                    detectHorizontalDragGestures(
                        onDragStart = { totalX = 0f },
                        onHorizontalDrag = { _, dragAmount -> totalX += dragAmount },
                        onDragEnd = {
                            if (abs(totalX) > 80.dp.toPx()) {
                                activeTab = if (totalX > 0) NavTab.PROFILE else NavTab.RESERVATIONS
                            }
                        }
                    )
                }
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .anchoredDraggable(sheetState, Orientation.Vertical)
                ) {
                    SheetDragHandle()

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AppColors.SurfaceSheet)
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
                            },
                            onFilterClick = { showFilterSheet = true },
                            activeFilterCount = uiState.activeFilters.size + if (uiState.maxDistanceKm != null) 1 else 0,
                            onClearQuery = { viewModel.onSearchQueryChange("") }
                        )
                    }

                }

                SheetScrollableContent(
                    uiState = uiState,
                    listState = listState,
                    onCardClick = viewModel::onCardClick,
                    modifier = Modifier.weight(1f),
                    filteredRestaurants = uiState.filteredRestaurants,
                    activeFilters = uiState.activeFilters,
                    onClearFilters = viewModel::clearFilters
                )
            }
        }

        RestaurantDetailSheet(
            restaurant = uiState.detailRestaurant,
            visible = uiState.detailRestaurant != null,
            reviews = uiState.detailReviews,
            googleReviews = uiState.detailGoogleReviews,
            isFavorite = uiState.detailRestaurant?.id?.let { it in uiState.favoriteIds } ?: false,
            onDismiss = viewModel::onDismissDetail,
            onAddReview = { restaurant ->
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    reviewRestaurant = restaurant
                } else {
                    pendingLoginAction = { reviewRestaurant = restaurant }
                    showLoginOverlay = true
                }
            },
            onReserve = { restaurant ->
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    restaurantToBook = restaurant
                } else {
                    pendingLoginAction = { restaurantToBook = restaurant }
                    showLoginOverlay = true
                }
            },
            onToggleFavorite = { restaurant ->
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    viewModel.onToggleFavorite(restaurant)
                } else {
                    pendingLoginAction = { viewModel.onToggleFavorite(restaurant) }
                    showLoginOverlay = true
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // ── ModalBottomSheet : Profil ───────────────────────────────────────
        if (activeTab == NavTab.PROFILE) {
            ModalBottomSheet(
                onDismissRequest = { activeTab = NavTab.HOME },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                contentWindowInsets = { topInsets },
                containerColor = AppColors.SurfaceSheet,
                shape = AppShapes.Sheet,
                tonalElevation = 0.dp,
                scrimColor = Color.Transparent,
                dragHandle = { SheetDragHandleBar() }
            ) {
                ProfileScreen(
                    isSheet = true,
                    onBack = { activeTab = NavTab.HOME },
                    onLoginSuccess = { activeTab = NavTab.HOME }
                )
            }
        }

        // ── ModalBottomSheet : Favoris ──────────────────────────────────────
        if (activeTab == NavTab.FAVORITES) {
            ModalBottomSheet(
                onDismissRequest = { activeTab = NavTab.HOME },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                contentWindowInsets = { topInsets },
                containerColor = AppColors.SurfaceSheet,
                shape = AppShapes.Sheet,
                tonalElevation = 0.dp,
                scrimColor = Color.Transparent,
                dragHandle = { SheetDragHandleBar() }
            ) {
                FavoritesScreen(
                    isSheet = true,
                    onBack = { activeTab = NavTab.HOME },
                    onViewOnMap = { restaurantId ->
                        activeTab = NavTab.HOME
                        viewModel.onMarkerDetailClick(restaurantId)
                    }
                )
            }
        }

        // ── ModalBottomSheet : Réservations ─────────────────────────────────
        if (activeTab == NavTab.RESERVATIONS) {
            ModalBottomSheet(
                onDismissRequest = { activeTab = NavTab.HOME },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                contentWindowInsets = { topInsets },
                containerColor = AppColors.SurfaceSheet,
                shape = AppShapes.Sheet,
                tonalElevation = 0.dp,
                scrimColor = Color.Transparent,
                dragHandle = { SheetDragHandleBar() }
            ) {
                ReservationScreen(
                    isSheet = true,
                    onBack = { activeTab = NavTab.HOME }
                )
            }
        }

        if (showFilterSheet) {
            FilterBottomSheet(
                activeFilters = uiState.activeFilters,
                maxDistanceKm = uiState.maxDistanceKm,
                onToggleFilter = viewModel::onToggleFilter,
                onDistanceChange = viewModel::onDistanceFilterChange,
                onClearFilters = viewModel::clearFilters,
                onDismiss = { showFilterSheet = false }
            )
        }

        // Overlay login (réservation, favori, avis)
        if (showLoginOverlay) {
            ProfileScreen(
                onBack = {
                    showLoginOverlay = false
                    pendingLoginAction = null
                },
                onLoginSuccess = {
                    showLoginOverlay = false
                    pendingLoginAction?.invoke()
                    pendingLoginAction = null
                }
            )
        }

        // Overlay AddReview (directement dans HomeScreen, sans navigation)
        reviewRestaurant?.let { restaurant ->
            AddReviewScreen(
                restaurant = restaurant,
                onDismiss = { reviewRestaurant = null },
                onReviewSubmitted = {
                    viewModel.onMarkerDetailClick(restaurant.id)
                    reviewRestaurant = null
                }
            )
        }

        restaurantToBook?.let { restaurant ->
            ReservationBookingDialog(
                restaurant = restaurant,
                onDismiss = { restaurantToBook = null },
                onConfirm = { dateMs, partySize, notes ->
                    val imageUrl = restaurant.imageUrls.firstOrNull() ?: ""
                    reservationViewModel.addReservation(
                        com.assgui.gourmandine.data.model.Reservation(
                            restaurantId = restaurant.id,
                            restaurantName = restaurant.name,
                            restaurantAddress = restaurant.address,
                            restaurantImageUrl = imageUrl,
                            dateMs = dateMs,
                            partySize = partySize,
                            notes = notes
                        )
                    )
                    restaurantToBook = null
                    activeTab = NavTab.RESERVATIONS
                }
            )
        }
    }
}

/** Drag handle partagé par tous les ModalBottomSheet de navigation */
@Composable
private fun SheetDragHandleBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(AppColors.OrangeLight)
        )
    }
}