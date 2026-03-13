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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.assgui.gourmandine.navigation.AppRoutes
import com.assgui.gourmandine.ui.components.PageSheet
import com.assgui.gourmandine.ui.screens.favorites.FavoritesScreen
import com.assgui.gourmandine.ui.screens.home.HomeScreen
import com.assgui.gourmandine.ui.screens.home.HomeViewModel
import com.assgui.gourmandine.ui.screens.profile.ProfileScreen
import com.assgui.gourmandine.ui.screens.reservation.ReservationScreen
import com.assgui.gourmandine.ui.theme.GourmandineTheme
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.libraries.places.api.Places
import com.google.firebase.FirebaseApp

enum class PageSheetType { PROFILE, FAVORITES, RESERVATION }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val hasInternet = cm.activeNetwork?.let { net ->
            cm.getNetworkCapabilities(net)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } ?: false
        if (!hasInternet) {
            Toast.makeText(this, "❌ Pas de connexion internet", Toast.LENGTH_LONG).show()
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

    val isLoggedIn by produceState(initialValue = FirebaseAuth.getInstance().currentUser != null) {
        val auth = FirebaseAuth.getInstance()
        val listener = FirebaseAuth.AuthStateListener { value = it.currentUser != null }
        auth.addAuthStateListener(listener)
        awaitDispose { auth.removeAuthStateListener(listener) }
    }

    var activeSheet by remember { mutableStateOf<PageSheetType?>(null) }

    fun openSheet(type: PageSheetType) {
        activeSheet = when {
            type != PageSheetType.PROFILE && !isLoggedIn -> PageSheetType.PROFILE
            else -> type
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = navigationBarPadding.calculateBottomPadding())
    ) {
        NavHost(
            navController = navController,
            startDestination = AppRoutes.HOME,
            enterTransition = { fadeIn(tween(180)) },
            exitTransition = { fadeOut(tween(180)) },
            popEnterTransition = { fadeIn(tween(180)) },
            popExitTransition = { fadeOut(tween(180)) }
        ) {
            composable(AppRoutes.HOME) {
                HomeScreen(
                    navController = navController,
                    viewModel = homeViewModel,
                    onOpenProfile = { openSheet(PageSheetType.PROFILE) },
                    onOpenReservation = { openSheet(PageSheetType.RESERVATION) },
                    onOpenFavorites = { openSheet(PageSheetType.FAVORITES) }
                )
            }

            // Routes legacy — redirigent vers HOME + ouvrent le sheet
            composable(AppRoutes.RESERVATION) {
                LaunchedEffect(Unit) {
                    openSheet(PageSheetType.RESERVATION)
                    navController.popBackStack(AppRoutes.HOME, false)
                }
            }
            composable(AppRoutes.FAVORITES) {
                LaunchedEffect(Unit) {
                    openSheet(PageSheetType.FAVORITES)
                    navController.popBackStack(AppRoutes.HOME, false)
                }
            }
            composable(AppRoutes.PROFILE) {
                LaunchedEffect(Unit) {
                    openSheet(PageSheetType.PROFILE)
                    navController.popBackStack(AppRoutes.HOME, false)
                }
            }

            composable(
                route = AppRoutes.RESTAURANT_DETAIL,
                arguments = listOf(navArgument("restaurantId") { type = NavType.StringType })
            ) { backStackEntry ->
                val restaurantId = backStackEntry.arguments?.getString("restaurantId") ?: return@composable
                LaunchedEffect(restaurantId) {
                    homeViewModel.openRestaurantById(restaurantId)
                }
                HomeScreen(
                    navController = navController,
                    viewModel = homeViewModel,
                    onOpenProfile = { openSheet(PageSheetType.PROFILE) },
                    onOpenReservation = { openSheet(PageSheetType.RESERVATION) },
                    onOpenFavorites = { openSheet(PageSheetType.FAVORITES) }
                )
            }
        }

        // ── Overlays sheets ────────────────────────────────────────────────

        PageSheet(
            visible = activeSheet == PageSheetType.PROFILE,
            onDismiss = { activeSheet = null }
        ) {
            ProfileScreen(
                isSheet = true,
                onBack = { activeSheet = null },
                onNavigateToHome = { activeSheet = null },
                onNavigateToReservations = { openSheet(PageSheetType.RESERVATION) },
                onNavigateToFavorites = { openSheet(PageSheetType.FAVORITES) }
            )
        }

        PageSheet(
            visible = activeSheet == PageSheetType.FAVORITES,
            onDismiss = { activeSheet = null }
        ) {
            FavoritesScreen(
                isSheet = true,
                onBack = { activeSheet = null },
                onNavigateToHome = { activeSheet = null },
                onNavigateToProfile = { openSheet(PageSheetType.PROFILE) },
                onNavigateToReservations = { openSheet(PageSheetType.RESERVATION) },
                onFavoriteRemoved = { restaurantId -> homeViewModel.onRemoveFavoriteFromList(restaurantId) },
                onViewOnMap = { restaurantId ->
                    homeViewModel.openRestaurantById(restaurantId)
                    activeSheet = null
                }
            )
        }

        PageSheet(
            visible = activeSheet == PageSheetType.RESERVATION,
            onDismiss = { activeSheet = null }
        ) {
            ReservationScreen(
                isSheet = true,
                onBack = { activeSheet = null },
                onViewOnMap = { restaurantId ->
                    homeViewModel.openRestaurantById(restaurantId)
                    activeSheet = null
                },
                onAddReview = { restaurantId ->
                    homeViewModel.openRestaurantForReview(restaurantId)
                    activeSheet = null
                }
            )
        }
    }
}
