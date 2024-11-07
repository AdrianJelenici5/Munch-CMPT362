package munch_cmpt362.database.groups

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import munch_cmpt362.database.groupxuser.GroupWithUsers

// This class holds information regarding groups for the UI and provides it to the UI when neccessary:
class GroupViewModel(private val repository: GroupRepository) : ViewModel() {

    // LiveData object of type List<Comment>. It observes data changes and updates the UI reactively.
    val allExercisesLiveData: LiveData<List<GroupEntry>> = repository.allEntries.asLiveData()

    // Calls the repository's insert command
    fun insertEntry(groupEntry: GroupEntry) {
        viewModelScope.launch {
            repository.insertEntry(groupEntry)
        }
    }

    // Calls the repository's getEntry command:
    fun getEntryById(entryId: Long): Flow<GroupEntry> {
        return repository.getEntry(entryId)
    }

    // Calls the repository's delete command:
    fun deleteEntry(entryId: Long) {
        viewModelScope.launch {
            repository.deleteEntry(entryId)
        }
    }

    fun getGroupWithUsers(groupId: Long):  Flow<GroupWithUsers> {
        return repository.getGroupWithUsers(groupId)
    }

}

// This class is responsible for creating instances of GroupViewModel
class GroupViewModelFactory(private val repository: GroupRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GroupViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GroupViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}