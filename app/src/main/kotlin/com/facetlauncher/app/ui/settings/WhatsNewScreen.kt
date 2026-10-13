package com.facetlauncher.app.ui.settings

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.facetlauncher.app.BuildConfig
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.ReleaseNotes
import com.facetlauncher.app.ui.components.BackButton
import com.facetlauncher.app.ui.components.SettingsCard
import com.facetlauncher.app.ui.components.StickyHeaderLayout
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.SurfaceContainer

/** Settings → About → What's new — one card per release, newest first, with the installed version marked. */
@Composable
fun WhatsNewScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WhatsNewViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    WhatsNewContent(uiState = uiState, installedVersion = BuildConfig.VERSION_NAME, onBack = onBack, modifier = modifier)
}

@Composable
private fun WhatsNewContent(
    uiState: WhatsNewUiState,
    installedVersion: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    StickyHeaderLayout(
        modifier = modifier,
        header = { WhatsNewHeader(onBack = onBack) },
        content = { headerHeight ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SurfaceContainer)
                    .testTag("whats_new_screen")
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .padding(horizontal = 24.dp),
                contentPadding = PaddingValues(top = headerHeight),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (!uiState.isLoading && uiState.releases.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.whats_new_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Muted,
                            modifier = Modifier.testTag("whats_new_empty"),
                        )
                    }
                }
                items(uiState.releases, key = { it.version }) { release ->
                    ReleaseCard(release = release, isInstalled = release.version == installedVersion)
                }
                item { Spacer(modifier = Modifier.height(8.dp)) }
            }
        },
    )
}

@Composable
private fun WhatsNewHeader(onBack: () -> Unit, modifier: Modifier = Modifier) {
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
        Text(text = stringResource(R.string.whats_new_header_title), style = MaterialTheme.typography.headlineSmall, color = Ink)
    }
}

@Composable
private fun ReleaseCard(release: ReleaseNotes, isInstalled: Boolean, modifier: Modifier = Modifier) {
    SettingsCard(modifier = modifier.testTag("whats_new_release_${release.version}")) {
        Column(modifier = Modifier.padding(vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.whats_new_version, release.version),
                    style = MaterialTheme.typography.titleMedium,
                    color = Ink,
                )
                if (isInstalled) {
                    Text(
                        text = stringResource(R.string.whats_new_installed),
                        style = MaterialTheme.typography.labelMedium,
                        color = Accent,
                        modifier = Modifier.testTag("whats_new_installed_${release.version}"),
                    )
                }
            }
            ReleaseSection(release = release, kind = ReleaseSectionKind.NEW)
            ReleaseSection(release = release, kind = ReleaseSectionKind.IMPROVED)
        }
    }
}

private enum class ReleaseSectionKind { NEW, IMPROVED }

@Composable
private fun ReleaseSection(release: ReleaseNotes, kind: ReleaseSectionKind) {
    val bullets = if (kind == ReleaseSectionKind.NEW) release.newFeatures else release.improvements
    if (bullets.isEmpty()) return
    val title = stringResource(if (kind == ReleaseSectionKind.NEW) R.string.whats_new_section_new else R.string.whats_new_section_improved)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = title, style = MaterialTheme.typography.labelLarge, color = Accent)
        bullets.forEach { bullet ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.padding(top = 8.dp).size(5.dp).background(Muted, CircleShape))
                Text(text = bullet, style = MaterialTheme.typography.bodyMedium, color = Ink)
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun WhatsNewScreenPreview() {
    FacetLauncherTheme {
        WhatsNewContent(
            uiState = WhatsNewUiState(
                releases = listOf(
                    ReleaseNotes(
                        version = "0.1.25",
                        newFeatures = listOf("An optional media player row on your home screen."),
                        improvements = listOf("Tapping the media row opens the player app."),
                    ),
                    ReleaseNotes(version = "0.1.24", newFeatures = emptyList(), improvements = listOf("More space under the calendar.")),
                ),
                isLoading = false,
            ),
            installedVersion = "0.1.25",
            onBack = {},
        )
    }
}
