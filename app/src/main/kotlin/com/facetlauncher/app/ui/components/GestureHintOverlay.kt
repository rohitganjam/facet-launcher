package com.facetlauncher.app.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.Scrim

/**
 * One-time gesture-hint overlay, shown over Home right after onboarding finishes — see
 * [com.facetlauncher.app.ui.home.HomeUiState.showGestureHint]. Minimal chevron glyphs (not
 * motion-trail arrows) to stay consistent with the app's restrained visual language. Kept in sync
 * with the real gesture map (`PRD.md` §5): swipe up → App Drawer, swipe right → Hub, swipe left →
 * Switch Profiles. Dismissed by [onDismiss], fired on a tap, a "Got it" tap, or the first frame of
 * any drag — that first swipe attempt just clears the hint rather than also completing the real
 * navigation underneath; a second swipe then behaves normally.
 */
@Composable
fun GestureHintOverlay(onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            // Bumped to 80% opacity (from Scrim's own ~28%/58% light/dark default) — at the
            // default strength Home showed through too strongly for the hint text to read
            // clearly over it (see chat history). Scoped to this overlay only, not the shared
            // Scrim token other surfaces (App Drawer, the default-launcher sheet) still use.
            .background(Scrim.copy(alpha = 0.8f))
            .testTag("gesture_hint_overlay")
            .clickable(onClick = onDismiss)
            .pointerInput(Unit) { detectDragGestures(onDragStart = { onDismiss() }) { _, _ -> } },
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp)) {
            Row(
                modifier = Modifier.padding(top = 96.dp).align(Alignment.End),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "Switch profiles", style = MaterialTheme.typography.headlineSmall, color = Ink)
                Text(text = "←", style = MaterialTheme.typography.headlineMedium, color = Muted)
            }
            Row(
                modifier = Modifier.padding(top = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "→", style = MaterialTheme.typography.headlineMedium, color = Muted)
                Text(text = "Widgets", style = MaterialTheme.typography.headlineSmall, color = Ink)
            }
            Column(
                modifier = Modifier.weight(1f).padding(bottom = 120.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(text = "All your apps", style = MaterialTheme.typography.headlineSmall, color = Ink)
                Text(text = "↑", style = MaterialTheme.typography.headlineMedium, color = Muted)
            }
        }
        SurfaceButton(
            text = "Got it",
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .testTag("gesture_hint_got_it"),
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun GestureHintOverlayPreview() {
    FacetLauncherTheme {
        GestureHintOverlay(onDismiss = {})
    }
}
