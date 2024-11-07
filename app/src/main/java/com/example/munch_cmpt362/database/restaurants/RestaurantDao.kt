package munch_cmpt362.database.restaurants

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// '@DAO' tells the compiler that this interface's role is to define how to access data in our db
// I.e. it's role is to provide methods that other classes can use to perform ops on restaurant_table
@Dao
interface RestaurantDao {

    // This method will insert data into the db:
    @Insert
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

}