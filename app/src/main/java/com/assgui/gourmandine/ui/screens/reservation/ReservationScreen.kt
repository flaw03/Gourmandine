package com.assgui.gourmandine.ui.screens.reservation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.assgui.gourmandine.navigation.AppRoutes
import com.assgui.gourmandine.ui.components.AppBottomNavBar
import com.assgui.gourmandine.ui.components.NavTab
import com.assgui.gourmandine.ui.screens.reservation.components.ReservationCard
import com.assgui.gourmandine.ui.theme.AppColors
import com.assgui.gourmandine.ui.theme.AppShapes
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationScreen(
    onBack: () -> Unit = {},
    navController: NavController? = null,
    viewModel: ReservationViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    // Date change dialog state
    var changeDateForId by remember { mutableStateOf<String?>(null) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    if (changeDateForId != null) {
        DatePickerDialog(
            onDismissRequest = { changeDateForId = null },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { ms ->
                        val cal = Calendar.getInstance().apply {
                            timeInMillis = ms
                            set(Calendar.HOUR_OF_DAY, 12)
                            set(Calendar.MINUTE, 0)
                        }
                        viewModel.updateDate(changeDateForId!!, cal.timeInMillis)
                    }
                    changeDateForId = null
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { changeDateForId = null }) { Text("Annuler") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.SurfaceWarm)
    ) {
        AppBottomNavBar(
            currentTab = NavTab.RESERVATIONS,
            onNavigateToHome = { navController?.popBackStack(AppRoutes.HOME, false) ?: onBack() },
            onNavigateToProfile = { navController?.navigate(AppRoutes.PROFILE) },
            onNavigateToFavorites = { navController?.navigate(AppRoutes.FAVORITES) },
            onNavigateToReservations = {}
        )

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = AppColors.SurfaceWarm,
            contentColor = AppColors.OrangeAccent,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = AppColors.OrangeAccent
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        "À venir (${uiState.upcoming.size})",
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        "Passées (${uiState.past.size})",
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppColors.OrangeAccent)
                }
            } else {
                val listToShow = if (selectedTab == 0) uiState.upcoming else uiState.past
                if (listToShow.isEmpty()) {
                    EmptyState(
                        message = if (selectedTab == 0) "Aucune réservation à venir"
                        else "Aucune réservation passée"
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(listToShow, key = { it.id }) { reservation ->
                            ReservationCard(
                                reservation = reservation,
                                onDelete = { viewModel.deleteReservation(reservation.id) },
                                onAddToCalendar = { viewModel.addToCalendar(context, reservation) },
                                onChangeDate = { changeDateForId = reservation.id },
                                onAddReview = {
                                    val user = FirebaseAuth.getInstance().currentUser
                                    if (navController != null) {
                                        if (user != null) {
                                            navController.navigate(AppRoutes.addReview(reservation.restaurantId))
                                        } else {
                                            navController.navigate(AppRoutes.loginForReview(reservation.restaurantId))
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

    }
}

@Composable
private fun EmptyState(message: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.EventBusy,
            contentDescription = null,
            tint = AppColors.LightGray,
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            fontSize = 16.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
    }
}
