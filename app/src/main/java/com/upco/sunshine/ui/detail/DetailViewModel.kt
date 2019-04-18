package com.upco.sunshine.ui.detail

import androidx.lifecycle.ViewModel
import com.upco.sunshine.data.SunshineRepository
import java.util.*

/**
 * {@link ViewModel} for {@link DetailActivity}.
 */
class DetailViewModel(repository: SunshineRepository, date: Date): ViewModel() {

    // Weather forecast the user is looking at
    val weather = repository.getWeatherByDate(date)
}