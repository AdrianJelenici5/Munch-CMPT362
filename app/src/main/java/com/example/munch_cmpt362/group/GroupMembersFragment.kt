package com.example.munch_cmpt362.group

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import androidx.annotation.UiThread
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

class GroupMembersFragment: Fragment() {

    private lateinit var addGroupMemberButton: Button
    private lateinit var myGroupMemberListView: ListView

    private lateinit var groupMemberList: ArrayList<User>
    lateinit var myGroupMemberListAdapter: GroupMemberListAdapter
    private lateinit var groupDatabase: GroupDatabase
    private lateinit var groupDatabaseDao: GroupDatabaseDao
    lateinit var groupViewModel: GroupViewModel
    private lateinit var groupRepository: GroupRepository
    private lateinit var groupViewModelFactory: GroupViewModelFactory

    private var STUB_USER_ID = 1L
    //private var STUB_GROUP_ID = 1L

    private lateinit var allUserGroupMembers: List<User>
    lateinit var group: Group

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.group_members_fragment, container, false)

        myGroupMemberListView = view.findViewById(R.id.Members_listview)
        addGroupMemberButton = view.findViewById(R.id.Add_members_button)

        groupMemberList = ArrayList()
        allUserGroupMembers = ArrayList()
        myGroupMemberListAdapter = GroupMemberListAdapter(requireActivity(), groupMemberList)
        myGroupMemberListView.adapter = myGroupMemberListAdapter

        groupDatabase = GroupDatabase.getInstance(requireActivity())
        groupDatabaseDao = groupDatabase.groupDatabaseDao

        groupRepository = GroupRepository(groupDatabaseDao, STUB_USER_ID)
        groupViewModelFactory = GroupViewModelFactory(groupRepository)
        groupViewModel = ViewModelProvider(requireActivity(), groupViewModelFactory).get(GroupViewModel::class.java)
        updateMembers()

        // Pressing add members button
        addGroupMemberButton.setOnClickListener(){
            groupViewModel.currentGroupAdding = group
            val addGroupMemberDialog = AddGroupMemberDialog()
            addGroupMemberDialog.show(parentFragmentManager, "add group member")

            // wait
            parentFragmentManager.executePendingTransactions()
            addGroupMemberDialog.dialog?.setOnDismissListener {
                updateMembers()
            }
        }

        return view
    }

    fun updateMembers(){
        CoroutineScope(IO).launch{
            allUserGroupMembers = groupDatabaseDao.getAllUsersInGroup(group.groupID)
            println("THIS IS GROUP: $allUserGroupMembers")
            activity?.runOnUiThread(){
                myGroupMemberListAdapter.replace(allUserGroupMembers)
                myGroupMemberListAdapter.notifyDataSetChanged()
            }
        }
    }

}