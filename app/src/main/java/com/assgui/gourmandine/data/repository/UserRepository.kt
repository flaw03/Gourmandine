package com.assgui.gourmandine.data.repository

import android.util.Log
import com.assgui.gourmandine.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

private const val TAG = "Firestore/Users"

class UserRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    suspend fun createUser(user: User): Result<User> {
        return try {
            usersCollection.document(user.uid).set(user).await()
            Log.d(TAG, "createUser: créé uid=${user.uid} (${user.prenom} ${user.nom})")
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "createUser: échec pour uid=${user.uid}", e)
            Result.failure(e)
        }
    }

    suspend fun getUser(uid: String): Result<User?> {
        return try {
            Log.d(TAG, "getUser: chargement uid=$uid")
            val document = usersCollection.document(uid).get().await()
            val user = document.toObject(User::class.java)
            Log.d(TAG, "getUser: ${if (user != null) "trouvé ${user.prenom} ${user.nom}" else "non trouvé"}")
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "getUser: échec pour uid=$uid", e)
            Result.failure(e)
        }
    }

    suspend fun updateUser(user: User): Result<User> {
        return try {
            usersCollection.document(user.uid).set(user).await()
            Log.d(TAG, "updateUser: mis à jour uid=${user.uid}")
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "updateUser: échec pour uid=${user.uid}", e)
            Result.failure(e)
        }
    }
}
