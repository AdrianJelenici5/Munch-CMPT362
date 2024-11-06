package com.example.munch_cmpt362.group

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
import com.example.munch_cmpt362.group.datadaoview.Group
import com.example.munch_cmpt362.group.datadaoview.GroupViewModel

class AddGroupDialog: DialogFragment(), DialogInterface.OnClickListener {
    private lateinit var editText: EditText
    private lateinit var groupViewModel: GroupViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        lateinit var ret: Dialog
        groupViewModel = ViewModelProvider(requireActivity()).get(GroupViewModel::class.java)
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
                // stub add group
                var group = Group()
                group.userID = 1L
                group.groupName = editText.text.toString()
                groupViewModel.insertGroup(group)
            }
            Toast.makeText(activity, "ok clicked", Toast.LENGTH_LONG).show()
        } else if (item == DialogInterface.BUTTON_NEGATIVE) {
            Toast.makeText(activity, "cancel clicked", Toast.LENGTH_LONG).show()
        }
    }

}