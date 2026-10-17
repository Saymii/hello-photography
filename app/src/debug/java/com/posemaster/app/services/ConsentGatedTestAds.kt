package com.posemaster.app.services

import android.app.Activity
import com.google.android.gms.ads.*
import com.google.android.ump.*

/** Opt-in developer harness. Not called from normal screens; no ad request occurs without UMP. */
object ConsentGatedTestAds {
 const val BANNER = "ca-app-pub-3940256099942544/6300978111"
 const val NATIVE = "ca-app-pub-3940256099942544/2247696110"
 const val REWARDED = "ca-app-pub-3940256099942544/5224354917"
 fun requestConsent(activity: Activity, result: (Boolean) -> Unit) {
  val consent = UserMessagingPlatform.getConsentInformation(activity)
  consent.requestConsentInfoUpdate(activity, ConsentRequestParameters.Builder().build(), {
   UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { error ->
    val allowed = error==null && consent.canRequestAds()
    if(allowed) MobileAds.initialize(activity) {}
    result(allowed)
   }
  }, {result(false)})
 }
 fun adaptiveBanner(activity: Activity, widthDp: Int): AdView {
  check(UserMessagingPlatform.getConsentInformation(activity).canRequestAds()) { "Consent required" }
  return AdView(activity).apply { adUnitId=BANNER;setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity,widthDp));loadAd(AdRequest.Builder().build()) }
 }
}
