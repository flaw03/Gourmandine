package com.assgui.gourmandine.ui.components.restaurantdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.assgui.gourmandine.data.model.Restaurant
import com.assgui.gourmandine.ui.theme.AppColors

@Composable
fun RestaurantInfoHeader(
    restaurant: Restaurant,
    onAddReview: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Status badge + country
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
    ) {
        StatusBadge(isOpen = restaurant.isOpen)

        if (restaurant.country.isNotBlank()) {
            CountryIndicator(country = restaurant.country)
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
        ActionButtons(onAddReview = onAddReview)
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Rating stars
    RatingRow(rating = restaurant.rating, reviewCount = restaurant.reviewCount)
}

@Composable
private fun StatusBadge(isOpen: Boolean) {
    val statusText = if (isOpen) "Open" else "Closed"
    val statusColor = if (isOpen) AppColors.Green else AppColors.Red

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
}

@Composable
private fun CountryIndicator(country: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(AppColors.OrangeAccent)
        )
        Text(
            text = country,
            fontSize = 14.sp,
            color = Color.DarkGray
        )
    }
}

@Composable
private fun ActionButtons(onAddReview: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ActionIconButton(
            icon = Icons.Default.RateReview,
            contentDescription = "Ajouter un avis",
            onClick = onAddReview
        )
        ActionIconButton(
            icon = Icons.Default.Bookmark,
            contentDescription = "Sauvegarder",
            onClick = { }
        )
    }
}

@Composable
private fun ActionIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(AppColors.OrangeAccent.copy(alpha = 0.15f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = AppColors.OrangeAccent,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun RatingRow(rating: Double, reviewCount: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        val fullStars = rating.toInt()
        val hasHalf = (rating - fullStars) >= 0.3

        repeat(5) { index ->
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = if (index < fullStars || (index == fullStars && hasHalf))
                    AppColors.OrangeAccent else AppColors.MediumGray,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$rating",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = Color.DarkGray
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "($reviewCount Reviews)",
            fontSize = 14.sp,
            color = AppColors.OrangeAccent
        )
    }
}