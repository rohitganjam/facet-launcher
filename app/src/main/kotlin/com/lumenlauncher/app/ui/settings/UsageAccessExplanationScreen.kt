package com.lumenlauncher.app.ui.settings

import android.content.Intent
import android.content.res.Configuration
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumenlauncher.app.ui.components.BackButton
import com.lumenlauncher.app.ui.theme.Accent
import com.lumenlauncher.app.ui.theme.Ink
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme
import com.lumenlauncher.app.ui.theme.Muted
import com.lumenlauncher.app.ui.theme.Surface
import com.lumenlauncher.app.ui.theme.SurfaceContainer

/**
 * PRD F2's required "onboarding explanation screen" shown before the `ACTION_USAGE_ACCESS_SETTINGS`
 * redirect — `PACKAGE_USAGE_STATS` is a special-access permission that can't be requested via a
 * normal runtime dialog. Reached whenever the user taps Home's usage-access strip, or Profile
 * Settings' "Apps to show" area while Recents/Most used is selected and access isn't granted yet.
 * Auto-dismisses ([onBack]) the moment [UsageAccessExplanationViewModel.isGranted] flips true —
 * checked on every resume, since there's no direct grant-change callback for this permission.
 */
@Composable
fun UsageAccessExplanationScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: UsageAccessExplanationViewModel = hiltViewModel(),
) {
    val isGranted by viewModel.isGranted.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(isGranted) {
        if (isGranted) onBack()
    }

    UsageAccessExplanationContent(
        onBack = onBack,
        onOpenSettingsClick = {
            runCatching { context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) }
        },
        modifier = modifier,
    )
}

@Composable
private fun UsageAccessExplanationContent(
    onBack: () -> Unit,
    onOpenSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceContainer)
            .testTag("usage_access_explanation_screen")
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(horizontal = 24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BackButton(onClick = onBack)
            Text(text = "Usage access", style = MaterialTheme.typography.headlineSmall, color = Ink)
        }

        // Centered when it fits the viewport, scrollable from the top once it doesn't (e.g. a
        // large system font scale) — weight() needs the bounded height this gets from the
        // non-scrollable outer Column, so only this inner one carries verticalScroll.
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Recents and Most used show apps based on how you actually use your phone.",
                style = MaterialTheme.typography.bodyLarge,
                color = Ink,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "This needs usage access, a special permission granted from system Settings — Lumen can't request it directly. " +
                    "It only reads which apps you open and for how long; nothing leaves your device.",
                style = MaterialTheme.typography.bodyMedium,
                color = Muted,
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Open settings",
                style = MaterialTheme.typography.bodyLarge,
                color = Accent,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    // M3's Button shapes default to CornerFull — see CLAUDE.md's Material 3 shape section.
                    .background(color = Accent.copy(alpha = 0.1f), shape = CircleShape)
                    .clickable(onClick = onOpenSettingsClick)
                    .testTag("open_usage_access_settings_button")
                    .padding(vertical = 14.dp),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun UsageAccessExplanationScreenPreview() {
    LumenLauncherTheme {
        UsageAccessExplanationContent(onBack = {}, onOpenSettingsClick = {})
    }
}
