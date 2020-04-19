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

const val AD_UNIT_ID = "ca-app-pub-9620521272164745/3440606585"

class MainActivity : AppCompatActivity() {

    lateinit var timerTextView: TextView
    lateinit var playPauseMaterialButton: MaterialButton
    lateinit var resetMaterialButton: MaterialButton

    lateinit var stopwatchViewModel: StopwatchViewModel

    private lateinit var afterTimerInterstitialAd: InterstitialAd

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

        //init admob vars
        MobileAds.initialize(this){}
        afterTimerInterstitialAd = InterstitialAd(this).apply {
            adUnitId = AD_UNIT_ID
            adListener = object : AdListener(){
                override fun onAdClosed() {
                    afterTimerInterstitialAd.loadAd(AdRequest.Builder().build())
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
            if(afterTimerInterstitialAd.isLoaded){
                afterTimerInterstitialAd.show()
            }
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
