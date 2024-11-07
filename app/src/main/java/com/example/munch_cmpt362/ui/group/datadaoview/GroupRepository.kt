package com.example.munch_cmpt362.ui.group.datadaoview

import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class GroupRepository (private val groupDatabaseDao: GroupDatabaseDao, private val userID: Long) {
    val allUserGroups: Flow<List<Group>> = groupDatabaseDao.getAllUserGroups(userID)

    fun insertGroup(group: Group){
        CoroutineScope(IO).launch {
            groupDatabaseDao.insertGroupAndUser(group)
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
            // will add the true vote
            groupDatabaseDao.insertGroupAndUser(group)
        }
    }

    fun voteDownRestaurant(groupID: Long, userID: Long, restaurantName: String, group: Group){
        CoroutineScope(IO).launch{
            // removes the false vote
            groupDatabaseDao.voteUpRestaurant(groupID, userID, restaurantName)
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

}