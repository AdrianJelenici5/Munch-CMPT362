package com.example.munch_cmpt362.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.munch_cmpt362.Business
import com.example.munch_cmpt362.BusinessHours
import com.example.munch_cmpt362.Category
import com.example.munch_cmpt362.Location

// '@Entity" annotation marks this class as a Room entity, meaning it represents a table in the database.
@Entity(tableName = "restaurant_table") // specifies the name of the table in the db

// Defining RestaurantEntry as a data class
// One instant of this class (i.e. object) will represent one row in the above table
data class RestaurantEntry (
    // Setting the id as the primary key
    @PrimaryKey val restaurantId: String,
    val name: String,
    val rating: Float,
    val reviewCount: Int,
    val price: String?,
    val location: Location,
    val phone: String,
    val category: Category,
    val websiteUrl: String,
    val imageUrl: String?,
    val businessHours: List<BusinessHours>,
    val userScore: Int = 100,
    // New fields for caching
    val lastFetched: Long = System.currentTimeMillis(),
    val isCached: Boolean = true,
    val isPreFetched: Boolean = false
) {

    fun toBusiness(): Business {
        return Business(
            id = restaurantId,
            name = name,
            rating = rating,
            review_count = reviewCount,
            price = price,
            location = location,
            phone = phone,
            categories = listOf(category),
            url = websiteUrl,
            image_url = imageUrl ?: "",
            business_hours = businessHours
        )
    }

    /*

    fun getRestaurantID() : Long {
        return id
    }

    fun getRestaurantName(): String {
        return restaurantName
    }

    fun getFoodType(): String {
        return when (foodType) {
            1 -> "Italian"
            2 -> "Spanish"
            3 -> "Greek"
            4 -> "Chinese"
            // ...
            else -> "Unknown"
        }
    }

    */



}