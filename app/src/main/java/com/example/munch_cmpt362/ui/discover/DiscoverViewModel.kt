package com.example.munch_cmpt362.ui.discover

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.munch_cmpt362.Business
import com.example.munch_cmpt362.data.remote.api.ApiHelper

class DiscoverViewModel : ViewModel() {
    private val _restaurants = MutableLiveData<List<Business>>()
    val restaurants: LiveData<List<Business>> = _restaurants


    private var dataFetched = false

    fun fetchRestaurants(latitude: Double, longitude: Double, radius: Int = 5000) {

//        Log.d("XD:", "XD: ReviewViewModel | Lat: $latitude, Long: $longitude")
        if (dataFetched) return
//        Log.d("XD:", "XD: passed if case")

        ApiHelper.callYelpNearbyRestaurantsApi(latitude, longitude, radius) { response ->
            Log.d("XD:", "XD: Received response: $response")
            response?.businesses?.let {
                Log.d("XD:", "XD: Updating restaurants LiveData")
                _restaurants.value = it
                dataFetched = true
            }
        }
    }

}