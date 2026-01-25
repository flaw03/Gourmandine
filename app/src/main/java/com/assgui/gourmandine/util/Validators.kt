package com.assgui.gourmandine.util

object Validators {

    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    fun getNomError(nom: String): String? {
        return when {
            nom.isBlank() -> "Le nom est requis"
            nom.length < 2 -> "Minimum 2 caractères"
            else -> null
        }
    }

    fun getPrenomError(prenom: String): String? {
        return when {
            prenom.isBlank() -> "Le prénom est requis"
            prenom.length < 2 -> "Minimum 2 caractères"
            else -> null
        }
    }

    fun getEmailError(email: String): String? {
        return when {
            email.isBlank() -> "L'email est requis"
            !isValidEmail(email) -> "Email invalide"
            else -> null
        }
    }

    fun getPasswordError(password: String): String? {
        return when {
            password.isBlank() -> "Le mot de passe est requis"
            password.length < 6 -> "Minimum 6 caractères"
            else -> null
        }
    }

    fun getConfirmPasswordError(password: String, confirmPassword: String): String? {
        return when {
            confirmPassword.isBlank() -> "Confirmez le mot de passe"
            confirmPassword != password -> "Les mots de passe ne correspondent pas"
            else -> null
        }
    }
}