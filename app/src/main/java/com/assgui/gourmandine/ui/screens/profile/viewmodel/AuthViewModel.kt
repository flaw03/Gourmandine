package com.assgui.gourmandine.ui.screens.profile.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.assgui.gourmandine.data.ServiceLocator
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
    private val authRepository: AuthRepository = ServiceLocator.authRepository,
    private val userRepository: UserRepository = ServiceLocator.userRepository
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
                    profile = it.profile.copy(userEmail = firebaseUser.email)
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
                                profile = state.profile.copy(
                                    userNom = it.nom,
                                    userPrenom = it.prenom,
                                    userEmail = it.email,
                                    userPhone = it.phone.ifBlank { null },
                                    preferredCuisines = it.preferredCuisines.toSet(),
                                    preferredBudgets = it.preferredBudgets.toSet(),
                                    preferredCity = it.preferredCity
                                )
                            )
                        }
                    }
                }
        }
    }

    // ─── Preferences ────────────────────────────────────────────────────────

    fun onCuisineToggle(cuisine: String) {
        val current = _uiState.value.profile.preferredCuisines
        val updated = if (cuisine in current) current - cuisine else current + cuisine
        _uiState.update { it.copy(profile = it.profile.copy(preferredCuisines = updated)) }
        savePreferences()
    }

    fun onBudgetToggle(budget: String) {
        val current = _uiState.value.profile.preferredBudgets
        val updated = if (budget in current) current - budget else current + budget
        _uiState.update { it.copy(profile = it.profile.copy(preferredBudgets = updated)) }
        savePreferences()
    }

    fun onPreferredCityChange(city: String) {
        _uiState.update { it.copy(profile = it.profile.copy(preferredCity = city)) }
    }

    fun onPreferredCitySave() {
        savePreferences()
    }

    private fun savePreferences() {
        val uid = authRepository.currentUser?.uid ?: return
        val profile = _uiState.value.profile
        viewModelScope.launch {
            val user = User(
                uid = uid,
                nom = profile.userNom ?: "",
                prenom = profile.userPrenom ?: "",
                email = profile.userEmail ?: "",
                phone = profile.userPhone ?: "",
                preferredCuisines = profile.preferredCuisines.toList(),
                preferredBudgets = profile.preferredBudgets.toList(),
                preferredCity = profile.preferredCity
            )
            userRepository.updateUser(user)
        }
    }

    // ─── Edit profile ───────────────────────────────────────────────────────

    fun openEditProfile() {
        val profile = _uiState.value.profile
        _uiState.update {
            it.copy(
                editProfile = EditProfileState(
                    isEditing = true,
                    nom = profile.userNom ?: "",
                    prenom = profile.userPrenom ?: "",
                    phone = profile.userPhone ?: ""
                ),
                errorMessage = null
            )
        }
    }

    fun closeEditProfile() {
        _uiState.update {
            it.copy(editProfile = it.editProfile.copy(isEditing = false, updateSuccess = false))
        }
    }

    fun onEditNomChange(nom: String) {
        _uiState.update { it.copy(editProfile = it.editProfile.copy(nom = nom, nomError = null)) }
    }

    fun onEditPrenomChange(prenom: String) {
        _uiState.update { it.copy(editProfile = it.editProfile.copy(prenom = prenom, prenomError = null)) }
    }

    fun onEditPhoneChange(phone: String) {
        _uiState.update { it.copy(editProfile = it.editProfile.copy(phone = phone)) }
    }

    fun saveProfile() {
        val edit = _uiState.value.editProfile
        val nomError = if (edit.nom.isBlank()) "Le nom est requis" else null
        val prenomError = if (edit.prenom.isBlank()) "Le prénom est requis" else null

        if (nomError != null || prenomError != null) {
            _uiState.update {
                it.copy(editProfile = it.editProfile.copy(nomError = nomError, prenomError = prenomError))
            }
            return
        }

        val uid = authRepository.currentUser?.uid ?: return
        _uiState.update { it.copy(isLoading = true) }

        val profile = _uiState.value.profile
        viewModelScope.launch {
            val user = User(
                uid = uid,
                nom = edit.nom.trim(),
                prenom = edit.prenom.trim(),
                email = profile.userEmail ?: "",
                phone = edit.phone.trim(),
                preferredCuisines = profile.preferredCuisines.toList(),
                preferredBudgets = profile.preferredBudgets.toList(),
                preferredCity = profile.preferredCity
            )
            userRepository.updateUser(user)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            editProfile = it.editProfile.copy(isEditing = false, updateSuccess = true),
                            profile = it.profile.copy(
                                userNom = user.nom,
                                userPrenom = user.prenom,
                                userPhone = user.phone.ifBlank { null }
                            )
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

    // ─── Registration form ──────────────────────────────────────────────────

    fun onRegisterNomChange(nom: String) {
        _uiState.update { it.copy(form = it.form.copy(nom = nom, nomError = null), errorMessage = null) }
    }

    fun onRegisterPrenomChange(prenom: String) {
        _uiState.update { it.copy(form = it.form.copy(prenom = prenom, prenomError = null), errorMessage = null) }
    }

    fun onRegisterEmailChange(email: String) {
        _uiState.update { it.copy(form = it.form.copy(email = email, emailError = null), errorMessage = null) }
    }

    fun onRegisterPasswordChange(password: String) {
        _uiState.update { it.copy(form = it.form.copy(password = password, passwordError = null), errorMessage = null) }
    }

    fun onRegisterConfirmPasswordChange(confirmPassword: String) {
        _uiState.update { it.copy(form = it.form.copy(confirmPassword = confirmPassword, confirmPasswordError = null), errorMessage = null) }
    }

    fun login() {
        val form = _uiState.value.form
        val emailError = Validators.getEmailError(form.email)
        val passwordError = Validators.getPasswordError(form.password)

        if (emailError != null || passwordError != null) {
            _uiState.update {
                it.copy(form = it.form.copy(emailError = emailError, passwordError = passwordError))
            }
            return
        }

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            when (val result = authRepository.login(form.email, form.password)) {
                is AuthResult.Success -> {
                    loadUserProfile(result.user.uid)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            profile = it.profile.copy(userEmail = result.user.email),
                            form = AuthFormState()
                        )
                    }
                }
                is AuthResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun register() {
        val form = _uiState.value.form
        val nomError = Validators.getNomError(form.nom)
        val prenomError = Validators.getPrenomError(form.prenom)
        val emailError = Validators.getEmailError(form.email)
        val passwordError = Validators.getPasswordError(form.password)
        val confirmPasswordError = Validators.getConfirmPasswordError(form.password, form.confirmPassword)

        if (nomError != null || prenomError != null || emailError != null ||
            passwordError != null || confirmPasswordError != null) {
            _uiState.update {
                it.copy(form = it.form.copy(
                    nomError = nomError,
                    prenomError = prenomError,
                    emailError = emailError,
                    passwordError = passwordError,
                    confirmPasswordError = confirmPasswordError
                ))
            }
            return
        }

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            when (val result = authRepository.register(form.email, form.password)) {
                is AuthResult.Success -> {
                    val user = User(
                        uid = result.user.uid,
                        nom = form.nom,
                        prenom = form.prenom,
                        email = form.email
                    )
                    userRepository.createUser(user)
                        .onSuccess {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    isLoggedIn = true,
                                    profile = it.profile.copy(
                                        userEmail = result.user.email,
                                        userNom = form.nom,
                                        userPrenom = form.prenom
                                    ),
                                    form = AuthFormState()
                                )
                            }
                        }
                        .onFailure { e ->
                            _uiState.update {
                                it.copy(isLoading = false, errorMessage = "Compte créé mais erreur profil: ${e.message}")
                            }
                        }
                }
                is AuthResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
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
                            profile = it.profile.copy(userEmail = firebaseUser.email)
                        )
                    }
                }
                is AuthResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun logout() {
        authRepository.logout()
        _uiState.update { AuthUiState() }
    }

    fun clearForm() {
        _uiState.update { it.copy(form = AuthFormState(), errorMessage = null) }
    }
}
