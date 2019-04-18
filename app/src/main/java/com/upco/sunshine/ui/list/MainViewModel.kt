package com.upco.sunshine.ui.list

import androidx.lifecycle.ViewModel
import com.upco.sunshine.data.SunshineRepository

/**
 * {@link ViewModel} for {@link MainActivity}.
 */
class MainViewModel(repository: SunshineRepository): ViewModel() {

    val forecast = repository.getCurrentWeatherForecasts()
}