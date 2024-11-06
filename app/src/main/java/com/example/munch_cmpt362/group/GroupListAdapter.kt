package com.example.munch_cmpt362.group

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.munch_cmpt362.R
import com.example.munch_cmpt362.group.datadaoview.Group

class GroupListAdapter(private val context: Context, private var groupList: List<Group>): BaseAdapter() {
    override  fun getItem(position: Int): Any{
        return groupList.get(position)
    }

    override fun getItemId(position: Int): Long{
        return position.toLong()
    }

    override fun getCount(): Int{
        return groupList.size
    }

    override fun getView(position: Int, view: View?, viewGroup: ViewGroup?): View {
        val view = View.inflate(context, R.layout.group_list_layout_adapter, null)
        val textViewId = view.findViewById<TextView>(R.id.group_id)
        val textViewName = view.findViewById<TextView>(R.id.group_name)
        textViewId.text = groupList.get(position).groupID.toString()
        textViewName.text = groupList.get(position).groupName
        return view
    }

    fun replace(newList: List<Group>){
        groupList = newList
    }

}