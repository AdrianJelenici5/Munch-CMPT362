package com.example.munch_cmpt362.di

import android.content.Context
import androidx.room.Room
import com.example.munch_cmpt362.data.local.dao.RestaurantDao
import com.example.munch_cmpt362.data.local.database.MunchDatabase
import com.example.munch_cmpt362.data.local.cache.RestaurantCacheManager
import com.example.munch_cmpt362.data.local.database.DatabaseMigrations
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import munch_cmpt362.database.restaurants.RestaurantRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MunchDatabase {
        return Room.databaseBuilder(
            context,
            MunchDatabase::class.java,
            "munch_database"
        )
            .addMigrations(DatabaseMigrations.MIGRATION_1_2)
            .build()
    }

    @Provides
    @Singleton
    fun provideRestaurantDao(database: MunchDatabase): RestaurantDao {
        return database.restaurantDao
    }

    @Provides
    @Singleton
    fun provideCoroutineScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }

    @Provides
    @Singleton
    fun provideRestaurantCacheManager(
        restaurantDao: RestaurantDao,
        coroutineScope: CoroutineScope
    ): RestaurantCacheManager {
        return RestaurantCacheManager(restaurantDao, coroutineScope)
    }

    @Provides
    @Singleton
    fun provideRestaurantRepository(
        restaurantDao: RestaurantDao
    ): RestaurantRepository {
        return RestaurantRepository(restaurantDao)
    }
}