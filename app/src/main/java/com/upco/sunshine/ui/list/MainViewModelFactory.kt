package com.upco.sunshine.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.upco.sunshine.data.SunshineRepository

/**
 * Factory method that allows us to create a ViewModel with a constructor that takes a
 * {@link SunshineRepository}.
 */
class MainViewModelFactory(private val repository: SunshineRepository):
        ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return MainViewModel(repository) as T
    }
}