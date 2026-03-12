package com.assgui.gourmandine.data.repository

import com.assgui.gourmandine.data.model.Reservation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ReservationRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun userId(): String? = auth.currentUser?.uid

    private fun itemsCollection(uid: String) =
        firestore.collection("reservations").document(uid).collection("items")

    suspend fun addReservation(reservation: Reservation): Result<String> {
        val uid = userId() ?: return Result.failure(Exception("Utilisateur non connecté"))
        return try {
            val docRef = itemsCollection(uid).document()
            val withId = reservation.copy(id = docRef.id, userId = uid)
            docRef.set(withId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getReservations(): Result<List<Reservation>> {
        val uid = userId() ?: return Result.failure(Exception("Utilisateur non connecté"))
        return try {
            val snapshot = itemsCollection(uid).get().await()
            val reservations = snapshot.documents.mapNotNull { it.toObject(Reservation::class.java) }
            Result.success(reservations)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteReservation(id: String): Result<Unit> {
        val uid = userId() ?: return Result.failure(Exception("Utilisateur non connecté"))
        return try {
            itemsCollection(uid).document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateReservationDate(id: String, newDateMs: Long): Result<Unit> {
        val uid = userId() ?: return Result.failure(Exception("Utilisateur non connecté"))
        return try {
            itemsCollection(uid).document(id).update("dateMs", newDateMs).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
