package com.upco.sunshine.data.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * {@link SunshineDatabase} database for the application including a table for
 * {@link WeatherEntry} with the DAO {@link WeatherDao}.
 */
@Database(entities = [WeatherEntry::class], version = 1)
@TypeConverters(DateConverter::class)
abstract class SunshineDatabase: RoomDatabase() {

    companion object {
        private val LOG_TAG = SunshineDatabase::class.java.simpleName
        private const val DATABASE_NAME = "weather"

        // For singleton instantiation
        private val LOCK = Any()
        @Volatile private var instance: SunshineDatabase? = null

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also { instance = it }
        }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                SunshineDatabase::class.java,
                DATABASE_NAME
            ).build()
    }

    // The associated DAOs for the database
    abstract fun weatherDao(): WeatherDao
}