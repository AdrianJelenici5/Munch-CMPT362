package com.example.munch_cmpt362.di

import android.content.Context
import androidx.room.Room
import com.example.munch_cmpt362.ui.group.datadaoview.GroupDatabase
import com.example.munch_cmpt362.ui.group.datadaoview.GroupDatabaseDao
import com.example.munch_cmpt362.ui.group.datadaoview.GroupRepository
import com.example.munch_cmpt362.data.local.cache.GroupCacheManager
import com.example.munch_cmpt362.ui.group.datadaoview.GroupDatabaseMigrations
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GroupModule {
    @Provides
    @Singleton
    fun provideGroupDatabase(
        @ApplicationContext context: Context
    ): GroupDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            GroupDatabase::class.java,
            "GC_db"
        )
            .addMigrations(GroupDatabaseMigrations.MIGRATION_6_7)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideGroupDao(database: GroupDatabase): GroupDatabaseDao {
        return database.groupDatabaseDao
    }

    @Provides
    @Singleton
    fun provideGroupCacheManager(
        groupDao: GroupDatabaseDao,
        coroutineScope: CoroutineScope // This will use the CoroutineScope from DatabaseModule
    ): GroupCacheManager {
        return GroupCacheManager(groupDao, coroutineScope)
    }

}