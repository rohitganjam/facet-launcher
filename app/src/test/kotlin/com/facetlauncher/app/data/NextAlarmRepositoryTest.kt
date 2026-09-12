package com.facetlauncher.app.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.ResolveInfo
import android.os.Looper
import android.provider.AlarmClock
import androidx.test.core.app.ApplicationProvider
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class NextAlarmRepositoryTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val alarmManager = context.getSystemService(AlarmManager::class.java)
    private val fixedNow = Instant.ofEpochMilli(1_700_000_000_000L)
    private val clock = Clock.fixed(fixedNow, ZoneOffset.UTC)
    private val repository = NextAlarmRepository(context, clock)

    private fun setNextAlarm(triggerTime: Long) {
        val showIntent = PendingIntent.getBroadcast(context, 0, Intent("show"), PendingIntent.FLAG_IMMUTABLE)
        alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(triggerTime, showIntent), showIntent)
    }

    /** Makes [packageName] the package that resolves [AlarmClock.ACTION_SHOW_ALARMS] — i.e. the device's "actual alarm app" the repository compares triggers against. */
    private fun setAlarmAppPackage(packageName: String) {
        val resolveInfo = ResolveInfo().apply {
            activityInfo = ActivityInfo().apply {
                name = "AlarmActivity"
                this.packageName = packageName
                applicationInfo = ApplicationInfo().apply { this.packageName = packageName }
            }
        }
        shadowOf(context.packageManager).setResolveInfosForIntent(Intent(AlarmClock.ACTION_SHOW_ALARMS), listOf(resolveInfo))
    }

    private fun TestScope.idle() {
        advanceUntilIdle()
        shadowOf(Looper.getMainLooper()).idle()
        advanceUntilIdle()
    }

    @Test
    fun `emits the current next alarm immediately on subscribe`() = runTest {
        // Given an alarm already set before anyone subscribes
        setNextAlarm(123_000L)

        // When observing the next alarm
        val results = mutableListOf<Long?>()
        val job = launch { repository.observeNextAlarmMillis().collect { results.add(it) } }
        idle()
        job.cancel()

        // Then its trigger time is emitted without waiting for a broadcast
        assertEquals(listOf(123_000L), results)
    }

    @Test
    fun `emits null when no alarm is set`() = runTest {
        // Given no alarm set at all
        // When observing the next alarm
        val results = mutableListOf<Long?>()
        val job = launch { repository.observeNextAlarmMillis().collect { results.add(it) } }
        idle()
        job.cancel()

        // Then it emits null, not a stale/default value
        assertEquals(listOf<Long?>(null), results)
        assertNull(results.first())
    }

    @Test
    fun `re-queries and re-emits when the system's next-alarm broadcast fires`() = runTest {
        // Given an active subscriber with no alarm set yet
        val results = mutableListOf<Long?>()
        val job = launch { repository.observeNextAlarmMillis().collect { results.add(it) } }
        idle()

        // When a new alarm is set and the system announces the change
        setNextAlarm(456_000L)
        context.sendBroadcast(Intent(AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED))
        idle()
        job.cancel()

        // Then the repository re-queries AlarmManager and emits the new trigger time
        assertEquals(456_000L, results.last())
    }

    @Test
    fun `filters out an alarm trigger not created by the resolved alarm app`() = runTest {
        // Given some other app is the trigger's creator than the device's actual alarm app (e.g. a Routine/Bedtime-mode toggle registering itself via setAlarmClock, not a real alarm)
        setAlarmAppPackage("com.other.clockapp")
        setNextAlarm(123_000L)

        // When observing the next alarm
        val results = mutableListOf<Long?>()
        val job = launch { repository.observeNextAlarmMillis().collect { results.add(it) } }
        idle()
        job.cancel()

        // Then the trigger is suppressed rather than surfaced as a real alarm
        assertEquals(listOf<Long?>(null), results)
    }

    @Test
    fun `surfaces an alarm trigger created by the resolved alarm app`() = runTest {
        // Given the resolved alarm app is the same package that registered the trigger
        setAlarmAppPackage(context.packageName)
        setNextAlarm(123_000L)

        // When observing the next alarm
        val results = mutableListOf<Long?>()
        val job = launch { repository.observeNextAlarmMillis().collect { results.add(it) } }
        idle()
        job.cancel()

        // Then its trigger time is surfaced
        assertEquals(listOf(123_000L), results)
    }

    @Test
    fun `surfaces a trigger comfortably within the 12-hour lookahead`() = runTest {
        // Given an alarm 1 hour from now
        val triggerTime = fixedNow.plus(Duration.ofHours(1)).toEpochMilli()
        setNextAlarm(triggerTime)

        // When observing the next alarm
        val results = mutableListOf<Long?>()
        val job = launch { repository.observeNextAlarmMillis().collect { results.add(it) } }
        idle()
        job.cancel()

        // Then it's surfaced
        assertEquals(listOf(triggerTime), results)
    }

    @Test
    fun `surfaces a trigger exactly 12 hours away`() = runTest {
        // Given an alarm exactly at the lookahead boundary
        val triggerTime = fixedNow.plus(Duration.ofHours(12)).toEpochMilli()
        setNextAlarm(triggerTime)

        // When observing the next alarm
        val results = mutableListOf<Long?>()
        val job = launch { repository.observeNextAlarmMillis().collect { results.add(it) } }
        idle()
        job.cancel()

        // Then the boundary itself is inclusive, so it's still surfaced
        assertEquals(listOf(triggerTime), results)
    }

    @Test
    fun `suppresses a trigger more than 12 hours away`() = runTest {
        // Given an alarm just past the lookahead boundary (e.g. tonight's bedtime toggle, hours before tomorrow's real wake alarm)
        setNextAlarm(fixedNow.plus(Duration.ofHours(13)).toEpochMilli())

        // When observing the next alarm
        val results = mutableListOf<Long?>()
        val job = launch { repository.observeNextAlarmMillis().collect { results.add(it) } }
        idle()
        job.cancel()

        // Then it's suppressed rather than surfaced as an imminent alarm
        assertEquals(listOf<Long?>(null), results)
    }
}
