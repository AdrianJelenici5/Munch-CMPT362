package com.example.munch_cmpt362.ui.group.datadaoview

import androidx.room.Insert
import androidx.room.Query
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupRepository @Inject constructor(
    private val groupDatabaseDao: GroupDatabaseDao,
    private val firestore: FirebaseFirestore,  // for cache: Added Firestore
    private val coroutineScope: CoroutineScope // for cache: Added CoroutineScope
) {
    companion object {
        private const val CACHE_EXPIRY_HOURS = 24L  // for cache: Added constant
    }
    val allUserGroups: Flow<List<Group>> = groupDatabaseDao.getAllUserGroups(1L) // TODO: Remove hardcoded userID

    fun insertGroup(group: Group){
        CoroutineScope(IO).launch {
            // for cache: Added cache handling
            group.isPendingSync = true
            group.lastFetched = System.currentTimeMillis()
            groupDatabaseDao.insertGroupAndUser(group)

            // for cache: Try to sync with Firestore
            try {
                val groupData = hashMapOf(
                    "groupName" to group.groupName,
                    "listOfUserIds" to listOf(group.userID.toString())
                )
                firestore.collection("group").document(group.groupID.toString())
                    .set(groupData)

                groupDatabaseDao.markAsSynced(group.groupID)
            } catch (e: Exception) {
                // Keep isPendingSync as true for later sync
            }
        }

    }

    fun getAllRestaurants(groupID: Long){
        CoroutineScope(IO).launch{
            groupDatabaseDao.getAllRestaurants(groupID)
        }
    }

    fun voteUpRestaurant(groupID: Long, userID: Long, restaurantName: String, group: Group){
        CoroutineScope(IO).launch{
            // removes the false vote
            groupDatabaseDao.voteUpRestaurant(groupID, userID, restaurantName)
            // for cache: Update lastFetched
            group.lastFetched = System.currentTimeMillis()
            // will add the true vote
            groupDatabaseDao.insertGroupAndUser(group)
        }
    }

    fun voteDownRestaurant(groupID: Long, userID: Long, restaurantName: String, group: Group){
        CoroutineScope(IO).launch{
            // removes the false vote
            groupDatabaseDao.voteUpRestaurant(groupID, userID, restaurantName)
            // for cache: Update lastFetched
            group.lastFetched = System.currentTimeMillis()
            // will add the true vote
            groupDatabaseDao.insertGroupAndUser(group)
        }
    }

    fun getUser(userID: Long){
        CoroutineScope(IO).launch {
            groupDatabaseDao.getUser(userID)
        }
    }

    // Maybe not used
    fun getAllUsersInGroup(groupID: Long){
        CoroutineScope(IO).launch{
            groupDatabaseDao.getAllUsersInGroup(groupID)
        }
    }

    fun insertCounter(counter: Counter){
        CoroutineScope(IO).launch{
            groupDatabaseDao.insertCounter(counter)
        }
    }

    fun getCounter(){
        CoroutineScope(IO).launch{
            groupDatabaseDao.getCounter()
        }
    }

    fun deleteCounter(num: Long){
        CoroutineScope(IO).launch{
            groupDatabaseDao.deleteCounter(num)
        }
    }

    // for cache: New methods for cache management
    suspend fun clearExpiredCache() {
        withContext(IO) {
            val expiryTime = System.currentTimeMillis() - (CACHE_EXPIRY_HOURS * 60 * 60 * 1000)
            groupDatabaseDao.clearOldCache(expiryTime)
        }
    }

    // for cache: Sync pending changes
    suspend fun syncPendingChanges() {
        withContext(IO) {
            val pendingGroups = groupDatabaseDao.getPendingChanges()
            pendingGroups.forEach { group ->
                try {
                    val groupData = hashMapOf(
                        "groupName" to group.groupName,
                        "listOfUserIds" to listOf(group.userID.toString())
                    )
                    firestore.collection("group")
                        .document(group.groupID.toString())
                        .set(groupData)

                    groupDatabaseDao.markAsSynced(group.groupID)
                } catch (e: Exception) {
                    // Keep isPendingSync as true for later retry
                }
            }
        }
    }

    // for cache: Force refresh cache
    suspend fun forceRefreshCache() {
        withContext(IO) {
            groupDatabaseDao.clearCache()
        }
    }

}