package com.example.munch_cmpt362.data.local.cache

import android.util.Log
import com.example.munch_cmpt362.data.local.dao.ProfileDao
import com.example.munch_cmpt362.data.local.entity.PendingProfileUpdate
import com.example.munch_cmpt362.data.local.entity.UserProfileEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileCacheManager @Inject constructor(
    private val profileDao: ProfileDao,
    private val scope: CoroutineScope
) {
    companion object {
        private const val CACHE_EXPIRY_HOURS = 24L
        private const val TAG = "ProfileCacheManager"
    }

    fun getProfileFlow(userId: String): Flow<UserProfileEntity?> {
        return profileDao.getProfileFlow(userId)
    }

    suspend fun cacheProfile(profile: UserProfileEntity) {
        withContext(Dispatchers.IO) {
            try {
                profileDao.insertProfile(profile.copy(lastUpdated = System.currentTimeMillis()))
                Log.d(TAG, "Profile cached successfully for user: ${profile.userId}")
            } catch (e: Exception) {
                Log.e(TAG, "Error caching profile", e)
            }
        }
    }

    suspend fun getCachedProfile(userId: String): UserProfileEntity? {
        return withContext(Dispatchers.IO) {
            try {
                profileDao.getProfile(userId)
            } catch (e: Exception) {
                Log.e(TAG, "Error getting cached profile", e)
                null
            }
        }
    }

    suspend fun queueOfflineUpdate(userId: String, updates: Map<String, Any>) {
        withContext(Dispatchers.IO) {
            try {
                val pendingUpdate = PendingProfileUpdate(
                    userId = userId,
                    updates = updates
                )
                profileDao.insertPendingUpdate(pendingUpdate)
                profileDao.updateSyncStatus(userId, true)
                Log.d(TAG, "Offline update queued for user: $userId")
            } catch (e: Exception) {
                Log.e(TAG, "Error queuing offline update", e)
            }
        }
    }

    suspend fun getPendingUpdates(userId: String): List<PendingProfileUpdate> {
        return withContext(Dispatchers.IO) {
            profileDao.getPendingUpdates(userId)
        }
    }

    suspend fun clearPendingUpdates(userId: String) {
        withContext(Dispatchers.IO) {
            profileDao.clearPendingUpdates(userId)
            profileDao.updateSyncStatus(userId, false)
        }
    }

    suspend fun isCacheExpired(userId: String): Boolean {
        return withContext(Dispatchers.IO) {
            val profile = profileDao.getProfile(userId)
            val expiryTime = System.currentTimeMillis() - (CACHE_EXPIRY_HOURS * 60 * 60 * 1000)
            profile?.lastUpdated ?: 0 < expiryTime
        }
    }
}