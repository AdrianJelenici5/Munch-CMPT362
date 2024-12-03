package com.example.munch_cmpt362.data.remote.api

import android.util.Log
import com.example.munch_cmpt362.BuildConfig
import com.example.munch_cmpt362.Business
import com.example.munch_cmpt362.YelpResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiHelper {
    private const val TAG = "ApiHelper"
    private const val MAX_RADIUS_METERS = 40000

    fun callYelpNearbyRestaurantsApi(
        latitude: Double,
        longitude: Double,
        radius: Int,
        onResult: (YelpResponse?) -> Unit
    ) {
        // Configure OkHttpClient with a logging interceptor
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Log.d("JP", message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY // Logs request and response body
        }

        // Ensure radius doesn't exceed Yelp's maximum
        val validRadius = radius.coerceAtMost(MAX_RADIUS_METERS)

        Log.d(TAG, "Calling Yelp API with radius: ${validRadius}m")

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
        val call = service.getNearbyRestaurants(
            authHeader = "Bearer ${BuildConfig.YELP_API_KEY}",
            latitude = latitude,
            longitude = longitude,
            radius = validRadius,
            term = "restaurant"
        )
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

    fun getRestaurantById(
        restaurantId: String,
        onResult: (Business?) -> Unit
    ){
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.yelp.com/") // Base URL for Yelp API
            .addConverterFactory(GsonConverterFactory.create()) // Gson for response deserialization
            .client(OkHttpClient.Builder().build()) // OkHttpClient to make the request
            .build()

        // Create YelpService instance
        val service = retrofit.create(YelpService::class.java)

        // Make the API call with the provided Yelp ID
        val call = service.getRestaurantDetails("Bearer ${BuildConfig.YELP_API_KEY}", restaurantId)

        call.enqueue(object : Callback<Business> {
            override fun onResponse(call: Call<Business>, response: Response<Business>) {
                if (response.isSuccessful) {
                    Log.d("YelpRestaurant", "Restaurant Details: ${response.body()}")
                    onResult(response.body()) // Pass the response body to the callback
                } else {
                    Log.e("YelpRestaurant", "Error: ${response.code()}")
                    onResult(null) // Pass null in case of failure
                }
            }

            override fun onFailure(call: Call<Business>, t: Throwable) {
                Log.e("Yelp Search Restaurant Id", "API call failed: ${t.message}")
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