package com.facetlauncher.app.data.model

/** The Play product ids of Facet's purchases. The id must match the product created in Play Console. */
object BillingProducts {
    const val PRO = "facet_pro"
}

/** What Play said about the Pro purchase. Only [Owned] and [NotOwned] change the entitlement. */
enum class ProQueryResult {
    /** A completed purchase exists. */
    Owned,

    /** Play answered and there is no purchase (never bought, refunded, or a different Google account). */
    NotOwned,

    /** A purchase is waiting on payment. Not Pro yet. */
    Pending,

    /** Play couldn't answer (not installed, offline, disconnected, an error). Says nothing about ownership. */
    Unavailable,
}
