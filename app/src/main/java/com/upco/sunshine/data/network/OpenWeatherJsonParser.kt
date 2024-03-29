package com.upco.sunshine.data.network

import com.upco.sunshine.data.database.WeatherEntry
import com.upco.sunshine.utilities.SunshineDateUtils
import org.json.JSONException
import org.json.JSONObject
import java.net.HttpURLConnection
import java.util.*

/**
 * Parser for OpenWeatherMap JSON data.
 */
object OpenWeatherJsonParser {

    // Weather information. Each day's forecast info is an element of the "list" array.
    private const val OWM_LIST = "list"

    private const val OWM_PRESSURE       = "pressure"
    private const val OWM_HUMIDITY       = "humidity"
    private const val OWM_WIND_SPEED     = "speed"
    private const val OWM_WIND_DIRECTION = "deg"

    // All temperatures are children of the "temp" object
    private const val OWM_TEMPERATURE = "temp"

    // Max temperature for the day
    private const val OWM_MAX = "max"
    private const val OWM_MIN = "min"

    private const val OWM_WEATHER = "weather"
    private const val OWM_WEATHER_ID = "id"

    private const val OWM_MESSAGE_CODE = "cod"

    /**
     * This method parses JSON from a web response and returns an array of Strings
     * describing the weather over various days from the forecast.
     *
     * @param forecastJsonStr JSON response from server.
     * @return Array of Strings describing weather data.
     * @throws JSONException If JSON data cannot be properly parsed.
     */
    @Throws(JSONException::class)
    fun parse(forecastJsonStr: String?): WeatherResponse? {
        val forecastJson = JSONObject(forecastJsonStr)

        // Is there an error?
        if (hasHttpError(forecastJson)) {
            return null
        }

        val weatherForecast = fromJson(forecastJson)

        return WeatherResponse(weatherForecast)
    }

    @Throws(JSONException::class)
    private fun fromJson(forecastJson: JSONObject): Array<WeatherEntry> {
        val jsonWeatherArray = forecastJson.getJSONArray(OWM_LIST)

        val weatherEntries = arrayListOf<WeatherEntry>()

        /*
         * OWM returns daily forecasts based upon the local time of the city that is being asked
         * for, which means that we need to know the GMT offset to translate this data properly.
         * Since this data is also sent in-order and the first day is always the current day, we're
         * going to take advantage of that to get a nice normalized UTC date for all of our weather.
         */
        val normalizedUtcStartDay = SunshineDateUtils.getNormalizedUtcMsForToday()

        for (i in 0 until jsonWeatherArray.length()) {
            // Get the JSON object representing the day
            val dayForecast = jsonWeatherArray.getJSONObject(i)

            // Create the weather entry object
            val dateTimeMillis = normalizedUtcStartDay + SunshineDateUtils.DAY_IN_MILLIS * i
            val weather = fromJson(dayForecast, dateTimeMillis)
            weatherEntries.add(weather)
        }

        return weatherEntries.toTypedArray()
    }

    @Throws(JSONException::class)
    private fun fromJson(dayForecast: JSONObject, dateTimeMillis: Long): WeatherEntry {
        // We ignore all the datetime values embedded in the JSON and assume that
        // the values are returned in-order by day (which is not guaranteed to be correct).

        val pressure = dayForecast.getDouble(OWM_PRESSURE)
        val humidity = dayForecast.getDouble(OWM_HUMIDITY)
        val windSpeed = dayForecast.getDouble(OWM_WIND_SPEED)
        val windDirection = dayForecast.getDouble(OWM_WIND_DIRECTION)

        // Description is in a child array called "weather", which is 1 element long.
        // That element also contains a weather code.
        val weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0)
        val weatherId = weatherObject.getInt(OWM_WEATHER_ID)

        // Temperatures are sent by OpenWeatherMap in a child object called "temp".
        val temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE)
        val max = temperatureObject.getDouble(OWM_MAX)
        val min = temperatureObject.getDouble(OWM_MIN)

        // Create the weather entry object.
        return WeatherEntry(
            weatherId,
            Date(dateTimeMillis),
            max,
            min,
            humidity,
            pressure,
            windSpeed,
            windDirection
        )
    }

    @Throws(JSONException::class)
    private fun hasHttpError(forecastJson: JSONObject): Boolean {
        if (forecastJson.has(OWM_MESSAGE_CODE)) {
            val errorCode = forecastJson.getInt(OWM_MESSAGE_CODE)

            return when (errorCode) {
                HttpURLConnection.HTTP_OK -> false
                HttpURLConnection.HTTP_NOT_FOUND -> true // Location invalid
                else -> true // Server probably down
            }
        }

        return false
    }
}