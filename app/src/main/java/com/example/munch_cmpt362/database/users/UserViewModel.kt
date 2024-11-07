package munch_cmpt362.database.users

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

// This class holds information for the UI and provides it to the UI when neccessary:

class UserViewModel(private val repository: UserRepository) : ViewModel() {

    // LiveData object of type List<UserEntry>. It observes data changes and updates the UI reactively.
    val allExercisesLiveData: LiveData<List<UserEntry>> = repository.allEntries.asLiveData()

    // Calls the repository's insert command
    fun insertEntry(userEntry: UserEntry) {
        viewModelScope.launch {
            repository.insertEntry(userEntry)
        }
    }

    // Calls the repository's getEntry command:
    fun getEntryById(entryId: Long): Flow<UserEntry> {
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
class UserViewModelFactory(private val repository: UserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}