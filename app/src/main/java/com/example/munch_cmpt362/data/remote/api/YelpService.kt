package com.example.munch_cmpt362.data.remote.api

import com.example.munch_cmpt362.YelpResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface YelpService {
    @GET("v3/businesses/search")
    fun getNearbyRestaurants(
        @Header("Authorization") authHeader: String,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("term") term: String = "restaurant",
        @Query("limit") limit: Int = 10
    ): Call<YelpResponse>

    @GET("v3/businesses/search")
    fun searchRestaurants(
        @Header("Authorization") authHeader: String,
        @Query("term") term: String,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double
    ): Call<YelpResponse>
}