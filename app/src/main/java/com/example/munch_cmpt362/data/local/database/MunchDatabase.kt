package com.example.munch_cmpt362.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.munch_cmpt362.data.local.dao.ProfileDao
import com.example.munch_cmpt362.data.local.dao.RestaurantDao
import com.example.munch_cmpt362.data.local.entity.RestaurantEntry
import com.example.munch_cmpt362.data.local.entity.UserProfileEntity
import com.example.munch_cmpt362.data.local.Converters
import com.example.munch_cmpt362.data.local.entity.PendingProfileUpdate
import munch_cmpt362.database.groups.*
import munch_cmpt362.database.groupxuser.*
import munch_cmpt362.database.users.*

// Using @Database annotation to mark this class as a Room DB
// This db will be made up of four tables:
@Database(entities = arrayOf(
    RestaurantEntry::class,
    UserProfileEntity::class,
    PendingProfileUpdate::class,
    UserEntry::class, GroupEntry::class,
    GroupUserCrossRef::class
),version = 4)
@TypeConverters(Converters::class)
// Telling the db to use a specific type of conversion method:
abstract class MunchDatabase : RoomDatabase() {

    // DOA provides the methods needed for accessing tables in the db
    abstract val restaurantDao: RestaurantDao
    abstract val userDao: UserDao
    abstract val groupDao: GroupDao
    abstract val profileDao: ProfileDao
    // abstract val groupCrossUserDao : GroupCrossUserDao
    //abstract val groupUserCrossRefDao: GroupUserCrossRefDao

    // Defining object that belongs to the class itself, not to an instance of the class
    // This allows sharing a single database instance across the entire app.
    companion object{
        //The Volatile keyword guarantees visibility of changes to the INSTANCE variable across threads
        @Volatile
        private var INSTANCE: MunchDatabase? = null

        // Function that initializes the database on the first access
        // Also returns existing instance on subsequent calls, ensuring only one instance of the db is ever used.
        fun getInstance(context: Context): MunchDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MunchDatabase::class.java,
                    "munch_database"
                )
                    .addMigrations(
                        DatabaseMigrations.MIGRATION_1_2,
                        DatabaseMigrations.MIGRATION_2_3,
                        DatabaseMigrations.MIGRATION_3_4  // Add new migration
                    )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

}