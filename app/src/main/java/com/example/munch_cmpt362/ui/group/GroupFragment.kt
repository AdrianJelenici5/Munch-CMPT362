package com.example.munch_cmpt362.ui.group

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.munch_cmpt362.R
import com.example.munch_cmpt362.ui.auth.AuthViewModel
import com.example.munch_cmpt362.ui.group.datadaoview.Counter
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
import kotlin.random.Random

class GroupFragment: Fragment() {

    private lateinit var addGroupButton: Button
    private lateinit var myGroupListView: ListView

    //private lateinit var groupList: ArrayList<Group>
    //private lateinit var myGroupListAdapter: GroupListAdapter
    //private lateinit var groupDatabase: GroupDatabase
    //private lateinit var groupDatabaseDao: GroupDatabaseDao
    //lateinit var groupViewModel: GroupViewModel
    //private lateinit var groupRepository: GroupRepository
    //private lateinit var groupViewModelFactory: GroupViewModelFactory

    private var STUB_USER_ID = 1L
    private var MEMBER_TAG = "member tag"

    private var arrayName = arrayOf("Alex", "Bob", "Charles", "Dan", "Evan",
        "Frank", "Gabriel")

    private lateinit var groupFbList: ArrayList<GroupFb>
    private lateinit var myGroupFbListAdapter: GroupFbListAdapter
    private lateinit var myGroupFbViewModel: GroupFbViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.group_fragment, container, false)

        myGroupListView = view.findViewById(R.id.group_list)
        addGroupButton = view.findViewById(R.id.add_group)

//        groupList = ArrayList()
//        myGroupListAdapter = GroupListAdapter(requireActivity(), groupList)
//        myGroupListView.adapter = myGroupListAdapter
//
//        groupDatabase = GroupDatabase.getInstance(requireActivity())
//        groupDatabaseDao = groupDatabase.groupDatabaseDao
//        // get our User and stub into database
//        var user = User()
//        user.userName = arrayName.get(Random.nextInt(arrayName.size))
//        CoroutineScope(IO).launch {
//            groupDatabaseDao.insertStubUser(user)
//        }
//        // hopefully autogenerates 1
//
//        groupRepository = GroupRepository(groupDatabaseDao, STUB_USER_ID)
//        groupViewModelFactory = GroupViewModelFactory(groupRepository)
//        groupViewModel = ViewModelProvider(requireActivity(), groupViewModelFactory).get(
//            GroupViewModel::class.java)
//        groupViewModel.allGroupsLiveData.observe(requireActivity()){
//            myGroupListAdapter.replace(it)
//            myGroupListAdapter.notifyDataSetChanged()
//        }
        // TRYING OUT THE FIREBASE
        val authViewModel = ViewModelProvider(requireActivity()).get(AuthViewModel::class.java)
        val userID = authViewModel.returnID()!!.uid

        groupFbList = ArrayList()
        myGroupFbListAdapter = GroupFbListAdapter(requireActivity(), groupFbList)
        myGroupListView.adapter = myGroupFbListAdapter

        println("GABRIEL THIS IS ID: $userID")
        val database = Firebase.firestore
        // Collection = entry, query = field
        CoroutineScope(IO).launch {
            database.collection("users").whereEqualTo("userId", userID).get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        Log.d("TAG", "GABRIEL ${document.id} => ${document.data}")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("TAG", "Error getting documents: ", exception)
                }
        }

        CoroutineScope(IO).launch {
            database.collection("group").whereArrayContains("listOfUserIds", userID).get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        var groupFb = GroupFb()
                        Log.d("TAG", "GABRIEL GROUP ${document.id} => ${document.data}")
                        groupFb.groupId = document.id
                        groupFb.groupName = document.data["groupName"].toString()
                        groupFb.listOfUserIds = document.data["listOfUserIds"] as MutableList<String>
                        groupFb.listOfRestaurants = document.data["listOfRestaurants"] as MutableList<String>
                        groupFbList.add(groupFb)
                    }
                    myGroupFbListAdapter.replace(groupFbList)
                    myGroupFbListAdapter.notifyDataSetChanged()
                }
                .addOnFailureListener { exception ->
                    Log.w("TAG", "Error getting documents: ", exception)
                }
        }
        myGroupFbViewModel = ViewModelProvider(requireActivity()).get(GroupFbViewModel::class.java)
        // Initialize counter
//        if(groupViewModel.getCounter() == null){
//            var counter = Counter()
//            groupViewModel.insertCounter(counter)
//        }

        addGroupButton.setOnClickListener(){
            //val addGroupDialog = AddGroupDialog()
            val addGroupDialog = AddGroupFbDialog()
            addGroupDialog.show(parentFragmentManager, "add group")

            parentFragmentManager.executePendingTransactions()
            addGroupDialog.dialog?.setOnDismissListener {
                if(myGroupFbViewModel.groupFb.value != null){
                    groupFbList.add(myGroupFbViewModel.groupFb.value!!)
                    myGroupFbListAdapter.replace(groupFbList)
                    myGroupFbListAdapter.notifyDataSetChanged()
                    myGroupFbViewModel.groupFb.value = null
                }
                (parentFragmentManager.findFragmentByTag("add group") as DialogFragment).dismiss()
            }
        }
//
        myGroupListView.setOnItemClickListener(){ parent, view, position, id ->
            Toast.makeText(requireActivity(), "Stub to display members", Toast.LENGTH_SHORT).show()
            // Get group object
            var groupFb: GroupFb = myGroupFbListAdapter.getItem(position)
            // Start the GROUP MEMBERS fragment, initialize group object in it
//            val groupMemberFragment = GroupMembersFragment()
//            groupMemberFragment.group = group
//            requireActivity().supportFragmentManager.beginTransaction()
//                .replace(android.R.id.content, groupMemberFragment).addToBackStack("group tag").commit()

            // Add clicked group to viewmodel
            myGroupFbViewModel.clickedGroup.value = groupFb
            findNavController().navigate(R.id.action_mainFragment_to_groupMembersFragment)
        }


        return view
    }
}