package com.example.munch_cmpt362.data.repository

import android.util.Log
import com.example.munch_cmpt362.data.remote.dto.UserDto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val TAG = "UserRepository"
    private val usersCollection = firestore.collection("users")

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

    fun getCurrentUserId(): String? = auth.currentUser?.uid
}