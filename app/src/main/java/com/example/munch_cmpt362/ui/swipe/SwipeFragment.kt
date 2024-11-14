package com.example.munch_cmpt362.ui.swipe

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.munch_cmpt362.data.remote.api.ApiHelper
import com.example.munch_cmpt362.Business
import com.example.munch_cmpt362.Category
import com.example.munch_cmpt362.Location
import com.example.munch_cmpt362.OpenHours
import com.example.munch_cmpt362.BusinessHours
import com.example.munch_cmpt362.R
import com.example.munch_cmpt362.YelpResponse
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.CardStackListener
import com.yuyakaido.android.cardstackview.CardStackView
import com.yuyakaido.android.cardstackview.Direction
import com.yuyakaido.android.cardstackview.StackFrom

class SwipeFragment : Fragment() {
    private lateinit var cardStackView: CardStackView
    private lateinit var noMoreRestaurantsText: TextView
    private lateinit var cardStackLayoutManager: CardStackLayoutManager
    private lateinit var restaurantAdapter: RestaurantAdapter
    private val swipeViewModel: SwipeViewModel by viewModels()

    private var lat = 0.0
    private var lng = 0.0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_swipe, container, false)
        cardStackView = view.findViewById(R.id.card_stack_view)
        noMoreRestaurantsText = view.findViewById(R.id.noMoreRestaurantsText)

        restaurantAdapter = RestaurantAdapter(emptyList())
        cardStackView.adapter = restaurantAdapter

        setupCardStackView()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("JP:", "api call on ${lat} ${lng}")
        swipeViewModel.restaurants.observe(viewLifecycleOwner) { restaurants ->
            if (restaurants.isNotEmpty()) {
                restaurantAdapter = RestaurantAdapter(restaurants)
                cardStackView.adapter = restaurantAdapter
            } else {
                noMoreRestaurantsText.visibility = View.VISIBLE
            }
        }
        swipeViewModel.fetchRestaurants(isFakeData = false, lat, lng)
    }

    fun updateLocation(latitude: Double, longitude: Double) {
        lat = latitude
        lng = longitude
        view?.findViewById<TextView>(R.id.myLocationText)?.text = "Lat: $lat, Long: $lng"
    }

        private fun setupCardStackView() {
        cardStackLayoutManager = CardStackLayoutManager(requireContext(), object : CardStackListener {
            override fun onCardDragging(direction: Direction?, ratio: Float) {
                when (direction) {
                    Direction.Right -> {

                    }

                    Direction.Left -> {

                    }

                    else -> {}
                }
            }

            override fun onCardSwiped(direction: Direction?) {
                if (cardStackLayoutManager.topPosition == restaurantAdapter.itemCount) {
                    // Show "No more restaurants" message when all cards are swiped
                    noMoreRestaurantsText.visibility = View.VISIBLE
                }
            }

            override fun onCardRewound() {

            }

            override fun onCardCanceled() {

            }

            override fun onCardAppeared(view: View?, position: Int) {
            }

            override fun onCardDisappeared(view: View?, position: Int) {
            }
        })

        cardStackLayoutManager.setStackFrom(StackFrom.None)
        cardStackLayoutManager.setVisibleCount(3)
        cardStackLayoutManager.setTranslationInterval(8.0f)
        cardStackLayoutManager.setScaleInterval(0.95f)
        cardStackLayoutManager.setSwipeThreshold(0.3f)
        cardStackLayoutManager.setMaxDegree(20.0f)
        cardStackLayoutManager.setDirections(Direction.HORIZONTAL)
        cardStackLayoutManager.setCanScrollHorizontal(true)
        cardStackLayoutManager.setCanScrollVertical(false)

        cardStackView.layoutManager = cardStackLayoutManager
    }
}
