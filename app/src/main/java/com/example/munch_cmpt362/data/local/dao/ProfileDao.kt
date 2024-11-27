package com.example.munch_cmpt362.data.local.dao

import androidx.room.*
import com.example.munch_cmpt362.data.local.entity.PendingProfileUpdate
import com.example.munch_cmpt362.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfileEntity)

    @Query("SELECT * FROM user_profiles WHERE userId = :userId")
    fun getProfileFlow(userId: String): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profiles WHERE userId = :userId")
    suspend fun getProfile(userId: String): UserProfileEntity?

    @Query("DELETE FROM user_profiles WHERE userId = :userId")
    suspend fun deleteProfile(userId: String)

    @Query("UPDATE user_profiles SET isPendingSync = :isPending WHERE userId = :userId")
    suspend fun updateSyncStatus(userId: String, isPending: Boolean)

    // Pending updates management
    @Insert
    suspend fun insertPendingUpdate(update: PendingProfileUpdate)

    @Query("SELECT * FROM pending_profile_updates WHERE userId = :userId ORDER BY timestamp ASC")
    suspend fun getPendingUpdates(userId: String): List<PendingProfileUpdate>

    @Delete
    suspend fun deletePendingUpdate(update: PendingProfileUpdate)

    @Query("DELETE FROM pending_profile_updates WHERE userId = :userId")
    suspend fun clearPendingUpdates(userId: String)
}