package com.example.munch_cmpt362.data.local.cache

import com.example.munch_cmpt362.data.local.dao.RestaurantDao
import com.example.munch_cmpt362.data.local.entity.RestaurantEntry
import com.example.munch_cmpt362.data.remote.api.ApiHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RestaurantCacheManager @Inject constructor(
    private val restaurantDao: RestaurantDao,
    private val scope: CoroutineScope
) {
    companion object {
        private const val CACHE_SIZE_LIMIT = 100
        private const val PREFETCH_SIZE = 20
        private const val CACHE_EXPIRY_HOURS = 24L
        private const val MIN_CACHE_SIZE = 30
    }

    private val seenRestaurantIds = mutableSetOf<String>()
    private var isFetching = false

    suspend fun initializeCache(latitude: Double, longitude: Double) {
        withContext(Dispatchers.IO) {
            if (restaurantDao.getCacheSize() < MIN_CACHE_SIZE) {
                fetchAndCacheRestaurants(latitude, longitude)
            }
            clearExpiredCache()
        }
    }

    suspend fun getNextBatchOfRestaurants(limit: Int): List<RestaurantEntry> {
        return withContext(Dispatchers.IO) {
            restaurantDao.getUnseenRestaurants(seenRestaurantIds.toList(), limit)
        }
    }

    suspend fun prefetchRestaurants(latitude: Double, longitude: Double) {
        if (isFetching) return

        withContext(Dispatchers.IO) {
            try {
                isFetching = true
                val currentCacheSize = restaurantDao.getCacheSize()

                if (currentCacheSize < CACHE_SIZE_LIMIT - PREFETCH_SIZE) {
                    fetchAndCacheRestaurants(latitude, longitude)
                } else {
                    val toRemove = PREFETCH_SIZE
                    restaurantDao.removeLowestRatedRestaurants(toRemove)
                    fetchAndCacheRestaurants(latitude, longitude)
                }
            } finally {
                isFetching = false
            }
        }
    }

    private suspend fun fetchAndCacheRestaurants(latitude: Double, longitude: Double) {
        withContext(Dispatchers.IO) {
            try {
                ApiHelper.callYelpNearbyRestaurantsApi(latitude, longitude) { response ->
                    response?.businesses?.let { businesses ->
                        scope.launch {
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
                                    isPreFetched = true
                                )
                            }
                            restaurantDao.insertAll(restaurantEntries)
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle API errors
            }
        }
    }

    private suspend fun clearExpiredCache() {
        withContext(Dispatchers.IO) {
            val expiryTime = System.currentTimeMillis() - (CACHE_EXPIRY_HOURS * 60 * 60 * 1000)
            restaurantDao.clearOldCache(expiryTime)
        }
    }

    fun markAsSeen(restaurantId: String) {
        seenRestaurantIds.add(restaurantId)
    }

    suspend fun updateLastFetched(restaurantId: String) {
        withContext(Dispatchers.IO) {
            restaurantDao.updateLastFetched(restaurantId, System.currentTimeMillis())
        }
    }
}