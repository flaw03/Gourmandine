package com.assgui.gourmandine.ui.screens.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.assgui.gourmandine.ui.screens.profile.viewmodel.AuthUiState
import com.assgui.gourmandine.ui.screens.profile.viewmodel.AuthViewModel

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

    if (uiState.isLoggedIn) {
        ProfileContent(
            uiState = uiState,
            onLogout = { viewModel.logout() }
        )
    } else {
        when (currentScreen) {
            AuthScreen.LOGIN -> LoginScreen(
                uiState = uiState,
                onEmailChange = viewModel::onEmailChange,
                onPasswordChange = viewModel::onPasswordChange,
                onLoginClick = viewModel::login,
                onNavigateToRegister = {
                    viewModel.clearForm()
                    currentScreen = AuthScreen.REGISTER
                }
            )
            AuthScreen.REGISTER -> RegisterScreen(
                uiState = uiState,
                onNomChange = viewModel::onNomChange,
                onPrenomChange = viewModel::onPrenomChange,
                onEmailChange = viewModel::onEmailChange,
                onPasswordChange = viewModel::onPasswordChange,
                onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
                onRegisterClick = viewModel::register,
                onNavigateToLogin = {
                    viewModel.clearForm()
                    currentScreen = AuthScreen.LOGIN
                }
            )
        }
    }
}

@Composable
private fun ProfileContent(
    uiState: AuthUiState,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Mon Profil",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (uiState.userPrenom != null || uiState.userNom != null) {
            Text(
                text = "${uiState.userPrenom ?: ""} ${uiState.userNom ?: ""}".trim(),
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Text(
            text = uiState.userEmail ?: "Email non disponible",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Se déconnecter")
        }
    }
}