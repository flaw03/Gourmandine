package com.assgui.gourmandine.data.repository

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ImageStorageRepository {
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    suspend fun uploadImages(imageUris: List<Uri>, userId: String): Result<List<String>> {
        return try {
            val downloadUrls = imageUris.map { uri ->
                val fileName = "${userId}_${UUID.randomUUID()}.jpg"
                val imageRef = storageRef.child("review_images/$fileName")
                imageRef.putFile(uri).await()
                imageRef.downloadUrl.await().toString()
            }
            Result.success(downloadUrls)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
