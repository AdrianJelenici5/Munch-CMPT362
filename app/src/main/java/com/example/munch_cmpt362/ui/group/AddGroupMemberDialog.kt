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
import com.example.munch_cmpt362.ui.group.datadaoview.Group
import com.example.munch_cmpt362.ui.group.datadaoview.GroupViewModel

class AddGroupMemberDialog: DialogFragment(), DialogInterface.OnClickListener {
    private lateinit var editText: EditText
    private lateinit var groupViewModel: GroupViewModel

//    private var STUB_USER_ID = 1L
//    private var STUB_GROUP_ID = 1L

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        lateinit var ret: Dialog
        groupViewModel = ViewModelProvider(requireActivity()).get(GroupViewModel::class.java)
        val builder = AlertDialog.Builder(requireActivity())
        val view: View = requireActivity().layoutInflater.inflate(R.layout.add_group_dialog,null)
        editText = view.findViewById(R.id.add_group_edittext)
        editText.hint = ("Add a member")
        builder.setView(view)
        builder.setTitle("Add Group Member")
        builder.setPositiveButton("ok", this)
        builder.setNegativeButton("cancel", this)
        ret = builder.create()
        return ret
    }

    override fun onClick(dialog: DialogInterface, item: Int) {
        if (item == DialogInterface.BUTTON_POSITIVE) {
            println("GABRIEL CHENG : ${editText.text}")
            if(editText.text.toString() != "") {
                // create group entry for added member
                var group = Group()
                // Add the user as the member
                group.groupID = groupViewModel.clickedGroup.value!!.groupID
                group.userID = editText.text.toString().toLong()
                group.groupName = groupViewModel.clickedGroup.value!!.groupName
                groupViewModel.insertGroup(group)
                // Try to update list
            }
            Toast.makeText(activity, "ok clicked", Toast.LENGTH_LONG).show()
        } else if (item == DialogInterface.BUTTON_NEGATIVE) {
            Toast.makeText(activity, "cancel clicked", Toast.LENGTH_LONG).show()
        }
    }
}