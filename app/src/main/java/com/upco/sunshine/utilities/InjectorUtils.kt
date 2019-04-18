package com.upco.sunshine.utilities

import android.content.Context
import com.upco.sunshine.AppExecutors
import com.upco.sunshine.data.SunshineRepository
import com.upco.sunshine.data.database.SunshineDatabase
import com.upco.sunshine.data.network.WeatherNetworkDataSource
import com.upco.sunshine.ui.detail.DetailViewModelFactory
import com.upco.sunshine.ui.list.MainViewModelFactory
import java.util.*

/**
 * Provides static methods to inject the various classes needed for Sunshine.
 */
object InjectorUtils {

    fun provideRepository(context: Context): SunshineRepository {
        val database = SunshineDatabase(context.applicationContext)
        val executors = AppExecutors()
        val networkDataSource = WeatherNetworkDataSource(context.applicationContext, executors)
        return SunshineRepository(database.weatherDao(), networkDataSource, executors)
    }

    fun provideNetworkDataSource(context: Context): WeatherNetworkDataSource {
        // This call to provide repository is necessary if the app starts from a service,
        // in this case the repository will not exist unless it is specifically created.
        provideRepository(context.applicationContext)
        val executors = AppExecutors()
        return WeatherNetworkDataSource(context.applicationContext, executors)
    }

    fun provideDetailViewModelFactory(context: Context, date: Date): DetailViewModelFactory {
        val repository = provideRepository(context.applicationContext)
        return DetailViewModelFactory(repository, date)
    }

    fun provideMainViewModelFactory(context: Context): MainViewModelFactory {
        val repository = provideRepository(context.applicationContext)
        return MainViewModelFactory(repository)
    }
}