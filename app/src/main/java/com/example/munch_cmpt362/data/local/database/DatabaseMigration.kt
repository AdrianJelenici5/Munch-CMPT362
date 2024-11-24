package com.example.munch_cmpt362.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add new columns for caching
            database.execSQL(
                "ALTER TABLE restaurant_table ADD COLUMN lastFetched INTEGER NOT NULL DEFAULT 0"
            )
            database.execSQL(
                "ALTER TABLE restaurant_table ADD COLUMN isCached INTEGER NOT NULL DEFAULT 1"
            )
            database.execSQL(
                "ALTER TABLE restaurant_table ADD COLUMN isPreFetched INTEGER NOT NULL DEFAULT 0"
            )
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Create profile tables
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS `user_profiles` (
                    `userId` TEXT NOT NULL,
                    `name` TEXT NOT NULL,
                    `email` TEXT NOT NULL,
                    `bio` TEXT NOT NULL,
                    `profilePictureUrl` TEXT,
                    `searchRadius` INTEGER NOT NULL DEFAULT 25,
                    `lastUpdated` INTEGER NOT NULL,
                    `isPendingSync` INTEGER NOT NULL DEFAULT 0,
                    PRIMARY KEY(`userId`)
                )
            """)

            database.execSQL("""
                CREATE TABLE IF NOT EXISTS `pending_profile_updates` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `userId` TEXT NOT NULL,
                    `updates` TEXT NOT NULL,
                    `timestamp` INTEGER NOT NULL DEFAULT 0
                )
            """)

            // Add any additional indices needed
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_pending_profile_updates_userId` ON `pending_profile_updates` (`userId`)")
        }
    }
}