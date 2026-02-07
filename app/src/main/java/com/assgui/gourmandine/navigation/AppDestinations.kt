package com.assgui.gourmandine.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Accueil", Icons.Default.Home),
    RESERVATION("Réservations", Icons.Default.DateRange),
    PROFILE("Profil", Icons.Default.AccountBox),
    ADD_REVIEW("Ajouter un avis", Icons.Default.RateReview),
    LOGIN_FOR_REVIEW("Connexion", Icons.Default.AccountBox),
}