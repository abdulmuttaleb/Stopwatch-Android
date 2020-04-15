package com.ahmad.stopwatch.viewmodel

import android.app.Application
import android.os.SystemClock
import android.util.Log
import android.widget.Chronometer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class StopwatchViewModel(application: Application): AndroidViewModel(application) {

    var state = MutableLiveData<STATE>()
    var pauseOffset: Long = 0
    init {
        state.postValue(STATE.STOPPED)
    }

    fun run(chronometer: Chronometer){
        when(state.value){
            //start chronometer
            STATE.STOPPED -> {
                state.postValue(STATE.RUNNING)
                chronometer.base = SystemClock.elapsedRealtime() - pauseOffset
                chronometer.start()
            }
            //resume chronometer
            STATE.PAUSED -> {
                state.postValue(STATE.RUNNING)
                chronometer.base = SystemClock.elapsedRealtime() - pauseOffset
                chronometer.start()
            }
            //pause chronometer
            STATE.RUNNING -> {
                state.postValue(STATE.PAUSED)
                chronometer.stop()
                pauseOffset = SystemClock.elapsedRealtime() - chronometer.base
            }
        }
    }

    fun stop(chronometer: Chronometer){
        state.postValue(STATE.STOPPED)
        chronometer.stop()
        chronometer.base = SystemClock.elapsedRealtime()
        pauseOffset = 0
    }

    sealed class STATE {
        object STOPPED : STATE()
        object PAUSED : STATE()
        object RUNNING: STATE()
    }

    companion object{
        const val TAG = "StopwatchViewModel"
    }
}