package com.upco.sunshine.data

import android.util.Log
import androidx.lifecycle.LiveData
import com.upco.sunshine.AppExecutors
import com.upco.sunshine.data.database.ListWeatherEntry
import com.upco.sunshine.data.database.WeatherDao
import com.upco.sunshine.data.database.WeatherEntry
import com.upco.sunshine.data.network.WeatherNetworkDataSource
import com.upco.sunshine.utilities.SunshineDateUtils
import java.util.*

/**
 * Handles data operations in Sunshine. Acts as a mediator between
 * {@link WeatherNetworkDataSource} and {@link WeatherDao}.
 */
class SunshineRepository private constructor(
    private val weatherDao: WeatherDao,
    private val weatherNetworkDataSource: WeatherNetworkDataSource,
    private val executors: AppExecutors
) {

    companion object {
        private val LOG_TAG = SunshineRepository::class.java.simpleName

        // For singleton instantiation
        private val LOCK = Any()
        @Volatile private var instance: SunshineRepository? = null

        operator fun invoke(
            weatherDao: WeatherDao,
            weatherNetworkDataSource: WeatherNetworkDataSource,
            executors: AppExecutors
        ) = instance ?: synchronized(LOCK) {
            instance ?: buildRepository(
                weatherDao,
                weatherNetworkDataSource,
                executors
            ).also { instance = it }
        }

        private fun buildRepository(
            weatherDao: WeatherDao,
            weatherNetworkDataSource: WeatherNetworkDataSource,
            executors: AppExecutors
        ) = SunshineRepository(weatherDao, weatherNetworkDataSource, executors)
    }

    private var initialized = false

    init {
        // As long as the repository exists, observe the network LiveData.
        // If that LiveData changes, update the database.
        val networkData = weatherNetworkDataSource.getCurrentWeatherForecasts()
        networkData.observeForever {
            executors.diskIO.execute {
                // Deletes old historical data
                deleteOldData()
                Log.d(LOG_TAG, "Old weather deleted")

                // Insert our new weather data into Sunshine's database
                weatherDao.bulkInsert(it)
                Log.d(LOG_TAG, "New values inserted")
            }
        }
    }

    /**
     * Database related operations.
     */

    fun getCurrentWeatherForecasts(): LiveData<List<ListWeatherEntry>> {
        initializeData()
        val today = SunshineDateUtils.getNormalizedUtcDateForToday()
        return weatherDao.getCurrentWeatherForecasts(today)
    }

    fun getWeatherByDate(date: Date): LiveData<WeatherEntry> {
        initializeData()
        return weatherDao.getWeatherByDate(date)
    }

    /**
     * Deletes old weather data because we don't need to keep multiples day's data.
     */
    private fun deleteOldData() {
        val today = SunshineDateUtils.getNormalizedUtcDateForToday()
        weatherDao.deleteOldWeather(today)
    }

    /**
     * Checks if there are enough days of future weather for the app to display all
     * the needed data.
     *
     * @return Whether a fetch is needed.
     */
    private fun isFetchNeeded(): Boolean {
        val today = SunshineDateUtils.getNormalizedUtcDateForToday()
        val count = weatherDao.countAllFutureWeather(today)
        return (count < WeatherNetworkDataSource.NUM_DAYS)
    }

    /**
     * Network related operations.
     */

    private fun startFetchWeatherService() {
        weatherNetworkDataSource.startFetchWeatherService()
    }

    /**
     * Creates periodic sync tasks and checks to see if an immediate sync is required. If an
     * immediate sync is required, this method will take care of making sure that sync occurs.
     */
    @Synchronized
    private fun initializeData() {
        // Only perform initialization once per app lifetime. If initialization has already been
        // performed, we have nothing to do in this method.
        if (initialized) return
        initialized = true

        // This method call triggers Sunshine to create its task to synchronize weather data
        // periodically.
        weatherNetworkDataSource.scheduleRecurringFetchWeatherSync()

        executors.diskIO.execute {
            if (isFetchNeeded()) {
                startFetchWeatherService()
            }
        }
    }
}