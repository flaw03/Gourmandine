package com.assgui.gourmandine.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.assgui.gourmandine.data.model.Restaurant
import com.assgui.gourmandine.ui.components.RestaurantCard
import com.assgui.gourmandine.ui.screens.home.HomeUiState
import com.assgui.gourmandine.ui.screens.home.RestaurantFilter
import com.assgui.gourmandine.ui.theme.AppColors
import com.assgui.gourmandine.ui.theme.AppShapes
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun SheetDragHandle() {
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

@Composable
fun SheetScrollableContent(
    uiState: HomeUiState,
    listState: LazyListState,
    onCardClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    filteredRestaurants: List<Restaurant> = uiState.filteredRestaurants,
    activeFilters: Set<RestaurantFilter> = uiState.activeFilters,
    onClearFilters: () -> Unit = {}
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
        filteredRestaurants.isEmpty() && uiState.restaurants.isNotEmpty() -> {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
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
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = null,
                        tint = AppColors.OrangeAccent,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("Aucun résultat pour ces filtres", color = Color.DarkGray, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Essayez d'élargir vos critères", color = Color.Gray, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onClearFilters,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.OrangeAccent)
                ) {
                    Text("Réinitialiser les filtres", color = Color.White, fontWeight = FontWeight.Bold)
                }
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

                items(items = filteredRestaurants, key = { it.id }) { restaurant ->
                    val distanceKm = uiState.userLocation?.let { userLoc ->
                        haversineKmLocal(
                            userLoc.latitude, userLoc.longitude,
                            restaurant.latitude, restaurant.longitude
                        ).toFloat()
                    }
                    RestaurantCard(
                        restaurant = restaurant,
                        isSelected = restaurant.id == uiState.selectedRestaurantId,
                        distanceKm = distanceKm,
                        onClick = { onCardClick(restaurant.id) }
                    )
                }
            }
        }
    }
}

private fun haversineKmLocal(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val r = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLng = Math.toRadians(lng2 - lng1)
    val sinLat = sin(dLat / 2)
    val sinLng = sin(dLng / 2)
    val a = sinLat * sinLat + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sinLng * sinLng
    return 2 * r * atan2(sqrt(a), sqrt(1 - a))
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