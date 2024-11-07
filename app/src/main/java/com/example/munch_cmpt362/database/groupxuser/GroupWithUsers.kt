package munch_cmpt362.database.groupxuser

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import munch_cmpt362.database.groups.GroupEntry
import munch_cmpt362.database.users.UserEntry

// This data class will act as a container for both a GroupEntry object and a list of related UserEntry objects
data class GroupWithUsers(
    // '@Embedded' tells room to include all the fields of the specified object
    // (GroupEntry in this case) as if they were part of the GroupWithUsers table itself.
    @Embedded val group: GroupEntry,
    // @Relation is a Room annotation used to specify a relationship between two tables
    // This allows Room to join data from related tables
    @Relation(
        parentColumn = "groupId", // column used to join with the foreign key column
        entityColumn = "userId", // column that matches the foreign key column
        associateBy = Junction(GroupUserCrossRef::class) // junction table that Room uses to establish the many-to-many relationship
    )
    val users: List<UserEntry>
)