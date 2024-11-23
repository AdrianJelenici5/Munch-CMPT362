package com.example.munch_cmpt362.ui.group

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.munch_cmpt362.R
import com.example.munch_cmpt362.ui.auth.AuthViewModel
import com.example.munch_cmpt362.ui.group.datadaoview.Counter
import com.example.munch_cmpt362.ui.group.datadaoview.Group
import com.example.munch_cmpt362.ui.group.datadaoview.GroupDatabase
import com.example.munch_cmpt362.ui.group.datadaoview.GroupViewModel
import com.example.munch_cmpt362.ui.group.fb.GroupFbViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class AddGroupFbDialog: DialogFragment(), DialogInterface.OnClickListener {
    private lateinit var editText: EditText

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        lateinit var ret: Dialog
        val builder = AlertDialog.Builder(requireActivity())
        val view: View = requireActivity().layoutInflater.inflate(R.layout.add_group_dialog,null)
        editText = view.findViewById(R.id.add_group_edittext)
        builder.setView(view)
        builder.setTitle("Add Group")
        builder.setPositiveButton("ok", this)
        builder.setNegativeButton("cancel", this)
        ret = builder.create()
        return ret
    }

    override fun onClick(dialog: DialogInterface, item: Int) {
        if (item == DialogInterface.BUTTON_POSITIVE) {
            println("GABRIEL CHENG : ${editText.text}")
            if(editText.text.toString() != "") {
                val authViewModel = ViewModelProvider(requireActivity()).get(AuthViewModel::class.java)
                val userID = authViewModel.returnID()!!.uid

                CoroutineScope(IO).launch{
                    val ref = Firebase.firestore.collection("group").document()
                    val list = ArrayList<String>()
                    list.add(userID)
                    val groupSet = hashMapOf(
                        "groupName" to editText.text.toString(),
                        "listOfRestaurants" to ArrayList<String>(),
                        "listOfUserIds" to list,
                    )
                    ref.set(groupSet)
                    val myGroupFbViewModel = ViewModelProvider(requireActivity()).get(GroupFbViewModel::class.java)
                    val groupFb = GroupFb()
                    groupFb.groupId = ref.id
                    groupFb.groupName = editText.text.toString()
                    groupFb.listOfRestaurants = ArrayList<String>()
                    groupFb.listOfUserIds = list
                    myGroupFbViewModel.groupFb.postValue(groupFb)
                }
            }
            Toast.makeText(activity, "ok clicked", Toast.LENGTH_LONG).show()
        } else if (item == DialogInterface.BUTTON_NEGATIVE) {
            Toast.makeText(activity, "cancel clicked", Toast.LENGTH_LONG).show()
        }
    }

}