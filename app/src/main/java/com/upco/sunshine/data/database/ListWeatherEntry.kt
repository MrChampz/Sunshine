package com.upco.sunshine.data.database

import java.util.*

/**
 * Simplified {@link WeatherEntry} which only contains the details needed for the weather list in
 * the {@link com.upco.sunshine.ui.list.ForecastAdapter}.
 */
data class ListWeatherEntry(
    val id: Int,
    val weatherIconId: Int,
    val date: Date,
    val min: Double,
    val max: Double
)