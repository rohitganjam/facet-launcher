package com.facetlauncher.app.data

import android.Manifest
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.Intent
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.facetlauncher.app.data.model.PairedBluetoothDevice
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class BluetoothRepositoryTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val adapter: BluetoothAdapter = context.getSystemService(BluetoothManager::class.java).adapter
    private val adapterShadow = shadowOf(adapter)
    private val repository = BluetoothRepository(context)

    init {
        adapterShadow.setEnabled(true)
        val app = shadowOf(context as Application)
        app.grantPermissions(Manifest.permission.BLUETOOTH_CONNECT, "${context.packageName}.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION")
    }

    private fun device(address: String): BluetoothDevice = adapter.getRemoteDevice(address)

    private fun TestScope.idle() {
        runCurrent()
        shadowOf(Looper.getMainLooper()).idle()
        runCurrent()
    }

    private fun TestScope.collectInto(results: MutableList<Set<String>?>) =
        launch { repository.observeConnectedAddresses().collect { results.add(it) } }

    private fun profileWith(vararg connected: BluetoothDevice): BluetoothProfile =
        mock(BluetoothProfile::class.java).also { `when`(it.connectedDevices).thenReturn(connected.toList()) }

    private fun answerProfiles(a2dp: BluetoothProfile = profileWith(), headset: BluetoothProfile = profileWith()) {
        adapterShadow.setProfileProxy(BluetoothProfile.A2DP, a2dp)
        adapterShadow.setProfileProxy(BluetoothProfile.HEADSET, headset)
    }

    private fun sendAcl(action: String, address: String) =
        context.sendBroadcast(Intent(action).putExtra(BluetoothDevice.EXTRA_DEVICE, device(address)))

    @Test
    fun `connections are unknown without the bluetooth permission`() = runTest {
        shadowOf(context as Application).denyPermissions(Manifest.permission.BLUETOOTH_CONNECT)

        val results = mutableListOf<Set<String>?>()
        val job = collectInto(results)
        idle()
        job.cancel()

        assertEquals(listOf<Set<String>?>(null), results)
    }

    @Test
    fun `with bluetooth switched off nothing is connected, and that is known at once`() = runTest {
        adapterShadow.setEnabled(false)

        val results = mutableListOf<Set<String>?>()
        val job = collectInto(results)
        idle()
        job.cancel()

        assertEquals(listOf<Set<String>?>(null, emptySet()), results)
    }

    @Test
    fun `stays unknown until the profile queries answer, then reports the connected devices`() = runTest {
        val car = device("AA:BB:CC:DD:EE:01")
        answerProfiles(a2dp = profileWith(car))

        val results = mutableListOf<Set<String>?>()
        val job = collectInto(results)
        idle()
        job.cancel()

        assertEquals(null, results.first())
        assertEquals(setOf("AA:BB:CC:DD:EE:01"), results.last())
    }

    @Test
    fun `a device found on the headset profile counts too`() = runTest {
        answerProfiles(headset = profileWith(device("AA:BB:CC:DD:EE:02")))

        val results = mutableListOf<Set<String>?>()
        val job = collectInto(results)
        idle()
        job.cancel()

        assertEquals(setOf("AA:BB:CC:DD:EE:02"), results.last())
    }

    @Test
    fun `with nothing connected the answer is an empty set, not unknown`() = runTest {
        answerProfiles()

        val results = mutableListOf<Set<String>?>()
        val job = collectInto(results)
        idle()
        job.cancel()

        assertEquals(emptySet<String>(), results.last())
    }

    @Test
    fun `follows connect and disconnect broadcasts once known`() = runTest {
        answerProfiles()
        val results = mutableListOf<Set<String>?>()
        val job = collectInto(results)
        idle()

        sendAcl(BluetoothDevice.ACTION_ACL_CONNECTED, "AA:BB:CC:DD:EE:03")
        idle()
        assertEquals(setOf("AA:BB:CC:DD:EE:03"), results.last())

        sendAcl(BluetoothDevice.ACTION_ACL_DISCONNECTED, "AA:BB:CC:DD:EE:03")
        idle()
        job.cancel()

        assertEquals(emptySet<String>(), results.last())
    }

    @Test
    fun `turning bluetooth off clears every connection`() = runTest {
        answerProfiles(a2dp = profileWith(device("AA:BB:CC:DD:EE:04")))
        val results = mutableListOf<Set<String>?>()
        val job = collectInto(results)
        idle()

        context.sendBroadcast(Intent(BluetoothAdapter.ACTION_STATE_CHANGED).putExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF))
        idle()
        job.cancel()

        assertEquals(emptySet<String>(), results.last())
    }

    @Test
    fun `a profile that never answers is given up on after the timeout`() = runTest {
        // No proxy override: the profile queries start but nothing ever answers them
        val results = mutableListOf<Set<String>?>()
        val job = collectInto(results)
        idle()
        assertEquals("still unknown before the timeout", listOf<Set<String>?>(null), results)

        advanceTimeBy(2_000)
        idle()
        job.cancel()

        assertEquals("known, with nothing connected, after the timeout", emptySet<String>(), results.last())
    }

    @Test
    fun `an unchanged answer is not re-emitted`() = runTest {
        answerProfiles()
        val results = mutableListOf<Set<String>?>()
        val job = collectInto(results)
        idle()
        val before = results.size

        sendAcl(BluetoothDevice.ACTION_ACL_DISCONNECTED, "AA:BB:CC:DD:EE:05")
        idle()
        job.cancel()

        assertEquals(before, results.size)
    }

    // --- paired devices for the picker ---

    @Test
    fun `paired devices are listed by name, falling back to the address`() {
        val zed = device("AA:BB:CC:DD:EE:0A")
        val car = device("AA:BB:CC:DD:EE:0B")
        val nameless = device("AA:BB:CC:DD:EE:0C")
        shadowOf(zed).setName("Zed headset")
        shadowOf(car).setName("car")
        adapterShadow.setBondedDevices(setOf(zed, car, nameless))

        assertEquals(
            listOf(
                PairedBluetoothDevice("AA:BB:CC:DD:EE:0C", "AA:BB:CC:DD:EE:0C"),
                PairedBluetoothDevice("AA:BB:CC:DD:EE:0B", "car"),
                PairedBluetoothDevice("AA:BB:CC:DD:EE:0A", "Zed headset"),
            ),
            repository.pairedDevices(),
        )
    }

    @Test
    fun `no paired devices are listed without the permission`() {
        adapterShadow.setBondedDevices(setOf(device("AA:BB:CC:DD:EE:0D")))
        shadowOf(context as Application).denyPermissions(Manifest.permission.BLUETOOTH_CONNECT)

        assertEquals(emptyList<PairedBluetoothDevice>(), repository.pairedDevices())
    }
}
