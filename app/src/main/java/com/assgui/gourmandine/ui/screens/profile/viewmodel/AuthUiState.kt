package com.assgui.gourmandine.ui.screens.profile.viewmodel

data class AuthUiState(
    // Champs formulaire
    val nom: String = "",
    val prenom: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",

    // Erreurs validation
    val nomError: String? = null,
    val prenomError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,

    // État global
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoggedIn: Boolean = false,

    // Infos utilisateur connecté
    val userEmail: String? = null,
    val userNom: String? = null,
    val userPrenom: String? = null
)