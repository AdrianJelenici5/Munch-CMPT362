package com.example.munch_cmpt362.ui.group

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.munch_cmpt362.Business
import com.example.munch_cmpt362.R
import com.example.munch_cmpt362.data.remote.api.ApiHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class VoteRestaurantListAdapter(private val context: Context, private var voteRestaurantList: List<String>): BaseAdapter() {
    // Cache to store restaurant by ID
    private val restaurantCache = mutableMapOf<String, Business>()

    // Restaurants being loaded
    private val loadingRestaurants = mutableSetOf<String>()

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
        val restaurantId = voteRestaurantList.get(position)
        textViewId.text = restaurantId
        //textViewName.text = voteRestaurantList.get(position)

        // Check if the restaurant details are already cached
        val cachedRestaurant = restaurantCache[restaurantId]
        if (cachedRestaurant != null) {
            // If cached, set the name directly
            textViewName.text = cachedRestaurant.name
            }
        else {
            // If not cached, check if it's already being loaded
            if (!loadingRestaurants.contains(restaurantId)) {
                // Mark the restaurant as being loaded
                loadingRestaurants.add(restaurantId)

                // Fetch restaurant details from API
                CoroutineScope(IO).launch {
                    ApiHelper.getRestaurantById(restaurantId) { restaurant ->
                        if (restaurant != null) {
                            // Cache the fetched restaurant details
                            restaurantCache[restaurantId] = restaurant

                            // Update the UI with the restaurant details
                            (context as Activity).runOnUiThread {
                                textViewName.text = restaurant.name
                                println("GABRIEL WHY NOT WORKING")
                                // Successfully retrieved restaurant details, handle them here
                                Log.d("RestaurantDetails", "Name: ${restaurant.name}")
                                Log.d("RestaurantDetails", "Rating: ${restaurant.rating}")
                                Log.d(
                                    "RestaurantDetails",
                                    "Address: ${restaurant.location.address1}, ${restaurant.location.city}"
                                )
                                notifyDataSetChanged()
                            }
                        }
                        else {
                            // Handle failure, set fallback text
                            (context as Activity).runOnUiThread {
                                textViewName.text = "Failed to retrieve restaurant details"
                            }
                        }

                        // After loading, remove it from the loading set
                        loadingRestaurants.remove(restaurantId)
                    }
                }
            }
            else {
                // If it's still being loaded, show a placeholder or loading text
                textViewName.text = "Loading..."
            }
        }
//        CoroutineScope(IO).launch{
//            // delay to send request slowly
//            delay(333L)
//            ApiHelper.getRestaurantById(voteRestaurantList.get(position)) { restaurant ->
//                if (restaurant != null) {
//                    (context as Activity).runOnUiThread{
//                        textViewName.text = restaurant.name
//                        // Successfully retrieved restaurant details, handle them here
//                        Log.d("RestaurantDetails", "Name: ${restaurant.name}")
//                        Log.d("RestaurantDetails", "Rating: ${restaurant.rating}")
//                        Log.d(
//                            "RestaurantDetails",
//                            "Address: ${restaurant.location.address1}, ${restaurant.location.city}"
//                        )
//                    }
//                } else {
//                    Log.e("RestaurantDetails", "Failed to retrieve restaurant details")
//                    (context as Activity).runOnUiThread{
//                        textViewName.text = "Failed to retrieve restaurant details"
//                    }
//                }
//            }
//        }
        return view
    }

    fun replace(newList: List<String>){
        voteRestaurantList = newList
    }

}