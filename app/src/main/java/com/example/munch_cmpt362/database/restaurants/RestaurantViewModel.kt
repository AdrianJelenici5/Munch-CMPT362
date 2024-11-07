package munch_cmpt362.database.restaurants

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

// This class holds information related to restaurant for the UI and provides it to the UI when neccessary:
class RestaurantViewModel(private val repository: RestaurantRepository) : ViewModel() {

    // LiveData object of type List<Comment>. It observes data changes and updates the UI reactively.
    val allExercisesLiveData: LiveData<List<RestaurantEntry>> = repository.allEntries.asLiveData()

    // Calls the repository's insert command
    fun insertEntry(restaurantEntry: RestaurantEntry) {
        viewModelScope.launch {
            repository.insertEntry(restaurantEntry)
        }
    }

    // Calls the repository's getEntry command:
    fun getEntryById(entryId: Long): Flow<RestaurantEntry> {
        return repository.getEntry(entryId)
    }

    // Calls the repository's delete command:
    fun deleteEntry(entryId: Long) {
        viewModelScope.launch {
            repository.deleteEntry(entryId)
        }
    }

}

// This class is responsible for creating instances of ExerciseViewModel
class RestaurantViewModelFactory(private val repository: RestaurantRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RestaurantViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RestaurantViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}