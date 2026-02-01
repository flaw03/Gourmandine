package com.assgui.gourmandine

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.assgui.gourmandine.navigation.AppDestinations
import com.assgui.gourmandine.ui.screens.home.HomeScreen
import com.assgui.gourmandine.ui.screens.profile.ProfileScreen
import com.assgui.gourmandine.ui.screens.reservation.ReservationScreen
import com.assgui.gourmandine.ui.theme.GourmandineTheme
import com.google.android.libraries.places.api.Places
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        println(FirebaseAuth.getInstance())

        if (!Places.isInitialized()) {
            Places.initializeWithNewPlacesApiEnabled(applicationContext, BuildConfig.MAPS_API_KEY)
        }
        enableEdgeToEdge()
        setContent {
            GourmandineTheme {
                GourmandineApp()
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun GourmandineApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    when (currentDestination) {
        AppDestinations.HOME -> HomeScreen(
            onProfileClick = { currentDestination = AppDestinations.PROFILE },
            onReservationClick = { currentDestination = AppDestinations.RESERVATION }
        )

        AppDestinations.RESERVATION -> ReservationScreen(
            onBack = { currentDestination = AppDestinations.HOME }
        )

        AppDestinations.PROFILE -> ProfileScreen(
            onBack = { currentDestination = AppDestinations.HOME }
        )
    }
}