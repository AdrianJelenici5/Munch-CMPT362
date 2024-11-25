package com.example.munch_cmpt362.ui.group.datadaoview

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Have stub users
@Database(entities = [User::class, Group::class, Counter::class], version = 7)
abstract class GroupDatabase: RoomDatabase() {
    abstract val groupDatabaseDao: GroupDatabaseDao

    companion object{
        @Volatile
        private var INSTANCE: GroupDatabase? = null

        fun getInstance(context: Context): GroupDatabase {
            synchronized(this){
                var instance = INSTANCE
                if(instance == null){
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        GroupDatabase::class.java,
                        "GC_db"
                    )
                        .addMigrations(GroupDatabaseMigrations.MIGRATION_6_7)
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }

    }
}