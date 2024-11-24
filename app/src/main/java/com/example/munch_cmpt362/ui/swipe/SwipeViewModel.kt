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
import com.example.munch_cmpt362.data.local.cache.RestaurantCacheManager
import com.example.munch_cmpt362.data.remote.api.ApiHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.munch_cmpt362.data.local.dao.RestaurantDao
import com.example.munch_cmpt362.data.local.entity.RestaurantEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SwipeViewModel @Inject constructor(
    private val cacheManager: RestaurantCacheManager,
    private val restaurantDao: RestaurantDao
) : ViewModel() {
    private val _restaurants = MutableLiveData<List<Business>>()
    val restaurants: LiveData<List<Business>> = _restaurants

    private val pendingRemovalIds = mutableListOf<String>()
    private var dataFetched = false
    private val excludeRestaurantIds = mutableSetOf<String>()

    // Caching
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun fetchRestaurants(isFakeData: Boolean = false, latitude: Double, longitude: Double, restaurantDao: RestaurantDao) {
        Log.d("JP:", "dataFetched value ${dataFetched}")
        if (dataFetched) return
        _isLoading.value = true

        if (isFakeData) {
            _restaurants.value = loadFakeBusinesses().businesses
            _isLoading.value = false
            return
        } else {
            Log.d("JP:", "API called to fetch")
            viewModelScope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        Log.d("CacheTest", "Checking cache for restaurants...")
                        val cacheSize = restaurantDao.getCacheSize()
                        Log.d("CacheTest", "Current cache size: $cacheSize")

                        val cachedRestaurants = cacheManager.getNextBatchOfRestaurants(10)
                        if (cachedRestaurants.isNotEmpty()) {
                            Log.d("CacheTest", "Found ${cachedRestaurants.size} restaurants in cache")
                            withContext(Dispatchers.Main) {
                                _restaurants.value = cachedRestaurants.map { it.toBusiness() }
                                dataFetched = true
                            }
                            Log.d("CacheTest", "Starting prefetch for next batch...")
                            // Prefetch next batch in background
                            cacheManager.prefetchRestaurants(latitude, longitude)
                        } else {
                            Log.d("CacheTest", "Cache empty, fetching from API...")
                            // If cache is empty, fetch from API
                            fetchFromApi(latitude, longitude)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("SwipeViewModel", "Error fetching restaurants: ${e.message}")
                    Log.e("CacheTest", "Error fetching restaurants: ${e.message}", e)
                } finally {
                    withContext(Dispatchers.Main) {
                        _isLoading.value = false
                    }
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

    private suspend fun fetchFromApi(latitude: Double, longitude: Double) {
        withContext(Dispatchers.IO) {
            Log.d("CacheTest", "Making API call...")
            ApiHelper.callYelpNearbyRestaurantsApi(latitude, longitude) { response ->
                response?.businesses?.let { businesses ->
                    Log.d("CacheTest", "Received ${businesses.size} restaurants from API")
                    viewModelScope.launch {
                        viewModelScope.launch {
                            try {
                                withContext(Dispatchers.IO) {
                                    val restaurantEntries = businesses.map { business ->
                                        RestaurantEntry(
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
                                            businessHours = business.business_hours,
                                            lastFetched = System.currentTimeMillis(),
                                            isCached = true,
                                            isPreFetched = false
                                        )
                                    }
                                    Log.d("CacheTest", "Caching ${restaurantEntries.size} restaurants")
                                    restaurantDao.insertAll(restaurantEntries)
                                    excludeRestaurantIds.addAll(businesses.map { it.id })

                                    val newCacheSize = restaurantDao.getCacheSize()
                                    Log.d("CacheTest", "New cache size after insert: $newCacheSize")
                                }

                                withContext(Dispatchers.Main) {
                                    dataFetched = true
                                    _restaurants.value = businesses
                                }
                            } catch (e: Exception) {
                                Log.e("SwipeViewModel", "Error in API fetch: ${e.message}")
                            }
                        }
                    }
                }
            }
        }
    }



    fun fetchRestaurantsWithExcludeList(latitude: Double, longitude: Double, restaurantDao: RestaurantDao, onResult: (Boolean) -> Unit ) {
//        Log.d("JP:", "expand API called to fetch")
//        Log.d("JP:", "exclude list value ${excludeRestaurantIds}")
        _isLoading.value = true
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    // Try to get unseen restaurants from cache first
                    val cachedRestaurants = cacheManager.getNextBatchOfRestaurants(10)
                    if (cachedRestaurants.isNotEmpty()) {
                        withContext(Dispatchers.Main) {
                            _restaurants.value = cachedRestaurants.map { it.toBusiness() }
                            onResult(false)
                        }
                        return@withContext
                    }

                    ApiHelper.callYelpNearbyRestaurantsApi(latitude, longitude) { response ->
                        response?.businesses?.let { businesses ->
                            viewModelScope.launch {
                                try {
                                    val filteredBusinesses = businesses.filterNot {
                                        excludeRestaurantIds.contains(it.id)
                                    }

                                    // Convert each Business object into a RestaurantEntry and insert it into the database
                                    if (filteredBusinesses.isNotEmpty()) {
                                        withContext(Dispatchers.IO) {
                                            val restaurantEntries =
                                                filteredBusinesses.map { business ->
                                                    RestaurantEntry(
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
                                                        businessHours = business.business_hours,
                                                        lastFetched = System.currentTimeMillis(),
                                                        isCached = true,
                                                        isPreFetched = true
                                                    )
                                                }
                                            restaurantDao.insertAll(restaurantEntries)
                                        }
                                        withContext(Dispatchers.Main) {
                                            _restaurants.value = filteredBusinesses
                                            onResult(false)
                                        }
                                    } else {
                                        withContext(Dispatchers.Main) {
                                            Log.d(
                                                "JP:",
                                                "No new restaurants to fetch, all are excluded."
                                            )
                                            onResult(true)
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.e(
                                        "SwipeViewModel",
                                        "Error in exclude list fetch: ${e.message}"
                                    )
                                }
                            }
                        }
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
            catch (e: Exception) {
                Log.e("SwipeViewModel", "Error in fetchRestaurantsWithExcludeList: ${e.message}")
            } finally {
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                }
            }
        }
    }



    fun queueRestaurantForRemoval(restaurantId: String) {
        pendingRemovalIds.add(restaurantId)

        // Mark restaurant as seen in cache
        cacheManager.markAsSeen(restaurantId)
    }

    fun processPendingRemovals() {
        if(pendingRemovalIds.isNotEmpty()) {
            _restaurants.value = _restaurants.value?.filterNot { it.id in pendingRemovalIds }
            pendingRemovalIds.clear()
        }
    }

    fun loadFakeBusinesses(): YelpResponse {
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

    override fun onCleared() {
        super.onCleared()
        // Clean up any resources if needed
    }
}
