package com.example.munch_cmpt362.ui.group.fb

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.munch_cmpt362.ui.group.GroupFb

class GroupFbViewModel: ViewModel() {
    val groupAddedName = MutableLiveData<String>()
    val groupFb = MutableLiveData<GroupFb>()
    val clickedGroup = MutableLiveData<GroupFb>()
    val addedUser = MutableLiveData<String>()
    val voteRestaurantName = MutableLiveData<String>()
    val votedYes = MutableLiveData<Boolean>()
    val votedNo = MutableLiveData<Boolean>()
    val lat = MutableLiveData<Double>()
    val lng = MutableLiveData<Double>()
}