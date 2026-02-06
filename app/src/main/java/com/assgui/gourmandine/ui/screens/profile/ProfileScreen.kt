package com.assgui.gourmandine.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.assgui.gourmandine.ui.screens.profile.viewmodel.AuthUiState
import com.assgui.gourmandine.ui.screens.profile.viewmodel.AuthViewModel
import com.assgui.gourmandine.ui.theme.AppColors


enum class AuthScreen {
    LOGIN,
    REGISTER
}

@Composable
fun ProfileScreen(
    onBack: () -> Unit = {},
    viewModel: AuthViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentScreen by remember { mutableStateOf(AuthScreen.LOGIN) }
    val context = LocalContext.current

    if (uiState.isLoggedIn) {
        ProfileContent(
            uiState = uiState,
            onLogout = { viewModel.logout() },
            onBack = onBack
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

@Composable
private fun ProfileContent(
    uiState: AuthUiState,
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Back button
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .statusBarsPadding()
                .padding(start = 8.dp, top = 4.dp)
                .align(Alignment.TopStart)
                .size(40.dp)
                .clip(CircleShape)
                .background(AppColors.BackgroundGray)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Retour",
                tint = Color.Black
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Avatar
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(AppColors.OrangeAccent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = AppColors.OrangeAccent,
                modifier = Modifier.size(50.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Mon Profil",
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (uiState.userPrenom != null || uiState.userNom != null) {
            Text(
                text = "${uiState.userPrenom ?: ""} ${uiState.userNom ?: ""}".trim(),
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Text(
            text = uiState.userEmail ?: "Email non disponible",
            fontSize = 15.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp),
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
}