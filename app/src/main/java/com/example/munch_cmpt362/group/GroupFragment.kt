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
import kotlin.random.Random

class GroupFragment: Fragment() {

    private lateinit var addGroupButton: Button
    private lateinit var myGroupListView: ListView

    private lateinit var groupList: ArrayList<Group>
    private lateinit var myGroupListAdapter: GroupListAdapter
    private lateinit var groupDatabase: GroupDatabase
    private lateinit var groupDatabaseDao: GroupDatabaseDao
    lateinit var groupViewModel: GroupViewModel
    private lateinit var groupRepository: GroupRepository
    private lateinit var groupViewModelFactory: GroupViewModelFactory

    private var STUB_USER_ID = 1L
    private var MEMBER_TAG = "member tag"

    private var arrayName = arrayOf("Alex", "Bob", "Charles", "Dan", "Evan",
        "Frank", "Gabriel")

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
        //user.userName = "Gabriel"
        user.userName = arrayName.get(Random.nextInt(arrayName.size))
        CoroutineScope(IO).launch {
            groupDatabaseDao.insertStubUser(user)
        }
        // hopefully autogenerates 1

        groupRepository = GroupRepository(groupDatabaseDao, STUB_USER_ID)
        groupViewModelFactory = GroupViewModelFactory(groupRepository)
        groupViewModel = ViewModelProvider(requireActivity(), groupViewModelFactory).get(GroupViewModel::class.java)
        groupViewModel.allGroupsLiveData.observe(requireActivity()){
            myGroupListAdapter.replace(it)
            myGroupListAdapter.notifyDataSetChanged()
        }

        addGroupButton.setOnClickListener(){
            val addGroupDialog = AddGroupDialog()
            addGroupDialog.show(parentFragmentManager, "add group")
        }

        myGroupListView.setOnItemClickListener(){ parent, view, position, id ->
            Toast.makeText(requireActivity(), "Stub to display members", Toast.LENGTH_SHORT).show()
            // Get group object
            var group: Group = myGroupListAdapter.getItem(position)
            // Start the GROUP MEMBERS fragment, initialize group object in it
            val groupMemberFragment = GroupMembersFragment()
            groupMemberFragment.group = group
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, groupMemberFragment).addToBackStack("group tag").commit()
        }


        return view
    }

}