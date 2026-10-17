package com.posemaster.app.services

import android.content.Context
import com.android.billingclient.api.*

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
  .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()).enableAutoServiceReconnection().build()
 fun connect(onResult: (Result<List<ProductDetails>>) -> Unit) {
  client.startConnection(object: BillingClientStateListener {
   override fun onBillingServiceDisconnected() { onResult(Result.failure(IllegalStateException("Google Play disconnected"))) }
   override fun onBillingSetupFinished(result: BillingResult) {
    if(result.responseCode != BillingClient.BillingResponseCode.OK) { onResult(Result.failure(IllegalStateException(result.debugMessage))); return }
    val products = listOf(config.monthlyProduct,config.annualProduct).map { QueryProductDetailsParams.Product.newBuilder().setProductId(it).setProductType(BillingClient.ProductType.SUBS).build() }
    client.queryProductDetailsAsync(QueryProductDetailsParams.newBuilder().setProductList(products).build()) { r, details ->
     if(r.responseCode==BillingClient.BillingResponseCode.OK) onResult(Result.success(details.productDetailsList)) else onResult(Result.failure(IllegalStateException(r.debugMessage)))
    }
   }
  })
 }
 fun close() = client.endConnection()
}
