package com.facetlauncher.app.data

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import com.facetlauncher.app.data.di.ApplicationScope
import com.facetlauncher.app.data.model.BillingProducts
import com.facetlauncher.app.data.model.ProQueryResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/** How a request to start the purchase flow went. The outcome of the purchase itself arrives on [BillingRepository.updates]. */
enum class PurchaseLaunch { Started, Unavailable }

/**
 * A thin wrapper over the Play Billing client for the one-time Pro product. Everything here talks to the
 * Play Store app over IPC; the app itself needs no network permission. `open` so tests can substitute it;
 * [EntitlementRepository] is the only thing that turns its answers into entitlement.
 *
 * Purchases aren't verified against a signature: there is no backend, and one low-priced product doesn't
 * justify embedding a key that a determined user could patch around anyway.
 */
@Singleton
open class BillingRepository @Inject constructor(
    @ApplicationContext context: Context,
    @ApplicationScope private val scope: CoroutineScope,
) {
    private val updatesFlow = MutableSharedFlow<ProQueryResult>(extraBufferCapacity = UPDATES_BUFFER)
    private val connectMutex = Mutex()

    /** Results of purchases that finish or change outside a query: a purchase made in the app, one redeemed elsewhere, a pending one completing. */
    open val updates: Flow<ProQueryResult> = updatesFlow

    private val client: BillingClient = BillingClient.newBuilder(context)
        .setListener { result, purchases -> onPurchasesUpdated(result, purchases.orEmpty()) }
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()

    /** Asks Play what this account owns. [ProQueryResult.Unavailable] if Play can't answer; that must never be read as "not owned". */
    open suspend fun queryPro(): ProQueryResult {
        if (!ensureConnected()) {
            Log.i(TAG, "queryPro: could not connect to Play")
            return ProQueryResult.Unavailable
        }
        val owned = client.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build())
        Log.i(TAG, "queryPro: code=${owned.billingResult.responseCode} purchases=${owned.purchasesList.map { "${it.products} state=${it.purchaseState} acknowledged=${it.isAcknowledged}" }}")
        return if (owned.billingResult.isOk()) handle(owned.purchasesList) else ProQueryResult.Unavailable
    }

    /** The price as Play formats it for this user, or null when Play can't say (so the UI shows no price rather than a wrong one). */
    open suspend fun priceText(): String? {
        if (!ensureConnected()) return null
        return queryProduct()?.oneTimePurchaseOfferDetails?.formattedPrice
    }

    /** Opens Play's purchase sheet over [activity]. */
    open suspend fun launchPurchase(activity: Activity): PurchaseLaunch {
        val details = if (ensureConnected()) queryProduct() else null
        val params = details?.let {
            BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(it).build()))
                .build()
        }
        val started = params != null && client.launchBillingFlow(activity, params).isOk()
        return if (started) PurchaseLaunch.Started else PurchaseLaunch.Unavailable
    }

    private suspend fun queryProduct() = client.queryProductDetails(
        QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(BillingProducts.PRO)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build(),
                ),
            )
            .build(),
    ).productDetailsList?.firstOrNull()

    private fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>) {
        scope.launch {
            when (result.responseCode) {
                BillingClient.BillingResponseCode.OK -> updatesFlow.emit(handle(purchases))
                // Play says we already own it: ask what we own instead of guessing.
                BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> updatesFlow.emit(queryPro())
                // A cancelled or failed purchase changes nothing.
                else -> Unit
            }
        }
    }

    private suspend fun handle(purchases: List<Purchase>): ProQueryResult {
        purchases.needingAcknowledgement().forEach { purchase ->
            client.acknowledgePurchase(AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build())
        }
        return purchases.toProQueryResult()
    }

    private suspend fun ensureConnected(): Boolean = connectMutex.withLock {
        client.isReady || suspendCancellableCoroutine { continuation ->
            client.startConnection(
                object : BillingClientStateListener {
                    override fun onBillingSetupFinished(result: BillingResult) {
                        Log.i(TAG, "setup: code=${result.responseCode} ${result.debugMessage}")
                        if (continuation.isActive) continuation.resume(result.isOk())
                    }

                    override fun onBillingServiceDisconnected() = Unit
                },
            )
        }
    }

    private fun BillingResult.isOk() = responseCode == BillingClient.BillingResponseCode.OK

    private companion object {
        const val TAG = "FacetBilling"
        const val UPDATES_BUFFER = 8
    }
}
