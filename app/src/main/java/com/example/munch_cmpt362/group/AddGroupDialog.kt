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
import com.example.munch_cmpt362.group.datadaoview.Counter
import com.example.munch_cmpt362.group.datadaoview.Group
import com.example.munch_cmpt362.group.datadaoview.GroupDatabase
import com.example.munch_cmpt362.group.datadaoview.GroupViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class AddGroupDialog: DialogFragment(), DialogInterface.OnClickListener {
    private lateinit var editText: EditText
    private lateinit var groupViewModel: GroupViewModel

    private var STUB_USER_ID = 1L

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
            var groupDatabase = GroupDatabase.getInstance(requireActivity())
            var groupDatabaseDao = groupDatabase.groupDatabaseDao
            if(editText.text.toString() != "") {
                // stub add group
                var group = Group()
                // get counter number, make group, add counter auto, delete old counter
                CoroutineScope(IO).launch{
                    var groupID = groupDatabaseDao.getCounter()
                    group.groupID = groupID
                    group.userID = STUB_USER_ID
                    group.groupName = editText.text.toString()
                    groupDatabaseDao.insertGroupAndUser(group)
                    var counter = Counter()
                    groupDatabaseDao.insertCounter(counter)
                    groupDatabaseDao.deleteCounter(groupID)
                }
            }
            Toast.makeText(activity, "ok clicked", Toast.LENGTH_LONG).show()
        } else if (item == DialogInterface.BUTTON_NEGATIVE) {
            Toast.makeText(activity, "cancel clicked", Toast.LENGTH_LONG).show()
        }
    }

}