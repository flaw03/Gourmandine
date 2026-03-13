package com.assgui.gourmandine.data.repository

import android.util.Log
import com.assgui.gourmandine.data.model.Reservation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await

private const val TAG = "Firestore/Reservations"

class ReservationRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun userId(): String? = auth.currentUser?.uid

    private fun itemsCollection(uid: String) =
        firestore.collection("reservations").document(uid).collection("items")

    suspend fun addReservation(reservation: Reservation): Result<String> {
        val uid = userId() ?: run {
            Log.w(TAG, "addReservation: utilisateur non connecté")
            return Result.failure(Exception("Utilisateur non connecté"))
        }
        return try {
            val docRef = itemsCollection(uid).document()
            val withId = reservation.copy(id = docRef.id, userId = uid)
            docRef.set(withId).await()
            Log.d(TAG, "addReservation: ajouté ${reservation.restaurantName} id=${docRef.id}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "addReservation: échec pour ${reservation.restaurantName}", e)
            Result.failure(e)
        }
    }

    suspend fun getReservations(fromCache: Boolean = false): Result<List<Reservation>> {
        val uid = userId() ?: run {
            Log.w(TAG, "getReservations: utilisateur non connecté")
            return Result.failure(Exception("Utilisateur non connecté"))
        }
        return try {
            val source = if (fromCache) Source.CACHE else Source.DEFAULT
            Log.d(TAG, "getReservations: chargement pour uid=$uid (cache=$fromCache)")
            val snapshot = itemsCollection(uid).get(source).await()
            val reservations = snapshot.documents.mapNotNull { it.toObject(Reservation::class.java) }
            Log.d(TAG, "getReservations: ${reservations.size} réservations chargées")
            Result.success(reservations)
        } catch (e: Exception) {
            Log.e(TAG, "getReservations: échec", e)
            Result.failure(e)
        }
    }

    suspend fun deleteReservation(id: String): Result<Unit> {
        val uid = userId() ?: run {
            Log.w(TAG, "deleteReservation: utilisateur non connecté")
            return Result.failure(Exception("Utilisateur non connecté"))
        }
        return try {
            itemsCollection(uid).document(id).delete().await()
            Log.d(TAG, "deleteReservation: supprimé $id")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "deleteReservation: échec pour $id", e)
            Result.failure(e)
        }
    }

    suspend fun updateReservationDate(id: String, newDateMs: Long): Result<Unit> {
        val uid = userId() ?: run {
            Log.w(TAG, "updateReservationDate: utilisateur non connecté")
            return Result.failure(Exception("Utilisateur non connecté"))
        }
        return try {
            itemsCollection(uid).document(id).update("dateMs", newDateMs).await()
            Log.d(TAG, "updateReservationDate: mis à jour $id -> $newDateMs")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "updateReservationDate: échec pour $id", e)
            Result.failure(e)
        }
    }
}
