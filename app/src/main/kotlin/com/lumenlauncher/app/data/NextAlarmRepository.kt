package com.lumenlauncher.app.data

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.provider.AlarmClock
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Clock
import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/** Alarms further out than this aren't shown — the clock's alarm accessory is meant as a "ringing soon" heads-up, not a long-range calendar lookup. */
private val LOOKAHEAD = Duration.ofHours(12)

/**
 * The system's next alarm trigger time (millis since epoch), or `null` when none is set — the
 * clock's alarm accessory item. `AlarmManager.getNextAlarmClock()` is system-wide: ANY app can
 * register a user-visible "alarm clock" through `setAlarmClock()`, not just the device's actual
 * alarm app — a Bedtime-mode/Routine toggle (e.g. Samsung's Routines scheduling a sleep-mode
 * switch) registers one too, and would otherwise surface here as if it were a real alarm (see
 * chat history — a Routine-driven 10:30pm "alarm" showed up despite no alarm being set). Fixed by
 * comparing the trigger's own [android.app.PendingIntent.getCreatorPackage] against
 * [alarmAppPackage] — the package that resolves [AlarmClock.ACTION_SHOW_ALARMS], i.e. the
 * device's actual alarm app — and only surfacing the trigger when they match. Note this doesn't
 * catch every case: on some OEMs (confirmed on Samsung) a Bedtime-mode toggle is registered by
 * the *same* package as real alarms, in which case this comparison can't tell them apart — see
 * chat history for the platform-level limitation this runs into.
 *
 * Also caps how far out a trigger can be and still count — see [LOOKAHEAD].
 */
@Singleton
class NextAlarmRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val clock: Clock,
) {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    /** `null` if no app on the device resolves as the alarm app (e.g. a stripped-down build) — in that case every trigger is trusted rather than silently suppressed. */
    private val alarmAppPackage: String? by lazy {
        context.packageManager.resolveActivity(Intent(AlarmClock.ACTION_SHOW_ALARMS), 0)?.activityInfo?.packageName
    }

    fun observeNextAlarmMillis(): Flow<Long?> = callbackFlow {
        fun query(): Long? {
            val nextAlarm = alarmManager?.nextAlarmClock ?: return null
            val creatorPackage = nextAlarm.showIntent?.creatorPackage
            if (alarmAppPackage != null && creatorPackage != alarmAppPackage) return null
            val untilTrigger = Duration.ofMillis(nextAlarm.triggerTime - clock.millis())
            return nextAlarm.triggerTime.takeIf { untilTrigger <= LOOKAHEAD }
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(receiverContext: Context, intent: Intent) {
                trySend(query())
            }
        }
        context.registerReceiver(receiver, IntentFilter(AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED))
        trySend(query())
        awaitClose { context.unregisterReceiver(receiver) }
    }
}
