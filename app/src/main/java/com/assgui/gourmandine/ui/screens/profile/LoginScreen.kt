package com.assgui.gourmandine.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.assgui.gourmandine.R
import com.assgui.gourmandine.ui.screens.profile.viewmodel.AuthUiState
import com.assgui.gourmandine.ui.theme.AppColors


@Composable
fun LoginScreen(
    uiState: AuthUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onGoogleSignIn: () -> Unit = {},
    onNavigateToRegister: () -> Unit,
    onBack: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 8.dp, top = 4.dp)
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
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo / icon
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(AppColors.OrangeAccent.copy(alpha = 0.15f), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Restaurant,
                contentDescription = null,
                tint = AppColors.OrangeAccent,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Connexion",
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            color = Color.Black
        )

        Text(
            text = "Connectez-vous pour continuer",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = uiState.email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            leadingIcon = {
                Icon(Icons.Default.Email, contentDescription = null, tint = Color.Gray)
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = AppColors.MediumGray,
                focusedBorderColor = AppColors.OrangeAccent,
                cursorColor = AppColors.OrangeAccent,
                focusedLabelColor = AppColors.OrangeAccent
            ),
            isError = uiState.emailError != null,
            supportingText = uiState.emailError?.let { { Text(it, color = AppColors.Red) } }
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = uiState.password,
            onValueChange = onPasswordChange,
            label = { Text("Mot de passe") },
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray)
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = AppColors.MediumGray,
                focusedBorderColor = AppColors.OrangeAccent,
                cursorColor = AppColors.OrangeAccent,
                focusedLabelColor = AppColors.OrangeAccent
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
            isError = uiState.passwordError != null,
            supportingText = uiState.passwordError?.let { { Text(it, color = AppColors.Red) } }
        )

        if (uiState.errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = uiState.errorMessage,
                color = AppColors.Red,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onLoginClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !uiState.isLoading,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.OrangeAccent,
                disabledContainerColor = AppColors.OrangeAccent.copy(alpha = 0.5f)
            )
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
            } else {
                Text(
                    text = "Se connecter",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Divider "ou"
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(AppColors.MediumGray)
            )
            Text(
                text = "  ou  ",
                fontSize = 13.sp,
                color = Color.Gray
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(AppColors.MediumGray)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Google Sign-In button
        OutlinedButton(
            onClick = onGoogleSignIn,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, AppColors.MediumGray),
            enabled = !uiState.isLoading
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_google),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "Continuer avec Google",
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = Color.DarkGray
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateToRegister) {
            Text(
                text = "Pas de compte ? ",
                color = Color.Gray,
                fontSize = 14.sp
            )
            Text(
                text = "S'inscrire",
                color = AppColors.OrangeAccent,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }
        }
    }
}