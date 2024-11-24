package com.example.munch_cmpt362.ui.swipe

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.munch_cmpt362.Business
import com.example.munch_cmpt362.BusinessHours
import com.example.munch_cmpt362.Category
import com.example.munch_cmpt362.Location
import com.example.munch_cmpt362.OpenHours
import com.example.munch_cmpt362.YelpResponse
import com.example.munch_cmpt362.data.remote.api.ApiHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import munch_cmpt362.database.MunchDatabase
import munch_cmpt362.database.restaurants.RestaurantDao
import munch_cmpt362.database.restaurants.RestaurantEntry

class SwipeViewModel : ViewModel() {
    private val _restaurants = MutableLiveData<List<Business>>()
    val restaurants: LiveData<List<Business>> = _restaurants

    private val pendingRemovalIds = mutableListOf<String>()

    private var dataFetched = false

    private val excludeRestaurantIds = mutableSetOf<String>()

    fun fetchRestaurants(isFakeData: Boolean = false, latitude: Double, longitude: Double, restaurantDao: RestaurantDao) {
        Log.d("JP:", "dataFetched value ${dataFetched}")
        if (dataFetched) return
        if(isFakeData) {
            _restaurants.value = loadFakeBusinesses().businesses
        }
        else {
            Log.d("JP:", "API called to fetch")
            ApiHelper.callYelpNearbyRestaurantsApi(latitude, longitude) { response ->
                response?.businesses?.let { businesses ->
                    dataFetched = true

                    // Convert each Business object into a RestaurantEntry and insert into the database
                    businesses.forEach { business ->
                        val restaurantEntry = RestaurantEntry(
                            restaurantId = business.id,
                            name = business.name,
                            rating = business.rating,
                            reviewCount = business.review_count,
                            price = business.price,
                            location = business.location,
                            phone = business.phone,
                            category = business.categories[0],
                            websiteUrl = business.url,
                            imageUrl = business.image_url,
                            businessHours = business.business_hours
                        )

                        // Run insertion on a background thread or coroutine
                        CoroutineScope(Dispatchers.IO).launch {
                            restaurantDao.insertEntry(restaurantEntry)
                        }
                        Log.d("JP:", "rest id  ${business.id}")
                        excludeRestaurantIds.add(business.id)
                    }

                    _restaurants.value = businesses
                }
            }

//            viewModelScope.launch {
//                restaurantDao.getAllEntries().collect { restaurantEntries ->
//                    _restaurants.value = restaurantEntries.map { it.toBusiness()
//                    }
//                }
//            }
        }
    }

    fun fetchRestaurantsWithExcludeList(latitude: Double, longitude: Double, restaurantDao: RestaurantDao, onResult: (Boolean) -> Unit ){
//        Log.d("JP:", "expand API called to fetch")
//        Log.d("JP:", "exclude list value ${excludeRestaurantIds}")

        ApiHelper.callYelpNearbyRestaurantsApi(latitude, longitude) { response ->
            response?.businesses?.let { businesses ->
                val filteredBusinesses = businesses.filterNot { excludeRestaurantIds.contains(it.id) }
                if (filteredBusinesses.isNotEmpty()) {

                    // Convert each Business object into a RestaurantEntry and insert it into the database
                    filteredBusinesses.forEach { business ->
                        val restaurantEntry = RestaurantEntry(
                            restaurantId = business.id,
                            name = business.name,
                            rating = business.rating,
                            reviewCount = business.review_count,
                            price = business.price,
                            location = business.location,
                            phone = business.phone,
                            category = business.categories[0],
                            websiteUrl = business.url,
                            imageUrl = business.image_url,
                            businessHours = business.business_hours
                        )

                        // Insert into the database on a background thread
                        CoroutineScope(Dispatchers.IO).launch {
                            restaurantDao.insertEntry(restaurantEntry)
                        }
                    }
                    _restaurants.value = filteredBusinesses
                    onResult(false)
                } else {
                    Log.d("JP:", "No new restaurants to fetch, all are excluded.")
                    onResult(true)
                }
            }
        }

        // Collect updated restaurant entries from the database
//        viewModelScope.launch {
//            restaurantDao.getAllEntries().collect { restaurantEntries ->
//                // Filter out excluded restaurants before posting
//                val validRestaurants = restaurantEntries.filterNot { excludeRestaurantIds.contains(it.restaurantId) }
//                _restaurants.value = validRestaurants.map { it.toBusiness() }
//            }
//        }
    }

    fun queueRestaurantForRemoval(restaurantId: String) {
        pendingRemovalIds.add(restaurantId)
    }

    fun processPendingRemovals() {
        if(pendingRemovalIds.isNotEmpty()) {
            _restaurants.value = _restaurants.value?.filterNot { it.id in pendingRemovalIds }
            pendingRemovalIds.clear()
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
