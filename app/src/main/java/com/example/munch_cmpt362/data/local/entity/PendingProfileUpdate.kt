package com.example.munch_cmpt362.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.munch_cmpt362.data.local.Converters

@Entity(tableName = "pending_profile_updates")
data class PendingProfileUpdate(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    @TypeConverters(Converters::class)
    val updates: Map<String, Any>,
    val timestamp: Long = System.currentTimeMillis()
)