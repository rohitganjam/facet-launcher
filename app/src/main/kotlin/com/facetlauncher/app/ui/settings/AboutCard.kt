package com.facetlauncher.app.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.BuildConfig
import com.facetlauncher.app.R
import com.facetlauncher.app.ui.components.CardDivider
import com.facetlauncher.app.ui.components.LocalSettingsRowInset
import com.facetlauncher.app.ui.components.SettingsCard
import com.facetlauncher.app.ui.components.SettingsIconBadgeInset
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.SettingsSectionHue

private const val DISCORD_INVITE_URL = "https://discord.gg/BRjwWZ23E"

/** Settings → About card: version (from [BuildConfig], so it can't drift from what `scripts/release.sh` bumps), What's new, a Play Store link and the community Discord. */
@Composable
fun AboutCard(onNavigateToWhatsNew: () -> Unit, onNavigateToLicenses: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    SettingsCard(modifier = modifier, fullBleedRows = true) {
        VersionRow(versionName = BuildConfig.VERSION_NAME)
        CardDivider(startInset = SettingsIconBadgeInset)
        ClickableRow(
            title = stringResource(R.string.about_whats_new),
            subtitle = stringResource(R.string.about_whats_new_subtitle),
            onClick = onNavigateToWhatsNew,
            testTag = "about_whats_new_row",
            leadingIcon = iconBadge(Icons.Outlined.NewReleases, SettingsSectionHue.SYSTEM),
        )
        CardDivider(startInset = SettingsIconBadgeInset)
        ClickableRow(
            title = stringResource(R.string.about_check_for_updates),
            subtitle = stringResource(R.string.about_check_for_updates_subtitle),
            onClick = {
                val url = "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"
                runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
            },
            testTag = "about_check_updates_row",
            leadingIcon = iconBadge(Icons.Outlined.SystemUpdate, SettingsSectionHue.SYSTEM),
        )
        CardDivider(startInset = SettingsIconBadgeInset)
        ClickableRow(
            title = stringResource(R.string.about_join_discord),
            subtitle = stringResource(R.string.about_join_discord_subtitle),
            onClick = { runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(DISCORD_INVITE_URL))) } },
            testTag = "about_discord_row",
            leadingIcon = iconBadge(Icons.Outlined.Forum, SettingsSectionHue.SYSTEM),
        )
        CardDivider(startInset = SettingsIconBadgeInset)
        ClickableRow(
            title = stringResource(R.string.about_licenses),
            subtitle = stringResource(R.string.about_licenses_subtitle),
            onClick = onNavigateToLicenses,
            testTag = "about_licenses_row",
            leadingIcon = iconBadge(Icons.Outlined.Description, SettingsSectionHue.SYSTEM),
        )
    }
}

/** Data-source credit shown under [AboutCard]. */
@Composable
fun AboutCredit(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.about_weather_credit),
        style = MaterialTheme.typography.bodySmall,
        color = Muted,
        modifier = modifier.padding(horizontal = 4.dp, vertical = 16.dp).testTag("about_weather_credit"),
    )
}

@Composable
private fun VersionRow(versionName: String) {
    Row(
        modifier = Modifier
            .testTag("about_version_row")
            // No clickable() to merge descendants, so set it for the title/subtitle to read off this node.
            .semantics(mergeDescendants = true) {}
            .fillMaxWidth()
            .padding(horizontal = LocalSettingsRowInset.current, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        iconBadge(Icons.Outlined.Info, SettingsSectionHue.SYSTEM)()
        Text(text = stringResource(R.string.about_version), style = MaterialTheme.typography.bodyLarge, color = Ink, modifier = Modifier.weight(1f))
        Text(text = versionName, style = MaterialTheme.typography.bodyMedium, color = Muted)
    }
}
