package com.example.munch_cmpt362.ui.group

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import androidx.annotation.UiThread
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.munch_cmpt362.R
import com.example.munch_cmpt362.ui.group.datadaoview.Group
import com.example.munch_cmpt362.ui.group.datadaoview.GroupDatabase
import com.example.munch_cmpt362.ui.group.datadaoview.GroupDatabaseDao
import com.example.munch_cmpt362.ui.group.datadaoview.GroupRepository
import com.example.munch_cmpt362.ui.group.datadaoview.GroupViewModel
import com.example.munch_cmpt362.ui.group.datadaoview.GroupViewModelFactory
import com.example.munch_cmpt362.ui.group.datadaoview.User
import com.example.munch_cmpt362.ui.group.fb.GroupFbViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.lang.Thread.sleep

class GroupMembersFragment: Fragment() {

    private lateinit var addGroupMemberButton: Button
    private lateinit var myGroupMemberListView: ListView

    //private lateinit var groupMemberList: ArrayList<User>
    //lateinit var myGroupMemberListAdapter: GroupMemberListAdapter
//    private lateinit var groupDatabase: GroupDatabase
//    private lateinit var groupDatabaseDao: GroupDatabaseDao
//    lateinit var groupViewModel: GroupViewModel
//    private lateinit var groupRepository: GroupRepository
//    private lateinit var groupViewModelFactory: GroupViewModelFactory
//
//    private var STUB_USER_ID = 1L
    //private var STUB_GROUP_ID = 1L

    //private lateinit var allUserGroupMembers: List<User>

    lateinit var myGroupFbMemberListAdapter: GroupFbMemberListAdapter
    private lateinit var groupFbMemberList: ArrayList<Users>
    private lateinit var allUserGroupFbMembers: MutableList<Users>
    private lateinit var myGroupFbViewModel: GroupFbViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.group_members_fragment, container, false)

        myGroupMemberListView = view.findViewById(R.id.Members_listview)
        addGroupMemberButton = view.findViewById(R.id.Add_members_button)

        //groupMemberList = ArrayList()
        //allUserGroupMembers = ArrayList()
        //myGroupMemberListAdapter = GroupMemberListAdapter(requireActivity(), groupMemberList)
        //myGroupMemberListView.adapter = myGroupMemberListAdapter

//        groupDatabase = GroupDatabase.getInstance(requireActivity())
//        groupDatabaseDao = groupDatabase.groupDatabaseDao
//
//        groupRepository = GroupRepository(groupDatabaseDao, STUB_USER_ID)
//        groupViewModelFactory = GroupViewModelFactory(groupRepository)
//        groupViewModel = ViewModelProvider(requireActivity(), groupViewModelFactory).get(
//            GroupViewModel::class.java)

        //updateMembers()


        groupFbMemberList = ArrayList()
        allUserGroupFbMembers = ArrayList()
        myGroupFbMemberListAdapter = GroupFbMemberListAdapter(requireActivity(), groupFbMemberList)
        myGroupMemberListView.adapter = myGroupFbMemberListAdapter

        myGroupFbViewModel = ViewModelProvider(requireActivity()).get(GroupFbViewModel::class.java)

        updateMembers()
        // Pressing add members button
        addGroupMemberButton.setOnClickListener(){
            val addGroupMemberDialog = AddGroupFbMemberDialog()
            addGroupMemberDialog.show(parentFragmentManager, "add group member")

            // wait
            parentFragmentManager.executePendingTransactions()
            addGroupMemberDialog.dialog?.setOnDismissListener {
                // waits for dialog to finish before calling update again
                sleep(1000L)
                updateMembers()
                (parentFragmentManager.findFragmentByTag("add group member") as DialogFragment).dismiss()
            }
        }

        return view
    }

//    fun updateMembers(){
//        CoroutineScope(IO).launch{
//            allUserGroupMembers = groupDatabaseDao.getAllUsersInGroup(groupViewModel.clickedGroup.value!!.groupID)
//            println("THIS IS GROUP: $allUserGroupMembers")
//            activity?.runOnUiThread(){
//                myGroupMemberListAdapter.replace(allUserGroupMembers)
//                myGroupMemberListAdapter.notifyDataSetChanged()
//            }
//        }
//    }
    fun updateMembers() {
        CoroutineScope(IO).launch {
            // get members in the group
            val groupFb = myGroupFbViewModel.clickedGroup.value!!
            val mutList: MutableList<Users> = ArrayList()
            val database = Firebase.firestore
            database.collection("group").whereEqualTo("groupId", groupFb.groupId).get()
                .addOnSuccessListener { documents ->
                    // There should only be one document
                    for (document in documents) {
                        Log.d("TAG", "GABRIEL ${document.id} => ${document.data}")
                        val listUsers = document.data["listOfUserIds"] as MutableList<String>
                        allUserGroupFbMembers.clear()
                        for (user in listUsers){
                            database.collection("users").whereEqualTo("userId", user).get()
                                .addOnSuccessListener { nestedDocuments ->
                                    for (nestedDocument in nestedDocuments) {
                                        println("GABRIEL user ${nestedDocument.data}")
                                        var myUser = Users()
                                        myUser.userId = nestedDocument.data["userId"] as String
                                        myUser.name = nestedDocument.data["name"] as String
                                        allUserGroupFbMembers.add(myUser)
                                        // update adapter after
                                        requireActivity().runOnUiThread {
                                            myGroupFbMemberListAdapter.replace(allUserGroupFbMembers)
                                            myGroupFbMemberListAdapter.notifyDataSetChanged()
                                            println("GABRIEL MYGROUPMEMBER end $allUserGroupFbMembers")
                                        }
                                    }
                                }
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("TAG", "Error getting documents: ", exception)
                }
        }
    }


}