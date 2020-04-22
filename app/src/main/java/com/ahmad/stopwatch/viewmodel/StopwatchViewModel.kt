package com.ahmad.stopwatch.viewmodel

import android.app.Application
import android.os.Handler
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class StopwatchViewModel(application: Application): AndroidViewModel(application) {

    var state = MutableLiveData<STATE>(STATE.STOPPED)
    var stopWatchTimeLiveData = MutableLiveData<Long>().apply { postValue(0) }

    var numberOfTimesUsed = 0
//    var lastAdTypeShown: Int? = null
    //new implementation using handlers
    var milliSeconds: Long = 0
    var startTime:Long = 0
    var pausedTime:Long = 0
    var toDisplayTime:Long = 0

    var handler = Handler()

    var runnable = object : Runnable {
        override fun run() {
            milliSeconds = SystemClock.uptimeMillis() - startTime
            toDisplayTime = pausedTime + milliSeconds
            stopWatchTimeLiveData.postValue(toDisplayTime)
            handler.postDelayed(this, 0)
        }
    }
    init {
        state.postValue(STATE.STOPPED)
    }

    fun run(){
        when(state.value){
            //start chronometer
            STATE.STOPPED -> {
                state.postValue(STATE.RUNNING)
                startTime = SystemClock.uptimeMillis()
                handler.postDelayed(runnable, 0)
            }
            //resume chronometer
            STATE.PAUSED -> {
                state.postValue(STATE.RUNNING)
                startTime = SystemClock.uptimeMillis()
                handler.postDelayed(runnable, 0)
            }
            //pause chronometer
            STATE.RUNNING -> {
                state.postValue(STATE.PAUSED)
                pausedTime += milliSeconds
                handler.removeCallbacks(runnable)
            }
        }
    }

    fun stop(){
        state.postValue(STATE.STOPPED)
        milliSeconds = 0
        startTime = 0
        pausedTime = 0
        toDisplayTime = 0
        stopWatchTimeLiveData.postValue(0)
        handler.removeCallbacks(runnable)
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