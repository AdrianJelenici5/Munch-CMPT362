package com.example.munch_cmpt362.ui.reviews

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.munch_cmpt362.Business
import com.example.munch_cmpt362.R
import com.example.munch_cmpt362.ui.swipe.RestaurantAdapter
import com.example.munch_cmpt362.ui.swipe.SwipeViewModel


// TODO:
// 1) Order restaurants by review score (if tied on review, by number of reviews)
// -----> add option to also order by number of reviews
// 2) Make cards clickable (opens up link)
// 3) Update Swipe Fragment to store a yes if swipe right on
// 4) Only show swipe right restaurants


class ReviewsFragment : Fragment() {

    private val reviewViewModel: ReviewViewModel by viewModels()

    private lateinit var recyclerView: RecyclerView
    private lateinit var reviewAdapter : ReviewAdapter

    private lateinit var sortTypeSpinner: Spinner
    private var selectedSortTypeId = 0

    private var lat = 0.0
    private var lng = 0.0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_reviews, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        reviewAdapter = ReviewAdapter(emptyList())
        recyclerView.adapter = reviewAdapter

        sortTypeSpinner = view.findViewById<Spinner>(R.id.sort_spinner)
        sortTypeSpinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {
                selectedSortTypeId = position // or `id` based on your logic
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

        Log.d("XD:", "api call on ${lat} ${lng}")
        reviewViewModel.restaurants.observe(viewLifecycleOwner) { restaurants ->
            Log.d("XD:", "Received restaurants: $restaurants")
            if (restaurants.isNotEmpty()) {
                val sortedRestaurants = sortRestaurants(restaurants)
                reviewAdapter = ReviewAdapter(sortedRestaurants)
                recyclerView.adapter = reviewAdapter
            } else {
//                noMoreRestaurantsText.visibility = View.VISIBLE
                Log.d("XD:", "No restaurants available.")
            }
        }
        reviewViewModel.fetchRestaurants(lat, lng)
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
            else -> restaurants // Fallback
        }
    }

    private fun sortRestaurants() {
        Log.d("XD:", "Sorting restaurants 2")
        reviewViewModel.restaurants.observe(viewLifecycleOwner) { restaurants ->
            if (restaurants.isNotEmpty()) {
                val sortedRestaurants = sortRestaurants(restaurants)
                reviewAdapter = ReviewAdapter(sortedRestaurants)
                recyclerView.adapter = reviewAdapter
            } else {
//                noMoreRestaurantsText.visibility = View.VISIBLE
                Log.d("XD:", "No restaurants available.")
            }
        }
        reviewViewModel.fetchRestaurants(lat, lng)
    }

}
