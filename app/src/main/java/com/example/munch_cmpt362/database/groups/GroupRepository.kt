package munch_cmpt362.database.groups

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import munch_cmpt362.database.groupxuser.GroupWithUsers

// Repository that will act as the API for the GroupViewModel to perform ops our group table
// I.e. Responsible for fetching data from the DB using DAO methods and providing the results to the viewModel
class GroupRepository(private val groupDao: GroupDao) {

    // public, immutable property that holds a Livedata of List<ExerciseEntry>,
    // retrieving all GroupEntries from the database
    val allEntries: Flow<List<GroupEntry>> = groupDao.getAllEntries()

    // Allowing insertion of a single GroupEntry object into the database using DAO
    fun insertEntry(groupEntry: GroupEntry) {
        CoroutineScope(IO).launch{
            groupDao.insertEntry(groupEntry)
        }
    }

    // Allowing access of Specific GroupEntry Object from the database using DAO
    fun getEntry(entryId: Long) : Flow<GroupEntry> {
        return groupDao.getEntryWithId(entryId)
    }

    // Allowing deletion of the single GroupEntry Object from the database using DAO
    fun deleteEntry(entryId: Long) {
        CoroutineScope(IO).launch {
            groupDao.deleteEntry(entryId)
        }
    }

    // Function to retrieve a group along with its associated users
    fun getGroupWithUsers(groupId: Long): Flow<GroupWithUsers> {
        return groupDao.getGroupWithUsers(groupId)
    }

}