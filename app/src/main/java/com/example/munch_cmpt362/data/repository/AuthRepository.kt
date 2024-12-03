package com.example.munch_cmpt362.data.repository

import android.util.Log
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth
) {
    var currentUser = auth.currentUser
        init {
            auth.addAuthStateListener { firebaseAuth ->
                currentUser = firebaseAuth.currentUser
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
        auth.signOut()
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

    fun logout() {
        auth.signOut()
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }
}