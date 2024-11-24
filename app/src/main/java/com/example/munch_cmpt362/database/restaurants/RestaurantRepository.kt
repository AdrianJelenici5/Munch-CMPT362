package munch_cmpt362.database.restaurants

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

// Repository that will act as the API for the RestaurantViewModel to perform ops our restaurant table
// I.e. Responsible for fetching data from the DB using DAO methods and providing the results to the viewModel
class RestaurantRepository(private val restaurantDao: RestaurantDao) {

    // public, immutable property that holds a Livedata of List<ExerciseEntry>,
    // retrieving all RestaurantEntries from the db
    val allEntries: Flow<List<RestaurantEntry>> = restaurantDao.getAllEntries()

    // Allowing insertion of a single RestaurantEntry object into the database using DAO
    fun insertEntry(restaurantEntry: RestaurantEntry) {
        CoroutineScope(IO).launch{
            restaurantDao.insertEntry(restaurantEntry)
        }
    }

    // Allowing access of Specific RestaurantEntry Object from the database using DAO
    fun getEntry(entryId: Long) : Flow<RestaurantEntry> {
        return restaurantDao.getEntryWithId(entryId)
    }

    // Allowing deletion of the single RestaurantEntry Object from the database using DAO
    fun deleteEntry(entryId: Long) {
        CoroutineScope(IO).launch {
            restaurantDao.deleteEntry(entryId)
        }
    }

    suspend fun getScoreById(restaurantId: String) : Int {
        return restaurantDao.getScoreById(restaurantId)
    }

    fun updateScore(restaurantId: String, newScore: Int) {
        CoroutineScope(IO).launch {
            restaurantDao.updateUserScore(restaurantId, newScore)
        }
    }

    suspend fun getTop3Restaurants() : List<String> {
        return restaurantDao.getTop3Restaurants()
    }

}