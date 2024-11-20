package com.example.munch_cmpt362.ui.reviews

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.munch_cmpt362.R
import com.example.munch_cmpt362.ui.swipe.RestaurantAdapter
import com.example.munch_cmpt362.ui.swipe.SwipeViewModel


// TODO:
// 1) Display Image
// 2) Add border around review cards as well as label up top
// 3) Order restaurants by review score (if tied on review, by number of reviews)
// -----> add option to also order by number of reviews
// 3) Update Swipe Fragment to store a yes if swipe right on
// 4) Only show swipe right restaurants

class ReviewsFragment : Fragment() {

    private val reviewViewModel: ReviewViewModel by viewModels()

    private lateinit var recyclerView: RecyclerView
    private lateinit var reviewAdapter : ReviewAdapter

    private var lat = 0.0
    private var lng = 0.0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_reviews, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        reviewAdapter = ReviewAdapter(emptyList())
        recyclerView.adapter = reviewAdapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("XD:", "api call on ${lat} ${lng}")
        reviewViewModel.restaurants.observe(viewLifecycleOwner) { restaurants ->
            Log.d("XD:", "Received restaurants: $restaurants")
            if (restaurants.isNotEmpty()) {
                reviewAdapter = ReviewAdapter(restaurants)
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

}
