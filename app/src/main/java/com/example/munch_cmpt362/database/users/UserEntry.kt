package munch_cmpt362.database.users

import androidx.room.Entity
import androidx.room.PrimaryKey

// '@Entity" annotation marks this class as a Room entity, meaning it represents a table in the database.
@Entity(tableName = "user_table") // specifies the name of the table in the db

// Defining UserEntry as a data class
// One instant of this class (i.e. object) will represent one row in the above table
data class UserEntry (
    // Setting the id as the primary key
    @PrimaryKey(autoGenerate = true) val userId: Long = 0L,
    // Listing all the other fields/attributes:
    val userName: String,
    val profileImage: Int, // TODO: Change this to a lob file?
) {

    /*

    fun getUserID() : Long {
        return id
    }

    fun getUserName(): String {
        return userName
    }

    */



}