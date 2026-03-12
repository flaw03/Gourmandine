package com.assgui.gourmandine.ui.screens.profile.viewmodel

data class AuthUiState(
    // Champs formulaire (login/register)
    val nom: String = "",
    val prenom: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",

    // Erreurs validation formulaire
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
    val userPrenom: String? = null,
    val userPhone: String? = null,

    // Édition profil
    val isEditingProfile: Boolean = false,
    val editNom: String = "",
    val editPrenom: String = "",
    val editPhone: String = "",
    val editNomError: String? = null,
    val editPrenomError: String? = null,
    val updateSuccess: Boolean = false
)
