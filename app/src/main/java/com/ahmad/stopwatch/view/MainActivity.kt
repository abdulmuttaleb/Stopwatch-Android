package com.ahmad.stopwatch.view

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.ahmad.stopwatch.R
import com.ahmad.stopwatch.utils.Constants
import com.ahmad.stopwatch.viewmodel.AdsViewModel
import com.ahmad.stopwatch.viewmodel.AdsViewModelFactory
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
import org.joda.time.Period
import org.joda.time.format.PeriodFormatterBuilder
import java.util.*
import kotlin.concurrent.fixedRateTimer

class MainActivity : AppCompatActivity() {

    lateinit var timerTextView: TextView
    lateinit var playPauseMaterialButton: MaterialButton
    lateinit var resetMaterialButton: MaterialButton

    lateinit var stopwatchViewModel: StopwatchViewModel
    lateinit var adsViewModel: AdsViewModel

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


        stopwatchViewModel = ViewModelProvider(this, StopwatchViewModelFactory(application)).get(StopwatchViewModel::class.java)
        adsViewModel = ViewModelProvider(this, AdsViewModelFactory(application, this)).get(AdsViewModel::class.java)

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
            Log.e(TAG, "numberOfTimesUsed -> ${stopwatchViewModel.numberOfTimesUsed}")
            if(stopwatchViewModel.numberOfTimesUsed %3 == 0){
                if (adsViewModel.mInterstitialAd.isLoaded) {
                    adsViewModel.mInterstitialAd.show()
                }
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
