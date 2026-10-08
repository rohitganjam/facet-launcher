package com.facetlauncher.app.data

import com.android.billingclient.api.Purchase
import com.facetlauncher.app.data.model.BillingProducts
import com.facetlauncher.app.data.model.ProQueryResult

/** Reads Play's list of owned purchases as the answer to "does this user have Pro?". Never [ProQueryResult.Unavailable]: Play did answer. */
internal fun List<Purchase>.toProQueryResult(): ProQueryResult {
    val pro = filter { BillingProducts.PRO in it.products }
    return when {
        pro.any { it.purchaseState == Purchase.PurchaseState.PURCHASED } -> ProQueryResult.Owned
        pro.any { it.purchaseState == Purchase.PurchaseState.PENDING } -> ProQueryResult.Pending
        else -> ProQueryResult.NotOwned
    }
}

/** The completed Pro purchases Play hasn't been told we received yet; unacknowledged ones are refunded after 3 days. */
internal fun List<Purchase>.needingAcknowledgement(): List<Purchase> =
    filter { BillingProducts.PRO in it.products && it.purchaseState == Purchase.PurchaseState.PURCHASED && !it.isAcknowledged }
