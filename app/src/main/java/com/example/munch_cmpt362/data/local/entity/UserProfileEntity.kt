package com.example.munch_cmpt362.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey
    val userId: String,
    val name: String,
    val email: String,
    val bio: String,
    val profilePictureUrl: String?,
    val searchRadius: Int,
    val lastUpdated: Long,
    val isPendingSync: Boolean = false
)
