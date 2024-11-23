package com.example.munch_cmpt362.ui.group

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.munch_cmpt362.R
import com.example.munch_cmpt362.ui.group.datadaoview.Group
import com.example.munch_cmpt362.ui.group.datadaoview.GroupViewModel
import com.example.munch_cmpt362.ui.group.fb.GroupFbViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class AddGroupFbMemberDialog: DialogFragment(), DialogInterface.OnClickListener {
    private lateinit var editText: EditText
    private lateinit var groupFbViewModel: GroupFbViewModel

//    private var STUB_USER_ID = 1L
//    private var STUB_GROUP_ID = 1L

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        lateinit var ret: Dialog
        groupFbViewModel = ViewModelProvider(requireActivity()).get(GroupFbViewModel::class.java)
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
                groupFbViewModel.addedUser.value = editText.text.toString()
            }
            Toast.makeText(activity, "ok clicked", Toast.LENGTH_LONG).show()
        } else if (item == DialogInterface.BUTTON_NEGATIVE) {
            Toast.makeText(activity, "cancel clicked", Toast.LENGTH_LONG).show()
        }
    }
}