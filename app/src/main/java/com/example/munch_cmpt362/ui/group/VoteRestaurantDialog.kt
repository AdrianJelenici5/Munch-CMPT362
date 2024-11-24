package com.example.munch_cmpt362.ui.group

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.munch_cmpt362.R
import com.example.munch_cmpt362.ui.group.fb.GroupFbViewModel

class VoteRestaurantDialog: DialogFragment(), DialogInterface.OnClickListener {
    private lateinit var textView: TextView
    private lateinit var myGroupFbViewModel: GroupFbViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        lateinit var ret: Dialog
        val builder = AlertDialog.Builder(requireActivity())
        val view: View = requireActivity().layoutInflater.inflate(R.layout.vote_restaurant_dialog,null)
        textView = view.findViewById(R.id.vote_restaurant_textview)
        builder.setView(view)
        builder.setTitle("Vote")
        myGroupFbViewModel = ViewModelProvider(requireActivity()).get(GroupFbViewModel::class.java)
        textView.setText("Vote for: ${myGroupFbViewModel.voteRestaurantName.value}")
        builder.setPositiveButton("Yes", this)
        builder.setNegativeButton("No", this)
        ret = builder.create()
        return ret
    }

    override fun onClick(dialog: DialogInterface, item: Int) {
        if (item == DialogInterface.BUTTON_POSITIVE) {
            myGroupFbViewModel.votedYes.value = true
            Toast.makeText(activity, "ok clicked", Toast.LENGTH_LONG).show()
        } else if (item == DialogInterface.BUTTON_NEGATIVE) {
            myGroupFbViewModel.votedNo.value = true
            Toast.makeText(activity, "cancel clicked", Toast.LENGTH_LONG).show()
        }
    }

}