package com.example.munch_cmpt362.ui.group

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.munch_cmpt362.Business
import com.example.munch_cmpt362.R
import com.example.munch_cmpt362.data.remote.api.ApiHelper
import com.example.munch_cmpt362.ui.auth.AuthViewModel
import com.example.munch_cmpt362.ui.group.fb.GroupFbViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GroupMembersFragment: Fragment() {

    private lateinit var addGroupMemberButton: Button
    private lateinit var myGroupMemberListView: ListView

    lateinit var myGroupFbMemberListAdapter: GroupFbMemberListAdapter
    private lateinit var groupFbMemberList: ArrayList<Users>
    private lateinit var allUserGroupFbMembers: MutableList<Users>
    private lateinit var myGroupFbViewModel: GroupFbViewModel

    private lateinit var topRestaurantTextview: TextView
    private lateinit var restaurantsListview: RecyclerView
    lateinit var myRestaurantsListAdapter: VoteRestaurantListAdapter
    private lateinit var voteRestaurantList: ArrayList<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.group_members_fragment, container, false)

        myGroupMemberListView = view.findViewById(R.id.Members_listview)
        addGroupMemberButton = view.findViewById(R.id.Add_members_button)

        topRestaurantTextview = view.findViewById(R.id.Most_voted)
        restaurantsListview = view.findViewById(R.id.Voting_listview)
        restaurantsListview.layoutManager = LinearLayoutManager(context)


        groupFbMemberList = ArrayList()
        allUserGroupFbMembers = ArrayList()
        myGroupFbMemberListAdapter = GroupFbMemberListAdapter(requireActivity(), groupFbMemberList)
        myGroupMemberListView.adapter = myGroupFbMemberListAdapter

        myGroupFbViewModel = ViewModelProvider(requireActivity()).get(GroupFbViewModel::class.java)
        voteRestaurantList = ArrayList()
        myRestaurantsListAdapter = VoteRestaurantListAdapter(requireActivity(), voteRestaurantList,
            myGroupFbViewModel.lat.value!!, myGroupFbViewModel.lng.value!!) {
                restaurantId ->
                // Put restaurant name instead of ID for Voting
                myGroupFbViewModel.voteRestaurantName.value = myRestaurantsListAdapter.restaurantCache[restaurantId]!!.name
                val voteRestaurantDialog = VoteRestaurantDialog()
                voteRestaurantDialog.show(parentFragmentManager, "voting")
                // wait
                parentFragmentManager.executePendingTransactions()
                voteRestaurantDialog.dialog?.setOnDismissListener{
                    // voted yes to restaurant
                    if(myGroupFbViewModel.votedYes.value == true){
                        // check if any database records from same group, user, restaurant
                        myGroupFbViewModel.votedYes.value = false
                        votedYes()
                    }

                    // voted no to restaurant
                    if(myGroupFbViewModel.votedNo.value == true){
                        myGroupFbViewModel.votedNo.value = false
                        votedNo()
                    }

                    (parentFragmentManager.findFragmentByTag("voting") as DialogFragment).dismiss()
                }
        }

        restaurantsListview.adapter = myRestaurantsListAdapter

        updateMembers()
        updateTopRestaurant()

        // Pressing add members button
        addGroupMemberButton.setOnClickListener(){
            val addGroupMemberDialog = AddGroupFbMemberDialog()
            addGroupMemberDialog.show(parentFragmentManager, "add group member")

            // wait
            parentFragmentManager.executePendingTransactions()
            addGroupMemberDialog.dialog?.setOnDismissListener {
                if(myGroupFbViewModel.addedUser.value != null){
                    var addedUser = myGroupFbViewModel.addedUser.value
                    // reset added user
                    myGroupFbViewModel.addedUser.value = null
                    // get group Id and list of members to add
                    var groupId = myGroupFbViewModel.clickedGroup.value!!.groupId
                    val database = Firebase.firestore
                    CoroutineScope(IO).launch {
                        database.collection("users").document(addedUser!!).get()
                            .addOnSuccessListener { checkUser ->
                                // No user id matched in database
                                if (!checkUser.exists()) {
                                    requireActivity().runOnUiThread {
                                        Toast.makeText(
                                            activity,
                                            "Sorry, user not found",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    return@addOnSuccessListener
                                }

                                database.collection("group").whereEqualTo("groupId", groupId).get()
                                    .addOnSuccessListener { documents ->
                                        // There should only be one document
                                        for (document in documents) {
                                            Log.d(
                                                "TAG",
                                                "GABRIEL ${document.id} => ${document.data}"
                                            )
                                            var listUsers =
                                                document.data["listOfUserIds"] as MutableList<String>
                                            listUsers.add(addedUser!!)
                                            // add user restaurant preferences too
                                            val userPrefRef =
                                                Firebase.firestore.collection("user-preference")
                                                    .document(addedUser).get()
                                                    .addOnSuccessListener { userPref ->
                                                        val listRestaurant: MutableList<String> =
                                                            ArrayList()
                                                        val rest1 =
                                                            userPref.data!!["restaurant_1"].toString()
                                                        val rest2 =
                                                            userPref.data!!["restaurant_2"].toString()
                                                        val rest3 =
                                                            userPref.data!!["restaurant_3"].toString()
                                                        if (rest1 != "null") {
                                                            listRestaurant.add(rest1)
                                                        }
                                                        if (rest2 != "null") {
                                                            listRestaurant.add(rest2)
                                                        }
                                                        if (rest3 != "null") {
                                                            listRestaurant.add(rest3)
                                                        }
                                                        var oldGroupRestaurant =
                                                            document.data["listOfRestaurants"] as MutableList<String>
                                                        // get distinct items from both list
                                                        var newGroupRestaurant =
                                                            oldGroupRestaurant.union(listRestaurant)
                                                                .toMutableList()
                                                        database.collection("group")
                                                            .document(groupId)
                                                            .update(
                                                                "listOfRestaurants",
                                                                newGroupRestaurant
                                                            )
                                                        database.collection("group")
                                                            .document(groupId)
                                                            .update("listOfUserIds", listUsers)
                                                        updateMembers()
                                                    }
                                        }
                                    }
                            }
                    }
                }
                (parentFragmentManager.findFragmentByTag("add group member") as DialogFragment).dismiss()
            }
        }

//        restaurantsListview.setOnItemClickListener { parent, view, position, id ->
//            var restaurantId = myRestaurantsListAdapter.getItem(position)
//            // Put restaurant name instead of ID for Voting
//            myGroupFbViewModel.voteRestaurantName.value = myRestaurantsListAdapter.restaurantCache[restaurantId]!!.name
//            val voteRestaurantDialog = VoteRestaurantDialog()
//            voteRestaurantDialog.show(parentFragmentManager, "voting")
//            // wait
//            parentFragmentManager.executePendingTransactions()
//            voteRestaurantDialog.dialog?.setOnDismissListener{
//                // voted yes to restaurant
//                if(myGroupFbViewModel.votedYes.value == true){
//                    // check if any database records from same group, user, restaurant
//                    myGroupFbViewModel.votedYes.value = false
//                    votedYes()
//                }
//
//                // voted no to restaurant
//                if(myGroupFbViewModel.votedNo.value == true){
//                    myGroupFbViewModel.votedNo.value = false
//                    votedNo()
//                }
//
//                (parentFragmentManager.findFragmentByTag("voting") as DialogFragment).dismiss()
//            }
//
//        }

        return view
    }

    fun updateMembers() {
        CoroutineScope(IO).launch {
            // get members in the group
            val groupFb = myGroupFbViewModel.clickedGroup.value!!
            val database = Firebase.firestore
            database.collection("group").whereEqualTo("groupId", groupFb.groupId).get()
                .addOnSuccessListener { documents ->
                    // There should only be one document
                    for (document in documents) {
                        Log.d("TAG", "GABRIEL ${document.id} => ${document.data}")
                        val listUsers = document.data["listOfUserIds"] as MutableList<String>
                        val listRestaurants = document.data["listOfRestaurants"] as MutableList<String>

                        myRestaurantsListAdapter.replace(listRestaurants)
                        myRestaurantsListAdapter.notifyDataSetChanged()

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

    fun votedYes(){
        CoroutineScope(IO).launch{
            val authViewModel = ViewModelProvider(requireActivity()).get(AuthViewModel::class.java)
            val userID = authViewModel.returnID()!!.uid
            val database = Firebase.firestore
            // check if any database records from same group, user, restaurant
            database.collection("voting").whereEqualTo("groupId", myGroupFbViewModel.clickedGroup.value!!.groupId)
                .whereEqualTo("userId", userID).whereEqualTo("restaurantId", myGroupFbViewModel.voteRestaurantName.value).get()
                .addOnSuccessListener { documents ->
                    if(documents.isEmpty) {
                        // not in database, add it
                        val voteSet = hashMapOf(
                            "groupId" to myGroupFbViewModel.clickedGroup.value!!.groupId,
                            "userId" to userID,
                            "restaurantId" to myGroupFbViewModel.voteRestaurantName.value
                        )
                        database.collection("voting").document().set(voteSet)
                    }

                    // Update the top restaurant after voting
                    updateTopRestaurant()

                }
                .addOnFailureListener{ exception ->
                    Log.w("TAG", "Error getting documents: ", exception)
                }
        }
    }

    fun votedNo(){
        CoroutineScope(IO).launch{
            val authViewModel = ViewModelProvider(requireActivity()).get(AuthViewModel::class.java)
            val userID = authViewModel.returnID()!!.uid
            val database = Firebase.firestore
            // check if any database records from same group, user, restaurant
            database.collection("voting").whereEqualTo("groupId", myGroupFbViewModel.clickedGroup.value!!.groupId)
                .whereEqualTo("userId", userID).whereEqualTo("restaurantId", myGroupFbViewModel.voteRestaurantName.value).get()
                .addOnSuccessListener { documents ->
                    if(documents.isEmpty) {
                        // not in database, don't do anything
                    }
                    else{
                        // should only be 1 record, delete it
                        for (document in documents){
                            document.reference.delete()
                        }
                    }
                    // Update the top restaurant after voting
                    updateTopRestaurant()
                }
                .addOnFailureListener{ exception ->
                    Log.w("TAG", "Error getting documents: ", exception)
                }
        }
    }

    fun updateTopRestaurant(){
        CoroutineScope(IO).launch {
            // Update the top restaurant initially
            val database = Firebase.firestore
            database.collection("voting").whereEqualTo("groupId", myGroupFbViewModel.clickedGroup.value!!.groupId).get()
                .addOnSuccessListener {
                        groupVotes ->
                    val voteArray = ArrayList<String>()
                    // put all the voted restaurants into a list
                    for (vote in groupVotes){
                        voteArray.add(vote.data["restaurantId"].toString())
                    }
                    // map restaurantId as key and count as value, then get first max
                    val topRestaurant = voteArray.groupingBy { it }.eachCount()
                    val bestString = topRestaurant.maxByOrNull {it.value}?.key
                    requireActivity().runOnUiThread {
                        if(bestString != null){
                            topRestaurantTextview.setText("Most Voted: $bestString")
                        }
                        else {
                            topRestaurantTextview.setText("Most Voted:")
                        }
                    }
                }
                .addOnFailureListener{ exception ->
                    Log.w("TAG", "Error getting documents: ", exception)
                }
        }
    }



}