package com.facetlauncher.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import com.facetlauncher.app.ui.launcher.LocalHomePressedEvent

/**
 * Calls [onDismiss] whenever Home is pressed while [enabled] — the one hook every sheet, dialog and
 * overlay should use so Home closes them consistently. Subscribes only while [enabled], and always
 * invokes the latest [onDismiss]. [onDismiss] must not suspend or animate inline: a cancellation from
 * an interrupted animation would escape `collect` and silently end the subscription.
 */
@Composable
fun DismissOnHomePress(enabled: Boolean = true, onDismiss: () -> Unit) {
    val homePressedEvent = LocalHomePressedEvent.current
    val currentOnDismiss by rememberUpdatedState(onDismiss)
    LaunchedEffect(enabled, homePressedEvent) {
        if (enabled) homePressedEvent?.collect { currentOnDismiss() }
    }
}
