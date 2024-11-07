package com.example.munch_cmpt362.group.datadaoview

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

// Sample user data
@Entity(tableName = "user_table")
data class User (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "user_ID")
    var userID: Long = 0L,

    @ColumnInfo(name = "user_name")
    var userName: String = ""
)

// Data for basic group information
@Entity(tableName = "group_table",
    primaryKeys = ["group_ID", "user_ID"],
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            // let one groupID able to have multiple userIDs (replicates list of users)
            parentColumns = arrayOf("user_ID"),
            childColumns = arrayOf("user_ID"),
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ]
    )
data class Group (
    @ColumnInfo(name = "group_ID")
    var groupID: Long = 0L,

    @ColumnInfo(name = "user_ID")
    var userID: Long = 0L,

    @ColumnInfo(name = "group_name")
    var groupName: String = "",

    @ColumnInfo(name = "restaurant_name")
    var restaurantName: String = "",

    @ColumnInfo(name = "voting")
    var voting: Boolean = false
)

// Basic counter
@Entity(tableName = "counter_table")
data class Counter (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "counter_num")
    var counterNum: Long = 0L
)