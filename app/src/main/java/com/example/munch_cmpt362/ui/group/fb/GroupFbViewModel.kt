package com.example.munch_cmpt362.ui.group.fb

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.munch_cmpt362.ui.group.GroupFb

class GroupFbViewModel: ViewModel() {
    val groupFb = MutableLiveData<GroupFb>()
    val clickedGroup = MutableLiveData<GroupFb>()
    val addedUser = MutableLiveData<String>()
}