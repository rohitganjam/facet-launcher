package com.lumenlauncher.app.data.model

/** Live battery reading from [com.lumenlauncher.app.data.BatteryRepository] — the clock's battery accessory item. */
data class BatteryStatus(
    val percent: Int,
    val isCharging: Boolean,
)
