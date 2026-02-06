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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import com.assgui.gourmandine.ui.theme.AppColors
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.assgui.gourmandine.ui.components.RestaurantCard
import com.assgui.gourmandine.ui.components.RestaurantDetailSheet
import com.assgui.gourmandine.ui.screens.home.components.RestaurantMapSection
import com.assgui.gourmandine.ui.screens.home.components.SearchBarRow
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

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val fullHeightPx = constraints.maxHeight.toFloat()
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

        // Mettre à jour les anchors si la taille change
        LaunchedEffect(anchors) {
            sheetState.updateAnchors(anchors)
        }

        val currentPosition by remember {
            derivedStateOf { sheetState.currentValue }
        }

        // Padding carte: Down=120dp, Middle ou Up=50% (ne change PAS quand clavier ouvert)
        val middleHeightDp = with(density) { middleHeightPx.toDp() }
        val mapBottomPaddingDp = when (currentPosition) {
            SheetPosition.Down -> 120.dp
            SheetPosition.Middle -> middleHeightDp
            SheetPosition.Up -> middleHeightDp
        }

        // Bouton localisation: visible en Down et Middle, caché en Up
        val isLocationButtonVisible = currentPosition != SheetPosition.Up

        // --- Événements ---

        // Clavier ouvert → Up
        LaunchedEffect(isKeyboardOpen) {
            if (isKeyboardOpen) {
                sheetState.animateTo(SheetPosition.Up)
            }
        }

        // Sheet revient à Down ou Middle → retirer le focus (fermer clavier)
        LaunchedEffect(currentPosition) {
            if (currentPosition == SheetPosition.Down || currentPosition == SheetPosition.Middle) {
                focusManager.clearFocus()
            }
        }

        // Sheet va à Up manuellement (swipe) → donner le focus à la barre de recherche
        LaunchedEffect(currentPosition) {
            if (currentPosition == SheetPosition.Up && !isKeyboardOpen) {
                focusRequester.requestFocus()
            }
        }

        // Caméra
        LaunchedEffect(uiState.cameraPosition, uiState.cameraZoom) {
            cameraPositionState.animate(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.fromLatLngZoom(uiState.cameraPosition, uiState.cameraZoom)
                )
            )
        }

        // Clic marqueur → Middle
        LaunchedEffect(uiState.selectedRestaurantId) {
            if (uiState.selectedRestaurantId != null && uiState.restaurants.isNotEmpty()) {
                sheetState.animateTo(SheetPosition.Middle)
                val index = uiState.restaurants.indexOfFirst { it.id == uiState.selectedRestaurantId }
                if (index >= 0) listState.animateScrollToItem(index)
            }
        }

        // Cluster click → zoom sur bounds
        LaunchedEffect(uiState.clusterBounds) {
            uiState.clusterBounds?.let { bounds ->
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngBounds(bounds, 100)
                )
            }
        }

        // Pas de restaurants → Down
        LaunchedEffect(uiState.restaurants.isEmpty()) {
            if (uiState.restaurants.isEmpty()) {
                sheetState.animateTo(SheetPosition.Down)
            }
        }

        // --- Layout ---

        // Carte
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

        // Sheet custom 3 positions
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .offset { IntOffset(0, sheetState.requireOffset().toInt()) }
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Color.White)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Zone draggable: drag handle + barre de recherche
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

                // Contenu scrollable (non draggable)
                SheetScrollableContent(
                    uiState = uiState,
                    listState = listState,
                    onCardClick = viewModel::onCardClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Detail restaurant
        RestaurantDetailSheet(
            restaurant = uiState.detailRestaurant,
            visible = uiState.detailRestaurant != null,
            reviews = uiState.detailReviews,
            onDismiss = viewModel::onDismissDetail,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun SheetDragHandle() {
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
                .background(AppColors.LightGray)
        )
    }
}

@Composable
private fun SheetScrollableContent(
    uiState: HomeUiState,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onCardClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        uiState.isLoading -> {
            Box(
                modifier = modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AppColors.OrangeAccent)
            }
        }
        uiState.isOffline && uiState.restaurants.isEmpty() -> {
            NoConnectionMessage(modifier = modifier.fillMaxWidth())
        }
        uiState.errorMessage != null -> {
            Box(
                modifier = modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(uiState.errorMessage ?: "", color = MaterialTheme.colorScheme.error)
            }
        }
        uiState.restaurants.isEmpty() -> {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Aucun restaurant trouvé", color = Color.Gray, fontSize = 14.sp)
            }
        }
        else -> {
            LazyColumn(
                state = listState,
                modifier = modifier.fillMaxWidth(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (uiState.searchQuery.isNotEmpty() && uiState.searchQuery.length >= 2) {
                    item {
                        Column(modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)) {
                            Text(
                                text = "Suggestions",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                items(items = uiState.restaurants, key = { it.id }) { restaurant ->
                    RestaurantCard(
                        restaurant = restaurant,
                        isSelected = restaurant.id == uiState.selectedRestaurantId,
                        onClick = { onCardClick(restaurant.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NoConnectionMessage(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(AppColors.OrangeAccent.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.WifiOff,
                contentDescription = "Pas de connexion",
                tint = AppColors.OrangeAccent,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Oups, pas de connexion !",
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Connectez-vous à internet pour\ndécouvrir les meilleurs restaurants",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}