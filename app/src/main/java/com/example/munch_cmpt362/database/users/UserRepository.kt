package munch_cmpt362.database.users

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

// Repository that will act as the API for the UserViewModel to perform ops our restaurant table
// I.e. Responsible for fetching data from the DB using DAO methods and providing the results to the viewModel
class UserRepository(private val userDao: UserDao) {

    // public, immutable property that holds a Livedata of List<ExerciseEntry>,
    // retrieving all UserEntries from the db
    val allEntries: Flow<List<UserEntry>> = userDao.getAllEntries()

    // Allowing insertion of a single UserEntry object into the database using DAO
    fun insertEntry(userEntry: UserEntry) {
        CoroutineScope(IO).launch{
            userDao.insertEntry(userEntry)
        }
    }

    // Allowing access of Specific UserEntry Object from the database using DAO
    fun getEntry(entryId: Long) : Flow<UserEntry> {
        return userDao.getEntryWithId(entryId)
    }

    // Allowing deletion of the single UserEntry Object from the database using DAO
    fun deleteEntry(entryId: Long) {
        CoroutineScope(IO).launch {
            userDao.deleteEntry(entryId)
        }
    }

}