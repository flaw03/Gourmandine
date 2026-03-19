package com.assgui.gourmandine

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.assgui.gourmandine.data.model.Reservation
import com.assgui.gourmandine.data.model.Restaurant
import com.assgui.gourmandine.ui.components.RestaurantDetailSheet
import com.assgui.gourmandine.ui.screens.addreview.AddReviewScreen
import com.assgui.gourmandine.ui.screens.favorites.FavoritesScreen
import com.assgui.gourmandine.ui.screens.home.HomeScreen
import com.assgui.gourmandine.ui.screens.home.HomeViewModel
import com.assgui.gourmandine.ui.screens.profile.ProfileScreen
import com.assgui.gourmandine.ui.screens.reservation.ReservationScreen
import com.assgui.gourmandine.ui.screens.reservation.ReservationViewModel
import com.assgui.gourmandine.ui.screens.reservation.components.ReservationBookingDialog
import com.assgui.gourmandine.ui.theme.AppColors
import com.assgui.gourmandine.ui.theme.GourmandineTheme
import com.google.android.libraries.places.api.Places
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

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
            Toast.makeText(this, "Pas de connexion internet", Toast.LENGTH_LONG).show()
        }

        if (!Places.isInitialized()) {
            Places.initializeWithNewPlacesApiEnabled(applicationContext, BuildConfig.MAPS_API_KEY)
        }
        com.assgui.gourmandine.data.ServiceLocator.initPlaces(applicationContext)
        enableEdgeToEdge()
        setContent {
            GourmandineTheme {
                GourmandineApp()
            }
        }
    }
}

private data class NavItem(
    val label: String,
    val icon: ImageVector,
    val requiresAuth: Boolean = false
)

