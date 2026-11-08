package com.facetlauncher.app.ui.onboarding

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.DrawerFolderDisplayMode
import com.facetlauncher.app.data.model.DrawerPresentation
import com.facetlauncher.app.data.model.HomeWallpaper
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.SearchBarPosition
import com.facetlauncher.app.ui.components.CardDivider
import com.facetlauncher.app.ui.components.LabeledDropdownRow
import com.facetlauncher.app.ui.components.SettingsCard
import com.facetlauncher.app.ui.components.SettingsToggleRow
import com.facetlauncher.app.ui.components.TextActionButton
import com.facetlauncher.app.ui.settings.AppDrawerSettingsPreview
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Muted

/**
 * Onboarding step 4 — the app drawer: "Show apps as" and the search options, with the same live preview the
 * App drawer settings screen shows ([AppDrawerSettingsPreview]) so each choice is visible as it is made.
 */
@Composable
fun OnboardingDrawerSetupPage(
    uiState: OnboardingUiState,
    onDrawerPresentationChange: (DrawerPresentation) -> Unit,
    onSearchBarPositionChange: (SearchBarPosition) -> Unit,
    onSearchContactsToggle: (Boolean) -> Unit,
    onSearchSettingsToggle: (Boolean) -> Unit,
    onDrawerFolderDisplayModeChange: (DrawerFolderDisplayMode) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val previewSettings = remember(uiState.drawerPresentation, uiState.searchBarPosition, uiState.drawerFolderDisplayMode) {
        LauncherSettings(
            drawerPresentation = uiState.drawerPresentation,
            searchBarPosition = uiState.searchBarPosition,
            drawerFolderDisplayMode = uiState.drawerFolderDisplayMode,
        )
    }
    Column(modifier = modifier.fillMaxSize().testTag("onboarding_drawer_setup_page")) {
        Column(modifier = Modifier.padding(horizontal = 24.dp).padding(top = 48.dp)) {
            Text(text = stringResource(R.string.onboarding_drawer_setup_headline), style = MaterialTheme.typography.headlineSmall, color = Ink)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = stringResource(R.string.onboarding_drawer_setup_subhead), style = MaterialTheme.typography.bodyLarge, color = Muted)
        }

        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 24.dp)) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                AppDrawerSettingsPreview(settings = previewSettings, wallpaper = HomeWallpaper.Unavailable, installedApps = uiState.installedApps)
                Spacer(modifier = Modifier.height(16.dp))
                DrawerSetupOptionsCard(
                    uiState = uiState,
                    onDrawerPresentationChange = onDrawerPresentationChange,
                    onSearchBarPositionChange = onSearchBarPositionChange,
                    onSearchContactsToggle = onSearchContactsToggle,
                    onSearchSettingsToggle = onSearchSettingsToggle,
                    onDrawerFolderDisplayModeChange = onDrawerFolderDisplayModeChange,
                )
            }
            item { Spacer(modifier = Modifier.height(96.dp)) }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 34.dp, top = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OnboardingDots(step = 3, totalSteps = ONBOARDING_STEP_COUNT)
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                TextActionButton(
                    text = stringResource(R.string.action_back),
                    onClick = onBack,
                    color = Muted,
                    modifier = Modifier.testTag("onboarding_back"),
                )
                TextActionButton(
                    text = stringResource(R.string.action_next),
                    onClick = onNext,
                    modifier = Modifier.testTag("onboarding_next"),
                )
            }
        }
    }
}

@Composable
private fun DrawerSetupOptionsCard(
    uiState: OnboardingUiState,
    onDrawerPresentationChange: (DrawerPresentation) -> Unit,
    onSearchBarPositionChange: (SearchBarPosition) -> Unit,
    onSearchContactsToggle: (Boolean) -> Unit,
    onSearchSettingsToggle: (Boolean) -> Unit,
    onDrawerFolderDisplayModeChange: (DrawerFolderDisplayMode) -> Unit,
) {
    SettingsCard {
        LabeledDropdownRow(
            title = stringResource(R.string.app_drawer_show_apps_as),
            options = DrawerPresentation.entries,
            selected = uiState.drawerPresentation,
            label = { stringResource(it.displayNameRes) },
            onSelect = onDrawerPresentationChange,
            testTag = "onboarding_drawer_presentation_row",
        )
        CardDivider()
        LabeledDropdownRow(
            title = stringResource(R.string.app_drawer_search_bar_position),
            options = SearchBarPosition.entries,
            selected = uiState.searchBarPosition,
            label = { stringResource(it.displayNameRes) },
            onSelect = onSearchBarPositionChange,
            testTag = "onboarding_search_bar_position_row",
        )
        CardDivider()
        SettingsToggleRow(
            title = stringResource(R.string.app_drawer_search_contacts_title),
            subtitle = stringResource(R.string.app_drawer_search_contacts_subtitle),
            checked = uiState.searchContactsEnabled,
            onCheckedChange = onSearchContactsToggle,
            testTag = "onboarding_search_contacts_toggle",
        )
        CardDivider()
        SettingsToggleRow(
            title = stringResource(R.string.app_drawer_search_settings_title),
            subtitle = stringResource(R.string.app_drawer_search_settings_subtitle),
            checked = uiState.searchSettingsEnabled,
            onCheckedChange = onSearchSettingsToggle,
            testTag = "onboarding_search_settings_toggle",
        )
        CardDivider()
        LabeledDropdownRow(
            title = stringResource(R.string.app_drawer_folders_in_drawer),
            options = DrawerFolderDisplayMode.entries,
            selected = uiState.drawerFolderDisplayMode,
            label = { stringResource(it.displayNameRes) },
            onSelect = onDrawerFolderDisplayModeChange,
            testTag = "onboarding_drawer_folder_display_mode_row",
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun OnboardingDrawerSetupPagePreview() {
    FacetLauncherTheme {
        OnboardingDrawerSetupPage(
            uiState = OnboardingUiState(),
            onDrawerPresentationChange = {},
            onSearchBarPositionChange = {},
            onSearchContactsToggle = {},
            onSearchSettingsToggle = {},
            onDrawerFolderDisplayModeChange = {},
            onBack = {},
            onNext = {},
        )
    }
}
