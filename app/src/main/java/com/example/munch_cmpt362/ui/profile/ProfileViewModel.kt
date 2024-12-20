package com.example.munch_cmpt362.ui.profile

import android.net.ConnectivityManager
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.munch_cmpt362.data.local.entity.UserProfileEntity
import com.example.munch_cmpt362.data.repository.AuthRepository
import com.example.munch_cmpt362.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val connectivityManager: ConnectivityManager
) : ViewModel() {
    private val TAG = "ProfileViewModel"

    // State for profile data
    private val _profileState = MutableLiveData<Result<UserProfileEntity>>()
    val profileState: LiveData<Result<UserProfileEntity>> = _profileState

    // State for update operations
    private val _updateResult = MutableLiveData<Result<Unit>>()
    val updateResult: LiveData<Result<Unit>> = _updateResult

    // State for profile picture upload
    private val _uploadResult = MutableLiveData<Result<String>>()
    val uploadResult: LiveData<Result<String>> = _uploadResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        // loadUserProfile()
    }

    private fun isOnline(): Boolean {
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d(TAG, "Starting to load profile")
                val currentUser = authRepository.currentUser
                Log.d(TAG, "Current user: ${currentUser?.uid}")
                val userId = authRepository.currentUser?.uid
                    ?: throw Exception("No authenticated user")

                userRepository.getProfileFlow(userId).collect { result ->
                    Log.d(TAG, "Profile loaded: $result")
                    _profileState.value = result
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading profile", e)
                _profileState.value = Result.failure(e)
                _isLoading.value = false
            }
        }
    }


    fun updateProfile(name: String, bio: String, searchRadius: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val userId = authRepository.currentUser?.uid ?: throw Exception("No authenticated user")
                val updates = mapOf(
                    "name" to name,
                    "bio" to bio,
                    "searchRadius" to searchRadius,
                    "lastUpdated" to System.currentTimeMillis()
                )

                val result = userRepository.updateProfile(
                    userId = userId,
                    updates = updates,
                    isOnline = isOnline()
                )

                if (result.isSuccess) {
                    Log.d(TAG, "Profile updated successfully")
                    loadUserProfile() // Reload profile after successful update
                }

                _updateResult.postValue(result)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating profile", e)
                _updateResult.postValue(Result.failure(e))
            } finally {
                _isLoading.value = false
            }
        }
    }



    fun uploadProfilePicture(file: File) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val userId = authRepository.currentUser?.uid ?: throw Exception("No authenticated user")
                val result = userRepository.uploadProfilePicture(userId, file)
                _uploadResult.value = result
            } catch (e: Exception) {
                _uploadResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        authRepository.logout()
    }
}