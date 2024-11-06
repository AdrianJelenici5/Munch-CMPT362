package com.example.munch_cmpt362.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.munch_cmpt362.R
import com.example.munch_cmpt362.group.datadaoview.Group
import com.example.munch_cmpt362.group.datadaoview.GroupDatabase
import com.example.munch_cmpt362.group.datadaoview.GroupDatabaseDao
import com.example.munch_cmpt362.group.datadaoview.GroupRepository
import com.example.munch_cmpt362.group.datadaoview.GroupViewModel
import com.example.munch_cmpt362.group.datadaoview.GroupViewModelFactory
import com.example.munch_cmpt362.group.datadaoview.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class GroupFragment: Fragment() {

    private lateinit var addGroupButton: Button
    private lateinit var myGroupListView: ListView

    private lateinit var groupList: ArrayList<Group>
    private lateinit var myGroupListAdapter: GroupListAdapter
    private lateinit var groupDatabase: GroupDatabase
    private lateinit var groupDatabaseDao: GroupDatabaseDao
    private lateinit var groupViewModel: GroupViewModel
    private lateinit var groupRepository: GroupRepository
    private lateinit var groupViewModelFactory: GroupViewModelFactory

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.group_fragment, container, false)


        myGroupListView = view.findViewById(R.id.group_list)
        addGroupButton = view.findViewById(R.id.add_group)

        groupList = ArrayList()
        myGroupListAdapter = GroupListAdapter(requireActivity(), groupList)
        myGroupListView.adapter = myGroupListAdapter

        groupDatabase = GroupDatabase.getInstance(requireActivity())
        groupDatabaseDao = groupDatabase.groupDatabaseDao
        // get our User and stub into database
        var user = User()
        user.userID = 0L
        user.userName = "Gabriel"
        CoroutineScope(IO).launch {
            groupDatabaseDao.insertStubUser(user)
        }
        // hopefully autogenerates 1

        groupRepository = GroupRepository(groupDatabaseDao, 1L)
        groupViewModelFactory = GroupViewModelFactory(groupRepository)
        groupViewModel = ViewModelProvider(requireActivity(), groupViewModelFactory).get(GroupViewModel::class.java)
        groupViewModel.allGroupsLiveData.observe(requireActivity()){
            myGroupListAdapter.replace(it)
            myGroupListAdapter.notifyDataSetChanged()
        }

        addGroupButton.setOnClickListener(){
            Toast.makeText(requireActivity(), "TESTING BUTTON", Toast.LENGTH_SHORT).show()
            // stub add group
            var group = Group()
            group.userID = 1L
            group.groupName = "HAHA"
            groupViewModel.insertGroup(group)
        }


        return view
    }

}