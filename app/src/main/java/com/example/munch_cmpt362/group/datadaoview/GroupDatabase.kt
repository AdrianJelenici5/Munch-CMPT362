package com.example.munch_cmpt362.group.datadaoview

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Have stub users
@Database(entities = [User::class, Group::class], version = 4)
abstract class GroupDatabase: RoomDatabase() {
    abstract val groupDatabaseDao: GroupDatabaseDao

    companion object{
        @Volatile
        private var INSTANCE: GroupDatabase? = null

        fun getInstance(context: Context): GroupDatabase{
            synchronized(this){
                var instance = INSTANCE
                if(instance == null){
                    instance = Room.databaseBuilder(context.applicationContext,
                        GroupDatabase::class.java, "GC db").fallbackToDestructiveMigration().build()
                    INSTANCE = instance
                }
                return instance
            }
        }

    }
}