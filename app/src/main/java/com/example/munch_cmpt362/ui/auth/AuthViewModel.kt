package com.example.munch_cmpt362.ui.auth

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.munch_cmpt362.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginResult = MutableLiveData<Result<Unit>>()
    val loginResult: LiveData<Result<Unit>> = _loginResult

    private val _registerResult = MutableLiveData<Result<Unit>>()
    val registerResult: LiveData<Result<Unit>> = _registerResult

    private val _userDetailsResult = MutableLiveData<Result<Unit>>()
    val userDetailsResult: LiveData<Result<Unit>> = _userDetailsResult

    private val _googleSignInResult = MutableLiveData<Result<Unit>>()
    val googleSignInResult: LiveData<Result<Unit>> = _googleSignInResult


    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                Log.d("AuthViewModel", "Attempting login with email: $email")
                _loginResult.value = authRepository.login(email, password)
                Log.d("AuthViewModel", "Login successful")
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Login failed", e)
                _loginResult.value = Result.failure(e)
            }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            try {
                Log.d("AuthViewModel", "Attempting registration with email: $email")
                _registerResult.value = authRepository.register(email, password)
                Log.d("AuthViewModel", "Registration successful")

            } catch (e: Exception) {
                Log.e("AuthViewModel", "Registration failed", e)
                _registerResult.value = Result.failure(e)
            }
        }
    }

    fun createUserProfile(username: String, name: String) {
        viewModelScope.launch {
            try {
                _userDetailsResult.value = authRepository.createUserProfile(username, name)
            } catch (e: Exception) {
                _userDetailsResult.value = Result.failure(e)
            }
        }
    }

    fun handleGoogleSignIn(idToken: String) {
        viewModelScope.launch {
            _googleSignInResult.value = authRepository.signInWithGoogle(idToken)
        }
    }

    fun logout() {
        authRepository.logout()
    }

    fun returnID(): FirebaseUser? {
        return authRepository.currentUser
    }

    fun getCurrentUser(): FirebaseUser? = authRepository.currentUser
}