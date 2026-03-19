package com.assgui.gourmandine.ui.screens.profile.viewmodel

data class AuthFormState(
    val nom: String = "",
    val prenom: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val nomError: String? = null,
    val prenomError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null
)

data class UserProfileState(
    val userEmail: String? = null,
    val userNom: String? = null,
    val userPrenom: String? = null,
    val userPhone: String? = null,
    val preferredCuisines: Set<String> = emptySet(),
    val preferredBudgets: Set<String> = emptySet(),
    val preferredCity: String = ""
)

data class EditProfileState(
    val isEditing: Boolean = false,
    val nom: String = "",
    val prenom: String = "",
    val phone: String = "",
    val nomError: String? = null,
    val prenomError: String? = null,
    val updateSuccess: Boolean = false
)

data class AuthUiState(
    val form: AuthFormState = AuthFormState(),
    val profile: UserProfileState = UserProfileState(),
    val editProfile: EditProfileState = EditProfileState(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoggedIn: Boolean = false
)
