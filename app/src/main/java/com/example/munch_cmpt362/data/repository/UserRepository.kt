package com.example.munch_cmpt362.data.repository

import android.net.Uri
import android.util.Log
import com.example.munch_cmpt362.data.local.cache.ProfileCacheManager
import com.example.munch_cmpt362.data.local.entity.UserProfileEntity
import com.example.munch_cmpt362.data.remote.dto.UserDto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    // For cache
    private val storage: FirebaseStorage,
    private val profileCacheManager: ProfileCacheManager
) {
    private val TAG = "UserRepository"
    private val usersCollection = firestore.collection("users")

    fun getProfileFlow(userId: String): Flow<Result<UserProfileEntity>> = flow {
        try {
            // Always try to emit cached data first
            val cachedProfile = profileCacheManager.getCachedProfile(userId)
            if (cachedProfile != null) {
                emit(Result.success(cachedProfile))
            }

            // Fetch fresh data from Firestore
            try {
                val document = usersCollection.document(userId).get().await()
                if (document.exists()) {
                    val data = document.data
                    if (data != null) {
                        val profile = UserProfileEntity(
                            userId = userId,
                            name = data["name"] as? String ?: "",
                            email = data["email"] as? String ?: "",
                            bio = data["bio"] as? String ?: "",
                            profilePictureUrl = data["profilePictureUrl"] as? String,
                            searchRadius = (data["searchRadius"] as? Number)?.toInt() ?: 25,
                            lastUpdated = System.currentTimeMillis()
                        )
                        profileCacheManager.cacheProfile(profile)
                        emit(Result.success(profile))
                    }
                } else {
                    // If no profile exists, create a default one
                    val defaultProfile = UserProfileEntity(
                        userId = userId,
                        name = "",
                        email = auth.currentUser?.email ?: "",
                        bio = "",
                        profilePictureUrl = null,
                        searchRadius = 25,
                        lastUpdated = System.currentTimeMillis()
                    )
                    createUserProfile(UserDto(
                        userId = userId,
                        email = defaultProfile.email,
                        name = defaultProfile.name,
                        bio = defaultProfile.bio
                    ))
                    profileCacheManager.cacheProfile(defaultProfile)
                    emit(Result.success(defaultProfile))
                }
            } catch (e: Exception) {
                // If we can't fetch from Firestore but have cached data, continue using that
                if (cachedProfile == null) {
                    emit(Result.failure(e))
                }
                Log.e(TAG, "Error fetching profile from Firestore", e)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in getProfileFlow", e)
            emit(Result.failure(e))
        }
    }

    suspend fun createInitialUserProfile(email: String): Result<Unit> = try {
        val userId = auth.currentUser?.uid ?: throw Exception("No authenticated user")
        val userDto = mapOf(
            "userId" to userId,
            "email" to email,
            "name" to "",
            "bio" to ""
        )

        Log.d(TAG, "Creating initial profile for user: $userId")
        usersCollection.document(userId)
            .set(userDto)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Error creating initial profile", e)
        Result.failure(e)
    }

    suspend fun createUserProfile(userDto: UserDto): Result<Unit> = try {
        usersCollection.document(userDto.userId)
            .set(userDto)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getUserProfile(): Result<Map<String, String>> = try {
        val userId = auth.currentUser?.uid ?: throw Exception("No authenticated user")
        Log.d(TAG, "Fetching profile for user: $userId")

        val document = usersCollection.document(userId).get().await()
        if (document.exists()) {
            val data = document.data
            if (data != null) {
                @Suppress("UNCHECKED_CAST")
                Result.success(data as Map<String, String>)
            } else {
                throw Exception("User data is null")
            }
        } else {
            throw Exception("User profile not found")
        }
    } catch (e: Exception) {
        Log.e(TAG, "Error fetching user profile", e)
        Result.failure(e)
    }


    suspend fun updateUserProfile(updates: Map<String, String>): Result<Unit> = try {
        val userId = auth.currentUser?.uid ?: throw Exception("No authenticated user")
        val email = auth.currentUser?.email ?: throw Exception("No user email found")
        val updateData = hashMapOf(
            "userId" to userId,
            "email" to email,
            "name" to (updates["name"] ?: ""),
            "bio" to (updates["bio"] ?: "")
        )
        Log.d(TAG, "Updating profile for user: $userId with data: $updates")
        usersCollection.document(userId)
            .set(updates, SetOptions.merge())
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Error updating user profile", e)
        Result.failure(e)
    }

    suspend fun updateProfile(
        userId: String,
        updates: Map<String, Any>,
        isOnline: Boolean
    ): Result<Unit> = try {
        if (isOnline) {
            // Online update
            usersCollection.document(userId)
                .set(updates, SetOptions.merge())
                .await()

            // Update cache
            val updatedProfile = profileCacheManager.getCachedProfile(userId)?.copy(
                name = updates["name"] as? String ?: "",
                bio = updates["bio"] as? String ?: "",
                searchRadius = (updates["searchRadius"] as? Number)?.toInt() ?: 25,
                lastUpdated = System.currentTimeMillis()
            )
            updatedProfile?.let { profileCacheManager.cacheProfile(it) }

            // Process any pending updates
            val pendingUpdates = profileCacheManager.getPendingUpdates(userId)
            pendingUpdates.forEach { pendingUpdate ->
                usersCollection.document(userId)
                    .set(pendingUpdate.updates, SetOptions.merge())
                    .await()
            }
            profileCacheManager.clearPendingUpdates(userId)
        } else {
            // Offline update
            profileCacheManager.queueOfflineUpdate(userId, updates)
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Error updating profile", e)
        Result.failure(e)
    }


    suspend fun uploadProfilePicture(userId: String, imageFile: File): Result<String> = try {
        val storageRef = storage.reference.child("profile_pictures/$userId.jpg")
        val uploadTask = storageRef.putFile(Uri.fromFile(imageFile)).await()
        val downloadUrl = uploadTask.storage.downloadUrl.await().toString()

        // Update profile with new image URL
        val updates = mapOf("profilePictureUrl" to downloadUrl)
        updateProfile(userId, updates, true)

        Result.success(downloadUrl)
    } catch (e: Exception) {
        Log.e(TAG, "Error uploading profile picture", e)
        Result.failure(e)
    }


    fun getCurrentUserId(): String? = auth.currentUser?.uid
}