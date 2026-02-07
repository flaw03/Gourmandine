package com.assgui.gourmandine

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.assgui.gourmandine.data.model.Restaurant
import com.assgui.gourmandine.navigation.AppDestinations
import com.assgui.gourmandine.ui.screens.addreview.AddReviewScreen
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
    var pendingReviewRestaurant by remember { mutableStateOf<Restaurant?>(null) }
    var reviewSubmittedForRestaurantId by remember { mutableStateOf<String?>(null) }
    val navigationBarPadding = WindowInsets.navigationBars.asPaddingValues()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = navigationBarPadding.calculateBottomPadding())
    ) {
        when (currentDestination) {
            AppDestinations.HOME -> {
                HomeScreen(
                    onProfileClick = { currentDestination = AppDestinations.PROFILE },
                    onReservationClick = { currentDestination = AppDestinations.RESERVATION },
                    onAddReview = { restaurant ->
                        pendingReviewRestaurant = restaurant
                        val user = FirebaseAuth.getInstance().currentUser
                        if (user != null) {
                            currentDestination = AppDestinations.ADD_REVIEW
                        } else {
                            currentDestination = AppDestinations.LOGIN_FOR_REVIEW
                        }
                    },
                    reviewSubmittedForRestaurantId = reviewSubmittedForRestaurantId,
                    onReviewSubmittedConsumed = { reviewSubmittedForRestaurantId = null }
                )
            }

            AppDestinations.RESERVATION -> {
                ReservationScreen(
                    onBack = { currentDestination = AppDestinations.HOME }
                )
            }

            AppDestinations.PROFILE -> {
                ProfileScreen(
                    onBack = { currentDestination = AppDestinations.HOME }
                )
            }

            AppDestinations.ADD_REVIEW -> {
                pendingReviewRestaurant?.let { restaurant ->
                    AddReviewScreen(
                        restaurant = restaurant,
                        onDismiss = {
                            pendingReviewRestaurant = null
                            currentDestination = AppDestinations.HOME
                        },
                        onReviewSubmitted = {
                            reviewSubmittedForRestaurantId = restaurant.id
                            pendingReviewRestaurant = null
                            currentDestination = AppDestinations.HOME
                        }
                    )
                }
            }

            AppDestinations.LOGIN_FOR_REVIEW -> {
                ProfileScreen(
                    onBack = {
                        pendingReviewRestaurant = null
                        currentDestination = AppDestinations.HOME
                    },
                    onLoginSuccess = {
                        currentDestination = AppDestinations.ADD_REVIEW
                    }
                )
            }
        }
    }
}
