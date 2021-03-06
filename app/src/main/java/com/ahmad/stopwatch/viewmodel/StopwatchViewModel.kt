package com.ahmad.stopwatch.viewmodel

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.os.Handler
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.ahmad.stopwatch.model.Milestone

class StopwatchViewModel(application: Application): AndroidViewModel(application) {

    var state = MutableLiveData<STATE>(STATE.STOPPED)
    var stopWatchTimeLiveData = MutableLiveData<Long>().apply { postValue(0) }

    var milestonesLiveData: MutableLiveData<ArrayList<Milestone>> = MutableLiveData(arrayListOf())

    //new implementation using handlers
    var milliSeconds: Long = 0
    var startTime:Long = 0
    var pausedTime:Long = 0
    var toDisplayTime:Long = 0

    var handler = Handler()

    private val context = getApplication<Application>().applicationContext

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
        resetMilestones()
        removeNotifications()
    }

    fun addMilestone(milestone: Milestone) {
        milestonesLiveData.value!!.add(milestone)
        milestonesLiveData.value = milestonesLiveData.value
    }

    fun setMilestonePassed(milestone: Milestone){
        milestonesLiveData.value!!.find { it.id == milestone.id }.apply {
            this?.passed = true
            milestonesLiveData.value = milestonesLiveData.value
        }
    }

    fun resetMilestones(){
        milestonesLiveData.value!!.forEach { it.passed = false }.apply { milestonesLiveData.value = milestonesLiveData.value }
    }



    fun removeNotifications(){
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
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