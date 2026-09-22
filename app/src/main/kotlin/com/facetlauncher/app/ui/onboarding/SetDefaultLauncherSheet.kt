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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.R
import com.facetlauncher.app.ui.components.AppIcon
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

/**
 * First-launch "make Facet your home screen" prompt (`4h`) — shown once, as an overlay on top of
 * the real [com.facetlauncher.app.ui.home.HomeScreen] itself (from
 * [com.facetlauncher.app.ui.launcher.HomeDrawerRoute], right after onboarding completes), not as
 * a step inside [OnboardingScreen]'s own swipeable flow. It therefore paints no backdrop of its
 * own — just a [Scrim] dimming whatever's already on screen behind it, matching every other
 * first-run overlay Home hosts (e.g. `GestureHintOverlay`). "Set as default" launches
 * [requestDefaultLauncherIntent] via [rememberLauncherForActivityResult]; **any** result (granted,
 * denied, or dismissed) — same as "Later" — calls [onFinish]. If Facet already holds the role
 * (reinstall), swaps in a Success-colored "already default" variant with a single Done button.
 */
@Composable
fun SetDefaultLauncherSheet(
    isDefaultLauncher: Boolean,
    requestDefaultLauncherIntent: () -> Intent,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { onFinish() }

    Box(modifier = modifier.fillMaxSize().testTag("set_default_launcher_prompt")) {
        // Tapping anywhere outside the sheet itself is treated the same as "Later"/"Done" — not
        // just those explicit buttons (see chat history).
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Scrim)
                .clickable(onClick = onFinish)
                .testTag("set_default_launcher_scrim"),
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Surface, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                // Swallows taps on the sheet's own non-interactive area (e.g. its body text) so
                // they don't fall through to the scrim's onFinish above — same pattern as
                // ContactConnectionsSheet's own `.clickable(enabled = false, onClick = {})`.
                // Placed after background/clip so a ripple (if this were ever enabled) would
                // respect the shape above rather than fill the full rectangular bounds.
                .clickable(enabled = false, onClick = {})
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

            if (isDefaultLauncher) {
                AlreadyDefaultContent(onDone = onFinish)
            } else {
                SetDefaultContent(onSetDefault = { launcher.launch(requestDefaultLauncherIntent()) })
            }

            if (!isDefaultLauncher) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = stringResource(R.string.onboarding_later),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Muted,
                    // 48dp minimum touch target (M3 guideline), centered on the text.
                    modifier = Modifier
                        .align(Alignment.End)
                        .clickable(onClick = onFinish)
                        .testTag("onboarding_later")
                        .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                        .wrapContentSize(Alignment.Center),
                )
            }
        }
    }
}

@Composable
private fun SetDefaultContent(onSetDefault: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = stringResource(R.string.set_default_launcher_headline), style = MaterialTheme.typography.titleMedium, color = Ink)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.set_default_launcher_body),
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
            Text(text = stringResource(R.string.app_name), style = MaterialTheme.typography.bodyLarge, color = Ink, modifier = Modifier.weight(1f))
            Text(text = stringResource(R.string.set_default_launcher_home_app_badge), style = MaterialTheme.typography.bodySmall, color = Muted)
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.set_default_launcher_cta),
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
            text = stringResource(R.string.set_default_launcher_permissions_note),
            style = MaterialTheme.typography.bodySmall,
            color = Faint,
        )
    }
}

@Composable
private fun AlreadyDefaultContent(onDone: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = stringResource(R.string.set_default_launcher_checkmark), style = MaterialTheme.typography.titleMedium, color = SuccessColor)
            Text(text = stringResource(R.string.set_default_launcher_already_default), style = MaterialTheme.typography.titleMedium, color = Ink)
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.action_done),
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
            isDefaultLauncher = false,
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
            isDefaultLauncher = true,
            requestDefaultLauncherIntent = { Intent() },
            onFinish = {},
        )
    }
}
