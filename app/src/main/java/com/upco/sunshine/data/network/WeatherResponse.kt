package com.upco.sunshine.data.network

import androidx.annotation.NonNull
import com.upco.sunshine.data.database.WeatherEntry

/**
 * Weather response from the backend. Contains the weather forecasts.
 */
data class WeatherResponse(
    @NonNull val weatherForecast: Array<WeatherEntry>
)