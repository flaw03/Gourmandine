package com.assgui.gourmandine.data

import android.content.Context
import com.assgui.gourmandine.data.repository.AuthRepository
import com.assgui.gourmandine.data.repository.FavoritesRepository
import com.assgui.gourmandine.data.repository.ImageStorageRepository
import com.assgui.gourmandine.data.repository.PlacesRepository
import com.assgui.gourmandine.data.repository.ReservationRepository
import com.assgui.gourmandine.data.repository.ReviewRepository
import com.assgui.gourmandine.data.repository.UserRepository

object ServiceLocator {

    val authRepository: AuthRepository by lazy { AuthRepository() }
    val userRepository: UserRepository by lazy { UserRepository() }
    val reviewRepository: ReviewRepository by lazy { ReviewRepository() }
    val favoritesRepository: FavoritesRepository by lazy { FavoritesRepository() }
    val reservationRepository: ReservationRepository by lazy { ReservationRepository() }
    val imageStorageRepository: ImageStorageRepository by lazy { ImageStorageRepository() }

    lateinit var placesRepository: PlacesRepository
        private set

    fun initPlaces(context: Context) {
        placesRepository = PlacesRepository.create(context)
    }
}
