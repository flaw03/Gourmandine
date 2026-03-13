package com.assgui.gourmandine.data.model

data class User(
    val uid: String = "",
    val nom: String = "",
    val prenom: String = "",
    val email: String = "",
    val phone: String = "",
    val preferredCuisines: List<String> = emptyList(),
    val preferredBudgets: List<String> = emptyList(),
    val preferredCity: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
