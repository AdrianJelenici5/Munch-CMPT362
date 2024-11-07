package munch_cmpt362.database.groupxuser

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import munch_cmpt362.database.groups.GroupEntry
import munch_cmpt362.database.users.UserEntry

// Since we cant have a list of users in our groupEntry, this table will workaround that
// Entries in this table will be as follows: {groupID, userID}
// Groups will have multiple users, thus multiple entries will have the same groupID
// To see what users make up a group, you would have to select rows from this table where groupID = <groupID>

// '@Entity" annotation marks this class as a Room entity, meaning it represents a table in the database.
@Entity(
    tableName = "group_user_cross_ref", // specifies the name of the table in the db
    // Specifying foreign keys:
    foreignKeys = [
        ForeignKey(
            entity = GroupEntry::class,   // The entity to reference
            parentColumns = ["groupId"],  // Column in the referenced table (group_table)
            childColumns = ["groupId"],   // Columns in this table
            onDelete = ForeignKey.CASCADE // Action on delete
        ),
        ForeignKey(
            entity = UserEntry::class,   // The entity to reference
            parentColumns = ["userId"],  // Column in the referenced table (user_table)
            childColumns = ["userId"],   // Columns in this table
            onDelete = ForeignKey.CASCADE  // Action on delete
        )
    ]
)

// Defining GroupUserCrossRef as a data class
// One instant of this class (i.e. object) will represent one row in the above table
data class GroupUserCrossRef(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,  // Unique ID for each row in the junction table
    val groupId: Long, // Foreign key to GroupEntry
    val userId: Long   // Foreign key to UserEntry
)