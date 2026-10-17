package com.posemaster.app.services

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.google.android.gms.ads.*
import com.google.android.ump.*

/** Only a trusted backend may grant an entitlement. Never persist a client boolean as proof. */
data class Entitlement(val premium: Boolean, val expiresAtEpochMillis: Long)
interface PurchaseVerificationBackend {
 suspend fun verifyAndAcknowledge(productId: String, purchaseToken: String): Result<Entitlement>
 suspend fun currentEntitlement(): Result<Entitlement>
}
/** Configuration is intentionally absent: no fabricated prices, purchases, or cloud endpoints. */
data class BillingConfiguration(val monthlyProduct: String, val annualProduct: String)
class PlayCatalog(context: Context, private val config: BillingConfiguration) {
 private val client = BillingClient.newBuilder(context).setListener { _, _ -> }
  .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()).build()
 fun connect(onResult: (Result<List<ProductDetails>>) -> Unit) {
  client.startConnection(object: BillingClientStateListener {
   override fun onBillingServiceDisconnected() { onResult(Result.failure(IllegalStateException("Google Play disconnected"))) }
   override fun onBillingSetupFinished(result: BillingResult) {
    if(result.responseCode != BillingClient.BillingResponseCode.OK) { onResult(Result.failure(IllegalStateException(result.debugMessage))); return }
    val products = listOf(config.monthlyProduct,config.annualProduct).map { QueryProductDetailsParams.Product.newBuilder().setProductId(it).setProductType(BillingClient.ProductType.SUBS).build() }
    client.queryProductDetailsAsync(QueryProductDetailsParams.newBuilder().setProductList(products).build()) { r, details ->
     if(r.responseCode==BillingClient.BillingResponseCode.OK) onResult(Result.success(details)) else onResult(Result.failure(IllegalStateException(r.debugMessage)))
    }
   }
  })
 }
 fun close() = client.endConnection()
}
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
