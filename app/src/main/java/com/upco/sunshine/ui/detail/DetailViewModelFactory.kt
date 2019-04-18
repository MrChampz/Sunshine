package com.upco.sunshine.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.upco.sunshine.data.SunshineRepository
import java.util.*

/**
 * Factory method that allows us to create a ViewModel with a constructor that takes a
 * {@link SunshineRepository} and an id for the current {@link WeatherEntry}.
 */
class DetailViewModelFactory(
    private val repository: SunshineRepository,
    private val date: Date
): ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return DetailViewModel(repository, date) as T
    }
}