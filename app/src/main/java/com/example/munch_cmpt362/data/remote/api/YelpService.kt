package com.example.munch_cmpt362.data.remote.api

import com.example.munch_cmpt362.Business
import com.example.munch_cmpt362.YelpResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface YelpService {
    @GET("v3/businesses/search")
    fun getNearbyRestaurants(
        @Header("Authorization") authHeader: String,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("radius") radius: Int = 1000,
        @Query("term") term: String = "restaurant",
        @Query("limit") limit: Int = 5
    ): Call<YelpResponse>

    @GET("v3/businesses/search")
    fun searchRestaurants(
        @Header("Authorization") authHeader: String,
        @Query("term") term: String,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double
    ): Call<YelpResponse>

    @GET("v3/businesses/{id}")
    fun getRestaurantDetails(
        @Header("Authorization") authHeader: String,
        @Path("id") restaurantId: String
    ): Call<Business>
}