package com.facetlauncher.app.ui.settings

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.BuildConfig
import com.facetlauncher.app.R
import com.facetlauncher.app.ui.components.BackButton
import com.facetlauncher.app.ui.components.CardDivider
import com.facetlauncher.app.ui.components.SettingsCard
import com.facetlauncher.app.ui.components.StickyHeaderLayout
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.SurfaceContainer

private const val DISCORD_INVITE_URL = "https://discord.gg/BRjwWZ23E"

/** Settings → About — version (from [BuildConfig], never hardcoded so it can't drift from what `scripts/release.sh` bumps), a Play Store link to check for updates, and the community Discord. */
@Composable
fun AboutScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    AboutContent(
        versionName = BuildConfig.VERSION_NAME,
        onBack = onBack,
        onCheckForUpdatesClick = {
            val url = "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"
            runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
        },
        onJoinDiscordClick = {
            runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(DISCORD_INVITE_URL))) }
        },
        modifier = modifier,
    )
}

@Composable
private fun AboutContent(
    versionName: String,
    onBack: () -> Unit,
    onCheckForUpdatesClick: () -> Unit,
    onJoinDiscordClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    StickyHeaderLayout(
        modifier = modifier,
        header = { AboutHeader(onBack = onBack) },
        content = { headerHeight ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SurfaceContainer)
                    .testTag("about_screen")
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .padding(horizontal = 24.dp),
                contentPadding = PaddingValues(top = headerHeight),
            ) {
                item {
                    SettingsCard {
                        InfoRow(title = stringResource(R.string.about_version), subtitle = versionName, testTag = "about_version_row")
                        CardDivider()
                        ClickableAboutRow(
                            title = stringResource(R.string.about_check_for_updates),
                            subtitle = stringResource(R.string.about_check_for_updates_subtitle),
                            onClick = onCheckForUpdatesClick,
                            testTag = "about_check_updates_row",
                        )
                        CardDivider()
                        ClickableAboutRow(
                            title = stringResource(R.string.about_join_discord),
                            subtitle = stringResource(R.string.about_join_discord_subtitle),
                            onClick = onJoinDiscordClick,
                            testTag = "about_discord_row",
                        )
                    }
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        },
    )
}

@Composable
private fun AboutHeader(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceContainer)
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BackButton(onClick = onBack)
        Text(text = stringResource(R.string.about_header_title), style = MaterialTheme.typography.headlineSmall, color = Ink)
    }
}

@Composable
private fun InfoRow(title: String, subtitle: String, testTag: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .testTag(testTag)
            // No clickable() here to pick up mergeDescendants the way the other rows on this
            // screen do — set it explicitly so the title/subtitle text is readable off this row's
            // own semantics node (matches ClickableAboutRow's behavior for touch-exploration too).
            .semantics(mergeDescendants = true) {}
            .fillMaxWidth()
            .padding(vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge, color = Ink)
        Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = Muted)
    }
}

@Composable
private fun ClickableAboutRow(title: String, subtitle: String, onClick: () -> Unit, testTag: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .testTag(testTag)
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, color = Ink)
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = Muted)
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AboutScreenPreview() {
    FacetLauncherTheme {
        AboutContent(
            versionName = "0.1.7",
            onBack = {},
            onCheckForUpdatesClick = {},
            onJoinDiscordClick = {},
        )
    }
}
