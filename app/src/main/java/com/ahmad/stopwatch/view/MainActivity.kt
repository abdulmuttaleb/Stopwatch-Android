package com.ahmad.stopwatch.view

import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Chronometer
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.ahmad.stopwatch.R
import com.ahmad.stopwatch.service.NotificationService
import com.ahmad.stopwatch.viewmodel.StopwatchViewModel
import com.ahmad.stopwatch.viewmodel.StopwatchViewModelFactory
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdCallback
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.material.button.MaterialButton
import org.joda.time.Duration
import org.joda.time.Period
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.PeriodFormatterBuilder
import java.lang.String.format
import java.text.DateFormat
import java.text.MessageFormat.format
import java.text.SimpleDateFormat
import java.util.*

const val INTER_AD_UNIT_ID = "ca-app-pub-9620521272164745/3440606585"
const val REWARDED_AD_UNIT_ID = "ca-app-pub-9620521272164745/4073255304"


class MainActivity : AppCompatActivity() {

    lateinit var timerTextView: TextView
    lateinit var playPauseMaterialButton: MaterialButton
    lateinit var resetMaterialButton: MaterialButton

    lateinit var stopwatchViewModel: StopwatchViewModel

    private lateinit var afterTimerInterstitialAd: InterstitialAd
    private lateinit var afterTimerRewardedAd: RewardedAd
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

       activityAdInit()

        stopwatchViewModel = ViewModelProvider(this, StopwatchViewModelFactory(application)).get(StopwatchViewModel::class.java)

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
            val time = periodFormatter.print(period)
            timerTextView.text = time
        })

        playPauseMaterialButton.setOnClickListener {
            stopwatchViewModel.run()
        }

        resetMaterialButton.setOnClickListener {
            stopwatchViewModel.stop()
            stopwatchViewModel.numberOfTimesUsed += 1
            if(stopwatchViewModel.numberOfTimesUsed == 2){
                Log.e(TAG, "numberOfTimesUsed -> ${stopwatchViewModel.numberOfTimesUsed}")
                if(afterTimerInterstitialAd.isLoaded){
                    afterTimerInterstitialAd.show()
                }else{
                    stopwatchViewModel.numberOfTimesUsed = 0
                    afterTimerInterstitialAd.loadAd(AdRequest.Builder().build())
                }
            }
        }
    }

    private fun activityAdInit(){
        //init admob vars
        MobileAds.initialize(this){}
        //interstitial ad setup
        afterTimerInterstitialAd = InterstitialAd(this).apply {
            adUnitId = INTER_AD_UNIT_ID

            adListener = object : AdListener(){
                override fun onAdClosed() {
                    afterTimerInterstitialAd.loadAd(AdRequest.Builder().build())
                    stopwatchViewModel.numberOfTimesUsed = 0
                }

                override fun onAdLoaded() {
                    Log.e(TAG, "Ad loaded")
                }

                override fun onAdFailedToLoad(errorCode: Int) {
                    when(errorCode){
                        AdRequest.ERROR_CODE_INTERNAL_ERROR ->{
                            Log.e(TAG, "adLoadFailed -> Something happened internally; for instance, an invalid response was received from the ad server.")
                        }
                        AdRequest.ERROR_CODE_INVALID_REQUEST -> {
                            Log.e(TAG, "adLoadFailed ->  The ad request was invalid; for instance, the ad unit ID was incorrect.")
                        }
                        AdRequest.ERROR_CODE_NETWORK_ERROR -> {
                            Log.e(TAG, "adLoadFailed -> The ad request was unsuccessful due to network connectivity.")
                        }
                        AdRequest.ERROR_CODE_NO_FILL -> {
                            Log.e(TAG, "adLoadFailed -> The ad request was successful, but no ad was returned due to lack of ad inventory.")
                        }
                    }
                }
            }
        }
        afterTimerInterstitialAd.loadAd(AdRequest.Builder().build())

        //rewarded video setup
//        afterTimerRewardedAd = createAndLoadRewardedAd()
    }

    private fun createAndLoadRewardedAd(): RewardedAd{
        val rewardedAd = RewardedAd(this, REWARDED_AD_UNIT_ID)
        val adLoadCallback = object: RewardedAdLoadCallback(){
            override fun onRewardedAdLoaded() {
                Log.e(TAG, "rewardedAd: loaded successfully")
            }

            override fun onRewardedAdFailedToLoad(errorCode: Int) {
                when(errorCode){
                    AdRequest.ERROR_CODE_INTERNAL_ERROR ->{
                        Log.e(TAG, "rewardedAdLoadFailed -> Something happened internally; for instance, an invalid response was received from the ad server.")
                    }
                    AdRequest.ERROR_CODE_INVALID_REQUEST -> {
                        Log.e(TAG, "rewardedAdLoadFailed ->  The ad request was invalid; for instance, the ad unit ID was incorrect.")
                    }
                    AdRequest.ERROR_CODE_NETWORK_ERROR -> {
                        Log.e(TAG, "rewardedAdLoadFailed -> The ad request was unsuccessful due to network connectivity.")
                    }
                    AdRequest.ERROR_CODE_NO_FILL -> {
                        Log.e(TAG, "rewardedAdLoadFailed -> The ad request was successful, but no ad was returned due to lack of ad inventory.")
                    }
                }
            }
        }

        rewardedAd.loadAd(AdRequest.Builder().build(), adLoadCallback)

        return rewardedAd
    }

    private fun showRewardedAd(){
        if(afterTimerRewardedAd.isLoaded){
            val adCallBack = object: RewardedAdCallback(){
                override fun onUserEarnedReward(reward: RewardItem) {
                    Log.e(TAG, "rewardedAdEarnedReward")
                }
                override fun onRewardedAdOpened() {
                    Log.e(TAG, "rewardedAdOpened")
                }
                override fun onRewardedAdClosed() {
                    afterTimerRewardedAd = createAndLoadRewardedAd()
                }
                override fun onRewardedAdFailedToShow(errorCode: Int) {
                    when(errorCode){
                        ERROR_CODE_AD_REUSED ->{
                            Log.e(TAG, "rewardedAdFailedToShow -> The rewarded ad has already been shown")
                        }
                        ERROR_CODE_INTERNAL_ERROR->{
                            Log.e(TAG, "rewardedAdFailedToShow -> Something happened internally.")
                        }
                        ERROR_CODE_NOT_READY -> {
                            Log.e(TAG, "rewardedAdFailedToShow -> The ad has not been successfully loaded.")
                        }
                        ERROR_CODE_APP_NOT_FOREGROUND ->{
                            Log.e(TAG, "rewardedAdFailedToShow -> The ad can not be shown when the app is not in foreground.")
                        }
                    }
                }
            }

            afterTimerRewardedAd.show(this, adCallBack)
        }
    }

    companion object{
        const val TAG = "MainActivity"

        val periodFormatter =
            PeriodFormatterBuilder()
                .printZeroAlways()
                .minimumPrintedDigits(2)
                .appendHours()
                .appendSeparator(":")
                .appendMinutes()
                .appendSeparator(":")
                .appendSeconds()
                .appendSeparator(":")
                .appendMillis3Digit()
                .toFormatter()!!
    }
}
