package com.example.munch_cmpt362.ui.group.datadaoview

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object GroupDatabaseMigrations {
    val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add new columns for caching to group_table
            database.execSQL("""
                ALTER TABLE group_table 
                ADD COLUMN lastFetched INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}
            """)

            database.execSQL("""
                ALTER TABLE group_table 
                ADD COLUMN isCached INTEGER NOT NULL DEFAULT 1
            """)

            database.execSQL("""
                ALTER TABLE group_table 
                ADD COLUMN isPendingSync INTEGER NOT NULL DEFAULT 0
            """)

            // Create index for better query performance
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS index_group_table_group_id ON group_table(group_ID)"
            )
        }
    }
}