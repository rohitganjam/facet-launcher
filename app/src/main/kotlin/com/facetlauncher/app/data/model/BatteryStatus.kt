package com.facetlauncher.app.data.model

/** Live battery reading from [com.facetlauncher.app.data.BatteryRepository] — the clock's battery accessory item. */
data class BatteryStatus(
    val percent: Int,
    val isCharging: Boolean,
)
