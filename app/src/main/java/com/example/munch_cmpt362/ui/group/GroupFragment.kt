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
import com.example.munch_cmpt362.ui.group.fb.GroupFbViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class GroupFragment: Fragment() {

    private lateinit var addGroupButton: Button
    private lateinit var myGroupListView: ListView

    private lateinit var groupFbList: ArrayList<GroupFb>
    private lateinit var myGroupFbListAdapter: GroupFbListAdapter
    private lateinit var myGroupFbViewModel: GroupFbViewModel

    private var lat = 0.0
    private var lng = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.group_fragment, container, false)

        myGroupListView = view.findViewById(R.id.group_list)
        addGroupButton = view.findViewById(R.id.add_group)

        // TRYING OUT THE FIREBASE
        val authViewModel = ViewModelProvider(requireActivity()).get(AuthViewModel::class.java)
        val userID = authViewModel.returnID()!!.uid

        groupFbList = ArrayList()
        myGroupFbListAdapter = GroupFbListAdapter(requireActivity(), groupFbList)
        myGroupListView.adapter = myGroupFbListAdapter

        val database = Firebase.firestore

        // Adding groups to grouplistview
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
                    requireActivity().runOnUiThread {
                        myGroupFbListAdapter.replace(groupFbList)
                        myGroupFbListAdapter.notifyDataSetChanged()
                        // Refreshes layout
                        myGroupListView.requestLayout()
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("TAG", "Error getting documents: ", exception)
                }
        }
        myGroupFbViewModel = ViewModelProvider(requireActivity()).get(GroupFbViewModel::class.java)

        addGroupButton.setOnClickListener(){
            //val addGroupDialog = AddGroupDialog()
            val addGroupDialog = AddGroupFbDialog()
            addGroupDialog.show(parentFragmentManager, "add group")

            parentFragmentManager.executePendingTransactions()
            addGroupDialog.dialog?.setOnDismissListener {
                if(myGroupFbViewModel.groupAddedName.value != null){
                    // Use groupAddedName and update database and list
                    val authViewModel = ViewModelProvider(requireActivity()).get(AuthViewModel::class.java)
                    val userID = authViewModel.returnID()!!.uid

                    CoroutineScope(IO).launch{
                        // Get users list of restaurants and add it to group list
                        val userPrefRef = Firebase.firestore.collection("user-preference").document(userID).get()
                            .addOnSuccessListener { document ->
                                val listRestaurant: MutableList<String> = ArrayList()
                                val rest1 = document.data!!["restaurant_1"].toString()
                                val rest2 = document.data!!["restaurant_2"].toString()
                                val rest3 = document.data!!["restaurant_3"].toString()
                                if(rest1 != ""){
                                    listRestaurant.add(rest1)
                                }
                                if(rest2 != ""){
                                    listRestaurant.add(rest2)
                                }
                                if(rest3 != ""){
                                    listRestaurant.add(rest3)
                                }

                                // Add list to group database
                                val ref = Firebase.firestore.collection("group").document()
                                val list = ArrayList<String>()
                                list.add(userID)
                                val groupSet = hashMapOf(
                                    "groupId" to ref.id,
                                    "groupName" to myGroupFbViewModel.groupAddedName.value,
                                    "listOfRestaurants" to listRestaurant,
                                    "listOfUserIds" to list,
                                )
                                ref.set(groupSet)
                                val groupFb = GroupFb()
                                groupFb.groupId = ref.id
                                groupFb.groupName = myGroupFbViewModel.groupAddedName.value!!
                                groupFb.listOfRestaurants = listRestaurant
                                groupFb.listOfUserIds = list
                                //myGroupFbViewModel.groupFb.postValue(groupFb)
                                groupFbList.add(groupFb)
                                requireActivity().runOnUiThread {
                                    myGroupFbListAdapter.replace(groupFbList)
                                    myGroupFbListAdapter.notifyDataSetChanged()
                                }
                                myGroupFbViewModel.groupAddedName.value = null
                            }
                            .addOnFailureListener { exception ->
                                Log.w("TAG", "Error getting documents: ", exception)
                            }
                    }
                }
                (parentFragmentManager.findFragmentByTag("add group") as DialogFragment).dismiss()
            }
        }
//
        myGroupListView.setOnItemClickListener(){ parent, view, position, id ->
            Toast.makeText(requireActivity(), "Stub to display members", Toast.LENGTH_SHORT).show()
            // Get group object
            var groupFb: GroupFb = myGroupFbListAdapter.getItem(position)

            // Add clicked group to viewmodel
            myGroupFbViewModel.clickedGroup.value = groupFb
            myGroupFbViewModel.lat.value = lat
            myGroupFbViewModel.lng.value = lng
            findNavController().navigate(R.id.action_mainFragment_to_groupMembersFragment)
        }


        return view
    }

    fun updateLocation(latitude: Double, longitude: Double) {
        lat = latitude
        lng = longitude
    }
}