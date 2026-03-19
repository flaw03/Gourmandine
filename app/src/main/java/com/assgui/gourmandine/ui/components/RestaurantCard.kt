package com.assgui.gourmandine.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.rounded.LocalDining
import androidx.compose.material.icons.rounded.NearMe
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.assgui.gourmandine.data.model.Restaurant
import com.assgui.gourmandine.ui.theme.AppColors
import com.assgui.gourmandine.ui.theme.AppShapes
import com.assgui.gourmandine.ui.theme.AppTypography
import com.assgui.gourmandine.ui.theme.BodyFontFamily
import com.assgui.gourmandine.ui.theme.DisplayFontFamily

@Composable
fun RestaurantCard(
    restaurant: Restaurant,
    isSelected: Boolean = false,
    distanceKm: Float? = null,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val elevation by animateDpAsState(
        targetValue = if (isSelected) 12.dp else 4.dp,
        animationSpec = tween(250),
        label = "cardElevation"
    )

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(
            2.dp,
            Brush.linearGradient(listOf(AppColors.OrangeAccent, AppColors.Amber))
        ) else null
    ) {
        Column {
            // ── Image section ───────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .clip(AppShapes.CardTop)
            ) {
                if (restaurant.imageUrls.isNotEmpty()) {
                    val pagerState = rememberPagerState(pageCount = { restaurant.imageUrls.size })
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        AsyncImage(
                            model = restaurant.imageUrls[page],
                            contentDescription = "${restaurant.name} photo ${page + 1}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Cinematic gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.5f)
                                    )
                                )
                            )
                    )

                    // Restaurant name overlaid on image
                    Text(
                        text = restaurant.name,
                        style = AppTypography.cardTitle.copy(
                            fontFamily = DisplayFontFamily,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 14.dp, bottom = 28.dp, end = 60.dp)
                    )

                    // Rating pill on image
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 14.dp, bottom = 8.dp)
                            .background(
                                Color.Black.copy(alpha = 0.5f),
                                AppShapes.Pill
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = AppColors.Amber,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "%.1f".format(restaurant.rating),
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color.White
                        )
                    }

                    // Pagination dots
                    if (restaurant.imageUrls.size > 1) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 14.dp, bottom = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            repeat(restaurant.imageUrls.size) { index ->
                                Box(
                                    modifier = Modifier
                                        .size(if (index == pagerState.currentPage) 8.dp else 5.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (index == pagerState.currentPage)
                                                Color.White
                                            else Color.White.copy(alpha = 0.4f)
                                        )
                                )
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(AppColors.BackgroundGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LocalDining,
                            contentDescription = null,
                            tint = AppColors.TextTertiary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Status badge — refined pill
                val statusText = if (restaurant.isOpen) "Ouvert" else "Fermé"
                val statusColor = if (restaurant.isOpen) AppColors.Green else AppColors.Red
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                        .shadow(4.dp, AppShapes.Pill)
                        .background(statusColor, AppShapes.Pill)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusText,
                        fontFamily = BodyFontFamily,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.3.sp
                    )
                }
            }

            // ── Meta strip ──────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (restaurant.cuisineType.isNotBlank()) {
                    MetaChip(text = restaurant.cuisineType)
                }

                if (distanceKm != null) {
                    MetaChip(
                        text = "%.1f km".format(distanceKm),
                        icon = true
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                val priceLabel = "€".repeat(restaurant.priceLevel.coerceIn(1, 3))
                Text(
                    text = priceLabel,
                    fontFamily = BodyFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Amber
                )
            }
        }
    }
}

@Composable
private fun MetaChip(text: String, icon: Boolean = false) {
    Row(
        modifier = Modifier
            .background(AppColors.BackgroundGray, AppShapes.Pill)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        if (icon) {
            Icon(
                imageVector = Icons.Rounded.NearMe,
                contentDescription = null,
                tint = AppColors.OrangeAccent,
                modifier = Modifier.size(11.dp)
            )
        }
        Text(
            text = text,
            fontFamily = BodyFontFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = AppColors.TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RestaurantCardPreview() {
    RestaurantCard(
        restaurant = Restaurant(
            name = "Le Petit Bistro Parisien",
            imageUrls = listOf("img1", "img2", "img3"),
            rating = 4.6,
            reviewCount = 120,
            country = "France",
            cuisineType = "Française",
            priceLevel = 2,
            isOpen = true
        ),
        isSelected = true,
        distanceKm = 1.2f
    )
}
