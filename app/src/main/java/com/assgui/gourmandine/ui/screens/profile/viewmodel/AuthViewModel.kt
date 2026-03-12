package com.assgui.gourmandine.ui.screens.profile.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.assgui.gourmandine.data.model.User
import com.assgui.gourmandine.data.repository.AuthRepository
import com.assgui.gourmandine.data.repository.AuthResult
import com.assgui.gourmandine.data.repository.UserRepository
import com.assgui.gourmandine.util.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        val firebaseUser = authRepository.currentUser
        if (firebaseUser != null) {
            _uiState.update {
                it.copy(
                    isLoggedIn = true,
                    userEmail = firebaseUser.email
                )
            }
            loadUserProfile(firebaseUser.uid)
        }
    }

    private fun loadUserProfile(uid: String) {
        viewModelScope.launch {
            userRepository.getUser(uid)
                .onSuccess { user ->
                    user?.let {
                        _uiState.update { state ->
                            state.copy(
                                userNom = it.nom,
                                userPrenom = it.prenom,
                                userEmail = it.email,
                                userPhone = it.phone.ifBlank { null }
                            )
                        }
                    }
                }
        }
    }

    fun openEditProfile() {
        val state = _uiState.value
        _uiState.update {
            it.copy(
                isEditingProfile = true,
                editNom = state.userNom ?: "",
                editPrenom = state.userPrenom ?: "",
                editPhone = state.userPhone ?: "",
                editNomError = null,
                editPrenomError = null,
                updateSuccess = false,
                errorMessage = null
            )
        }
    }

    fun closeEditProfile() {
        _uiState.update { it.copy(isEditingProfile = false, updateSuccess = false) }
    }

    fun onEditNomChange(nom: String) {
        _uiState.update { it.copy(editNom = nom, editNomError = null) }
    }

    fun onEditPrenomChange(prenom: String) {
        _uiState.update { it.copy(editPrenom = prenom, editPrenomError = null) }
    }

    fun onEditPhoneChange(phone: String) {
        _uiState.update { it.copy(editPhone = phone) }
    }

    fun saveProfile() {
        val state = _uiState.value
        val nomError = if (state.editNom.isBlank()) "Le nom est requis" else null
        val prenomError = if (state.editPrenom.isBlank()) "Le prénom est requis" else null

        if (nomError != null || prenomError != null) {
            _uiState.update { it.copy(editNomError = nomError, editPrenomError = prenomError) }
            return
        }

        val uid = authRepository.currentUser?.uid ?: return
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val user = User(
                uid = uid,
                nom = state.editNom.trim(),
                prenom = state.editPrenom.trim(),
                email = state.userEmail ?: "",
                phone = state.editPhone.trim()
            )
            userRepository.updateUser(user)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isEditingProfile = false,
                            updateSuccess = true,
                            userNom = user.nom,
                            userPrenom = user.prenom,
                            userPhone = user.phone.ifBlank { null }
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = "Erreur : ${e.message}")
                    }
                }
        }
    }

    fun onNomChange(nom: String) {
        _uiState.update {
            it.copy(
                nom = nom,
                nomError = null,
                errorMessage = null
            )
        }
    }

    fun onPrenomChange(prenom: String) {
        _uiState.update {
            it.copy(
                prenom = prenom,
                prenomError = null,
                errorMessage = null
            )
        }
    }

    fun onEmailChange(email: String) {
        _uiState.update {
            it.copy(
                email = email,
                emailError = null,
                errorMessage = null
            )
        }
    }

    fun onPasswordChange(password: String) {
        _uiState.update {
            it.copy(
                password = password,
                passwordError = null,
                errorMessage = null
            )
        }
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.update {
            it.copy(
                confirmPassword = confirmPassword,
                confirmPasswordError = null,
                errorMessage = null
            )
        }
    }

    fun login() {
        val state = _uiState.value
        val emailError = Validators.getEmailError(state.email)
        val passwordError = Validators.getPasswordError(state.password)

        if (emailError != null || passwordError != null) {
            _uiState.update {
                it.copy(
                    emailError = emailError,
                    passwordError = passwordError
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            when (val result = authRepository.login(state.email, state.password)) {
                is AuthResult.Success -> {
                    // Charger le profil depuis Firestore
                    loadUserProfile(result.user.uid)

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            userEmail = result.user.email,
                            email = "",
                            password = ""
                        )
                    }
                }
                is AuthResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun register() {
        val state = _uiState.value
        val nomError = Validators.getNomError(state.nom)
        val prenomError = Validators.getPrenomError(state.prenom)
        val emailError = Validators.getEmailError(state.email)
        val passwordError = Validators.getPasswordError(state.password)
        val confirmPasswordError = Validators.getConfirmPasswordError(state.password, state.confirmPassword)

        if (nomError != null || prenomError != null || emailError != null ||
            passwordError != null || confirmPasswordError != null) {
            _uiState.update {
                it.copy(
                    nomError = nomError,
                    prenomError = prenomError,
                    emailError = emailError,
                    passwordError = passwordError,
                    confirmPasswordError = confirmPasswordError
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            when (val result = authRepository.register(state.email, state.password)) {
                is AuthResult.Success -> {
                    // Créer le profil dans Firestore
                    val user = User(
                        uid = result.user.uid,
                        nom = state.nom,
                        prenom = state.prenom,
                        email = state.email
                    )

                    userRepository.createUser(user)
                        .onSuccess {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    isLoggedIn = true,
                                    userEmail = result.user.email,
                                    userNom = state.nom,
                                    userPrenom = state.prenom,
                                    nom = "",
                                    prenom = "",
                                    email = "",
                                    password = "",
                                    confirmPassword = ""
                                )
                            }
                        }
                        .onFailure { e ->
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = "Compte créé mais erreur profil: ${e.message}"
                                )
                            }
                        }
                }
                is AuthResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun signInWithGoogle(context: Context) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            when (val result = authRepository.signInWithGoogle(context)) {
                is AuthResult.Success -> {
                    val firebaseUser = result.user
                    // Créer le profil Firestore si nouveau
                    val existingUser = userRepository.getUser(firebaseUser.uid).getOrNull()
                    if (existingUser == null) {
                        val displayName = firebaseUser.displayName ?: ""
                        val parts = displayName.split(" ", limit = 2)
                        val user = User(
                            uid = firebaseUser.uid,
                            prenom = parts.getOrElse(0) { "" },
                            nom = parts.getOrElse(1) { "" },
                            email = firebaseUser.email ?: ""
                        )
                        userRepository.createUser(user)
                    }

                    loadUserProfile(firebaseUser.uid)

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            userEmail = firebaseUser.email
                        )
                    }
                }
                is AuthResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun logout() {
        authRepository.logout()
        _uiState.update {
            AuthUiState()
        }
    }

    fun clearForm() {
        _uiState.update {
            it.copy(
                nom = "",
                prenom = "",
                email = "",
                password = "",
                confirmPassword = "",
                nomError = null,
                prenomError = null,
                emailError = null,
                passwordError = null,
                confirmPasswordError = null,
                errorMessage = null
            )
        }
    }
}