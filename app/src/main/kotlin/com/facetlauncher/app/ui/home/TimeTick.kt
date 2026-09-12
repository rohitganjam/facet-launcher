package com.facetlauncher.app.ui.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import java.time.Clock
import java.time.LocalDateTime

/**
 * Live minute-precision "now", shared by [ClockBlock]/`ClockDisplay`/[CalendarEventsBlock] so
 * each can tick independently (composable independence is the point — see the clock template
 * gallery) without each hand-rolling its own [Intent.ACTION_TIME_TICK] receiver. The OS already
 * sends that broadcast every real minute rollover regardless of this app; also catches
 * [Intent.ACTION_TIME_CHANGED]/[Intent.ACTION_TIMEZONE_CHANGED] immediately rather than leaving
 * the display stale.
 */
@Composable
fun rememberTickingNow(clock: Clock): State<LocalDateTime> {
    val nowState = remember(clock) { mutableStateOf(LocalDateTime.now(clock)) }
    val context = LocalContext.current
    DisposableEffect(context, clock) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                nowState.value = LocalDateTime.now(clock)
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_TIME_TICK)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }
        // ACTION_TIME_TICK is a protected system broadcast — only the OS can send it, so
        // NOT_EXPORTED (no other app needs to trigger this receiver) is the correct, more secure
        // flag; required explicitly for a context-registered receiver at this app's targetSdk.
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        onDispose { context.unregisterReceiver(receiver) }
    }
    return nowState
}
