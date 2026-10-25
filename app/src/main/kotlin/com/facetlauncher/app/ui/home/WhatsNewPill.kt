package com.facetlauncher.app.ui.home

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.R
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Hairline
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Surface
import kotlinx.coroutines.delay

private const val AUTO_HIDE_MS = 8_000L
private const val SLIDE_MS = 320
private const val DISMISS_DRAG_PX = 12f

/** "Updated to 1.2.3 · See what's new": a pill hovering over Home. Tapping it opens What's new, swiping it up dismisses it. */
@Composable
internal fun WhatsNewPill(version: String, onClick: () -> Unit, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    val description = stringResource(R.string.whats_new_pill_description, version)
    Row(
        modifier = modifier
            .testTag("whats_new_pill")
            .shadow(6.dp, CircleShape)
            .clip(CircleShape)
            .background(Surface, CircleShape)
            .border(1.dp, Hairline, CircleShape)
            .pointerInput(Unit) {
                detectVerticalDragGestures { change, dragAmount ->
                    if (dragAmount < -DISMISS_DRAG_PX) {
                        change.consume()
                        onDismiss()
                    }
                }
            }
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Accent, modifier = Modifier.size(18.dp))
        Text(
            text = stringResource(R.string.whats_new_pill_title, version),
            style = MaterialTheme.typography.labelLarge,
            color = Ink,
            modifier = Modifier.clearAndSetSemantics { },
        )
        Text(
            text = stringResource(R.string.whats_new_pill_action),
            style = MaterialTheme.typography.labelLarge,
            color = Accent,
            modifier = Modifier.clearAndSetSemantics { },
        )
    }
}

/**
 * Drops [WhatsNewPill] in from the top once [version] is to be announced and Home is [ready] (at rest, with no earlier prompt
 * pending). It hovers over Home and moves nothing. The version is latched when it first shows, because [onShown] retires it
 * and [version] then goes back to `null`; the pill leaves on its own after a few seconds, when swiped up, or when tapped.
 */
@Composable
internal fun WhatsNewPillHost(version: String?, ready: Boolean, onShow: () -> Unit, onOpen: () -> Unit, modifier: Modifier = Modifier) {
    var announced by remember { mutableStateOf<String?>(null) }
    var visible by remember { mutableStateOf(false) }
    val currentOnShow by rememberUpdatedState(onShow)
    LaunchedEffect(version, ready) {
        if (ready && version != null && announced == null) {
            announced = version
            visible = true
            currentOnShow()
        }
    }
    LaunchedEffect(visible) {
        if (visible) {
            delay(AUTO_HIDE_MS)
            visible = false
        }
    }
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = slideInVertically(tween(SLIDE_MS)) { -it * 2 } + fadeIn(tween(SLIDE_MS)),
        exit = slideOutVertically(tween(SLIDE_MS)) { -it * 2 } + fadeOut(tween(SLIDE_MS)),
    ) {
        WhatsNewPill(
            version = announced.orEmpty(),
            onClick = {
                visible = false
                onOpen()
            },
            onDismiss = { visible = false },
        )
    }
}

@Preview(showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun WhatsNewPillPreview() {
    FacetLauncherTheme {
        WhatsNewPill(version = "0.1.32", onClick = {}, onDismiss = {}, modifier = Modifier.padding(16.dp))
    }
}
