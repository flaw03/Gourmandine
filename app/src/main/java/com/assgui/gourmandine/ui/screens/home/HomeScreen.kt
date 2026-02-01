package com.assgui.gourmandine.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
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

private val OrangeAccent = Color(0xFFFF6B35)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onProfileClick: () -> Unit = {},
    onReservationClick: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val sheetMaxHeight = screenHeight * 0.5f

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(uiState.cameraPosition, uiState.cameraZoom)
    }

    val bottomSheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.PartiallyExpanded
    )
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = bottomSheetState)

    val isSheetExpanded by remember {
        derivedStateOf { bottomSheetState.currentValue == SheetValue.Expanded }
    }

    val imeHeightPx = WindowInsets.ime.getBottom(density)
    val isKeyboardOpen by remember { derivedStateOf { imeHeightPx > 0 } }

    LaunchedEffect(isKeyboardOpen) {
        if (isKeyboardOpen) bottomSheetState.partialExpand()
    }

    val sheetMaxHeightPx = with(density) { sheetMaxHeight.roundToPx() }
    val peekHeightPx = with(density) { 120.dp.roundToPx() }
    val mapBottomPaddingPx = if (isSheetExpanded) sheetMaxHeightPx else peekHeightPx

    LaunchedEffect(uiState.cameraPosition, uiState.cameraZoom) {
        cameraPositionState.animate(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.fromLatLngZoom(uiState.cameraPosition, uiState.cameraZoom)
            )
        )
    }

    LaunchedEffect(isSheetExpanded) {
        cameraPositionState.animate(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.fromLatLngZoom(uiState.cameraPosition, uiState.cameraZoom)
            )
        )
    }

    LaunchedEffect(uiState.selectedRestaurantId) {
        if (uiState.selectedRestaurantId != null && uiState.restaurants.isNotEmpty()) {
            bottomSheetState.expand()
            val index = uiState.restaurants.indexOfFirst { it.id == uiState.selectedRestaurantId }
            if (index >= 0) listState.animateScrollToItem(index)
        }
    }

    LaunchedEffect(uiState.clusterBounds) {
        uiState.clusterBounds?.let { bounds ->
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngBounds(bounds, 100)
            )
        }
    }

    LaunchedEffect(uiState.restaurants.isEmpty()) {
        if (uiState.restaurants.isEmpty()) bottomSheetState.partialExpand()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = 120.dp,
            sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            sheetContainerColor = Color.White,
            sheetDragHandle = { SheetDragHandle() },
            sheetContent = {
                SheetContent(
                    uiState = uiState,
                    sheetMaxHeight = sheetMaxHeight,
                    listState = listState,
                    onQueryChange = viewModel::onSearchQueryChange,
                    onSearch = {
                        viewModel.onSearchSubmit(uiState.searchQuery)
                        focusManager.clearFocus()
                    },
                    onCardClick = viewModel::onCardClick
                )
            }
        ) {
            RestaurantMapSection(
                restaurants = uiState.restaurants,
                selectedRestaurantId = uiState.selectedRestaurantId,
                cameraPositionState = cameraPositionState,
                mapBottomPadding = with(density) { mapBottomPaddingPx.toDp() },
                onMarkerClick = viewModel::onMarkerClick,
                onMarkerDetailClick = viewModel::onMarkerDetailClick,
                onClusterClick = viewModel::onClusterClick,
                onProfileClick = onProfileClick,
                onReservationClick = onReservationClick
            )
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
                .background(Color(0xFFD0D0D0))
        )
    }
}

@Composable
private fun SheetContent(
    uiState: HomeUiState,
    sheetMaxHeight: androidx.compose.ui.unit.Dp,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onCardClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = sheetMaxHeight)
            .padding(horizontal = 16.dp)
    ) {
        SearchBarRow(
            query = uiState.searchQuery,
            onQueryChange = onQueryChange,
            onSearch = onSearch
        )

        Spacer(modifier = Modifier.height(12.dp))

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = OrangeAccent)
                }
            }
            uiState.errorMessage != null -> {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
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
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(items = uiState.restaurants, key = { it.id }) { restaurant ->
                        RestaurantCard(
                            restaurant = restaurant,
                            isSelected = restaurant.id == uiState.selectedRestaurantId,
                            onClick = { onCardClick(restaurant.id) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}