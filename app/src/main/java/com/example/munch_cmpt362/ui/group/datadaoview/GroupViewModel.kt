package com.example.munch_cmpt362.ui.group.datadaoview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.munch_cmpt362.ui.auth.AuthViewModel
import com.example.munch_cmpt362.ui.group.GroupListAdapter
import com.example.munch_cmpt362.ui.group.GroupMemberListAdapter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val authViewModel: AuthViewModel
) : ViewModel() {
    private val _groups = MutableStateFlow<List<Group>>(emptyList())
    val groups: StateFlow<List<Group>> = _groups

    val clickedGroup = MutableLiveData<Group>()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadGroups()
    }

    private fun loadGroups() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // This will automatically handle cache and network operations
                groupRepository.allUserGroups.collect { groupList ->
                    _groups.value = groupList
                }
            } catch (e: Exception) {
                _error.value = "Error loading groups: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun insertGroup(group: Group){
        viewModelScope.launch {
            try {
                groupRepository.insertGroup(group)
            } catch (e: Exception) {
                _error.value = "Error creating group: ${e.message}"
            }
        }
    }

    fun getAllRestaurants(groupID: Long){
        viewModelScope.launch {
            try {
                groupRepository.getAllRestaurants(groupID)
            } catch (e: Exception) {
                _error.value = "Error getting restaurants: ${e.message}"
            }
        }
    }

    fun voteUpRestaurant(groupID: Long, userID: Long, restaurantName: String, group: Group){
        viewModelScope.launch {
            try {
                groupRepository.voteUpRestaurant(groupID, userID, restaurantName, group)
            } catch (e: Exception) {
                _error.value = "Error voting up restaurant: ${e.message}"
            }
        }
    }

    fun voteDownRestaurant(groupID: Long, userID: Long, restaurantName: String, group: Group) {
        viewModelScope.launch {
            try {
                groupRepository.voteDownRestaurant(groupID, userID, restaurantName, group)
            } catch (e: Exception) {
                _error.value = "Error voting down restaurant: ${e.message}"
            }
        }
    }

    // Cache management functions
    fun refreshGroups() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                groupRepository.forceRefreshCache()
                loadGroups()
            } catch (e: Exception) {
                _error.value = "Error refreshing groups: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun syncPendingChanges() {
        viewModelScope.launch {
            try {
                groupRepository.syncPendingChanges()
            } catch (e: Exception) {
                _error.value = "Error syncing changes: ${e.message}"
            }
        }
    }

    // Maybe not used
    fun getAllUsersInGroup(groupID: Long){
        viewModelScope.launch {
            try {
                groupRepository.getAllUsersInGroup(groupID)
            } catch (e: Exception) {
                _error.value = "Error getting users: ${e.message}"
            }
        }
    }

    fun getUser(userID: Long){
        groupRepository.getUser(userID)
    }

    fun insertCounter(counter: Counter){
        groupRepository.insertCounter(counter)
    }

    fun getCounter(){
        groupRepository.getCounter()
    }

    fun deleteCounter(num: Long){
        groupRepository.deleteCounter(num)
    }
}
