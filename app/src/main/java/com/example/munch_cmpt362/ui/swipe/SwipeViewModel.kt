package com.example.munch_cmpt362.ui.swipe

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.munch_cmpt362.Business
import com.example.munch_cmpt362.BusinessHours
import com.example.munch_cmpt362.Category
import com.example.munch_cmpt362.Location
import com.example.munch_cmpt362.OpenHours
import com.example.munch_cmpt362.YelpResponse
import com.example.munch_cmpt362.data.remote.api.ApiHelper

class SwipeViewModel : ViewModel() {
    private val _restaurants = MutableLiveData<List<Business>>()
    val restaurants: LiveData<List<Business>> = _restaurants

    private var dataFetched = false

    fun fetchRestaurants(isFakeData: Boolean = false, latitude: Double, longitude: Double) {
        if (dataFetched) return
        if(isFakeData) {
            _restaurants.value = loadFakeBusinesses().businesses
        }
        else {
            ApiHelper.callYelpNearbyRestaurantsApi(latitude, longitude) { response ->
                response?.businesses?.let {
                    _restaurants.value = it
                    dataFetched = true
                }
            }
        }
    }

    private fun loadFakeBusinesses(): YelpResponse {
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
        return fakeResponse
    }
}
