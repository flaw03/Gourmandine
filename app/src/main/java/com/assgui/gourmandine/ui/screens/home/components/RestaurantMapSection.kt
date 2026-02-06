package com.assgui.gourmandine.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.assgui.gourmandine.data.model.Restaurant
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.clustering.Clustering
import com.assgui.gourmandine.ui.theme.AppColors


data class RestaurantClusterItem(
    val restaurant: Restaurant
) : ClusterItem {
    override fun getPosition(): LatLng = LatLng(restaurant.latitude, restaurant.longitude)
    override fun getTitle(): String = restaurant.name
    override fun getSnippet(): String = "${restaurant.rating}"
    override fun getZIndex(): Float = 0f
}

@OptIn(MapsComposeExperimentalApi::class)
@Composable
fun RestaurantMapSection(
    restaurants: List<Restaurant>,
    selectedRestaurantId: String?,
    cameraPositionState: CameraPositionState,
    mapBottomPadding: Dp,
    onMarkerClick: (String) -> Unit,
    onMarkerDetailClick: (String) -> Unit,
    onClusterClick: (LatLngBounds) -> Unit,
    onProfileClick: () -> Unit,
    onReservationClick: () -> Unit,
    onMyLocationClick: () -> Unit = {},
    userLocation: LatLng? = null,
    isLocationButtonVisible: Boolean = true,
    modifier: Modifier = Modifier
) {
    val selectedRestaurant = remember(restaurants, selectedRestaurantId) {
        restaurants.find { it.id == selectedRestaurantId }
    }
    val unselectedItems = remember(restaurants, selectedRestaurantId) {
        restaurants
            .filter { it.id != selectedRestaurantId }
            .map { RestaurantClusterItem(it) }
    }

    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            contentPadding = PaddingValues(bottom = mapBottomPadding)
        ) {
            // Clustering for unselected markers only
            Clustering(
                items = unselectedItems,
                onClusterClick = { cluster ->
                    val builder = LatLngBounds.builder()
                    cluster.items.forEach { builder.include(it.position) }
                    onClusterClick(builder.build())
                    true
                },
                onClusterItemClick = { item ->
                    onMarkerClick(item.restaurant.id)
                    true
                },
                clusterContent = { cluster ->
                    ClusterMarker(count = cluster.size)
                },
                clusterItemContent = {
                    UnselectedMarker()
                }
            )

            // Selected marker rendered separately so it updates reactively
            selectedRestaurant?.let { restaurant ->
                MarkerComposable(
                    keys = arrayOf(restaurant.id),
                    state = MarkerState(
                        position = LatLng(restaurant.latitude, restaurant.longitude)
                    ),
                    anchor = androidx.compose.ui.geometry.Offset(0.1f, 1f),
                    zIndex = 1f,
                    onClick = {
                        onMarkerDetailClick(restaurant.id)
                        true
                    }
                ) {
                    SelectedMarker(
                        restaurant = restaurant,
                        onMoreDetails = { onMarkerDetailClick(restaurant.id) }
                    )
                }
            }

            // User location marker (blue dot)
            userLocation?.let { location ->
                MarkerComposable(
                    keys = arrayOf("user_location"),
                    state = MarkerState(position = location),
                    zIndex = 2f
                ) {
                    UserLocationMarker()
                }
            }
        }

        MapHeaderOverlay(
            onProfileClick = onProfileClick,
            onReservationClick = onReservationClick,
            modifier = Modifier.align(Alignment.TopStart)
        )

        if (isLocationButtonVisible) {
            MapLocationButton(
                onLocationClick = onMyLocationClick,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = mapBottomPadding + 16.dp)
            )
        }
    }
}

@Composable
private fun UnselectedMarker() {
    Box(
        modifier = Modifier
            .size(20.dp)
            .background(AppColors.OrangeAccent, CircleShape)
            .border(2.dp, Color.White, CircleShape)
    )
}

@Composable
private fun UserLocationMarker() {
    Box(
        modifier = Modifier
            .size(24.dp)
            .background(AppColors.GoogleBlue, CircleShape)
            .border(3.dp, Color.White, CircleShape)
    )
}

@Composable
private fun ClusterMarker(count: Int) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(AppColors.OrangeAccent, CircleShape)
            .border(2.5.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = count.toString(),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

private val TriangleShape = GenericShape { size, _ ->
    moveTo(0f, 0f)
    lineTo(size.width, 0f)
    lineTo(size.width / 2f, size.height)
    close()
}

@Composable
private fun SelectedMarker(
    restaurant: Restaurant,
    onMoreDetails: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.Start
    ) {
        // Bubble — offset to the right so triangle stays bottom-left
        Column(
            modifier = Modifier
                .padding(start = 8.dp)
                .background(AppColors.OrangeAccent, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Text(
                text = restaurant.name,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White, RoundedCornerShape(20.dp))
                        .clickable { onMoreDetails() }
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "More details",
                        color = AppColors.OrangeAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${restaurant.rating}",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Triangle pointer — bottom left, offset 2x triangle width from edge
        Box(
            modifier = Modifier
                .offset(x = 30.dp, y = -1.dp)
                .width(16.dp)
                .height(10.dp)
                .clip(TriangleShape)
                .background(AppColors.OrangeAccent)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFEEEEEE)
@Composable
private fun SelectedMarkerPreview() {
    SelectedMarker(
        restaurant = Restaurant(
            name = "Linfa",
            rating = 4.6,
            isOpen = true
        ),
        onMoreDetails = {}
    )
}
