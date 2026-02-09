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
import com.assgui.gourmandine.ui.screens.addreview.AddReviewScreen
import com.assgui.gourmandine.ui.screens.home.HomeScreen
import com.assgui.gourmandine.ui.screens.home.HomeViewModel
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
            composable(AppRoutes.HOME) { backStackEntry ->
                // Listen for reviewSubmitted result from AddReviewScreen
                val reviewSubmittedId = backStackEntry.savedStateHandle
                    .getStateFlow<String?>("reviewSubmitted", null)
                    .collectAsState().value

                LaunchedEffect(reviewSubmittedId) {
                    reviewSubmittedId?.let { restaurantId ->
                        homeViewModel.onMarkerDetailClick(restaurantId)
                        backStackEntry.savedStateHandle.remove<String>("reviewSubmitted")
                    }
                }

                HomeScreen(
                    navController = navController,
                    viewModel = homeViewModel
                )
            }

            composable(AppRoutes.RESERVATION) {
                ReservationScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(AppRoutes.PROFILE) {
                ProfileScreen(
                    onBack = { navController.popBackStack() }
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

            composable(
                route = AppRoutes.ADD_REVIEW,
                arguments = listOf(navArgument("restaurantId") { type = NavType.StringType })
            ) { backStackEntry ->
                val restaurantId = backStackEntry.arguments?.getString("restaurantId") ?: return@composable
                val uiState by homeViewModel.uiState.collectAsState()
                val restaurant = uiState.restaurants.find { it.id == restaurantId }

                restaurant?.let {
                    AddReviewScreen(
                        restaurant = it,
                        onDismiss = { navController.popBackStack() },
                        onReviewSubmitted = {
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("reviewSubmitted", restaurantId)
                            navController.popBackStack()
                        }
                    )
                }
            }

            composable(
                route = AppRoutes.LOGIN_FOR_REVIEW,
                arguments = listOf(navArgument("restaurantId") { type = NavType.StringType })
            ) { backStackEntry ->
                val restaurantId = backStackEntry.arguments?.getString("restaurantId") ?: return@composable

                ProfileScreen(
                    onBack = { navController.popBackStack() },
                    onLoginSuccess = {
                        navController.navigate(AppRoutes.addReview(restaurantId)) {
                            popUpTo(AppRoutes.LOGIN_FOR_REVIEW) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
