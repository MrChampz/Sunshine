package com.upco.sunshine.data.database

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.*

/**
 * Defines the schema of a table in {@link Room} for a single weather forecast.
 * The date is used as an {@link Index} so that its uniqueness can be ensured.
 * Indexes also allow for fast lookup for the column.
 *
 * Constructor used by the Room to create WeatherEntries.
 */
@Entity(tableName = "weather", indices = [Index(value = ["date"], unique = true)])
data class WeatherEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int?,
    val weatherIconId: Int,
    val date: Date,
    val min: Double,
    val max: Double,
    val humidity: Double,
    val pressure: Double,
    val wind: Double,
    val degrees: Double
) {
    /**
     * This constructor is used by OpenWeatherJsonParser. When the network fetch has JSON data,
     * it converts this data to WeatherEntry objects using this constructor.
     *
     * @param weatherIconId Image id for weather.
     * @param date          Date of weather.
     * @param min           Min temperature.
     * @param max           Max temperature.
     * @param humidity      Humidity for the day.
     * @param pressure      Barometric pressure.
     * @param wind          Wind speed.
     * @param degrees       Wind direction.
     */
    @Ignore
    constructor(
        weatherIconId: Int,
        date: Date,
        min: Double,
        max: Double,
        humidity: Double,
        pressure: Double,
        wind: Double,
        degrees: Double
    ): this(null, weatherIconId, date, min, max, humidity, pressure, wind, degrees)
}