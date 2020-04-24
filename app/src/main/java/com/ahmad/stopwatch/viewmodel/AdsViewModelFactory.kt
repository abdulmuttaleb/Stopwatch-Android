package com.ahmad.stopwatch.viewmodel

import android.app.Activity
import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AdsViewModelFactory(val application: Application, val activity: AppCompatActivity): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdsViewModel::class.java)) {
            return AdsViewModel(application, activity) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}