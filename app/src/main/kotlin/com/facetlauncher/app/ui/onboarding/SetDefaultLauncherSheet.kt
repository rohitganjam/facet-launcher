package com.facetlauncher.app.ui.onboarding

import android.content.Intent
import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.ui.components.AppIcon
import com.facetlauncher.app.ui.components.HomeSurfacePreview
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.Faint
import com.facetlauncher.app.ui.theme.Hairline
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.InkInverted
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.Scrim
import com.facetlauncher.app.ui.theme.Surface
import com.facetlauncher.app.ui.theme.SuccessColor
import com.facetlauncher.app.ui.theme.resolve

/**
 * Onboarding's final action (`4h`) — the one that matters. A bottom sheet over the user's real
 * Home so far (their step-2 picks, dimmed under [Scrim]). Not one of the numbered swipeable steps
 * (its dots below stay on Facets' own last-dot state) and not reversible — reachable only
 * forward from Facets, with no "Back" of its own. "Set as default" launches
 * [requestDefaultLauncherIntent] via [rememberLauncherForActivityResult]; **any** result (granted,
 * denied, or dismissed) — same as "Later" — calls [onFinish]. If Facet already holds the role
 * (reinstall), swaps in a Success-colored "already default" variant with a single Done button.
 */
@Composable
fun SetDefaultLauncherSheet(
    uiState: OnboardingUiState,
    requestDefaultLauncherIntent: () -> Intent,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { onFinish() }

    Box(modifier = modifier.fillMaxSize().testTag("onboarding_set_default_page")) {
        HomeSurfacePreview(
            appList = uiState.favoriteApps,
            dockApps = uiState.dockApps,
            labelColor = uiState.appLabelColorOption.resolve(),
            labelFontWeight = uiState.homeAppsFontWeight.resolve(),
            homeWallpaper = uiState.homeWallpaper,
            dockDisplayMode = uiState.dockDisplayMode,
            maxAppRows = 5,
            modifier = Modifier.fillMaxSize(),
        )
        // Tapping anywhere outside the sheet itself is treated the same as "Later"/"Done" — not
        // just those explicit buttons (see chat history).
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Scrim)
                .clickable(onClick = onFinish)
                .testTag("onboarding_set_default_scrim"),
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                // Swallows taps on the sheet's own non-interactive area (e.g. its body text) so
                // they don't fall through to the scrim's onFinish above — same pattern as
                // ContactConnectionsSheet's own `.clickable(enabled = false, onClick = {})`.
                .clickable(enabled = false, onClick = {})
                .background(Surface, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .padding(horizontal = 24.dp)
                .padding(top = 20.dp, bottom = 34.dp),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 16.dp)
                    .size(width = 34.dp, height = 4.dp)
                    .background(Faint, RoundedCornerShape(2.dp)),
            )

            if (uiState.isDefaultLauncher) {
                AlreadyDefaultContent(onDone = onFinish)
            } else {
                SetDefaultContent(onSetDefault = { launcher.launch(requestDefaultLauncherIntent()) })
            }

            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Same last-dot state as Facets — this sheet doesn't advance the stepper, it's
                // the final action reached from that last step, not a step of its own.
                OnboardingDots(step = ONBOARDING_STEP_COUNT - 1, totalSteps = ONBOARDING_STEP_COUNT)
                if (!uiState.isDefaultLauncher) {
                    Text(
                        text = "Later",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Muted,
                        // 48dp minimum touch target (M3 guideline), centered on the text.
                        modifier = Modifier
                            .clickable(onClick = onFinish)
                            .testTag("onboarding_later")
                            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            .wrapContentSize(Alignment.Center),
                    )
                }
            }
        }
    }
}

@Composable
private fun SetDefaultContent(onSetDefault: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = "Make Facet your home screen", style = MaterialTheme.typography.titleMedium, color = Ink)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Android will ask you to confirm. You can switch back to your old launcher any time from Settings.",
            style = MaterialTheme.typography.bodyMedium,
            color = Muted,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Hairline, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AppIcon(icon = null, size = 28.dp, cornerRadius = 8.dp, contentDescription = null)
            Text(text = "Facet Launcher", style = MaterialTheme.typography.bodyLarge, color = Ink, modifier = Modifier.weight(1f))
            Text(text = "Home app", style = MaterialTheme.typography.bodySmall, color = Muted)
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Set as default",
            style = MaterialTheme.typography.bodyLarge,
            color = InkInverted,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .background(Accent, CircleShape)
                .clickable(onClick = onSetDefault)
                .testTag("onboarding_set_default")
                .padding(vertical = 14.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Permissions come later, one at a time, only when a feature needs them.",
            style = MaterialTheme.typography.bodySmall,
            color = Faint,
        )
    }
}

@Composable
private fun AlreadyDefaultContent(onDone: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "✓", style = MaterialTheme.typography.titleMedium, color = SuccessColor)
            Text(text = "Facet is already your home screen", style = MaterialTheme.typography.titleMedium, color = Ink)
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Done",
            style = MaterialTheme.typography.bodyLarge,
            color = InkInverted,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .background(Accent, CircleShape)
                .clickable(onClick = onDone)
                .testTag("onboarding_done")
                .padding(vertical = 14.dp),
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SetDefaultLauncherSheetPreview() {
    FacetLauncherTheme {
        SetDefaultLauncherSheet(
            uiState = OnboardingUiState(),
            requestDefaultLauncherIntent = { Intent() },
            onFinish = {},
        )
    }
}

@Preview(name = "AlreadyDefault", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun SetDefaultLauncherSheetAlreadyDefaultPreview() {
    FacetLauncherTheme {
        SetDefaultLauncherSheet(
            uiState = OnboardingUiState(isDefaultLauncher = true),
            requestDefaultLauncherIntent = { Intent() },
            onFinish = {},
        )
    }
}
