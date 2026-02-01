package com.assgui.gourmandine.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.assgui.gourmandine.data.model.Restaurant
import com.assgui.gourmandine.data.model.Review
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

private val OrangeAccent = Color(0xFFFF6B35)

// ─── Drag handle bar ───────────────────────────────────────────

@Composable
private fun DragHandleBar(onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount > 40) onDismiss()
                }
            }
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

// ─── Restaurant Detail Sheet ───────────────────────────────────

@Composable
fun RestaurantDetailSheet(
    restaurant: Restaurant?,
    visible: Boolean,
    reviews: List<Review> = emptyList(),
    onDismiss: () -> Unit,
    onAddReview: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible && restaurant != null,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        restaurant?.let { resto ->
            RestaurantDetailContent(
                restaurant = resto,
                reviews = reviews,
                onDismiss = onDismiss,
                onAddReview = { onAddReview(resto.id) }
            )
        }
    }
}

@Composable
private fun RestaurantDetailContent(
    restaurant: Restaurant,
    reviews: List<Review> = emptyList(),
    onDismiss: () -> Unit,
    onAddReview: () -> Unit = {}
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val testReviews = reviews.ifEmpty {
        listOf(
            Review(
                id = "1",
                restaurantId = "rest_123",
                userId = "user_42",
                userName = "Ahmed Benali",
                userReviewCount = 27,
                userCreatedAt = 1625140800000,
                imageUrls = listOf(
                    "https://picsum.photos/200",
                    "https://picsum.photos/201"
                ),
                text = "Très bon restaurant, service rapide et plats délicieux. Je recommande \uD83D\uDC4D",
                rating = 4.5,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    var selectedReview by remember { mutableStateOf<Review?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // Drag handle
            DragHandleBar(onDismiss = onDismiss)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                // Image carousel
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
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
                                contentDescription = "${restaurant.name} photo",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                        if (restaurant.imageUrls.size > 1) {
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                repeat(restaurant.imageUrls.size) { index ->
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
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
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFE0E0E0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No image", color = Color.Gray)
                        }
                    }
                }

                // Content below image
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    // Status badge + country
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val statusText = if (restaurant.isOpen) "Open" else "Closed"
                        val statusColor =
                            if (restaurant.isOpen) Color(0xFF4CAF50) else Color(0xFFF44336)
                        Box(
                            modifier = Modifier
                                .background(statusColor, RoundedCornerShape(6.dp))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = statusText,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
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
                                    fontSize = 14.sp,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Name + actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = restaurant.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = Color.Black,
                            modifier = Modifier.weight(1f)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(OrangeAccent.copy(alpha = 0.15f))
                                    .clickable { onAddReview() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RateReview,
                                    contentDescription = "Ajouter un avis",
                                    tint = OrangeAccent,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(OrangeAccent.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bookmark,
                                    contentDescription = "Sauvegarder",
                                    tint = OrangeAccent,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Rating stars
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        val fullStars = restaurant.rating.toInt()
                        val hasHalf = (restaurant.rating - fullStars) >= 0.3
                        repeat(5) { index ->
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (index < fullStars || (index == fullStars && hasHalf))
                                    OrangeAccent else Color(0xFFE0E0E0),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${restaurant.rating}",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "(${restaurant.reviewCount} Reviews)",
                            fontSize = 14.sp,
                            color = OrangeAccent
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Mini map
                    val markerPosition = LatLng(restaurant.latitude, restaurant.longitude)
                    val miniMapCamera = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(markerPosition, 15f)
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = miniMapCamera,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Marker(
                                state = MarkerState(position = markerPosition),
                                title = restaurant.name
                            )
                        }
                    }

                    // Description
                    if (restaurant.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Description",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = restaurant.description,
                            fontSize = 14.sp,
                            color = Color.Gray,
                            lineHeight = 20.sp
                        )
                    }

                    // Reviews section
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Avis (${testReviews.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Reviews horizontal carousel
                if (testReviews.isNotEmpty()) {
                    val reviewPagerState = rememberPagerState(pageCount = { testReviews.size })
                    HorizontalPager(
                        state = reviewPagerState,
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        pageSpacing = 12.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) { page ->
                        ReviewCard(
                            review = testReviews[page],
                            compact = true,
                            modifier = Modifier.clickable { selectedReview = testReviews[page] }
                        )
                    }
                    // Page indicators
                    if (testReviews.size > 1) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            repeat(testReviews.size) { index ->
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 3.dp)
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (index == reviewPagerState.currentPage) OrangeAccent
                                            else Color(0xFFD0D0D0)
                                        )
                                )
                            }
                        }
                    }
                }

                // Bottom buttons
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { /* TODO */ },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
                        ) {
                            Text(
                                text = "View Menu",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(OrangeAccent.copy(alpha = 0.15f))
                                .clickable {
                                    val uri = Uri.parse(
                                        "google.navigation:q=${restaurant.latitude},${restaurant.longitude}"
                                    )
                                    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                                        setPackage("com.google.android.apps.maps")
                                    }
                                    context.startActivity(intent)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Directions,
                                contentDescription = "Y aller",
                                tint = OrangeAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        if (restaurant.phoneNumber.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(OrangeAccent.copy(alpha = 0.15f))
                                    .clickable {
                                        val intent = Intent(Intent.ACTION_DIAL).apply {
                                            data = Uri.parse("tel:${restaurant.phoneNumber}")
                                        }
                                        context.startActivity(intent)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "Appeler",
                                    tint = OrangeAccent,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // Full-screen reviews sheet (over restaurant detail)
        ReviewsFullSheet(
            visible = selectedReview != null,
            reviews = testReviews,
            initialReview = selectedReview,
            onDismiss = { selectedReview = null }
        )
    }
}

// ─── Reviews Full Sheet ────────────────────────────────────────

@Composable
private fun ReviewsFullSheet(
    visible: Boolean,
    reviews: List<Review>,
    initialReview: Review?,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // Drag handle
            DragHandleBar(onDismiss = onDismiss)

            // Reviews list
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Avis (${reviews.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black
                )
                reviews.forEach { review ->
                    ReviewCard(review = review)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
