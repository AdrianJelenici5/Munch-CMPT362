package com.example.munch_cmpt362.ui.swipe

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
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
    private var hasFetchedRestaurants = false

    private var lat = 0.0
    private var lng = 0.0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_swipe, container, false)
        cardStackView = view.findViewById(R.id.card_stack_view)
        noMoreRestaurantsText = view.findViewById(R.id.noMoreRestaurantsText)
        setupCardStackView()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!hasFetchedRestaurants) {
            Log.d("JP:", "api call on ${lat} ${lng}")
            fetchNearbyRestaurants(lat, lng)
            hasFetchedRestaurants = true
        }
    }

    private fun fetchNearbyRestaurants(latitude: Double, longitude: Double) {
        ApiHelper.callYelpNearbyRestaurantsApi(latitude, longitude) { response ->
            if (response?.businesses != null) {
                Log.d("SwipeFragment", "${response.businesses}")
                restaurantAdapter = RestaurantAdapter(response.businesses)
                cardStackView.adapter = restaurantAdapter
            } else {
                Log.e("SwipeFragment", "Failed to retrieve businesses or response is null")
                noMoreRestaurantsText.visibility = View.VISIBLE
            }
        }

//        loadFakeBusinesses()

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

    private fun loadFakeBusinesses() {
        val fakeBusinesses = listOf(
            Business(
                id = "1",
                name = "Sushi Place",
                rating = 4.5f,
                review_count = 200,
                price = "$$",
                location = Location(
                    address1 = "123 Sushi St",
                    city = "Foodville",
                    zip_code = "12345",
                    country = "US"
                ),
                phone = "+123456789",
                categories = listOf(
                    Category(alias = "sushi", title = "Sushi"),
                    Category(alias = "japanese", title = "Japanese")
                ),
                url = "https://www.yelp.ca/biz/k-and-j-cuisine-langley",
                image_url = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ1xlfhdSh4CM01_1b-15J8unZmytbBBk9PiQ&s",
                business_hours = listOf(
                    BusinessHours(
                        hour_type = "Regular",
                        open = listOf(
                            OpenHours(day = 1, start = "1100", end = "2100", is_overnight = false),
                            OpenHours(day = 2, start = "1100", end = "2100", is_overnight = false),
                            OpenHours(day = 3, start = "1100", end = "2100", is_overnight = false),
                            OpenHours(day = 4, start = "1100", end = "2100", is_overnight = false),
                            OpenHours(day = 5, start = "1100", end = "2200", is_overnight = false),
                            OpenHours(day = 6, start = "1100", end = "2200", is_overnight = false),
                            OpenHours(day = 0, start = "1200", end = "2000", is_overnight = false)
                        ),
                        is_open_now = true
                    )
                )
            ),
            Business(
                id = "2",
                name = "Pizza Heaven",
                rating = 4.0f,
                review_count = 150,
                price = "$",
                location = Location(
                    address1 = "456 Pizza Ave",
                    city = "Pizzatown",
                    zip_code = "67890",
                    country = "US"
                ),
                phone = "+198765432",
                categories = listOf(
                    Category(alias = "pizza", title = "Pizza"),
                    Category(alias = "italian", title = "Italian")
                ),
                url = "https://www.yelp.ca/biz/pizza-pizzeria-burnaby",
                image_url = "https://media.istockphoto.com/id/1442417585/photo/person-getting-a-piece-of-cheesy-pepperoni-pizza.jpg?s=612x612&w=0&k=20&c=k60TjxKIOIxJpd4F4yLMVjsniB4W1BpEV4Mi_nb4uJU=",
                business_hours = listOf(
                    BusinessHours(
                        hour_type = "Regular",
                        open = listOf(
                            OpenHours(day = 1, start = "1100", end = "1100", is_overnight = false),
                            OpenHours(day = 2, start = "1100", end = "1100", is_overnight = false),
                            OpenHours(day = 3, start = "1100", end = "1100", is_overnight = false),
                            OpenHours(day = 4, start = "1100", end = "1100", is_overnight = false),
                            OpenHours(day = 5, start = "1100", end = "1100", is_overnight = false),
                            OpenHours(day = 6, start = "1100", end = "1100", is_overnight = false),
                            OpenHours(day = 0, start = "1200", end = "1100", is_overnight = false)
                        ),
                        is_open_now = false
                    )
                )
            ),
        )

        val fakeResponse = YelpResponse(businesses = fakeBusinesses)

        restaurantAdapter = RestaurantAdapter(fakeResponse.businesses)
        cardStackView.adapter = restaurantAdapter
    }
}
