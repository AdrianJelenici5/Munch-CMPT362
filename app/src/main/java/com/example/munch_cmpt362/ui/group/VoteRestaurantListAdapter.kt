package com.example.munch_cmpt362.ui.group

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.munch_cmpt362.R
import com.example.munch_cmpt362.ui.group.datadaoview.User

class VoteRestaurantListAdapter(private val context: Context, private var voteRestaurantList: List<String>): BaseAdapter() {
    override  fun getItem(position: Int): Any{
        return voteRestaurantList.get(position)
    }

    override fun getItemId(position: Int): Long{
        return position.toLong()
    }

    override fun getCount(): Int{
        return voteRestaurantList.size
    }

    override fun getView(position: Int, view: View?, viewGroup: ViewGroup?): View {
        val view = View.inflate(context, R.layout.group_list_layout_adapter, null)
        val textViewId = view.findViewById<TextView>(R.id.group_id)
        val textViewName = view.findViewById<TextView>(R.id.group_name)
        textViewId.text = voteRestaurantList.get(position)
        textViewName.text = voteRestaurantList.get(position)
        return view
    }

    fun replace(newList: List<String>){
        voteRestaurantList = newList
    }

}