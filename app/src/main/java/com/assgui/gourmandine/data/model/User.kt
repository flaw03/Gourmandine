package com.assgui.gourmandine.data.model

data class User(
    val uid: String = "",
    val nom: String = "",
    val prenom: String = "",
    val email: String = "",
    val phone: String = "",
    val createdAt: Long = System.currentTimeMillis()
)