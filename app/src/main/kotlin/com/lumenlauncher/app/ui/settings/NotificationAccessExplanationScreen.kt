package com.lumenlauncher.app.ui.settings

import android.content.Intent
import android.content.res.Configuration
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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

/**
 * F13's required onboarding explanation screen shown before the
 * `ACTION_NOTIFICATION_LISTENER_SETTINGS` redirect — notification listener access is a
 * special-access permission that can't be requested via a normal runtime dialog. Reached by
 * turning on "Notification badges" in [NotificationSettingsScreen]. Auto-dismisses ([onBack]) the
 * moment [NotificationAccessExplanationViewModel.isGranted] flips true — checked on every resume,
 * since there's no direct grant-change callback for this permission.
 */
@Composable
fun NotificationAccessExplanationScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotificationAccessExplanationViewModel = hiltViewModel(),
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

    NotificationAccessExplanationContent(
        onBack = onBack,
        onOpenSettingsClick = {
            runCatching { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }
        },
        modifier = modifier,
    )
}

@Composable
private fun NotificationAccessExplanationContent(
    onBack: () -> Unit,
    onOpenSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Surface)
            .testTag("notification_access_explanation_screen")
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(horizontal = 24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BackButton(onClick = onBack)
            Text(text = "Notification access", style = MaterialTheme.typography.headlineSmall, color = Ink)
        }

        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Notification badges show a dot or count on apps with something waiting for you.",
                style = MaterialTheme.typography.bodyLarge,
                color = Ink,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "This needs notification access, a special permission granted from system Settings — Lumen can't request it directly. " +
                    "It only reads which apps have active notifications and whether they're silent; nothing leaves your device.",
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
                    .testTag("open_notification_access_settings_button")
                    .padding(vertical = 14.dp),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun NotificationAccessExplanationScreenPreview() {
    LumenLauncherTheme {
        NotificationAccessExplanationContent(onBack = {}, onOpenSettingsClick = {})
    }
}
