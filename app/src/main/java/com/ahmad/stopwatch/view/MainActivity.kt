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
import com.ahmad.stopwatch.viewmodel.StopwatchViewModel
import com.ahmad.stopwatch.viewmodel.StopwatchViewModelFactory
import com.appodeal.ads.Appodeal
import com.appodeal.ads.InterstitialCallbacks
import com.appodeal.ads.NonSkippableVideoCallbacks
import com.google.android.material.button.MaterialButton
import org.joda.time.Period
import org.joda.time.format.PeriodFormatterBuilder

class MainActivity : AppCompatActivity() {

    lateinit var timerTextView: TextView
    lateinit var playPauseMaterialButton: MaterialButton
    lateinit var resetMaterialButton: MaterialButton

    lateinit var stopwatchViewModel: StopwatchViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        appodealInit()

        activityInit()

    }

    private fun appodealInit(){
        val adTypes =  Appodeal.NON_SKIPPABLE_VIDEO or Appodeal.INTERSTITIAL
        val appodealApiKey = getString(R.string.appodeal_key)
        Appodeal.disableLocationPermissionCheck()
        Appodeal.initialize(this, appodealApiKey, adTypes )

        //callbacks
        Appodeal.setNonSkippableVideoCallbacks(object : NonSkippableVideoCallbacks {
            override fun onNonSkippableVideoLoaded(p0: Boolean) {
                Log.e(TAG,"nonSkippable: video loaded")
            }

            override fun onNonSkippableVideoClosed(p0: Boolean) {
                Log.e(TAG,"nonSkippable: video closed")
            }

            override fun onNonSkippableVideoShowFailed() {
                Log.e(TAG,"nonSkippable: video show failed")
            }

            override fun onNonSkippableVideoExpired() {
                Log.e(TAG,"nonSkippable: video expired")
            }

            override fun onNonSkippableVideoFinished() {
                Log.e(TAG,"nonSkippable: video finished")
                stopwatchViewModel.lastAdTypeShown = Appodeal.NON_SKIPPABLE_VIDEO
            }

            override fun onNonSkippableVideoShown() {
                Log.e(TAG,"nonSkippable: video shown")
            }

            override fun onNonSkippableVideoFailedToLoad() {
                Log.e(TAG,"nonSkippable: video failed to load")
            }
        })

        Appodeal.setInterstitialCallbacks(object : InterstitialCallbacks {
            override fun onInterstitialLoaded(p0: Boolean) {
                Log.e(TAG, "interstitialCallback: loaded")
            }

            override fun onInterstitialShown() {
                Log.e(TAG, "interstitialCallback: shown")
                stopwatchViewModel.lastAdTypeShown = Appodeal.INTERSTITIAL
            }

            override fun onInterstitialShowFailed() {
                Log.e(TAG, "interstitialCallback: show failed")
            }

            override fun onInterstitialClicked() {
                Log.e(TAG, "interstitialCallback: clicked")
            }

            override fun onInterstitialFailedToLoad() {
                Log.e(TAG, "interstitialCallback: failed to load")
            }

            override fun onInterstitialClosed() {
                Log.e(TAG, "interstitialCallback: closed")
            }

            override fun onInterstitialExpired() {
                Log.e(TAG, "interstitialCallback: expired")
            }
        })
    }
    private fun activityInit(){
        timerTextView = findViewById(R.id.tv_timer)
        playPauseMaterialButton = findViewById(R.id.btn_play_pause)
        resetMaterialButton = findViewById(R.id.btn_reset)

//       activityAdInit()

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
            Log.e(TAG, "numberOfTimesUsed -> ${stopwatchViewModel.numberOfTimesUsed}")
            if(stopwatchViewModel.numberOfTimesUsed %2 == 0){
                if(stopwatchViewModel.lastAdTypeShown == null || stopwatchViewModel.lastAdTypeShown == Appodeal.NON_SKIPPABLE_VIDEO) {
                    if (Appodeal.isLoaded(Appodeal.INTERSTITIAL)) {
                        Appodeal.show(this, Appodeal.INTERSTITIAL)
                    }
                }else if(stopwatchViewModel.lastAdTypeShown == Appodeal.INTERSTITIAL){
                    if(Appodeal.isLoaded(Appodeal.NON_SKIPPABLE_VIDEO)){
                        Appodeal.show(this, Appodeal.NON_SKIPPABLE_VIDEO)
                    }
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
