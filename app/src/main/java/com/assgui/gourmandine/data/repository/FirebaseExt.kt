package com.assgui.gourmandine.data.repository

import com.google.firebase.auth.FirebaseAuth

fun requireUserId(): Result<String> {
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    return if (uid != null) Result.success(uid)
    else Result.failure(Exception("Utilisateur non connecté"))
}
