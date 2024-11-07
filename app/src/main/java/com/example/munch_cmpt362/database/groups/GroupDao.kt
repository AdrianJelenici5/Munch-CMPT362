package munch_cmpt362.database.groups

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import munch_cmpt362.database.groupxuser.GroupWithUsers

// '@DAO' tells the compiler that this interface's role is to define how to access data in our db
// I.e. it's role is to provide methods that other classes can use to perform ops on group_table
@Dao
interface GroupDao {

    // This method will insert a group entry into the db:
    @Insert
    fun insertEntry(groupEntry: GroupEntry)

    // This method will select all entries from the db
    @Query("SELECT * FROM group_table")
    fun getAllEntries(): Flow<List<GroupEntry>>

    // This method will select specific entry from the db
    @Query("SELECT * FROM group_table WHERE groupId = :entryId")
    fun getEntryWithId(entryId: Long): Flow<GroupEntry>

    // This entry will delete specific entry from the db
    @Query("DELETE FROM group_table WHERE groupId = :entryId")
    fun deleteEntry(entryId: Long)

    // This query will get the groupId of a table
    // and then using GroupUserCrossRef, it will get all the users assocated with that group
    // and create a object class that holds a groupEntry and a list of userEntrys
    @Transaction // ensures that Room will retrieve all records s within a single transaction, so the data remains consistent
    @Query("SELECT * FROM group_table WHERE groupId = :groupId")
    fun getGroupWithUsers(groupId: Long): Flow<GroupWithUsers>

}