package com.assgui.gourmandine.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.assgui.gourmandine.data.model.Restaurant

private val OrangeAccent = Color(0xFFFF6B35)

@Composable
fun RestaurantCard(
    restaurant: Restaurant,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    onViewDetail: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(
            2.dp, OrangeAccent
        ) else null
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Square image with carousel
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    if (restaurant.imageUrls.isNotEmpty()) {
                        val pagerState =
                            rememberPagerState(pageCount = { restaurant.imageUrls.size })
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
                        if (restaurant.imageUrls.size > 1) {
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                repeat(restaurant.imageUrls.size) { index ->
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (index == pagerState.currentPage) Color.White
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
                                .background(Color(0xFFE0E0E0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No image", color = Color.Gray, fontSize = 10.sp)
                        }
                    }
                }

                // Info section
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = restaurant.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.Black,
                        maxLines = 1
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = OrangeAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${restaurant.rating}",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = Color.DarkGray
                        )
                        Text("·", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Gray)
                        Text(
                            text = "${restaurant.reviewCount} Reviews",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val statusText = if (restaurant.isOpen) "Open" else "Closed"
                        val statusColor =
                            if (restaurant.isOpen) Color(0xFF4CAF50) else Color(0xFFF44336)

                        Box(
                            modifier = Modifier
                                .background(statusColor, RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = statusText,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }

                        if (restaurant.country.isNotBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(OrangeAccent)
                                )
                                Text(
                                    text = restaurant.country,
                                    fontSize = 13.sp,
                                    color = Color.DarkGray,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // "More details" button — only visible when selected
            AnimatedVisibility(
                visible = isSelected,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp, bottom = 16.dp)
                ) {
                    TextButton(
                        onClick = onViewDetail,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                            .background(OrangeAccent, RoundedCornerShape(10.dp)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "More details",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
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
            priceLevel = 2,
            isOpen = true
        ),
        isSelected = true
    )
}