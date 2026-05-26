package ru.fefu.countryexplorer.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        FavouriteEntity::class,
        HistoryEntity::class,
        NoteEntity::class,
        CountryEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favouriteDao(): FavouriteDao
    abstract fun historyDao(): HistoryDao
    abstract fun noteDao(): NoteDao
    abstract fun countryDao(): CountryDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS history (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        countryId TEXT NOT NULL,
                        countryName TEXT NOT NULL,
                        timestamp INTEGER NOT NULL
                    )
                """)

                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS notes (
                        countryId TEXT PRIMARY KEY NOT NULL,
                        text TEXT NOT NULL,
                        lastEdited INTEGER NOT NULL
                    )
                """)

                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS cached_countries (
                        id TEXT PRIMARY KEY NOT NULL,
                        name TEXT NOT NULL,
                        capital TEXT,
                        region TEXT NOT NULL,
                        population INTEGER NOT NULL,
                        area REAL,
                        flagUrl TEXT,
                        timestamp INTEGER NOT NULL
                    )
                """)
            }
        }
    }
}