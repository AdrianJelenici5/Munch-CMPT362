package com.example.munch_cmpt362.data.repository

import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val usersCollection = firestore.collection("users")

    private val TAG = "AuthRepository"

    val currentUser: FirebaseUser?
        get() {
            val user = auth.currentUser
            Log.d(TAG, "Getting current user: ${user?.uid}")
            return user
        }

    init {
        // Add auth state listener for debugging
        auth.addAuthStateListener { firebaseAuth ->
            Log.d(TAG, "Auth state changed. Current user: ${firebaseAuth.currentUser?.uid}")
        }
    }

    suspend fun login(email: String, password: String): Result<Unit> = try {
        Log.d("AuthRepository", "Attempting login for email: $email")
        auth.signInWithEmailAndPassword(email, password).await()
        Log.d("AuthRepository", "Login successful")
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e("AuthRepository", "Login failed", e)
        val message = when (e) {
            is FirebaseAuthInvalidUserException -> "No account exists with this email"
            is FirebaseAuthInvalidCredentialsException -> "Invalid password"
            is FirebaseNetworkException -> "Network error. Please check your connection"
            else -> "Login failed: ${e.message}"
        }
        Result.failure(Exception(message))
    }

    suspend fun register(email: String, password: String): Result<Unit> = try {
        Log.d("AuthRepository", "Attempting registration for email: $email")
        auth.createUserWithEmailAndPassword(email, password).await()
        Log.d("AuthRepository", "Registration successful")
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e("AuthRepository", "Registration failed", e)
        val message = when (e) {
            is FirebaseAuthWeakPasswordException -> "Password should be at least 6 characters"
            is FirebaseAuthInvalidCredentialsException -> "Invalid email format"
            is FirebaseNetworkException -> "Network error. Please check your connection"
            else -> "Registration failed: ${e.message}"
        }
        Result.failure(Exception(message))
    }

    suspend fun createUserProfile(username: String, name: String): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("No authenticated user")

            // Check if username is unique
            val usernameQuery = usersCollection
                .whereEqualTo("username", username)
                .get()
                .await()

            if (!usernameQuery.isEmpty) {
                return Result.failure(Exception("Username already exists"))
            }

            // Create user profile
            val userProfile = hashMapOf(
                "userId" to userId,
                "username" to username,
                "name" to name,
                "email" to (auth.currentUser?.email ?: ""),
                "createdAt" to System.currentTimeMillis(),
                "searchRadius" to 5  // Set default radius to 5km
            )

            usersCollection.document(userId)
                .set(userProfile)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogle(idToken: String): Result<Unit> = try {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val authResult = auth.signInWithCredential(credential).await()

        // Check if this is a new user or if profile doesn't exist
        val isNewUser = authResult.additionalUserInfo?.isNewUser ?: false
        if (!isNewUser) {
            // Check if user profile exists
            val userDoc = usersCollection.document(authResult.user?.uid ?: "").get().await()
            if (!userDoc.exists()) {
                // User needs to create profile
                throw Exception("NEEDS_PROFILE")
            }
        } else {
            // New user needs to create profile
            throw Exception("NEEDS_PROFILE")
        }

        Result.success(Unit) // User has complete profile
    } catch (e: Exception) {
        when (e.message) {
            "NEEDS_PROFILE" -> Result.failure(Exception("NEEDS_PROFILE"))
            else -> Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }
}