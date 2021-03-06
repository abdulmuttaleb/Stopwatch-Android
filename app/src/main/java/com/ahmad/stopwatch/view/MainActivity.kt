package com.ahmad.stopwatch.view

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ahmad.stopwatch.R
import com.ahmad.stopwatch.adapter.MilestoneRecyclerViewAdapter
import com.ahmad.stopwatch.model.Milestone
import com.ahmad.stopwatch.utils.Constants
import com.ahmad.stopwatch.viewmodel.AdsViewModel
import com.ahmad.stopwatch.viewmodel.AdsViewModelFactory
import com.ahmad.stopwatch.viewmodel.StopwatchViewModel
import com.ahmad.stopwatch.viewmodel.StopwatchViewModelFactory
import com.google.android.gms.ads.*
import com.google.android.material.button.MaterialButton
import org.joda.time.Duration
import org.joda.time.Period
import org.joda.time.format.PeriodFormatterBuilder
import java.util.*

class MainActivity : AppCompatActivity() {

    lateinit var timerTextView: TextView
    lateinit var playPauseMaterialButton: MaterialButton
    lateinit var resetMaterialButton: MaterialButton
    lateinit var milestonesRecyclerView: RecyclerView
    lateinit var milestonesAdapter: MilestoneRecyclerViewAdapter
    lateinit var addMilestoneButton: MaterialButton

    lateinit var stopwatchViewModel: StopwatchViewModel
    lateinit var adsViewModel: AdsViewModel
    lateinit var bannerAdView: AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        activityInit()

    }
    private fun activityInit(){
        timerTextView = findViewById(R.id.tv_timer)
        playPauseMaterialButton = findViewById(R.id.btn_play_pause)
        resetMaterialButton = findViewById(R.id.btn_reset)
        bannerAdView = findViewById(R.id.av_banner_top)
        milestonesRecyclerView = findViewById(R.id.rv_milestones)
        addMilestoneButton = findViewById(R.id.btn_add_milestone)

        stopwatchViewModel = ViewModelProvider(this, StopwatchViewModelFactory(application)).get(StopwatchViewModel::class.java)
        adsViewModel = ViewModelProvider(this, AdsViewModelFactory(application, this)).get(AdsViewModel::class.java)
        adsViewModel.setViewModelAdView(bannerAdView)

        milestonesAdapter = MilestoneRecyclerViewAdapter()
        val linearLayoutManager = LinearLayoutManager(this)
        milestonesRecyclerView.layoutManager = linearLayoutManager
        milestonesRecyclerView.adapter = milestonesAdapter

        stopwatchViewModel.state.observe(this, Observer {
            when(it){
                StopwatchViewModel.STATE.STOPPED ->{
                    playPauseMaterialButton.text = "PLAY"
                    playPauseMaterialButton.icon = getDrawable(R.drawable.ic_play_arrow)
                    playPauseMaterialButton.strokeColor = getColorStateList(R.color.color_med_turquoise)
                    playPauseMaterialButton.rippleColor = getColorStateList(R.color.color_med_turquoise)
                    playPauseMaterialButton.iconTint = getColorStateList(R.color.color_med_turquoise)
                    playPauseMaterialButton.setTextColor(getColorStateList(R.color.color_med_turquoise))

                    resetMaterialButton.visibility = View.INVISIBLE
                }
                StopwatchViewModel.STATE.PAUSED ->{
                    playPauseMaterialButton.text = "PLAY"
                    playPauseMaterialButton.icon = getDrawable(R.drawable.ic_play_arrow)
                    playPauseMaterialButton.strokeColor = getColorStateList(R.color.color_med_turquoise)
                    playPauseMaterialButton.rippleColor = getColorStateList(R.color.color_med_turquoise)
                    playPauseMaterialButton.iconTint = getColorStateList(R.color.color_med_turquoise)
                    playPauseMaterialButton.setTextColor(getColorStateList(R.color.color_med_turquoise))

                    resetMaterialButton.visibility = View.VISIBLE

                }
                StopwatchViewModel.STATE.RUNNING ->{
                    playPauseMaterialButton.text = "PAUSE"
                    playPauseMaterialButton.icon = getDrawable(R.drawable.ic_pause)
                    playPauseMaterialButton.strokeColor = getColorStateList(R.color.color_mustard)
                    playPauseMaterialButton.rippleColor = getColorStateList(R.color.color_mustard)
                    playPauseMaterialButton.iconTint = getColorStateList(R.color.color_mustard)
                    playPauseMaterialButton.setTextColor(getColorStateList(R.color.color_mustard))

                    resetMaterialButton.visibility = View.VISIBLE
                }
            }
        })

        stopwatchViewModel.stopWatchTimeLiveData.observe(this, Observer {
            val period = Period(it)
            val time = Constants.periodFormatter.print(period)
            timerTextView.text = time
            checkIfPeriodHits(it)
        })

        stopwatchViewModel.milestonesLiveData.observe(this, Observer {
            milestonesAdapter.milestones = it
        })

        playPauseMaterialButton.setOnClickListener {
            stopwatchViewModel.run()
        }

        resetMaterialButton.setOnClickListener {
            stopwatchViewModel.stop()
        }

        addMilestoneButton.setOnClickListener {
            val intent = Intent(this, SelectTimerActivity::class.java)
            intent.putExtra("milestonesSize", stopwatchViewModel.milestonesLiveData.value!!.size)
            startActivityForResult(intent, TIMER_REQUEST_CODE)
        }
    }

    fun checkIfPeriodHits(millis:Long){
            stopwatchViewModel.milestonesLiveData.value
                ?.filter { !it.passed }
                ?.forEach { milestone ->
                    if(LongRange(milestone.period.toStandardDuration().millis-20L, milestone.period.toStandardDuration().millis+20L).contains(millis)){
                        Log.e(TAG, "inRange: ${milestone.period.toStandardDuration().millis} and $millis")
                        stopwatchViewModel.setMilestonePassed(milestone)
                        //TODO: fire notification
                        showNotification(milestone)
                    }
                }

    }

    fun showNotification(milestone: Milestone){
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setContentTitle("Timer")
            .setContentText("${milestone.name} has passed")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(null)
            .setOnlyAlertOnce(true)

        createNotificationChannel()

        with(NotificationManagerCompat.from(this)){
            notify(milestone.period.toStandardDuration().millis.toInt(), builder.build())
        }


    }

    fun createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val name = CHANNEL_NAME
            val descriptionText = CHANNEL_DESC
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    override fun onBackPressed() {
        if(adsViewModel.mInterstitialAd.isLoaded){
            adsViewModel.mInterstitialAd.adListener = object : AdListener() {
                override fun onAdClosed() {
                    finish()
                }
            }
            adsViewModel.mInterstitialAd.show()
        }else{
            super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == TIMER_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            val milestone = data!!.extras!!.getSerializable("milestone") as Milestone
            stopwatchViewModel.addMilestone(milestone)
        }
    }

    companion object{
        const val TAG = "MainActivity"
        const val TIMER_REQUEST_CODE = 1
        const val CHANNEL_ID = "stopwatch_notification_channel"
        const val CHANNEL_NAME = "Stopwatch Channel"
        const val CHANNEL_DESC = "Channel for displaying notification of timers"
    }

}