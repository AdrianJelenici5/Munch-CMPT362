package com.example.munch_cmpt362.group

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.munch_cmpt362.R
import com.example.munch_cmpt362.group.datadaoview.User

class GroupMemberListAdapter(private val context: Context, private var groupMemberList: List<User>): BaseAdapter() {
    override  fun getItem(position: Int): Any{
        return groupMemberList.get(position)
    }

    override fun getItemId(position: Int): Long{
        return position.toLong()
    }

    override fun getCount(): Int{
        return groupMemberList.size
    }

    override fun getView(position: Int, view: View?, viewGroup: ViewGroup?): View {
        val view = View.inflate(context, R.layout.group_list_layout_adapter, null)
        val textViewId = view.findViewById<TextView>(R.id.group_id)
        val textViewName = view.findViewById<TextView>(R.id.group_name)
        textViewId.text = groupMemberList.get(position).userID.toString()
        textViewName.text = groupMemberList.get(position).userName
        return view
    }

    fun replace(newList: List<User>){
        groupMemberList = newList
    }

}