package com.ahmad.stopwatch.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class StopwatchViewModel(application: Application): AndroidViewModel(application) {

    var time = MutableLiveData<Float>()
    var state = MutableLiveData<STATE>()

    init {
        time.postValue(0.0f)
        state.postValue(STATE.STOPPED)
    }

    fun run(){
        when(state.value){
            STATE.STOPPED, STATE.PAUSED->  state.postValue(STATE.RUNNING)
            STATE.RUNNING -> state.postValue(STATE.PAUSED)
        }
        Log.e(TAG, "stateValue: ${state.value.toString()}")
    }

    fun stop(){
        state.postValue(STATE.STOPPED)
        Log.e(TAG, "stateValue: ${state.value.toString()}")
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