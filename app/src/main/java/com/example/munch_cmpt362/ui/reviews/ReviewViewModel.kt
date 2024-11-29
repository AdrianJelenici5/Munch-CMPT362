package com.example.munch_cmpt362.ui.reviews

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.munch_cmpt362.Business
import com.example.munch_cmpt362.data.local.dao.RestaurantDao
import com.example.munch_cmpt362.data.remote.api.ApiHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val restaurantDao: RestaurantDao
) : ViewModel() {
    private val _restaurants = MutableLiveData<List<Business>>()
    val restaurants: LiveData<List<Business>> = _restaurants

    private val restaurantIds = mutableListOf<String>()


    fun fetchRestaurants() {
//        Log.d("XD:", "XD: passed if case")

        viewModelScope.launch {
            val fetchedRestaurants = withContext(Dispatchers.IO) {
                restaurantIds.mapNotNull { id ->
                    restaurantDao.getRestaurantById(id)
                }
            }
            val businessList = fetchedRestaurants.map { it.toBusiness() }
            _restaurants.value = businessList
        }
    }

    fun addRestaurantId(restaurantId: String) {
        restaurantIds.add(restaurantId)
    }
}