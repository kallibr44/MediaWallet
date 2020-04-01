package com.kallibr44.mediawallet

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import kotlinx.android.synthetic.main.activity_graphic_daily.*

/*

Данный файл является deprecated

*/
data class tx_tick(val ts: Long, val new_balance: Long, val tx_amount: Long)

class graphic_daily : AppCompatActivity() {

    val data_array = mutableListOf<tx_tick>()
    var address = "null"
    var login = "null"
    private lateinit var mInterstitialAd: InterstitialAd
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_graphic_daily)
        explorer_main.settings.javaScriptEnabled = true
        explorer_main.loadUrl("https://explorer.mediacoin.pro")
        mInterstitialAd =  InterstitialAd(this).apply {
            adListener = (object : AdListener() {
                override fun onAdLoaded() {
                    mInterstitialAd.show()
                }

                override fun onAdFailedToLoad(errorCode: Int) {

                }

                override fun onAdClosed() {
                    //none
                }
            })
        }
        mInterstitialAd.setAdUnitId("ca-app-pub-1102059162601993/1132272591")
        mInterstitialAd.loadAd(AdRequest.Builder().build())
        val mAdView = adView
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

    }





}

