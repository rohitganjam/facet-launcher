package com.facetlauncher.app.data

import android.content.Context
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

private val HEADPHONE_OUTPUT_TYPES = setOf(
    AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
    AudioDeviceInfo.TYPE_WIRED_HEADSET,
    AudioDeviceInfo.TYPE_USB_HEADSET,
    AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
    AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
    AudioDeviceInfo.TYPE_BLE_HEADSET,
)

/**
 * Whether headphones are plugged in or connected, from the audio output devices: wired headphones or
 * headset, a USB headset, or a Bluetooth audio device. Android doesn't distinguish headphones from other
 * Bluetooth audio (a car stereo or a speaker also counts); a Bluetooth rule is the precise way to match
 * one device. Needs no permission.
 */
@Singleton
class HeadphonesRepository @Inject constructor(@ApplicationContext private val context: Context) {

    private val audioManager: AudioManager? get() = context.getSystemService(AudioManager::class.java)

    /** Emits the current state on subscribe, then on every change. */
    fun observeHeadphonesPluggedIn(): Flow<Boolean> = callbackFlow {
        val manager = audioManager
        if (manager == null) {
            trySend(false)
            awaitClose { }
        } else {
            val callback = object : AudioDeviceCallback() {
                override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>) {
                    trySend(manager.hasHeadphones())
                }

                override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>) {
                    trySend(manager.hasHeadphones())
                }
            }
            manager.registerAudioDeviceCallback(callback, null)
            trySend(manager.hasHeadphones())
            awaitClose { manager.unregisterAudioDeviceCallback(callback) }
        }
    }.distinctUntilChanged()
}

private fun AudioManager.hasHeadphones(): Boolean =
    getDevices(AudioManager.GET_DEVICES_OUTPUTS).any { it.type in HEADPHONE_OUTPUT_TYPES }
