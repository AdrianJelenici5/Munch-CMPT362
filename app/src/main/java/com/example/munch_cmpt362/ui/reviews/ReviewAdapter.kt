package com.example.munch_cmpt362.ui.reviews

import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.munch_cmpt362.Business
import com.example.munch_cmpt362.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class ReviewAdapter(private var restaurants: List<Business>, private val currentLat: Double, private val currentLng: Double
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    fun updateData(newRestaurants: List<Business>) {
        restaurants = newRestaurants
        notifyDataSetChanged()  // Notify the adapter that the data has changed
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        // Log.d("XD:", "XD: onCreateViewHolder")
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_restaurant_review, parent, false)
        return ReviewViewHolder(view, currentLat, currentLng)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        // Log.d("XD:", "XD: onBindViewHolder")
        val restaurant = restaurants[position]
        holder.bind(restaurant, holder.itemView.context)
    }

    override fun getItemCount(): Int = restaurants.size

    class ReviewViewHolder(itemView: View, private val currentLat: Double, private val currentLng: Double) : RecyclerView.ViewHolder(itemView) {

        private val nameTextView: TextView = itemView.findViewById(R.id.tvName)
        private val restaurantInfoTextView: TextView = itemView.findViewById(R.id.restaurantInfo)
        private val ratingTextView: TextView = itemView.findViewById(R.id.tvReview)
        private val imageView: ImageView = itemView.findViewById(R.id.ivImage)
        private val openOrClosed: ImageView = itemView.findViewById(R.id.openOrClosed)
        private val restaurantType: TextView = itemView.findViewById(R.id.restaurantType)

        private val geocodedAddressesCache = mutableMapOf<String, Pair<Double, Double>>()

        fun bind(restaurant: Business, context: Context) {
            // Log.d("XD:", "XD: binding")

            val price = if (restaurant.price != null) "(${restaurant.price}) " else ""
            nameTextView.text = "${restaurant.name} ${price}"
            for (hours in restaurant.business_hours) {
                if (hours.is_open_now) {
                    openOrClosed.setImageResource(R.drawable.green_dot)
                } else {
                    openOrClosed.setImageResource(R.drawable.red_dot)
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

            ratingTextView.text = "Rating: ${restaurant.rating} / 5  (${restaurant.review_count} User Reviews)"

            Glide.with(itemView.context).load(restaurant.image_url).into(imageView)
            //locationTextView.text = restaurant.location

            itemView.setOnClickListener {
                Log.d("XD:", "XD: Item Clicked")
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(restaurant.url))
                itemView.context.startActivity(intent)
            }

        }

        private fun getLatLngFromAddress(context: Context, address: String): Pair<Double, Double>? {
            val addressKey = address
            geocodedAddressesCache[addressKey]?.let { return it }

            val geocoder = Geocoder(context, Locale.getDefault())
            val addressList: List<Address>? = geocoder.getFromLocationName(address, 1)

            if (!addressList.isNullOrEmpty()) {
                val address = addressList[0]
                val latLng = Pair(address.latitude, address.longitude)
                // Cache the result using the address string as the key
                geocodedAddressesCache[addressKey] = latLng
                return latLng
            }

            return null
        }

        private fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
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


}


