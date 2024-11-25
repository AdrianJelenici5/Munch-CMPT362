package com.example.munch_cmpt362.data.local.cache

import com.example.munch_cmpt362.ui.group.datadaoview.Group
import com.example.munch_cmpt362.ui.group.GroupFb
import com.example.munch_cmpt362.ui.group.datadaoview.GroupDatabaseDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupCacheManager @Inject constructor(
    private val groupDao: GroupDatabaseDao,
    private val scope: CoroutineScope
) {
    companion object {
        private const val CACHE_EXPIRY_HOURS = 24L
        private const val MIN_CACHE_SIZE = 10
    }

    suspend fun initializeCache(userId: Long) {
        withContext(Dispatchers.IO) {
            if (groupDao.getCacheSize() < MIN_CACHE_SIZE) {
                clearExpiredCache()
            }
        }
    }

    // Convert GroupFb to Group entities and cache them
    suspend fun cacheGroupFb(groupFb: GroupFb, userId: Long) {
        withContext(Dispatchers.IO) {
            val group = Group(
                groupID = groupFb.groupId.toLong(),
                userID = userId,
                groupName = groupFb.groupName,
                restaurantName = groupFb.listOfRestaurants.firstOrNull() ?: "",
                lastFetched = System.currentTimeMillis(),
                isCached = true
            )
            groupDao.insertGroupAndUser(group)
        }
    }

    suspend fun cacheGroupsFb(groupsList: List<GroupFb>, userId: Long) {
        withContext(Dispatchers.IO) {
            groupsList.forEach { groupFb ->
                cacheGroupFb(groupFb, userId)
            }
        }
    }

    suspend fun getCachedGroups(userId: Long): Flow<List<Group>> {
        return groupDao.getAllUserGroups(userId)
    }

    suspend fun updateGroupCache(group: Group) {
        withContext(Dispatchers.IO) {
            group.lastFetched = System.currentTimeMillis()
            groupDao.insertGroupAndUser(group)
        }
    }

    private suspend fun clearExpiredCache() {
        withContext(Dispatchers.IO) {
            val expiryTime = System.currentTimeMillis() - (CACHE_EXPIRY_HOURS * 60 * 60 * 1000)
            groupDao.clearOldCache(expiryTime)
        }
    }

    suspend fun updateLastFetched(groupId: Long) {
        withContext(Dispatchers.IO) {
            groupDao.updateLastFetched(groupId, System.currentTimeMillis())
        }
    }

    suspend fun getPendingChanges(): List<Group> {
        return withContext(Dispatchers.IO) {
            groupDao.getPendingChanges()
        }
    }

    suspend fun markAsSynced(groupId: Long) {
        withContext(Dispatchers.IO) {
            groupDao.markAsSynced(groupId)
        }
    }

    suspend fun invalidateCache() {
        withContext(Dispatchers.IO) {
            groupDao.clearCache()
        }
    }

    // Helper method to convert Firebase group to local group
    suspend fun convertAndCacheFirebaseGroup(
        groupId: String,
        groupName: String,
        restaurants: List<String>,
        userId: Long
    ) {
        val group = Group(
            groupID = groupId.toLong(),
            userID = userId,
            groupName = groupName,
            restaurantName = restaurants.firstOrNull() ?: "",
            lastFetched = System.currentTimeMillis(),
            isCached = true
        )
        updateGroupCache(group)
    }
}