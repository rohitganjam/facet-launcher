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
import androidx.compose.material.icons.filled.Wallpaper
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

private const val AUTO_HIDE_MS = 5_000L
private const val SLIDE_MS = 320
private const val DISMISS_DRAG_PX = 12f

/** "Capture wallpaper for glass effects · Capture": a pill hovering over Home. Tapping it opens the capture screen, swiping it up dismisses it. */
@Composable
internal fun WallpaperCopyPill(onClick: () -> Unit, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    val description = stringResource(R.string.wallpaper_copy_pill_description)
    Row(
        modifier = modifier
            .testTag("wallpaper_copy_pill")
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
        Icon(imageVector = Icons.Default.Wallpaper, contentDescription = null, tint = Accent, modifier = Modifier.size(18.dp))
        Text(text = stringResource(R.string.wallpaper_copy_pill_title), style = MaterialTheme.typography.labelLarge, color = Ink, modifier = Modifier.clearAndSetSemantics { })
        Text(text = stringResource(R.string.wallpaper_copy_pill_action), style = MaterialTheme.typography.labelLarge, color = Accent, modifier = Modifier.clearAndSetSemantics { })
    }
}

/**
 * Drops [WallpaperCopyPill] in from the top once [offer] says the view showing needs a capture and Home is [ready]. Like the What's new
 * pill it hovers over Home and moves nothing; it leaves on its own after a few seconds, when swiped up, or when tapped. [onShow] is
 * called with the key when it appears, so the view is not offered again.
 */
@Composable
internal fun WallpaperCopyPillHost(offer: WallpaperCopyOffer?, ready: Boolean, onShow: (String) -> Unit, onOpen: () -> Unit, modifier: Modifier = Modifier) {
    var visible by remember { mutableStateOf(false) }
    // Kept while the pill leaves, so it does not change its words as it slides out.
    var shown by remember { mutableStateOf(offer) }
    val currentOnShow by rememberUpdatedState(onShow)
    LaunchedEffect(offer, ready) {
        if (ready && offer != null && !visible) {
            shown = offer
            visible = true
            currentOnShow(offer.key)
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
        if (shown != null) {
            WallpaperCopyPill(
                onClick = {
                    visible = false
                    onOpen()
                },
                onDismiss = { visible = false },
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun WallpaperCopyPillPreview() {
    FacetLauncherTheme {
        WallpaperCopyPill(onClick = {}, onDismiss = {}, modifier = Modifier.padding(16.dp))
    }
}
