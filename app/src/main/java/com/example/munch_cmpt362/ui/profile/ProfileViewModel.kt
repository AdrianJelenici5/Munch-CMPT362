package com.example.munch_cmpt362.ui.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.munch_cmpt362.data.remote.dto.UserDto
import com.example.munch_cmpt362.data.repository.AuthRepository
import com.example.munch_cmpt362.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val TAG = "ProfileViewModel"
    private val _userProfile = MutableLiveData<Result<Map<String, String>>>()
    val userProfile: MutableLiveData<Result<Map<String, String>>> = _userProfile

    private val _updateResult = MutableLiveData<Result<Unit>>()
    val updateResult: LiveData<Result<Unit>> = _updateResult

    fun loadUserProfile() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading user profile")
                val result = userRepository.getUserProfile()
                _userProfile.postValue(result)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading profile", e)
                _userProfile.value = Result.failure(e)
            }
        }
    }

    fun updateProfile(name: String, bio: String) {
        viewModelScope.launch {
            try {
                val userId = userRepository.getCurrentUserId() ?: throw Exception("No authenticated user")
                val updates = mapOf(
                    "userId" to userId,
                    "name" to name,
                    "bio" to bio
                )
                Log.d(TAG, "Updating profile with: $updates")
                val result = userRepository.updateUserProfile(updates)
                _updateResult.postValue(result)

                if (_updateResult.value?.isSuccess == true) {
                    loadUserProfile()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating profile", e)
                _updateResult.value = Result.failure(e)
            }
        }
    }

    fun logout() {
        authRepository.logout()
    }
}