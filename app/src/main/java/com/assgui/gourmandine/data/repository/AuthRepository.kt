package com.assgui.gourmandine.data.repository

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

sealed class AuthResult {
    data class Success(val user: FirebaseUser) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val isLoggedIn: Boolean
        get() = currentUser != null

    suspend fun login(email: String, password: String): AuthResult {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let {
                AuthResult.Success(it)
            } ?: AuthResult.Error("Utilisateur non trouvé")
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Erreur de connexion")
        }
    }

    suspend fun register(email: String, password: String): AuthResult {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let {
                AuthResult.Success(it)
            } ?: AuthResult.Error("Erreur lors de la création du compte")
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Erreur d'inscription")
        }
    }

    suspend fun signInWithGoogle(context: Context): AuthResult {
        return try {
            val credentialManager = CredentialManager.create(context)

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(WEB_CLIENT_ID)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(context, request)
            val googleIdToken = GoogleIdTokenCredential.createFrom(result.credential.data)
            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken.idToken, null)

            val authResult = auth.signInWithCredential(firebaseCredential).await()
            authResult.user?.let {
                AuthResult.Success(it)
            } ?: AuthResult.Error("Erreur de connexion Google")
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Erreur de connexion Google")
        }
    }

    fun logout() {
        auth.signOut()
    }

    companion object {
        private const val WEB_CLIENT_ID =
            "588683615939-2g9lpc9p1t804jk689tn9vrbjak3ni6k.apps.googleusercontent.com"
    }
}