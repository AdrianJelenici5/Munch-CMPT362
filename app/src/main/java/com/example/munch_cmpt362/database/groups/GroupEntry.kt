package munch_cmpt362.database.groups

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import munch_cmpt362.database.restaurants.RestaurantEntry

// '@Entity" annotation marks this class as a Room entity, meaning it represents a table in the database.
@Entity(
    tableName = "group_table", // specifies the name of the table in the db
    // Specifying foreign keys:
    foreignKeys = [
        ForeignKey(
            entity = RestaurantEntry::class,           // The entity to reference
            parentColumns = ["restaurantId"],          // Column in the referenced table (restaurant_table)
            childColumns = ["preferredRestaurantId"],  // Column in this table (group_table)
            onDelete = ForeignKey.CASCADE              // Action on delete
        )
    ]
)

// Defining GroupEntry as a data class
// One instant of this class (i.e. object) will represent one row in the above table
data class GroupEntry (
    // Setting the id as the primary key
    @PrimaryKey(autoGenerate = true) val groupId: Long = 0L,
    // Listing all the other fields/attributes:
    val preferredRestaurantId: String, // This is a foreign key

    /////////////////////
    // IMPORTANT NOTE //
    ////////////////////

    // Room doesn’t support lists directly as fields, nor lists where each entry is foreign key
    // Therefore, to workaround this constraint, we will create a seperate crossref table
    // Entries in this table will be as follows: {groupID, userID (user that is in that group)}
    // Groups will have multiple users, thus multiple entries will have the same groupID
    // Then we can create a relationship between groupEntries and userEntires with this table
    // And finally define a custom class that holds GroupEntry object and a list of related UserEntry objects

    // To get the list of users from one group, we can get the desired groupId from group_table
    // And then use the crossref table and above relationship to store that list of users in our custom class

    // Therefore, groupEntry will not have a field that holds a list of users

) {

    /*

    fun getGroupID() : Long {
        return id
    }

    fun getUsers(): Int {
        return users
    }

    fun getPreferredRestaurant(): String {
        return preferredRestaurant
    }

     */

}