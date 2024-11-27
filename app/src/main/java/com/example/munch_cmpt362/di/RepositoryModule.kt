package com.example.munch_cmpt362.di

import com.example.munch_cmpt362.data.local.cache.ProfileCacheManager
import com.example.munch_cmpt362.data.repository.AuthRepository
import com.example.munch_cmpt362.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideUserRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
        storage: FirebaseStorage,
        profileCacheManager: ProfileCacheManager
    ): UserRepository {
        return UserRepository(
            firestore = firestore,
            auth = auth,
            storage = storage,
            profileCacheManager = profileCacheManager
        )
    }
}