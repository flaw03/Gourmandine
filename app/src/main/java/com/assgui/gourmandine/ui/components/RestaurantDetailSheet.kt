package com.assgui.gourmandine.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import com.assgui.gourmandine.ui.theme.AppShapes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.assgui.gourmandine.data.model.Restaurant
import com.assgui.gourmandine.data.model.Review
import com.assgui.gourmandine.ui.components.restaurantdetail.ImageCarousel
import com.assgui.gourmandine.ui.components.restaurantdetail.RestaurantActionButtons
import com.assgui.gourmandine.ui.components.restaurantdetail.RestaurantInfoHeader
import com.assgui.gourmandine.ui.components.restaurantdetail.RestaurantMiniMap
import com.assgui.gourmandine.ui.components.restaurantdetail.ReviewsSection
import com.assgui.gourmandine.ui.theme.AppColors

@Composable
fun RestaurantDetailSheet(
    restaurant: Restaurant?,
    visible: Boolean,
    reviews: List<Review> = emptyList(),
    googleReviews: List<Review> = emptyList(),
    isFavorite: Boolean = false,
    onDismiss: () -> Unit,
    onAddReview: (Restaurant) -> Unit = {},
    onReserve: (Restaurant) -> Unit = {},
    onToggleFavorite: (Restaurant) -> Unit = {},
    modifier: Modifier = Modifier
) {
    SwipeableSheet(
        visible = visible && restaurant != null,
        onDismiss = onDismiss,
        modifier = modifier
    ) {
        restaurant?.let { resto ->
            RestaurantDetailContent(
                restaurant = resto,
                reviews = reviews,
                googleReviews = googleReviews,
                isFavorite = isFavorite,
                onAddReview = { onAddReview(resto) },
                onReserve = { onReserve(resto) },
                onToggleFavorite = { onToggleFavorite(resto) }
            )
        }
    }
}

@Composable
private fun RestaurantDetailContent(
    restaurant: Restaurant,
    reviews: List<Review>,
    googleReviews: List<Review>,
    isFavorite: Boolean,
    onAddReview: () -> Unit,
    onReserve: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Image carousel
            ImageCarousel(
                imageUrls = restaurant.imageUrls,
                contentDescription = restaurant.name
            )

            // Info header (status, name, rating, actions)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                RestaurantInfoHeader(
                    restaurant = restaurant,
                    isFavorite = isFavorite,
                    onAddReview = onAddReview,
                    onToggleFavorite = onToggleFavorite
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Prix + cuisine + services
                RestaurantQuickInfo(restaurant = restaurant)

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = AppColors.Divider)
                Spacer(modifier = Modifier.height(14.dp))

                // Adresse
                if (restaurant.address.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = AppColors.OrangeAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = restaurant.address,
                            fontSize = 14.sp,
                            color = Color.DarkGray,
                            lineHeight = 20.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Téléphone
                if (restaurant.phoneNumber.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = AppColors.OrangeAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = restaurant.phoneNumber,
                            fontSize = 14.sp,
                            color = Color.DarkGray
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Description
                if (restaurant.description.isNotBlank()) {
                    HorizontalDivider(color = AppColors.Divider)
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "À propos",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = restaurant.description,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        lineHeight = 21.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Mini map
                HorizontalDivider(color = AppColors.Divider)
                Spacer(modifier = Modifier.height(14.dp))
                RestaurantMiniMap(
                    latitude = restaurant.latitude,
                    longitude = restaurant.longitude,
                    restaurantName = restaurant.name
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Reviews section
            ReviewsSection(reviews = reviews, googleReviews = googleReviews)

            // Action buttons
            RestaurantActionButtons(
                latitude = restaurant.latitude,
                longitude = restaurant.longitude,
                phoneNumber = restaurant.phoneNumber,
                onReserve = onReserve
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RestaurantQuickInfo(restaurant: Restaurant) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val priceLabel = "€".repeat(restaurant.priceLevel.coerceIn(1, 3))
        InfoBadge(priceLabel, textColor = AppColors.OrangeAccent, bgColor = AppColors.OrangeLight)
        if (restaurant.cuisineType.isNotBlank())
            InfoBadge(restaurant.cuisineType, textColor = Color.DarkGray, bgColor = AppColors.BackgroundGray)
        if (restaurant.hasDineIn)   InfoBadge("Sur place",  bgColor = AppColors.OrangeLight)
        if (restaurant.hasTakeout)  InfoBadge("À emporter", bgColor = AppColors.OrangeLight)
        if (restaurant.hasDelivery) InfoBadge("Livraison",  bgColor = AppColors.OrangeLight)
    }
}

@Composable
private fun InfoBadge(label: String, textColor: Color = Color.Black, bgColor: Color) {
    Box(
        modifier = Modifier
            .background(bgColor, AppShapes.Pill)
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}