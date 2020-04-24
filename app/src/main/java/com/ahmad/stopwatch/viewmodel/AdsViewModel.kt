package com.ahmad.stopwatch.viewmodel

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import com.ahmad.stopwatch.utils.Constants
import com.google.android.gms.ads.*
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdCallback
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import java.util.*
import kotlin.concurrent.fixedRateTimer

class AdsViewModel(application: Application, val activity: AppCompatActivity): AndroidViewModel(application) {

    lateinit var rewardedAdsFixedRateTimer: Timer

    //admob vars
    lateinit var mInterstitialAd: InterstitialAd
    lateinit var mRewardedAd: RewardedAd
    lateinit var adView:AdView

    private val interstitialId = Constants.ADMOB_INTERSTITIAL
    private val rewardedId = Constants.ADMOB_REWARDED
    private val bannerId = Constants.ADMOB_BANNER

    private val context = application.applicationContext
    init {
        activityAdInit()
    }

    private fun activityAdInit(){
        //init admob vars
        MobileAds.initialize(context){}
        //interstitial ad setup
        mInterstitialAd = InterstitialAd(context).apply {
            adUnitId = interstitialId

            adListener = object : AdListener(){
                override fun onAdClosed() {
                    mInterstitialAd.loadAd(AdRequest.Builder().build())
                }

                override fun onAdLoaded() {
                    Log.e(TAG, "interstitialAd: Ad loaded")
                }

                override fun onAdFailedToLoad(errorCode: Int) {
                    when(errorCode){
                        AdRequest.ERROR_CODE_INTERNAL_ERROR ->{
                            Log.e(TAG, "InterstitialLoadFailed -> Something happened internally; for instance, an invalid response was received from the ad server.")
                        }
                        AdRequest.ERROR_CODE_INVALID_REQUEST -> {
                            Log.e(TAG, "InterstitialLoadFailed ->  The ad request was invalid; for instance, the ad unit ID was incorrect.")
                        }
                        AdRequest.ERROR_CODE_NETWORK_ERROR -> {
                            Log.e(TAG, "InterstitialLoadFailed -> The ad request was unsuccessful due to network connectivity.")
                        }
                        AdRequest.ERROR_CODE_NO_FILL -> {
                            Log.e(TAG, "InterstitialLoadFailed -> The ad request was successful, but no ad was returned due to lack of ad inventory.")
                        }
                    }
                }
            }
        }
        mInterstitialAd.loadAd(AdRequest.Builder().build())

        //rewarded video setup
        mRewardedAd = createAndLoadRewardedAd()

        rewardedAdsFixedRateTimer = fixedRateTimer("rewardedAdTimer",false,0,3*60*1000){
            activity.runOnUiThread {
                showRewardedAd()
            }
        }
    }

    private fun createAndLoadRewardedAd(): RewardedAd{
        val rewardedAd = RewardedAd(context, rewardedId)
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
        if(mRewardedAd.isLoaded){
            val adCallBack = object: RewardedAdCallback(){
                override fun onUserEarnedReward(reward: RewardItem) {
                    Log.e(TAG, "rewardedAdEarnedReward")
                }
                override fun onRewardedAdOpened() {
                    Log.e(TAG, "rewardedAdOpened")
                }
                override fun onRewardedAdClosed() {
                    mRewardedAd = createAndLoadRewardedAd()
                }
                override fun onRewardedAdFailedToShow(errorCode: Int) {
                    when(errorCode){
                        ERROR_CODE_AD_REUSED ->{
                            Log.e(TAG, "rewardedAdFailedToShow -> The rewarded ad has already been shown")
                        }
                        ERROR_CODE_INTERNAL_ERROR ->{
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

            mRewardedAd.show(activity, adCallBack)
        }
    }

    fun setViewModelAdView(adView: AdView){
        this.adView = adView
        adView.loadAd(AdRequest.Builder().build())
    }

    companion object{
        const val TAG = "AdsViewModel"
    }
}