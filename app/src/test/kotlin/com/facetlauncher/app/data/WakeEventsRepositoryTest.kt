package com.facetlauncher.app.data

import android.app.Application
import android.content.Context
import android.content.Intent
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

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class WakeEventsRepositoryTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val repository = WakeEventsRepository(context)

    init {
        // On API 31-32 AndroidX's RECEIVER_NOT_EXPORTED shim needs this app-signature permission. The
        // shipped manifest declares and requests it (merged in from androidx.core); Robolectric doesn't grant it.
        shadowOf(context as Application).grantPermissions("${context.packageName}.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION")
    }

    private fun TestScope.idle() {
        advanceUntilIdle()
        shadowOf(Looper.getMainLooper()).idle()
        advanceUntilIdle()
    }

    private fun TestScope.eventsAfter(vararg actions: String): Int {
        var count = 0
        val job = launch { repository.observeWakeEvents().collect { count++ } }
        idle()
        actions.forEach { context.sendBroadcast(Intent(it)) }
        idle()
        job.cancel()
        return count
    }

    @Test
    fun `screen on emits a wake event`() = runTest {
        assertEquals(1, eventsAfter(Intent.ACTION_SCREEN_ON))
    }

    @Test
    fun `unlock emits a wake event`() = runTest {
        assertEquals(1, eventsAfter(Intent.ACTION_USER_PRESENT))
    }

    @Test
    fun `a time or timezone change emits a wake event`() = runTest {
        assertEquals(2, eventsAfter(Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED))
    }

    @Test
    fun `an unrelated broadcast emits nothing`() = runTest {
        assertEquals(0, eventsAfter(Intent.ACTION_BATTERY_LOW))
    }

    @Test
    fun `nothing is emitted after the collector is cancelled`() = runTest {
        // Given a collector that has been cancelled
        var count = 0
        val job = launch { repository.observeWakeEvents().collect { count++ } }
        idle()
        job.cancel()
        idle()

        // When a wake broadcast arrives
        context.sendBroadcast(Intent(Intent.ACTION_SCREEN_ON))
        idle()

        // Then the receiver has been unregistered and nothing was counted
        assertEquals(0, count)
    }
}
