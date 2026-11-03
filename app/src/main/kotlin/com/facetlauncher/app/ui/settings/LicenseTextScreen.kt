package com.facetlauncher.app.ui.settings

import android.content.res.Configuration
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.facetlauncher.app.data.model.LicenseCatalog
import com.facetlauncher.app.data.model.LicenseSection
import com.facetlauncher.app.ui.components.BackButton
import com.facetlauncher.app.ui.components.SettingsCard
import com.facetlauncher.app.ui.components.StickyHeaderLayout
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.SurfaceContainer
import kotlinx.collections.immutable.persistentListOf

private const val PREVIEW_TEXT = "Attribution 4.0 International\n\nCreative Commons Corporation ..."

/** Settings → About → Open source licenses → one entry: the licence text(s) exactly as published. */
@Composable
fun LicenseTextScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LicenseTextViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LicenseTextContent(uiState = uiState, onBack = onBack, modifier = modifier)
}

@Composable
private fun LicenseTextContent(uiState: LicenseTextUiState, onBack: () -> Unit, modifier: Modifier = Modifier) {
    StickyHeaderLayout(
        modifier = modifier,
        header = { LicenseTextHeader(title = uiState.info?.let { licenseTitle(it) }.orEmpty(), onBack = onBack) },
        content = { headerHeight ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SurfaceContainer)
                    .testTag("license_text_screen")
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .padding(horizontal = 24.dp),
                contentPadding = PaddingValues(top = headerHeight),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                itemsIndexed(uiState.sections, key = { index, _ -> index }) { _, section -> LicenseSectionCard(section) }
                item { Spacer(modifier = Modifier.height(8.dp)) }
            }
        },
    )
}

@Composable
private fun LicenseSectionCard(section: LicenseSection, modifier: Modifier = Modifier) {
    SettingsCard(modifier = modifier) {
        section.heading?.let { heading ->
            Text(text = heading, style = MaterialTheme.typography.titleMedium, color = Ink, modifier = Modifier.padding(bottom = 8.dp))
        }
        Text(
            text = section.text,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = Muted,
            modifier = Modifier.testTag("license_text_body"),
        )
    }
}

@Composable
private fun LicenseTextHeader(title: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
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
        Text(text = title, style = MaterialTheme.typography.titleLarge, color = Ink)
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LicenseTextScreenPreview() {
    FacetLauncherTheme {
        LicenseTextContent(
            uiState = LicenseTextUiState(
                info = LicenseCatalog.dataSources.first(),
                sections = persistentListOf(LicenseSection(heading = null, text = PREVIEW_TEXT)),
                isLoading = false,
            ),
            onBack = {},
        )
    }
}
