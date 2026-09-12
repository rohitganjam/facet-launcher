package com.lumenlauncher.app.data

import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.lumenlauncher.app.data.model.BatteryStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class BatteryRepositoryTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val repository = BatteryRepository(context)

    private fun batteryIntent(level: Int, scale: Int, status: Int) =
        Intent(Intent.ACTION_BATTERY_CHANGED)
            .putExtra(BatteryManager.EXTRA_LEVEL, level)
            .putExtra(BatteryManager.EXTRA_SCALE, scale)
            .putExtra(BatteryManager.EXTRA_STATUS, status)

    private fun TestScope.idle() {
        advanceUntilIdle()
        shadowOf(Looper.getMainLooper()).idle()
        advanceUntilIdle()
    }

    @Test
    fun `emits the sticky battery state immediately on subscribe`() = runTest {
        // Given a sticky ACTION_BATTERY_CHANGED broadcast already present, discharging at 64%
        context.sendStickyBroadcast(batteryIntent(level = 64, scale = 100, status = BatteryManager.BATTERY_STATUS_DISCHARGING))

        // When observing battery status
        val results = mutableListOf<BatteryStatus>()
        val job = launch { repository.observeBatteryStatus().collect { results.add(it) } }
        idle()
        job.cancel()

        // Then the sticky state is emitted without waiting for a further broadcast (Robolectric's
        // shadow can deliver the sticky value both as registerReceiver's return value and as a
        // queued onReceive callback, so this tolerates one or more identical emissions rather than
        // pinning an exact count real Android wouldn't guarantee either).
        assertTrue(results.isNotEmpty())
        assertTrue(results.all { it == BatteryStatus(percent = 64, isCharging = false) })
    }

    @Test
    fun `re-emits when the battery state changes`() = runTest {
        // Given no sticky state yet, an active subscriber
        val results = mutableListOf<BatteryStatus>()
        val job = launch { repository.observeBatteryStatus().collect { results.add(it) } }
        idle()

        // When a new battery broadcast arrives, charging at 50%
        context.sendBroadcast(batteryIntent(level = 50, scale = 100, status = BatteryManager.BATTERY_STATUS_CHARGING))
        idle()
        job.cancel()

        // Then the new reading is emitted
        assertEquals(BatteryStatus(percent = 50, isCharging = true), results.last())
    }

    @Test
    fun `BATTERY_STATUS_FULL counts as charging`() = runTest {
        // Given a subscriber
        val results = mutableListOf<BatteryStatus>()
        val job = launch { repository.observeBatteryStatus().collect { results.add(it) } }
        idle()

        // When the battery reports full
        context.sendBroadcast(batteryIntent(level = 100, scale = 100, status = BatteryManager.BATTERY_STATUS_FULL))
        idle()
        job.cancel()

        // Then it's still treated as charging (accent color / not-low state)
        assertEquals(BatteryStatus(percent = 100, isCharging = true), results.last())
    }
}
