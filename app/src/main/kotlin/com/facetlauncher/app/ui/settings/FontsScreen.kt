package com.facetlauncher.app.ui.settings

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.FontScaleOption
import com.facetlauncher.app.data.model.FontWeightOption
import com.facetlauncher.app.data.model.HomeWallpaper
import com.facetlauncher.app.data.model.LauncherFontOption
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.ui.components.CardDivider
import com.facetlauncher.app.ui.components.FontSizeSlider
import com.facetlauncher.app.ui.components.FontWeightSlider
import com.facetlauncher.app.ui.components.LabeledDropdownRow
import com.facetlauncher.app.ui.components.SettingsCard
import com.facetlauncher.app.ui.components.StickyHeaderLayout
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.SurfaceContainer

/**
 * Settings → Appearance → Fonts — launcher font, font color, size and weight, with a sample of the text. Global only: a facet has no
 * override for any of these, so a facet's Appearance screen has no link here. Shares [AppearanceSettingsViewModel] with [AppearanceSettingsScreen].
 */
@Composable
fun FontsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AppearanceSettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val homeWallpaper by viewModel.homeWallpaper.collectAsState()
    FontsContent(
        settings = settings,
        uiState = uiState,
        homeWallpaper = homeWallpaper,
        onBack = onBack,
        onLauncherFontOptionChange = viewModel::setLauncherFontOption,
        onAppLabelColorOptionChange = viewModel::setAppLabelColorOption,
        onHomeAppsFontWeightChange = viewModel::setHomeAppsFontWeight,
        onFontScaleOptionChange = viewModel::setFontScaleOption,
        modifier = modifier,
    )
}

@Composable
private fun FontsContent(
    settings: LauncherSettings,
    uiState: AppearanceSettingsUiState,
    homeWallpaper: HomeWallpaper,
    onBack: () -> Unit,
    onLauncherFontOptionChange: (LauncherFontOption) -> Unit,
    onAppLabelColorOptionChange: (ClockColorOption) -> Unit,
    onHomeAppsFontWeightChange: (FontWeightOption) -> Unit,
    onFontScaleOptionChange: (FontScaleOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    StickyHeaderLayout(
        modifier = modifier,
        header = { AppearanceSettingsHeader(title = stringResource(R.string.appearance_section_fonts), onBack = onBack) },
        content = { headerHeight ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SurfaceContainer)
                    .testTag("fonts_screen")
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .padding(horizontal = 24.dp),
                contentPadding = PaddingValues(top = headerHeight, bottom = 24.dp),
            ) {
                item {
                    AppearancePreviewCard(
                        uiState = uiState,
                        homeWallpaper = homeWallpaper,
                        modifier = Modifier.padding(bottom = 20.dp).testTag("appearance_preview_card"),
                    )
                }
                item {
                    FontsCard(
                        settings = settings,
                        onLauncherFontOptionChange = onLauncherFontOptionChange,
                        onAppLabelColorOptionChange = onAppLabelColorOptionChange,
                        onHomeAppsFontWeightChange = onHomeAppsFontWeightChange,
                        onFontScaleOptionChange = onFontScaleOptionChange,
                    )
                }
            }
        },
    )
}

@Composable
private fun FontsCard(
    settings: LauncherSettings,
    onLauncherFontOptionChange: (LauncherFontOption) -> Unit,
    onAppLabelColorOptionChange: (ClockColorOption) -> Unit,
    onHomeAppsFontWeightChange: (FontWeightOption) -> Unit,
    onFontScaleOptionChange: (FontScaleOption) -> Unit,
) {
    SettingsCard {
        LabeledDropdownRow(
            title = stringResource(R.string.appearance_launcher_font),
            options = LauncherFontOption.entries,
            selected = settings.launcherFontOption,
            label = { stringResource(it.displayNameRes) },
            onSelect = onLauncherFontOptionChange,
            testTag = "appearance_font_row",
        )
        CardDivider()
        LabeledDropdownRow(
            title = stringResource(R.string.appearance_font_color),
            options = ClockColorOption.entries,
            selected = settings.appLabelColorOption,
            label = { stringResource(it.displayNameRes) },
            onSelect = onAppLabelColorOptionChange,
            testTag = "appearance_app_label_color_row",
        )
        CardDivider()
        FontSizeSlider(
            selected = settings.fontScaleOption,
            onSelectedChange = onFontScaleOptionChange,
            label = stringResource(R.string.appearance_font_size),
            showPreview = false,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 13.dp)
                .testTag("appearance_font_size_slider"),
            sliderTestTag = "appearance_font_size_slider_control",
        )
        CardDivider()
        FontWeightSlider(
            selected = settings.homeAppsFontWeight,
            onSelectedChange = onHomeAppsFontWeightChange,
            label = stringResource(R.string.appearance_font_weight),
            showPreview = false,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 13.dp)
                .testTag("appearance_font_weight_slider"),
            sliderTestTag = "appearance_font_weight_slider_control",
        )
        CardDivider()
        FontPreviewRow(settings = settings)
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FontsScreenPreview() {
    FacetLauncherTheme {
        FontsContent(
            settings = LauncherSettings(),
            uiState = AppearanceSettingsUiState(globalFavorites = DesignTimeFavorites, globalDockItems = DesignTimeDockItems),
            homeWallpaper = HomeWallpaper.Unavailable,
            onBack = {},
            onLauncherFontOptionChange = {},
            onAppLabelColorOptionChange = {},
            onHomeAppsFontWeightChange = {},
            onFontScaleOptionChange = {},
        )
    }
}
