package com.assgui.gourmandine.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.assgui.gourmandine.ui.theme.AppShapes

@OptIn(ExperimentalMaterial3Api::class)
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
    if (!visible || restaurant == null) return

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true   // ouvre directement en plein écran
    )

    // Gap en haut = statusBar + 72dp → laisse le header avec les icônes visible
    val topInsets = WindowInsets.statusBars.add(WindowInsets(top = 72.dp))

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        windowInsets = topInsets,
        containerColor = AppColors.SurfaceSheet,
        shape = AppShapes.Sheet,
        tonalElevation = 0.dp,
        scrimColor = Color.Transparent,
        dragHandle = {
            // Drag handle dans le style de l'app
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
    ) {
        RestaurantDetailContent(
            restaurant = restaurant,
            reviews = reviews,
            googleReviews = googleReviews,
            isFavorite = isFavorite,
            onAddReview = { onAddReview(restaurant) },
            onReserve = { onReserve(restaurant) },
            onToggleFavorite = { onToggleFavorite(restaurant) }
        )
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
    // ModalBottomSheet gère le scroll + drag nativement via NestedScroll
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        ImageCarousel(
            imageUrls = restaurant.imageUrls,
            contentDescription = restaurant.name
        )

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
            RestaurantQuickInfo(restaurant = restaurant)
            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = AppColors.Divider)
            Spacer(modifier = Modifier.height(14.dp))

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
                        color = AppColors.TextSecondary,
                        lineHeight = 20.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

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
                        color = AppColors.TextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            if (restaurant.description.isNotBlank()) {
                HorizontalDivider(color = AppColors.Divider)
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "À propos",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = AppColors.TextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = restaurant.description,
                    fontSize = 14.sp,
                    color = AppColors.TextTertiary,
                    lineHeight = 21.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            HorizontalDivider(color = AppColors.Divider)
            Spacer(modifier = Modifier.height(14.dp))
            RestaurantMiniMap(
                latitude = restaurant.latitude,
                longitude = restaurant.longitude,
                restaurantName = restaurant.name
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        ReviewsSection(reviews = reviews, googleReviews = googleReviews)

        RestaurantActionButtons(
            latitude = restaurant.latitude,
            longitude = restaurant.longitude,
            phoneNumber = restaurant.phoneNumber,
            onReserve = onReserve
        )

        Spacer(modifier = Modifier.height(32.dp))
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
