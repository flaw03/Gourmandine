package com.assgui.gourmandine.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

@Composable
fun RestaurantDetailSheet(
    restaurant: Restaurant?,
    visible: Boolean,
    reviews: List<Review> = emptyList(),
    googleReviews: List<Review> = emptyList(),
    onDismiss: () -> Unit,
    onAddReview: (Restaurant) -> Unit = {},
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
                onAddReview = { onAddReview(resto) }
            )
        }
    }
}

@Composable
private fun RestaurantDetailContent(
    restaurant: Restaurant,
    reviews: List<Review>,
    googleReviews: List<Review>,
    onAddReview: () -> Unit
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
                    onAddReview = onAddReview
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Mini map
                RestaurantMiniMap(
                    latitude = restaurant.latitude,
                    longitude = restaurant.longitude,
                    restaurantName = restaurant.name
                )

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

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Reviews section
            ReviewsSection(reviews = reviews, googleReviews = googleReviews)

            // Action buttons
            RestaurantActionButtons(
                latitude = restaurant.latitude,
                longitude = restaurant.longitude,
                phoneNumber = restaurant.phoneNumber
            )
        }
    }
}