package com.assgui.gourmandine.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.assgui.gourmandine.ui.components.MapHeaderOverlay
import com.assgui.gourmandine.ui.components.NavTab
import com.assgui.gourmandine.ui.screens.profile.viewmodel.AuthUiState
import com.assgui.gourmandine.ui.screens.profile.viewmodel.AuthViewModel
import com.assgui.gourmandine.ui.theme.AppColors
import com.assgui.gourmandine.ui.theme.AppShapes


enum class AuthScreen {
    LOGIN,
    REGISTER
}

@Composable
fun ProfileScreen(
    isSheet: Boolean = false,
    onBack: () -> Unit = {},
    onLoginSuccess: (() -> Unit)? = null,
    onNavigateToHome: () -> Unit = {},
    onNavigateToReservations: () -> Unit = {},
    onNavigateToFavorites: () -> Unit = {},
    viewModel: AuthViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentScreen by remember { mutableStateOf(AuthScreen.LOGIN) }
    val context = LocalContext.current

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn && onLoginSuccess != null) {
            onLoginSuccess()
        }
    }

    if (uiState.isLoggedIn) {
        ProfileContent(
            isSheet = isSheet,
            uiState = uiState,
            onLogout = { viewModel.logout() },
            onBack = onBack,
            onNavigateToHome = onNavigateToHome,
            onNavigateToReservations = onNavigateToReservations,
            onNavigateToFavorites = onNavigateToFavorites,
            onEditProfile = { viewModel.openEditProfile() },
            onCloseEditProfile = { viewModel.closeEditProfile() },
            onEditNomChange = viewModel::onEditNomChange,
            onEditPrenomChange = viewModel::onEditPrenomChange,
            onEditPhoneChange = viewModel::onEditPhoneChange,
            onSaveProfile = { viewModel.saveProfile() },
            onCuisineToggle = viewModel::onCuisineToggle,
            onBudgetToggle = viewModel::onBudgetToggle,
            onPreferredCityChange = viewModel::onPreferredCityChange,
            onPreferredCitySave = viewModel::onPreferredCitySave
        )
    } else {
        when (currentScreen) {
            AuthScreen.LOGIN -> LoginScreen(
                uiState = uiState,
                onEmailChange = viewModel::onEmailChange,
                onPasswordChange = viewModel::onPasswordChange,
                onLoginClick = viewModel::login,
                onGoogleSignIn = { viewModel.signInWithGoogle(context) },
                onNavigateToRegister = {
                    viewModel.clearForm()
                    currentScreen = AuthScreen.REGISTER
                },
                onBack = onBack
            )
            AuthScreen.REGISTER -> RegisterScreen(
                uiState = uiState,
                onNomChange = viewModel::onNomChange,
                onPrenomChange = viewModel::onPrenomChange,
                onEmailChange = viewModel::onEmailChange,
                onPasswordChange = viewModel::onPasswordChange,
                onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
                onRegisterClick = viewModel::register,
                onGoogleSignIn = { viewModel.signInWithGoogle(context) },
                onNavigateToLogin = {
                    viewModel.clearForm()
                    currentScreen = AuthScreen.LOGIN
                },
                onBack = onBack
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileContent(
    isSheet: Boolean = false,
    uiState: AuthUiState,
    onLogout: () -> Unit,
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToReservations: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onEditProfile: () -> Unit,
    onCloseEditProfile: () -> Unit,
    onEditNomChange: (String) -> Unit,
    onEditPrenomChange: (String) -> Unit,
    onEditPhoneChange: (String) -> Unit,
    onSaveProfile: () -> Unit,
    onCuisineToggle: (String) -> Unit,
    onBudgetToggle: (String) -> Unit,
    onPreferredCityChange: (String) -> Unit,
    onPreferredCitySave: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.SurfaceWarm)
    ) {
        if (!isSheet) {
            MapHeaderOverlay(
                currentTab = NavTab.PROFILE,
                onNavigateToHome = onNavigateToHome,
                onNavigateToProfile = {},
                onNavigateToFavorites = onNavigateToFavorites,
                onNavigateToReservations = onNavigateToReservations,
                isLoggedIn = true
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Avatar avec initiales
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(AppColors.OrangeAccent),
                contentAlignment = Alignment.Center
            ) {
                val initials = buildString {
                    uiState.userPrenom?.firstOrNull()?.let { append(it.uppercaseChar()) }
                    uiState.userNom?.firstOrNull()?.let { append(it.uppercaseChar()) }
                }
                if (initials.isNotEmpty()) {
                    Text(
                        text = initials,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 38.sp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(55.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.userPrenom != null || uiState.userNom != null) {
                Text(
                    text = "${uiState.userPrenom ?: ""} ${uiState.userNom ?: ""}".trim(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = AppColors.TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            Text(
                text = uiState.userEmail ?: "Email non disponible",
                fontSize = 14.sp,
                color = AppColors.TextSecondary
            )

            if (uiState.userPhone != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = AppColors.TextTertiary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = uiState.userPhone,
                        fontSize = 14.sp,
                        color = AppColors.TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Menu items
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(AppShapes.Large)
                    .background(AppColors.SurfaceCard)
            ) {
                ProfileMenuItem(
                    icon = Icons.Default.Edit,
                    label = "Modifier mes informations",
                    onClick = onEditProfile
                )
                HorizontalDivider(color = AppColors.Divider, thickness = 1.dp)
                ProfileMenuItem(
                    icon = Icons.Default.Favorite,
                    label = "Mes Favoris",
                    onClick = onNavigateToFavorites
                )
                HorizontalDivider(color = AppColors.Divider, thickness = 1.dp)
                ProfileMenuItem(
                    icon = Icons.AutoMirrored.Filled.EventNote,
                    label = "Mes Réservations",
                    onClick = onNavigateToReservations
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section préférences
            PreferencesSection(
                selectedCuisines = uiState.preferredCuisines,
                selectedBudgets = uiState.preferredBudgets,
                city = uiState.preferredCity,
                onCuisineToggle = onCuisineToggle,
                onBudgetToggle = onBudgetToggle,
                onCityChange = onPreferredCityChange,
                onCitySave = onPreferredCitySave
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = AppShapes.Large,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.OrangeAccent
                )
            ) {
                Text(
                    text = "Se déconnecter",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Bottom sheet édition profil
    if (uiState.isEditingProfile) {
        ModalBottomSheet(
            onDismissRequest = onCloseEditProfile,
            sheetState = sheetState,
            shape = AppShapes.Sheet,
            containerColor = AppColors.SurfaceWarm
        ) {
            EditProfileSheet(
                uiState = uiState,
                onNomChange = onEditNomChange,
                onPrenomChange = onEditPrenomChange,
                onPhoneChange = onEditPhoneChange,
                onSave = onSaveProfile,
                onCancel = onCloseEditProfile
            )
        }
    }
}

@Composable
private fun EditProfileSheet(
    uiState: AuthUiState,
    onNomChange: (String) -> Unit,
    onPrenomChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Modifier mes informations",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = AppColors.TextPrimary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = uiState.editPrenom,
            onValueChange = onPrenomChange,
            label = { Text("Prénom") },
            isError = uiState.editPrenomError != null,
            supportingText = uiState.editPrenomError?.let { { Text(it, color = Color.Red) } },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = AppShapes.Large,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = AppColors.MediumGray,
                focusedBorderColor = AppColors.OrangeAccent,
                cursorColor = AppColors.OrangeAccent,
                focusedLabelColor = AppColors.OrangeAccent
            )
        )

        OutlinedTextField(
            value = uiState.editNom,
            onValueChange = onNomChange,
            label = { Text("Nom") },
            isError = uiState.editNomError != null,
            supportingText = uiState.editNomError?.let { { Text(it, color = Color.Red) } },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = AppShapes.Large,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = AppColors.MediumGray,
                focusedBorderColor = AppColors.OrangeAccent,
                cursorColor = AppColors.OrangeAccent,
                focusedLabelColor = AppColors.OrangeAccent
            )
        )

        OutlinedTextField(
            value = uiState.editPhone,
            onValueChange = onPhoneChange,
            label = { Text("Téléphone (optionnel)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = AppShapes.Large,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = AppColors.MediumGray,
                focusedBorderColor = AppColors.OrangeAccent,
                cursorColor = AppColors.OrangeAccent,
                focusedLabelColor = AppColors.OrangeAccent
            )
        )

        if (uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage,
                color = Color.Red,
                fontSize = 13.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = onSave,
            enabled = !uiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = AppShapes.Large,
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.OrangeAccent)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
            } else {
                Text("Enregistrer", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        Button(
            onClick = onCancel,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = AppShapes.Large,
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.SurfaceCard,
                contentColor = AppColors.TextSecondary
            )
        ) {
            Text("Annuler", fontWeight = FontWeight.Medium, fontSize = 15.sp)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PreferencesSection(
    selectedCuisines: Set<String>,
    selectedBudgets: Set<String>,
    city: String,
    onCuisineToggle: (String) -> Unit,
    onBudgetToggle: (String) -> Unit,
    onCityChange: (String) -> Unit,
    onCitySave: () -> Unit
) {
    val cuisines = listOf("Française", "Italienne", "Asiatique", "Mexicaine", "Japonaise", "Américaine")
    val budgets = listOf("€", "€€", "€€€")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppShapes.Large)
            .background(AppColors.SurfaceCard)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Préférences",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = AppColors.TextPrimary
        )

        OutlinedTextField(
            value = city,
            onValueChange = onCityChange,
            label = { Text("Ville préférée") },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { if (!it.isFocused) onCitySave() },
            singleLine = true,
            shape = AppShapes.Large,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = AppColors.MediumGray,
                focusedBorderColor = AppColors.OrangeAccent,
                cursorColor = AppColors.OrangeAccent,
                focusedLabelColor = AppColors.OrangeAccent
            )
        )

        Text(
            text = "Type de cuisine",
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = AppColors.TextSecondary
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            cuisines.forEach { cuisine ->
                FilterChip(
                    selected = cuisine in selectedCuisines,
                    onClick = { onCuisineToggle(cuisine) },
                    label = { Text(cuisine, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AppColors.OrangeLight,
                        selectedLabelColor = AppColors.OrangeAccent
                    )
                )
            }
        }

        Text(
            text = "Budget",
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = AppColors.TextSecondary
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            budgets.forEach { budget ->
                FilterChip(
                    selected = budget in selectedBudgets,
                    onClick = { onBudgetToggle(budget) },
                    label = { Text(budget, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AppColors.OrangeLight,
                        selectedLabelColor = AppColors.OrangeAccent
                    )
                )
            }
        }
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(AppShapes.Small)
                    .background(AppColors.OrangeAccent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AppColors.OrangeAccent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = AppColors.TextTertiary,
            modifier = Modifier.size(20.dp)
        )
    }
}
