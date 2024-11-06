package com.example.munch_cmpt362.data.remote.dto

data class UserDto(
    val userId: String = "",
    val email: String = "",
    val name: String = "",
    val bio: String = "",
    val profilePictureUrl: String = ""
)