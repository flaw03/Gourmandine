package com.assgui.gourmandine.ui.components.restaurantdetail

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.assgui.gourmandine.ui.theme.AppColors

@Composable
fun RestaurantActionButtons(
    latitude: Double,
    longitude: Double,
    phoneNumber: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val navigationBarPadding = WindowInsets.navigationBars.asPaddingValues()

    Spacer(modifier = Modifier.height(24.dp))

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = navigationBarPadding.calculateBottomPadding()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = { /* TODO */ },
            modifier = Modifier
                .weight(1f)
                .height(50.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.OrangeAccent)
        ) {
            Text(
                text = "View Menu",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        DirectionsButton(
            latitude = latitude,
            longitude = longitude,
            context = context
        )

        if (phoneNumber.isNotBlank()) {
            CallButton(phoneNumber = phoneNumber, context = context)
        }
    }

    Spacer(modifier = Modifier.height(24.dp))
}

@Composable
private fun DirectionsButton(
    latitude: Double,
    longitude: Double,
    context: Context
) {
    Box(
        modifier = Modifier
            .size(50.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(AppColors.OrangeAccent.copy(alpha = 0.15f))
            .clickable {
                val uri = Uri.parse("google.navigation:q=$latitude,$longitude")
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
            tint = AppColors.OrangeAccent,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun CallButton(
    phoneNumber: String,
    context: Context
) {
    Box(
        modifier = Modifier
            .size(50.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(AppColors.OrangeAccent.copy(alpha = 0.15f))
            .clickable {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$phoneNumber")
                }
                context.startActivity(intent)
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Call,
            contentDescription = "Appeler",
            tint = AppColors.OrangeAccent,
            modifier = Modifier.size(24.dp)
        )
    }
}