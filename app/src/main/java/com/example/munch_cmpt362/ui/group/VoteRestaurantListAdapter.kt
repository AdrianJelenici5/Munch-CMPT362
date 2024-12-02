package com.example.munch_cmpt362.ui.group

import android.app.Activity
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.munch_cmpt362.Business
import com.example.munch_cmpt362.R
import com.example.munch_cmpt362.data.remote.api.ApiHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class VoteRestaurantListAdapter(private val context: Context, private var voteRestaurantList: List<String>,
                                private val currentLat: Double, private val currentLng: Double): BaseAdapter() {
    // Cache to store restaurant by ID
    val restaurantCache = mutableMapOf<String, Business>()

    // Restaurants being loaded
    val loadingRestaurants = mutableSetOf<String>()

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
        val view = View.inflate(context, R.layout.item_restaurant_review, null)
        val nameTextView: TextView = view.findViewById(R.id.tvName)
        val restaurantInfoTextView: TextView = view.findViewById(R.id.restaurantInfo)
        val ratingTextView: TextView = view.findViewById(R.id.tvReview)
        val imageView: ImageView = view.findViewById(R.id.ivImage)
        val openOrClosed: ImageView = view.findViewById(R.id.openOrClosed)
        val restaurantType: TextView = view.findViewById(R.id.restaurantType)
        //val textViewId = view.findViewById<TextView>(R.id.group_id)
        //val textViewName = view.findViewById<TextView>(R.id.group_name)
        val restaurantId = voteRestaurantList.get(position)
        //textViewId.text = restaurantId
        //textViewName.text = voteRestaurantList.get(position)

        // Check if the restaurant details are already cached
        val cachedRestaurant = restaurantCache[restaurantId]
        if (cachedRestaurant != null) {
            // If cached, set the name directly
            //textViewName.text = cachedRestaurant.name
            val price = if (cachedRestaurant.price != null) "(${cachedRestaurant.price}) " else ""
            nameTextView.text = "${cachedRestaurant.name} ${price}"
            if (cachedRestaurant.business_hours != null) {
                for (hours in cachedRestaurant.business_hours) {
                    if (hours.is_open_now) {
                        openOrClosed.setImageResource(R.drawable.green_dot)
                    } else {
                        openOrClosed.setImageResource(R.drawable.red_dot)
                    }
                }
            }

            restaurantType.text = cachedRestaurant.categories[0].title

            val address = cachedRestaurant.location.address1
            val city = cachedRestaurant.location.city

            var string_dist = "0.0"
            GlobalScope.launch(Dispatchers.Main) {
                val latLng = getLatLngFromAddress(context,
                    "${cachedRestaurant.location.address1}, ${cachedRestaurant.location.city}, ${cachedRestaurant.location.country}")
                latLng?.let {
                    // Log.d("XD:", "XD: current: (${currentLat}, ${currentLng}) vs. (${it.first}, ${it.second}) : restaurant")
                    var dist = calculateDistance(currentLat, currentLng, it.first, it.second)
                    // Log.d("XD:", "XD: distance: ${dist}")
                    dist = dist/1000
                    // Log.d("XD:", "XD: distance/1000: ${dist}")
                    string_dist = String.format("%.2f", dist)
                    // Log.d("XD:", "XD: distance formatted: ${string_dist}")
                    restaurantInfoTextView.text = "${string_dist}km | ${address}, ${city}"
                }
            }

            // Log.d("XD:", "XD: distance outside: ${string_dist}")
            // If I dont include this, some restaurants dont get displayed in the list:
            restaurantInfoTextView.text = "${string_dist}km | ${address}, ${city}"

            ratingTextView.text = "Rating: ${cachedRestaurant.rating} / 5  (${cachedRestaurant.review_count} User Reviews)"

            Glide.with(view.context).load(cachedRestaurant.image_url).into(imageView)
            //locationTextView.text = restaurant.location

            println("GABRIEL WHY NOT WORKING")
            // Successfully retrieved restaurant details, handle them here
            Log.d("RestaurantDetails", "Name: ${cachedRestaurant.name}")
            Log.d("RestaurantDetails", "Rating: ${cachedRestaurant.rating}")
            Log.d(
                "RestaurantDetails",
                "Address: ${cachedRestaurant.location.address1}, ${cachedRestaurant.location.city}"
            )
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
                                //textViewName.text = restaurant.name
                                val price = if (restaurant.price != null) "(${restaurant.price}) " else ""
                                nameTextView.text = "${restaurant.name} ${price}"
                                if (restaurant.business_hours != null) {
                                    for (hours in restaurant.business_hours) {
                                        if (hours.is_open_now) {
                                            openOrClosed.setImageResource(R.drawable.green_dot)
                                        } else {
                                            openOrClosed.setImageResource(R.drawable.red_dot)
                                        }
                                    }
                                }

                                restaurantType.text = restaurant.categories[0].title

                                val address = restaurant.location.address1
                                val city = restaurant.location.city

                                var string_dist = "0.0"
                                GlobalScope.launch(Dispatchers.Main) {
                                    val latLng = getLatLngFromAddress(context,
                                        "${restaurant.location.address1}, ${restaurant.location.city}, ${restaurant.location.country}")
                                    latLng?.let {
                                        // Log.d("XD:", "XD: current: (${currentLat}, ${currentLng}) vs. (${it.first}, ${it.second}) : restaurant")
                                        var dist = calculateDistance(0.0, 0.0, it.first, it.second)
                                        // Log.d("XD:", "XD: distance: ${dist}")
                                        dist = dist/1000
                                        // Log.d("XD:", "XD: distance/1000: ${dist}")
                                        string_dist = String.format("%.2f", dist)
                                        // Log.d("XD:", "XD: distance formatted: ${string_dist}")
                                        restaurantInfoTextView.text = "${string_dist}km | ${address}, ${city}"
                                    }
                                }

                                // Log.d("XD:", "XD: distance outside: ${string_dist}")
                                // If I dont include this, some restaurants dont get displayed in the list:
                                restaurantInfoTextView.text = "${string_dist}km | ${address}, ${city}"

                                ratingTextView.text = "Rating: ${restaurant.rating} / 5  (${restaurant.review_count} User Reviews)"

                                Glide.with(view.context).load(restaurant.image_url).into(imageView)
                                //locationTextView.text = restaurant.location

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
                                //textViewName.text = "Failed to retrieve restaurant details"
                                nameTextView.text = "Failed to retrieve restaurant details"
                            }
                        }

                        // After loading, remove it from the loading set
                        loadingRestaurants.remove(restaurantId)
                    }
                }
            }
            else {
                // If it's still being loaded, show a placeholder or loading text
                //textViewName.text = "Loading..."
                nameTextView.text = "Loading..."
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

    private suspend fun getLatLngFromAddress(context: Context, address: String): Pair<Double, Double>? {
        return withContext(Dispatchers.IO) {
            val geocoder = Geocoder(context, Locale.getDefault())
            // Attempt to get the list of addresses based on the address string
            val addressList: List<Address>? = geocoder.getFromLocationName(address, 1)

            if (!addressList.isNullOrEmpty()) {
                // If the list is not null or empty, get the first address
                val location = addressList[0]
                val latitude = location.latitude
                val longitude = location.longitude
                Pair(latitude, longitude)
            } else {
                Log.e("Geocoding", "Address not found or geocoding failed.")
                null // Return null if no result is found
            }
        }
    }

    private suspend fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        // Radius of the Earth in meters
        val R = 6371000.0

        // Convert latitude and longitude from degrees to radians
        val lat1Rad = Math.toRadians(lat1)
        val lng1Rad = Math.toRadians(lng1)
        val lat2Rad = Math.toRadians(lat2)
        val lng2Rad = Math.toRadians(lng2)

        // Difference in coordinates
        val dLat = lat2Rad - lat1Rad
        val dLng = lng2Rad - lng1Rad

        // Haversine formula
        val a = sin(dLat / 2).pow(2) + cos(lat1Rad) * cos(lat2Rad) * sin(dLng / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        // Distance in meters
        return R * c
    }

}