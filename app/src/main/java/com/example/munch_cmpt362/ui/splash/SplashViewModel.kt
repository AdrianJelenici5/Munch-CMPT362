package com.example.munch_cmpt362.ui.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _authState = MutableLiveData<Boolean>()
    val authState: LiveData<Boolean> = _authState

    fun checkAuthState() {
        _authState.value = auth.currentUser != null
    }
}