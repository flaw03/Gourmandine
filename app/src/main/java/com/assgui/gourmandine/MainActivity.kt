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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.assgui.gourmandine.navigation.AppRoutes
import com.assgui.gourmandine.ui.screens.favorites.FavoritesScreen
import com.assgui.gourmandine.ui.screens.home.HomeScreen
import com.assgui.gourmandine.ui.screens.home.HomeViewModel
import com.assgui.gourmandine.ui.screens.profile.ProfileScreen
import com.assgui.gourmandine.ui.screens.reservation.ReservationScreen
import com.assgui.gourmandine.ui.theme.GourmandineTheme
import android.widget.Toast
import com.google.android.libraries.places.api.Places
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        // Vérification connexion Firebase au démarrage
        FirebaseFirestore.getInstance()
            .collection("_ping").limit(1).get()
            .addOnFailureListener { e ->
                Toast.makeText(this, "❌ Firebase non connecté : ${e.message}", Toast.LENGTH_LONG).show()
            }

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

@Composable
fun GourmandineApp() {
    val navController = rememberNavController()
    val homeViewModel: HomeViewModel = viewModel()
    val navigationBarPadding = WindowInsets.navigationBars.asPaddingValues()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = navigationBarPadding.calculateBottomPadding())
    ) {
        NavHost(
            navController = navController,
            startDestination = AppRoutes.HOME
        ) {
            composable(AppRoutes.HOME) {
                HomeScreen(
                    navController = navController,
                    viewModel = homeViewModel
                )
            }

            composable(AppRoutes.RESERVATION) {
                ReservationScreen(
                    onBack = { navController.popBackStack() },
                    navController = navController
                )
            }

            composable(AppRoutes.FAVORITES) {
                FavoritesScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToHome = { navController.popBackStack(AppRoutes.HOME, false) },
                    onNavigateToProfile = { navController.navigate(AppRoutes.PROFILE) },
                    onNavigateToReservations = { navController.navigate(AppRoutes.RESERVATION) }
                )
            }

            composable(AppRoutes.PROFILE) {
                ProfileScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToHome = { navController.popBackStack(AppRoutes.HOME, false) },
                    onNavigateToReservations = { navController.navigate(AppRoutes.RESERVATION) },
                    onNavigateToFavorites = { navController.navigate(AppRoutes.FAVORITES) }
                )
            }

            composable(
                route = AppRoutes.RESTAURANT_DETAIL,
                arguments = listOf(navArgument("restaurantId") { type = NavType.StringType })
            ) { backStackEntry ->
                val restaurantId = backStackEntry.arguments?.getString("restaurantId") ?: return@composable

                // Open HOME with the restaurant detail sheet
                LaunchedEffect(restaurantId) {
                    homeViewModel.onMarkerDetailClick(restaurantId)
                }

                HomeScreen(
                    navController = navController,
                    viewModel = homeViewModel
                )
            }

        }
    }
}
