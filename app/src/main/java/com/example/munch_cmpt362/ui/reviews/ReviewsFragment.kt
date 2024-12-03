package com.example.munch_cmpt362.ui.reviews

import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.munch_cmpt362.Business
import com.example.munch_cmpt362.R
import com.example.munch_cmpt362.ui.swipe.SwipeViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import kotlin.math.*


// TODO: sorting by distance can take a long time and with a lot of entries can cause the app to crash.
//  Find out how to fix performance so this doesnt happen
// TODO: Some restaurants arent being displayed in the list for some reason
//  even tho I get them using same method as swipe fragment
@AndroidEntryPoint
class ReviewsFragment : Fragment() {

    private val reviewViewModel: ReviewViewModel by activityViewModels()

    private lateinit var recyclerView: RecyclerView
    private lateinit var reviewAdapter : ReviewAdapter
    private lateinit var emptyTextView: TextView

    private lateinit var sortTypeSpinner: Spinner
    private var selectedSortTypeId = 0

    private var lat = 0.0
    private var lng = 0.0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_reviews, container, false)

        emptyTextView = view.findViewById(R.id.emptyTextView)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        reviewAdapter = ReviewAdapter(emptyList(), lat, lng)
        recyclerView.adapter = reviewAdapter

        sortTypeSpinner = view.findViewById<Spinner>(R.id.sort_spinner)
        sortTypeSpinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {
                selectedSortTypeId = position
                sortRestaurants()
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                // your code here
            }
        })


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        Log.d("XD:", "api call on ${lat} ${lng}")
        reviewViewModel.restaurants.observe(viewLifecycleOwner) { restaurants ->
//            Log.d("XD:", "Received restaurants: $restaurants")
            if (restaurants.isNotEmpty()) {
                val sortedRestaurants = sortRestaurants(restaurants)
                reviewAdapter = ReviewAdapter(sortedRestaurants, lat, lng)
                recyclerView.adapter = reviewAdapter
                recyclerView.visibility = View.VISIBLE
                emptyTextView.visibility = View.GONE
            } else {
//                noMoreRestaurantsText.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
                emptyTextView.visibility = View.VISIBLE
                Log.d("XD:", "No restaurants available.")
            }
        }
        reviewViewModel.fetchRestaurants()
    }

    fun updateLocation(latitude: Double, longitude: Double) {
        lat = latitude
        lng = longitude
    }

    private fun sortRestaurants(restaurants: List<Business>): List<Business> {
        Log.d("XD:", "Sorting restaurants")
        return when (selectedSortTypeId) {
            0 -> restaurants.sortedByDescending { it.rating } // Sort by name
            1 -> restaurants.sortedByDescending { it.review_count } // Sort by descending rating
            2 -> restaurants.sortedBy { it.name } // Sort by descending rating
            3 -> restaurants.sortedBy { it.price?.length ?: Int.MAX_VALUE }
            4 -> restaurants.sortedBy { it.categories[0].title }
            5 -> restaurants.sortedByDescending { it.isOpenNow() }
            6 -> sortRestaurantsByDistance(restaurants, lat, lng)
            else -> restaurants
        }
    }

    private fun Business.isOpenNow(): Boolean {
        return this.business_hours.any { it.is_open_now }
    }

    private fun getLatLngFromAddress(context: Context, address: String): Pair<Double, Double>? {
        val geocoder = Geocoder(context, Locale.getDefault())

        // Attempt to get the list of addresses based on the address string
        val addressList: List<Address>? = geocoder.getFromLocationName(address, 1)

        if (!addressList.isNullOrEmpty()) {
            // If the list is not null or empty, get the first address
            val address = addressList[0]
            val latitude = address.latitude
            val longitude = address.longitude
            return Pair(latitude, longitude)
        } else {
            // Handle the case where the address could not be geocoded
            Log.e("Geocoding", "Address not found or geocoding failed.")
        }

        return null // Return null if geocoding fails or no results are found
    }

    fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
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

//        Log.d("XD:", "XD: current: (${lng1}, ${lng1}) vs. (${lng2}, ${lng2}) : restaurant")
//        Log.d("XD:", "XD: distance: ${R*c}")

        // Distance in meters
        return R * c
    }

    fun sortRestaurantsByDistance(
        restaurants: List<Business>,
        currentLat: Double,
        currentLng: Double
    ): List<Business> {
        return restaurants.sortedBy { business ->
            // Get LatLng for the business address
            val latLng = getLatLngFromAddress(requireContext(), "${business.location.address1}, ${business.location.city}, ${business.location.country}")

            // If LatLng is null, assign a large value to ensure it's sorted last
            val distance = if (latLng != null) {
                calculateDistance(currentLat, currentLng, latLng.first, latLng.second)
            } else {
                Double.MAX_VALUE  // Business without valid coordinates will be last
            }

            distance
        }
    }

    private fun sortRestaurants() {
        Log.d("XD:", "Sorting restaurants 2")
        reviewViewModel.restaurants.observe(viewLifecycleOwner) { restaurants ->
            if (restaurants.isNotEmpty()) {
                val sortedRestaurants = sortRestaurants(restaurants)
                reviewAdapter = ReviewAdapter(sortedRestaurants, lat, lng)
                recyclerView.adapter = reviewAdapter
            } else {
//                noMoreRestaurantsText.visibility = View.VISIBLE
                Log.d("XD:", "No restaurants available.")
            }
        }
        reviewViewModel.fetchRestaurants()
    }

//    private fun openLink(url: String) {
//        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
//        startActivity(intent)
//    }

    override fun onResume() {
        super.onResume()
        reviewViewModel.fetchRestaurants()
    }

}
