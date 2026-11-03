package com.facetlauncher.app.ui.settings

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.LicenseCatalog
import com.facetlauncher.app.data.model.LicenseInfo
import com.facetlauncher.app.ui.components.BackButton
import com.facetlauncher.app.ui.components.CardDivider
import com.facetlauncher.app.ui.components.SettingsCard
import com.facetlauncher.app.ui.components.SettingsSectionHeader
import com.facetlauncher.app.ui.components.StickyHeaderLayout
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.SurfaceContainer
import kotlinx.collections.immutable.ImmutableList

/** Settings → About → Open source licenses: what the app is built on and the data it shows; each row opens its bundled licence text. */
@Composable
fun LicensesScreen(onBack: () -> Unit, onOpenLicense: (id: String) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    LicensesContent(
        onBack = onBack,
        onOpenLicense = onOpenLicense,
        onOpenUrl = { url -> runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) } },
        modifier = modifier,
    )
}

@Composable
private fun LicensesContent(
    onBack: () -> Unit,
    onOpenLicense: (id: String) -> Unit,
    onOpenUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    StickyHeaderLayout(
        modifier = modifier,
        header = { LicensesHeader(onBack = onBack) },
        content = { headerHeight ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SurfaceContainer)
                    .testTag("licenses_screen")
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .padding(horizontal = 24.dp),
                contentPadding = PaddingValues(top = headerHeight),
            ) {
                item { SettingsSectionHeader(stringResource(R.string.licenses_section_libraries)) }
                item { LicenseCard(LicenseCatalog.librariesAndFonts, onOpenLicense, onOpenUrl) }
                item { SettingsSectionHeader(stringResource(R.string.licenses_section_data)) }
                item { LicenseCard(LicenseCatalog.dataSources, onOpenLicense, onOpenUrl) }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        },
    )
}

@Composable
private fun LicenseCard(entries: ImmutableList<LicenseInfo>, onOpenLicense: (String) -> Unit, onOpenUrl: (String) -> Unit) {
    SettingsCard(fullBleedRows = true) {
        entries.forEachIndexed { index, entry ->
            if (index > 0) CardDivider()
            ClickableRow(
                title = licenseTitle(entry),
                subtitle = entry.licenseName,
                onClick = { if (entry.files.isEmpty()) entry.url?.let(onOpenUrl) else onOpenLicense(entry.id) },
                testTag = "license_row_${entry.id}",
            )
        }
    }
}

/** The entry's name; a null name is the localized "Fonts" label. */
@Composable
internal fun licenseTitle(info: LicenseInfo): String = info.name ?: stringResource(R.string.licenses_section_fonts)

@Composable
private fun LicensesHeader(onBack: () -> Unit, modifier: Modifier = Modifier) {
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
        Text(text = stringResource(R.string.about_licenses), style = MaterialTheme.typography.headlineSmall, color = Ink)
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LicensesScreenPreview() {
    FacetLauncherTheme { LicensesContent(onBack = {}, onOpenLicense = {}, onOpenUrl = {}) }
}
