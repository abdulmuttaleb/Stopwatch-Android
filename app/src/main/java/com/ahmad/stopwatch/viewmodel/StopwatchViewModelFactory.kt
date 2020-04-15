package com.ahmad.stopwatch.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class StopwatchViewModelFactory(val application: Application): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StopwatchViewModel::class.java)) {
            return StopwatchViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}