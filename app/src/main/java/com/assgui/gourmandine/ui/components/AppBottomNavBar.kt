package com.assgui.gourmandine.ui.components

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
import androidx.compose.material.icons.filled.Map
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

enum class NavTab { HOME, PROFILE, FAVORITES, RESERVATIONS }

@Composable
fun AppBottomNavBar(
    currentTab: NavTab,
    onNavigateToHome: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToReservations: () -> Unit
) {
    val title = when (currentTab) {
        NavTab.PROFILE -> "Mon Profil"
        NavTab.FAVORITES -> "Mes Favoris"
        NavTab.RESERVATIONS -> "Mes Réservations"
        NavTab.HOME -> "Gourmandine"
    }

    Card(
        modifier = Modifier
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
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = AppColors.TextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NavIconButton(
                    icon = Icons.Default.Map,
                    label = "Carte",
                    selected = currentTab == NavTab.HOME,
                    onClick = onNavigateToHome
                )
                NavIconButton(
                    icon = Icons.Default.Person,
                    label = "Profil",
                    selected = currentTab == NavTab.PROFILE,
                    onClick = onNavigateToProfile
                )
                NavIconButton(
                    icon = Icons.Default.Favorite,
                    label = "Favoris",
                    selected = currentTab == NavTab.FAVORITES,
                    onClick = onNavigateToFavorites
                )
                NavIconButton(
                    icon = Icons.AutoMirrored.Filled.EventNote,
                    label = "Réservations",
                    selected = currentTab == NavTab.RESERVATIONS,
                    onClick = onNavigateToReservations
                )
            }
        }
    }
}

@Composable
private fun NavIconButton(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(AppShapes.Small)
            .background(if (selected) AppColors.OrangeAccent else AppColors.OrangeLight)
            .clickable(onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) Color.White else AppColors.OrangeAccent,
            modifier = Modifier.size(22.dp)
        )
    }
}
