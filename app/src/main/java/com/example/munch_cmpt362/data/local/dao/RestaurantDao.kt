package com.example.munch_cmpt362.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import com.example.munch_cmpt362.data.local.entity.RestaurantEntry

// '@DAO' tells the compiler that this interface's role is to define how to access data in our db
// I.e. it's role is to provide methods that other classes can use to perform ops on restaurant_table
@Dao
interface RestaurantDao {
    // This method will insert data into the db:
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertEntry(restaurantEntry: RestaurantEntry)

    // This method will select all entries from the db
    @Query("SELECT * FROM restaurant_table")
    fun getAllEntries(): Flow<List<RestaurantEntry>>

    // This method will select specific entry from the db
    @Query("SELECT * FROM restaurant_table WHERE restaurantId = :entryId")
    fun getEntryWithId(entryId: Long): Flow<RestaurantEntry>

    // This entry will delete specific entry from the db
    @Query("DELETE FROM restaurant_table WHERE restaurantId = :entryId")
    fun deleteEntry(entryId: Long)

    @Query("UPDATE restaurant_table SET userScore = :newScore WHERE restaurantId = :restaurantId")
    fun updateUserScore(restaurantId: String, newScore: Int)

    // This method will return score of restaurant based on ID
    @Query("SELECT userScore FROM restaurant_table WHERE restaurantId = :restaurantId")
    fun getScoreById(restaurantId: String): Int

    // This method will return individual top 3 restaurant preference based on score.
    @Query("SELECT restaurantId FROM restaurant_table ORDER BY userScore DESC LIMIT 3")
    fun getTop3Restaurants(): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(restaurants: List<RestaurantEntry>)

    // New caching-related methods
    @Query("SELECT COUNT(*) FROM restaurant_table")
    suspend fun getCacheSize(): Int

    @Query("SELECT * FROM restaurant_table WHERE restaurantId NOT IN (:seenIds) LIMIT :limit")
    suspend fun getUnseenRestaurants(seenIds: List<String>, limit: Int): List<RestaurantEntry>

    @Query("DELETE FROM restaurant_table WHERE restaurantId IN " +
            "(SELECT restaurantId FROM restaurant_table ORDER BY userScore ASC LIMIT :count)")
    suspend fun removeLowestRatedRestaurants(count: Int)

    @Query("SELECT * FROM restaurant_table WHERE userScore >= 0 ORDER BY userScore DESC LIMIT :limit")
    suspend fun getTopRatedRestaurants(limit: Int): List<RestaurantEntry>

    @Query("UPDATE restaurant_table SET lastFetched = :timestamp WHERE restaurantId = :id")
    suspend fun updateLastFetched(id: String, timestamp: Long)

    @Query("DELETE FROM restaurant_table WHERE lastFetched < :timestamp")
    suspend fun clearOldCache(timestamp: Long)

    @Query("SELECT * FROM restaurant_table WHERE restaurantId = :id")
    suspend fun getRestaurantById(id: String): RestaurantEntry?

    @Query("UPDATE restaurant_table SET isSwiped = :isSwiped WHERE restaurantId = :restaurantId")
    fun updateIsSwiped(restaurantId: String, isSwiped: Boolean)

    @Query("SELECT * FROM restaurant_table WHERE isSwiped = 1")
    suspend fun getSwipedRestaurant(): List<RestaurantEntry>
}