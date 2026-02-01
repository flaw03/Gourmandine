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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.assgui.gourmandine.R
import com.assgui.gourmandine.ui.screens.profile.viewmodel.AuthUiState

private val OrangeAccent = Color(0xFFFF6B35)

private val fieldColors
    @Composable get() = OutlinedTextFieldDefaults.colors(
        unfocusedBorderColor = Color(0xFFE0E0E0),
        focusedBorderColor = OrangeAccent,
        cursorColor = OrangeAccent,
        focusedLabelColor = OrangeAccent
    )

@Composable
fun RegisterScreen(
    uiState: AuthUiState,
    onNomChange: (String) -> Unit,
    onPrenomChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onGoogleSignIn: () -> Unit = {},
    onNavigateToLogin: () -> Unit,
    onBack: () -> Unit = {}
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
                .background(Color(0xFFF0F0F0))
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Logo / icon
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(OrangeAccent.copy(alpha = 0.15f), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Restaurant,
                contentDescription = null,
                tint = OrangeAccent,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Créer un compte",
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            color = Color.Black
        )

        Text(
            text = "Inscrivez-vous pour commencer",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Prénom et Nom sur la même ligne
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = uiState.prenom,
                onValueChange = onPrenomChange,
                label = { Text("Prénom") },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray)
                },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors,
                isError = uiState.prenomError != null,
                supportingText = uiState.prenomError?.let { { Text(it, color = Color(0xFFF44336)) } }
            )

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedTextField(
                value = uiState.nom,
                onValueChange = onNomChange,
                label = { Text("Nom") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors,
                isError = uiState.nomError != null,
                supportingText = uiState.nomError?.let { { Text(it, color = Color(0xFFF44336)) } }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

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
            colors = fieldColors,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = uiState.emailError != null,
            supportingText = uiState.emailError?.let { { Text(it, color = Color(0xFFF44336)) } }
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
            colors = fieldColors,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
            isError = uiState.passwordError != null,
            supportingText = uiState.passwordError?.let { { Text(it, color = Color(0xFFF44336)) } }
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = uiState.confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = { Text("Confirmer le mot de passe") },
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray)
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = fieldColors,
            visualTransformation = PasswordVisualTransformation(),
            isError = uiState.confirmPasswordError != null,
            supportingText = uiState.confirmPasswordError?.let { { Text(it, color = Color(0xFFF44336)) } }
        )

        if (uiState.errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = uiState.errorMessage,
                color = Color(0xFFF44336),
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRegisterClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !uiState.isLoading,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = OrangeAccent,
                disabledContainerColor = OrangeAccent.copy(alpha = 0.5f)
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
                    text = "S'inscrire",
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
                    .background(Color(0xFFE0E0E0))
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
                    .background(Color(0xFFE0E0E0))
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
            border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
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

        TextButton(onClick = onNavigateToLogin) {
            Text(
                text = "Déjà un compte ? ",
                color = Color.Gray,
                fontSize = 14.sp
            )
            Text(
                text = "Se connecter",
                color = OrangeAccent,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(48.dp))
        }
    }
}