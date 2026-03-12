package com.assgui.gourmandine.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

@Composable
fun RestaurantCard(
    restaurant: Restaurant,
    isSelected: Boolean = false,
    distanceKm: Float? = null,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, AppColors.OrangeAccent) else null
    ) {
        Column {
            // Image pleine largeur en haut
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
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

                    // Gradient overlay bas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.3f))
                                )
                            )
                    )

                    // Pagination dots
                    if (restaurant.imageUrls.size > 1) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            repeat(restaurant.imageUrls.size) { index ->
                                Box(
                                    modifier = Modifier
                                        .size(if (index == pagerState.currentPage) 10.dp else 6.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (index == pagerState.currentPage) AppColors.OrangeAccent
                                            else Color.White.copy(alpha = 0.6f)
                                        )
                                )
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(AppColors.MediumGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No image", color = AppColors.TextTertiary, fontSize = 12.sp)
                    }
                }

                // Badge Ouvert/Fermé (top-left overlay)
                val statusText = if (restaurant.isOpen) "Ouvert" else "Fermé"
                val statusColor = if (restaurant.isOpen) AppColors.Green else AppColors.Red
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .background(statusColor, AppShapes.Pill)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = statusText,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    )
                }
            }

            // Info strip
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Nom
                Text(
                    text = restaurant.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = AppColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Row: ⭐ 4.5 · Italienne · 1.2 km · €€
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = AppColors.OrangeAccent,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "%.1f".format(restaurant.rating),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = AppColors.TextPrimary
                    )

                    if (restaurant.cuisineType.isNotBlank()) {
                        Text("·", fontSize = 12.sp, color = AppColors.TextTertiary)
                        Text(
                            text = restaurant.cuisineType,
                            fontSize = 12.sp,
                            color = AppColors.TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }

                    if (distanceKm != null) {
                        Text("·", fontSize = 12.sp, color = AppColors.TextTertiary)
                        Text(
                            text = "%.1f km".format(distanceKm),
                            fontSize = 12.sp,
                            color = AppColors.TextSecondary
                        )
                    }

                    val priceLabel = "€".repeat(restaurant.priceLevel.coerceIn(1, 3))
                    Text("·", fontSize = 12.sp, color = AppColors.TextTertiary)
                    Text(
                        text = priceLabel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.OrangeAccent
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RestaurantCardPreview() {
    RestaurantCard(
        restaurant = Restaurant(
            name = "Flo's V8 Cafe",
            imageUrls = listOf("img1", "img2", "img3"),
            rating = 4.6,
            reviewCount = 120,
            country = "USA",
            cuisineType = "Italian",
            priceLevel = 2,
            isOpen = true
        ),
        isSelected = true,
        distanceKm = 1.2f
    )
}
