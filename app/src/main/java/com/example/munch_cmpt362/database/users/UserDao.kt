package munch_cmpt362.database.users

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// '@DAO' tells the compiler that this interface's role is to define how to access data in our db
// I.e. it's role is to provide methods that other classes can use to perform ops on user_table
@Dao
interface UserDao {

    // This method will insert data into the db:
    @Insert
    fun insertEntry(userEntry: UserEntry)

    // This method will select all entries from the db
    @Query("SELECT * FROM user_table")
    fun getAllEntries(): Flow<List<UserEntry>>

    // This method will select specific entry from the db
    @Query("SELECT * FROM user_table WHERE userId = :entryId")
    fun getEntryWithId(entryId: Long): Flow<UserEntry>

    // This entry will delete specific entry from the db
    @Query("DELETE FROM user_table WHERE userId = :entryId")
    fun deleteEntry(entryId: Long)

}