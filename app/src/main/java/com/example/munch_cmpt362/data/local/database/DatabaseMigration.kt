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
}