package com.assgui.gourmandine.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.assgui.gourmandine.ui.theme.AppColors
import com.assgui.gourmandine.ui.theme.AppShapes

@Composable
fun MapHeaderOverlay(
    onProfileClick: () -> Unit,
    onReservationClick: () -> Unit,
    onFavoritesClick: () -> Unit = {},
    isLoggedIn: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        shape = AppShapes.Large,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            NavIconButton(
                icon = Icons.Default.Person,
                label = "Profil",
                onClick = onProfileClick
            )

            Text(
                text = "Gourmandine",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = AppColors.TextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isLoggedIn) {
                    NavIconButton(
                        icon = Icons.Default.Favorite,
                        label = "Favoris",
                        onClick = onFavoritesClick
                    )
                    NavIconButton(
                        icon = Icons.AutoMirrored.Filled.EventNote,
                        label = "Réservations",
                        onClick = onReservationClick
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .clip(AppShapes.Small)
                            .background(AppColors.OrangeLight)
                            .clickable(onClick = onProfileClick)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Connexion",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.OrangeAccent
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NavIconButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(AppShapes.Small)
            .background(AppColors.OrangeLight)
            .clickable(onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = AppColors.OrangeAccent,
            modifier = Modifier.size(22.dp)
        )
    }
}

