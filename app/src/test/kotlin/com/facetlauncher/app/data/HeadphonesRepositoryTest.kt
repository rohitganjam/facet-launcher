package com.facetlauncher.app.data

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.AudioDeviceInfoBuilder

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class HeadphonesRepositoryTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val audio = shadowOf(context.getSystemService(AudioManager::class.java))
    private val repository = HeadphonesRepository(context)

    private fun device(type: Int): AudioDeviceInfo = AudioDeviceInfoBuilder.newBuilder().setType(type).build()

    private fun TestScope.idle() {
        advanceUntilIdle()
        shadowOf(Looper.getMainLooper()).idle()
        advanceUntilIdle()
    }

    private fun TestScope.collectInto(results: MutableList<Boolean>) =
        launch { repository.observeHeadphonesPluggedIn().collect { results.add(it) } }

    @Test
    fun `emits false when nothing is connected`() = runTest {
        val results = mutableListOf<Boolean>()
        val job = collectInto(results)
        idle()
        job.cancel()

        assertEquals(listOf(false), results)
    }

    @Test
    fun `emits true immediately when headphones are already plugged in`() = runTest {
        audio.setOutputDevices(listOf(device(AudioDeviceInfo.TYPE_WIRED_HEADPHONES)))

        val results = mutableListOf<Boolean>()
        val job = collectInto(results)
        idle()
        job.cancel()

        assertEquals(listOf(true), results)
    }

    @Test
    fun `follows a headset being plugged in and unplugged`() = runTest {
        val results = mutableListOf<Boolean>()
        val job = collectInto(results)
        idle()

        val headset = device(AudioDeviceInfo.TYPE_WIRED_HEADSET)
        audio.addOutputDevice(headset, true)
        idle()
        audio.removeOutputDevice(headset, true)
        idle()
        job.cancel()

        assertEquals(listOf(false, true, false), results)
    }

    @Test
    fun `usb headsets and bluetooth audio count as headphones`() = runTest {
        listOf(AudioDeviceInfo.TYPE_USB_HEADSET, AudioDeviceInfo.TYPE_BLUETOOTH_A2DP, AudioDeviceInfo.TYPE_BLUETOOTH_SCO).forEach { type ->
            audio.setOutputDevices(listOf(device(type)))
            val results = mutableListOf<Boolean>()
            val job = collectInto(results)
            idle()
            job.cancel()

            assertEquals("type $type", listOf(true), results)
        }
    }

    @Test
    fun `the built-in speaker does not count`() = runTest {
        audio.setOutputDevices(listOf(device(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER)))

        val results = mutableListOf<Boolean>()
        val job = collectInto(results)
        idle()
        job.cancel()

        assertEquals(listOf(false), results)
    }

    @Test
    fun `repeated identical states are not re-emitted`() = runTest {
        val results = mutableListOf<Boolean>()
        val job = collectInto(results)
        idle()

        audio.addOutputDevice(device(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER), true)
        idle()
        job.cancel()

        assertEquals(listOf(false), results)
    }
}
