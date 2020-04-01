package com.kallibr44.mediawallet

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_tx_full_item.*
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.InterstitialAd
import android.view.View
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


class tx_full_item : AppCompatActivity() {

    private lateinit var mInterstitialAd: InterstitialAd
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tx_full_item)
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
        //загрузка рекламного блока
        mInterstitialAd.setAdUnitId("ca-app-pub-1102059162601993/1132272591")
        mInterstitialAd.loadAd(AdRequest.Builder().build())
        val mAdView = adView
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
        //загрузка рекламного блока
        val tx_id = intent.getStringExtra("tx_id")
        explorer.settings.javaScriptEnabled = true
        explorer.loadUrl("https://explorer.mediacoin.pro/tx/$tx_id")
    }
}
