package com.example.munch_cmpt362.group.datadaoview

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDatabaseDao {
    // Creating or adding members (add row to group database)
    @Insert
    suspend fun insertGroupAndUser(group: Group)

    // Get all the groups from this one user
    @Query("SELECT * FROM group_table WHERE user_ID=:key")
    fun getAllUserGroups(key: Long): Flow<List<Group>>

    // Get all restaurants
    @Query("SELECT restaurant_name FROM group_table WHERE groupID=:key")
    fun getAllRestaurants(key: Long): List<String>

    // Voting, delete the false
    @Query("DELETE FROM group_table WHERE groupID=:key AND user_ID = :userID " +
            "AND restaurant_name = :restaurantName  AND voting = 0")
    suspend fun voteUpRestaurant(key: Long, userID: Long, restaurantName: String)

    // Voting, delete the true
    @Query("DELETE FROM group_table WHERE groupID = :key AND user_ID = :userID " +
            "AND restaurant_name = :restaurantName  AND voting = 1")
    suspend fun voteDownRestaurant(key: Long, userID: Long, restaurantName: String)

    // Get the user from user_id
    @Query("SELECT * FROM USER_TABLE WHERE user_ID = :key")
    fun getUser(key: Long): User

    // Get all people in the group
    @Query("SELECT * FROM USER_TABLE WHERE user_ID IN (SELECT user_ID FROM group_table WHERE groupID = :key)")
    fun getAllUsersInGroup(key: Long): List<User>

    // Stub User insert
    @Insert
    suspend fun insertStubUser(user: User)
}