private val navItems = listOf(
    NavItem("Carte", Icons.Default.Map),
    NavItem("Favoris", Icons.Default.Favorite, requiresAuth = true),
    NavItem("Réservations", Icons.AutoMirrored.Filled.EventNote, requiresAuth = true),
    NavItem("Profil", Icons.Default.Person)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GourmandineApp() {
    val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
    val reservationViewModel: ReservationViewModel = viewModel()
    val homeUiState by homeViewModel.uiState.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var restaurantToBook by remember { mutableStateOf<Restaurant?>(null) }
    var reviewRestaurant by remember { mutableStateOf<Restaurant?>(null) }
    var pendingLoginAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var showLoginSheet by remember { mutableStateOf(false) }

    val isLoggedIn by produceState(initialValue = FirebaseAuth.getInstance().currentUser != null) {
        val auth = FirebaseAuth.getInstance()
        val listener = FirebaseAuth.AuthStateListener { value = it.currentUser != null }
        auth.addAuthStateListener(listener)
        awaitDispose { auth.removeAuthStateListener(listener) }
    }

    // Ouvre automatiquement le review quand demandé depuis Réservations
    LaunchedEffect(homeUiState.pendingReview, homeUiState.detailRestaurant) {
        if (homeUiState.pendingReview && homeUiState.detailRestaurant != null) {
            reviewRestaurant = homeUiState.detailRestaurant
            homeViewModel.consumePendingReview()
            homeViewModel.onDismissDetail()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 0.dp
                ) {
                    navItems.forEachIndexed { index, item ->
                        NavigationBarItem(
                            selected = selectedTab == index,
                            onClick = {
                                if (item.requiresAuth && !isLoggedIn) {
                                    pendingLoginAction = { selectedTab = index }
                                    showLoginSheet = true
                                } else {
                                    selectedTab = index
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label
                                )
                            },
                            label = {
                                Text(
                                    text = item.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = AppColors.OrangeAccent,
                                selectedTextColor = AppColors.OrangeAccent,
                                indicatorColor = AppColors.OrangeLight,
                                unselectedIconColor = AppColors.TextTertiary,
                                unselectedTextColor = AppColors.TextTertiary
                            )
                        )
                    }
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Crossfade(targetState = selectedTab, animationSpec = tween(200), label = "tab") { tab ->
                    when (tab) {
                        0 -> HomeScreen(viewModel = homeViewModel)
                        1 -> FavoritesScreen(
                            onRestaurantClick = { restaurantId ->
                                homeViewModel.openRestaurantById(restaurantId)
                            },
                            onFavoriteRemoved = { restaurantId ->
                                homeViewModel.onRemoveFavoriteFromList(restaurantId)
                            }
                        )
                        2 -> ReservationScreen(
                            viewModel = reservationViewModel,
                            onViewOnMap = { restaurantId ->
                                homeViewModel.openRestaurantById(restaurantId)
                            },
                            onAddReview = { restaurantId ->
                                homeViewModel.openRestaurantForReview(restaurantId)
                            }
                        )
                        3 -> ProfileScreen(
                            onNavigateToFavorites = {
                                if (isLoggedIn) selectedTab = 1
                            },
                            onNavigateToReservations = {
                                if (isLoggedIn) selectedTab = 2
                            }
                        )
                    }
                }
            }

            // ── Restaurant Detail ModalBottomSheet ────────────────────────────
            RestaurantDetailSheet(
                restaurant = homeUiState.detailRestaurant,
                visible = homeUiState.detailRestaurant != null,
                reviews = homeUiState.detailReviews,
                googleReviews = homeUiState.detailGoogleReviews,
                isFavorite = homeUiState.detailRestaurant?.id?.let { it in homeUiState.favoriteIds } ?: false,
                onDismiss = homeViewModel::onDismissDetail,
                onAddReview = { restaurant ->
                    if (isLoggedIn) {
                        reviewRestaurant = restaurant
                    } else {
                        pendingLoginAction = { reviewRestaurant = restaurant }
                        showLoginSheet = true
                    }
                },
                onReserve = { restaurant ->
                    if (isLoggedIn) {
                        restaurantToBook = restaurant
                    } else {
                        pendingLoginAction = { restaurantToBook = restaurant }
                        showLoginSheet = true
                    }
                },
                onToggleFavorite = { restaurant ->
                    if (isLoggedIn) {
                        homeViewModel.onToggleFavorite(restaurant)
                    } else {
                        pendingLoginAction = { homeViewModel.onToggleFavorite(restaurant) }
                        showLoginSheet = true
                    }
                }
            )

            // ── Login ModalBottomSheet ────────────────────────────────────────
            if (showLoginSheet) {
                val loginSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                ModalBottomSheet(
                    onDismissRequest = {
                        showLoginSheet = false
                        pendingLoginAction = null
                    },
                    sheetState = loginSheetState,
                    containerColor = AppColors.SurfaceWarm
                ) {
                    ProfileScreen(
                        isSheet = true,
                        onLoginSuccess = {
                            showLoginSheet = false
                            pendingLoginAction?.invoke()
                            pendingLoginAction = null
                        },
                        onNavigateToFavorites = {
                            showLoginSheet = false
                            selectedTab = 1
                        },
                        onNavigateToReservations = {
                            showLoginSheet = false
                            selectedTab = 2
                        }
                    )
                }
            }

            // ── Booking Dialog ────────────────────────────────────────────────
            restaurantToBook?.let { restaurant ->
                ReservationBookingDialog(
                    restaurant = restaurant,
                    onDismiss = { restaurantToBook = null },
                    onConfirm = { dateMs, partySize, notes ->
                        val imageUrl = restaurant.imageUrls.firstOrNull() ?: ""
                        reservationViewModel.addReservation(
                            Reservation(
                                restaurantId = restaurant.id,
                                restaurantName = restaurant.name,
                                restaurantAddress = restaurant.address,
                                restaurantImageUrl = imageUrl,
                                dateMs = dateMs,
                                partySize = partySize,
                                notes = notes
                            )
                        )
                        restaurantToBook = null
                        selectedTab = 2
                    }
                )
            }
        }

        // ── Overlay AddReview (hors Scaffold pour éviter la consommation des insets) ──
        reviewRestaurant?.let { restaurant ->
            AddReviewScreen(
                restaurant = restaurant,
                onDismiss = { reviewRestaurant = null },
                onReviewSubmitted = {
                    homeViewModel.onMarkerDetailClick(restaurant.id)
                    reservationViewModel.loadMyReviews()
                    reviewRestaurant = null
                }
            )
        }
    }
}
