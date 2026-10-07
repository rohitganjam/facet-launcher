package com.facetlauncher.app.data.model

/** A paired Bluetooth device the user can pick for a rule; [name] falls back to the address for a device with none. */
data class PairedBluetoothDevice(
    val address: String,
    val name: String,
)
