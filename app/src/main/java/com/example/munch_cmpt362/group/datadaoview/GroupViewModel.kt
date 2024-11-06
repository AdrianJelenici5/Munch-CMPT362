package com.example.munch_cmpt362.group.datadaoview

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData

class GroupViewModel(private val groupRepository: GroupRepository): ViewModel() {
    val allGroupsLiveData: LiveData<List<Group>> = groupRepository.allUserGroups.asLiveData()

    fun insertGroup(group: Group){
        groupRepository.insertGroup(group)
    }

    fun getAllRestaurants(groupID: Long){
        groupRepository.getAllRestaurants(groupID)
    }

    fun voteUpRestaurant(groupID: Long, userID: Long, restaurantName: String, group: Group){
        groupRepository.voteUpRestaurant(groupID, userID, restaurantName, group)
    }

    fun voteDownRestaurant(groupID: Long, userID: Long, restaurantName: String, group: Group) {
        groupRepository.voteDownRestaurant(groupID, userID, restaurantName, group)
    }
}

class GroupViewModelFactory(private val repository: GroupRepository): ViewModelProvider.Factory{
    override fun <T: ViewModel> create(modelClass: Class<T>): T{
        if(modelClass.isAssignableFrom(GroupViewModel::class.java))
            return GroupViewModel(repository) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}