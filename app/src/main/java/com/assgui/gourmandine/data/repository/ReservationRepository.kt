package com.assgui.gourmandine.data.repository

import android.util.Log
import com.assgui.gourmandine.data.cache.CacheManager
import com.assgui.gourmandine.data.model.Reservation
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await

private const val TAG = "Firestore/Reservations"

class ReservationRepository {

    private val firestore = FirebaseFirestore.getInstance()

    private fun itemsCollection(uid: String) =
        firestore.collection("reservations").document(uid).collection("items")

    suspend fun addReservation(reservation: Reservation): Result<String> {
        val uid = requireUserId().getOrElse { return Result.failure(it) }
        return try {
            val docRef = itemsCollection(uid).document()
            val withId = reservation.copy(id = docRef.id, userId = uid)
            docRef.set(withId).await()
            CacheManager.addReservation(withId)
            Log.d(TAG, "addReservation: ajouté id=${docRef.id}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "addReservation: échec", e)
            Result.failure(e)
        }
    }

    suspend fun getReservations(fromCache: Boolean = false): Result<List<Reservation>> {
        val uid = requireUserId().getOrElse { return Result.failure(it) }
        if (fromCache) {
            CacheManager.getReservations()?.let {
                Log.d(TAG, "getReservations: ${it.size} depuis cache mémoire")
                return Result.success(it)
            }
        }
        return try {
            val source = if (fromCache) Source.CACHE else Source.DEFAULT
            Log.d(TAG, "getReservations: chargement pour uid=$uid (cache=$fromCache)")
            val snapshot = itemsCollection(uid).get(source).await()
            val reservations = snapshot.documents.mapNotNull { it.toObject(Reservation::class.java) }
            CacheManager.putReservations(reservations)
            Log.d(TAG, "getReservations: ${reservations.size} réservations chargées")
            Result.success(reservations)
        } catch (e: Exception) {
            Log.e(TAG, "getReservations: échec", e)
            Result.failure(e)
        }
    }

    suspend fun deleteReservation(id: String): Result<Unit> {
        val uid = requireUserId().getOrElse { return Result.failure(it) }
        return try {
            itemsCollection(uid).document(id).delete().await()
            CacheManager.removeReservation(id)
            Log.d(TAG, "deleteReservation: supprimé $id")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "deleteReservation: échec pour $id", e)
            Result.failure(e)
        }
    }

    suspend fun updateReservationDate(id: String, newDateMs: Long): Result<Unit> {
        val uid = requireUserId().getOrElse { return Result.failure(it) }
        return try {
            itemsCollection(uid).document(id).update("dateMs", newDateMs).await()
            CacheManager.updateReservation(id) { it.copy(dateMs = newDateMs) }
            Log.d(TAG, "updateReservationDate: mis à jour $id -> $newDateMs")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "updateReservationDate: échec pour $id", e)
            Result.failure(e)
        }
    }
}
