package com.example.munch_cmpt362.data.remote.api

import android.util.Log
import com.example.munch_cmpt362.BuildConfig
import com.example.munch_cmpt362.YelpResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiHelper {

    fun callYelpNearbyRestaurantsApi(
        latitude: Double,
        longitude: Double,
        onResult: (YelpResponse?) -> Unit
    ) {
        // Configure OkHttpClient with a logging interceptor
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Log.d("JP", message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY // Logs request and response body
        }

        val client = OkHttpClient.Builder()
//            .addInterceptor(loggingInterceptor) // Add the interceptor
            .build()

        // Set up Retrofit with the logging-enabled client
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.yelp.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        val service = retrofit.create(YelpService::class.java)
        val call = service.getNearbyRestaurants("Bearer ${BuildConfig.YELP_API_KEY}", latitude, longitude)

        call.enqueue(object : Callback<YelpResponse> {
            override fun onResponse(call: Call<YelpResponse>, response: Response<YelpResponse>) {
                if (response.isSuccessful) {
                    Log.d("Yelp Search Api", "Body: ${response.body()}")
                    onResult(response.body())
                } else {
                    Log.e("Yelp Nearby Api", "Error: ${response.code()}")
                    onResult(null)
                }
            }

            override fun onFailure(call: Call<YelpResponse>, t: Throwable) {
                Log.e("Yelp Nearby Api", "API call failed: ${t.message}")
                onResult(null)
            }
        })
    }

    fun searchYelpRestaurantsApi(
        term: String,
        latitude: Double,
        longitude: Double,
        onResult: (YelpResponse?) -> Unit
    ) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.yelp.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(OkHttpClient.Builder().build())
            .build()

        val service = retrofit.create(YelpService::class.java)
        val call = service.searchRestaurants("Bearer ${BuildConfig.YELP_API_KEY}", "${term} + restaurant", latitude, longitude)
        call.enqueue(object : Callback<YelpResponse> {
            override fun onResponse(call: Call<YelpResponse>, response: Response<YelpResponse>) {
                if (response.isSuccessful) {
                    Log.e("Yelp Search Api", "Body: ${response.body()}")
                    onResult(response.body())
                } else {
                    Log.e("Yelp Search Api", "Error: ${response.code()}")
                    onResult(null)
                }
            }

            override fun onFailure(call: Call<YelpResponse>, t: Throwable) {
                Log.e("Yelp Search Api", "API call failed: ${t.message}")
                onResult(null)
            }
        })
    }

    /* how to use
    private fun searchRestaurantsByInput(term: String, latitude: Double, longitude: Double) {
    ApiHelper.searchYelpRestaurantsApi(term, latitude, longitude) { response ->
        response?.businesses?.let {
            restaurantAdapter = RestaurantAdapter(it)
            cardStackView.adapter = restaurantAdapter
        } ?: Log.e("SwipeFragment", "Failed to retrieve search results")
    }
}
     */
}