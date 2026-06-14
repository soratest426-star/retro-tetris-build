package com.yosebooster.racingrush.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "AdManager"

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null

    private val interstitialId = "ca-app-pub-2046756431875834/5486199349"
    private val rewardedId = "ca-app-pub-2046756431875834/4852945380"

    private var isInterstitialLoading = false
    private var isRewardedLoading = false

    fun loadInterstitialAd() {
        if (interstitialAd != null || isInterstitialLoading) return
        
        isInterstitialLoading = true
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context, interstitialId, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e(TAG, "Interstitial failed to load: ${adError.message}")
                interstitialAd = null
                isInterstitialLoading = false
            }

            override fun onAdLoaded(ad: InterstitialAd) {
                Log.d(TAG, "Interstitial Ad was loaded.")
                interstitialAd = ad
                isInterstitialLoading = false
            }
        })
    }

    fun showInterstitialAd(activity: Activity, onAdClosed: () -> Unit) {
        if (interstitialAd != null) {
            interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Interstitial Ad dismissed.")
                    interstitialAd = null
                    loadInterstitialAd()
                    onAdClosed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e(TAG, "Interstitial failed to show: ${adError.message}")
                    interstitialAd = null
                    onAdClosed()
                }
            }
            interstitialAd?.show(activity)
        } else {
            Log.w(TAG, "The interstitial ad wasn't ready yet.")
            loadInterstitialAd()
            onAdClosed()
        }
    }

    fun loadRewardedAd() {
        if (rewardedAd != null || isRewardedLoading) return

        isRewardedLoading = true
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(context, rewardedId, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e(TAG, "Rewarded ad failed to load: ${adError.message}")
                rewardedAd = null
                isRewardedLoading = false
            }

            override fun onAdLoaded(ad: RewardedAd) {
                Log.d(TAG, "Rewarded Ad was loaded.")
                rewardedAd = ad
                isRewardedLoading = false
            }
        })
    }

    fun showRewardedAd(activity: Activity, onRewardEarned: () -> Unit, onAdClosed: () -> Unit) {
        if (rewardedAd != null) {
            var earnedReward = false
            rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Rewarded Ad dismissed.")
                    rewardedAd = null
                    loadRewardedAd()
                    if (earnedReward) {
                        onRewardEarned()
                    }
                    onAdClosed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e(TAG, "Rewarded ad failed to show: ${adError.message}")
                    rewardedAd = null
                    onAdClosed()
                }
            }
            
            rewardedAd?.show(activity, OnUserEarnedRewardListener {
                Log.d(TAG, "User earned reward.")
                earnedReward = true
            })
        } else {
            Log.w(TAG, "The rewarded ad wasn't ready yet.")
            loadRewardedAd()
            onAdClosed()
        }
    }

    fun showRewardDoublerAd(activity: Activity, onResult: () -> Unit) {
        showRewardedAd(
            activity = activity,
            onRewardEarned = {
                onResult()
            },
            onAdClosed = {}
        )
    }

    fun showContinueGameAd(activity: Activity, onCanContinue: () -> Unit) {
        showRewardedAd(
            activity = activity,
            onRewardEarned = {
                onCanContinue()
            },
            onAdClosed = {}
        )
    }
}
