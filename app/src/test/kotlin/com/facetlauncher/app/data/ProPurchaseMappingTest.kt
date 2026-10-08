package com.facetlauncher.app.data

import com.android.billingclient.api.Purchase
import com.facetlauncher.app.data.model.ProQueryResult
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

// Robolectric supplies the real org.json that Purchase parses its JSON with.
@RunWith(RobolectricTestRunner::class)
class ProPurchaseMappingTest {

    private fun purchase(productId: String = "facet_pro", state: Int = 0, acknowledged: Boolean = false) = Purchase(
        """{"orderId":"GPA.1","packageName":"com.facetlauncher.app","productIds":["$productId"],"purchaseTime":1,"purchaseState":$state,"purchaseToken":"token-$productId-$state","acknowledged":$acknowledged}""",
        "signature",
    )

    @Test
    fun `no purchases means not owned`() {
        assertEquals(ProQueryResult.NotOwned, emptyList<Purchase>().toProQueryResult())
    }

    @Test
    fun `a completed pro purchase means owned`() {
        assertEquals(ProQueryResult.Owned, listOf(purchase()).toProQueryResult())
    }

    @Test
    fun `a pending pro purchase is pending, not owned`() {
        assertEquals(ProQueryResult.Pending, listOf(purchase(state = 4)).toProQueryResult())
    }

    @Test
    fun `a purchase of another product does not count`() {
        assertEquals(ProQueryResult.NotOwned, listOf(purchase(productId = "something_else")).toProQueryResult())
    }

    @Test
    fun `a completed purchase wins over a pending one`() {
        assertEquals(ProQueryResult.Owned, listOf(purchase(state = 4), purchase()).toProQueryResult())
    }

    @Test
    fun `only an unacknowledged completed pro purchase needs acknowledging`() {
        val toAcknowledge = purchase()
        val purchases = listOf(toAcknowledge, purchase(acknowledged = true), purchase(state = 4), purchase(productId = "other"))

        assertEquals(listOf(toAcknowledge.purchaseToken), purchases.needingAcknowledgement().map { it.purchaseToken })
    }
}
