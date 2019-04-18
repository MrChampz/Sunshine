package com.upco.sunshine.data.database

import androidx.room.TypeConverter
import java.util.*

/**
 * {@link TypeConverter} for long to {@link Date}.
 * <p>
 * This replaces the date as a long in the database, but returns it as a {@link Date}.
 */
object DateConverter {

    @TypeConverter
    @JvmStatic
    fun toDate(timestamp: Long?): Date? {
        return if (timestamp == null) null else Date(timestamp)
    }

    @TypeConverter
    @JvmStatic
    fun toTimestamp(date: Date?): Long? {
        return date?.time
    }
}