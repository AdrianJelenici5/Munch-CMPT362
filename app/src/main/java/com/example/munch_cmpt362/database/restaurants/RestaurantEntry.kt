package munch_cmpt362.database.restaurants

import androidx.room.Entity
import androidx.room.PrimaryKey

// '@Entity" annotation marks this class as a Room entity, meaning it represents a table in the database.
@Entity(tableName = "restaurant_table") // specifies the name of the table in the db

// Defining RestaurantEntry as a data class
// One instant of this class (i.e. object) will represent one row in the above table
data class RestaurantEntry (
    // Setting the id as the primary key
    @PrimaryKey(autoGenerate = true) val restaurantId: Long = 0L,
    // Listing all the other fields/attributes:
    val restaurantName: String,
    val coordinates: Int, // TODO: change the type?
    val foodType: Int, // TODO: keep this as int or change to string?
    val rating: Int,
    val websiteUrl: String,
    val ReviewCount: Int,
) {

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