package com.lumenlauncher.app.ui.settings

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.data.model.AppRowPosition
import com.lumenlauncher.app.data.model.AppRowPresentation
import com.lumenlauncher.app.data.model.ClockColorOption
import com.lumenlauncher.app.data.model.DockDisplayMode
import com.lumenlauncher.app.data.model.FontWeightOption
import com.lumenlauncher.app.data.model.IconRenderMode
import com.lumenlauncher.app.data.model.LauncherFontOption
import com.lumenlauncher.app.data.model.LauncherSettings
import com.lumenlauncher.app.data.model.NotificationBadgeStyle
import com.lumenlauncher.app.data.model.ThemeMode
import com.lumenlauncher.app.ui.components.BackButton
import com.lumenlauncher.app.ui.components.CardDivider
import com.lumenlauncher.app.ui.components.FontWeightSlider
import com.lumenlauncher.app.ui.components.LabeledDropdownRow
import com.lumenlauncher.app.ui.components.SettingsCard
import com.lumenlauncher.app.ui.components.StickyHeaderLayout
import com.lumenlauncher.app.ui.home.AppRow
import com.lumenlauncher.app.ui.home.DockIcon
import com.lumenlauncher.app.ui.theme.AccentSwatch
import com.lumenlauncher.app.ui.theme.Hairline
import com.lumenlauncher.app.ui.theme.Ink
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme
import com.lumenlauncher.app.ui.theme.Muted
import com.lumenlauncher.app.ui.theme.Surface
import com.lumenlauncher.app.ui.theme.SurfaceContainer
import com.lumenlauncher.app.ui.theme.Wallpaper
import com.lumenlauncher.app.ui.theme.resolve

/**
 * Settings → Appearance (`3c`) — theme, accent color, icons, and launcher font/app-label-color,
 * split out of the main Settings list into its own screen (see chat history: the main list was
 * getting too long to scan). Every field here is global only, no per-profile override.
 */
@Composable
fun AppearanceSettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AppearanceSettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsState()
    AppearanceSettingsContent(
        settings = settings,
        onBack = onBack,
        onThemeModeChanged = viewModel::setThemeMode,
        onAccentFromSystemChanged = viewModel::setAccentFromSystem,
        onCustomAccentSwatchChanged = viewModel::setCustomAccentSwatch,
        onIconRenderModeChanged = viewModel::setIconRenderMode,
        onLauncherFontOptionChanged = viewModel::setLauncherFontOption,
        onAppLabelColorOptionChanged = viewModel::setAppLabelColorOption,
        onHomeAppsFontWeightChanged = viewModel::setHomeAppsFontWeight,
        modifier = modifier,
    )
}

@Composable
private fun AppearanceSettingsContent(
    settings: LauncherSettings,
    onBack: () -> Unit,
    onThemeModeChanged: (ThemeMode) -> Unit,
    onAccentFromSystemChanged: (Boolean) -> Unit,
    onCustomAccentSwatchChanged: (AccentSwatch) -> Unit,
    onIconRenderModeChanged: (IconRenderMode) -> Unit,
    onLauncherFontOptionChanged: (LauncherFontOption) -> Unit,
    onAppLabelColorOptionChanged: (ClockColorOption) -> Unit,
    onHomeAppsFontWeightChanged: (FontWeightOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    StickyHeaderLayout(
        modifier = modifier,
        header = { AppearanceSettingsHeader(onBack = onBack) },
        content = { headerHeight ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SurfaceContainer)
                    .testTag("appearance_settings_screen")
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .padding(horizontal = 24.dp),
                contentPadding = PaddingValues(top = headerHeight, bottom = 24.dp),
            ) {
                item {
                    AppearancePreviewCard(
                        appRowPosition = settings.appRowPosition,
                        appRowPresentation = settings.appRowPresentation,
                        appLabelColorOption = settings.appLabelColorOption,
                        homeAppsFontWeight = settings.homeAppsFontWeight,
                        dockDisplayMode = settings.dockDisplayMode,
                        modifier = Modifier.padding(bottom = 20.dp).testTag("appearance_preview_card"),
                    )
                }
                item {
                    SettingsCard {
                        LabeledDropdownRow(
                            title = "Launcher theme",
                            options = ThemeMode.entries,
                            selected = settings.themeMode,
                            label = { it.appearanceDisplayLabel() },
                            onSelect = onThemeModeChanged,
                            testTag = "theme_mode_dropdown",
                        )
                        CardDivider()
                        AccentColorSection(
                            accentFromSystem = settings.accentFromSystem,
                            customAccentSwatch = settings.customAccentSwatch,
                            onAccentFromSystemChanged = onAccentFromSystemChanged,
                            onCustomAccentSwatchChanged = onCustomAccentSwatchChanged,
                        )
                        CardDivider()
                        LabeledDropdownRow(
                            title = "Icons",
                            options = IconRenderMode.entries,
                            selected = settings.iconRenderMode,
                            label = { it.displayName },
                            onSelect = onIconRenderModeChanged,
                            testTag = "appearance_icons_row",
                        )
                        CardDivider()
                        LabeledDropdownRow(
                            title = "Launcher Font",
                            options = LauncherFontOption.entries,
                            selected = settings.launcherFontOption,
                            label = { it.displayName },
                            onSelect = onLauncherFontOptionChanged,
                            testTag = "appearance_font_row",
                        )
                        CardDivider()
                        FontWeightSlider(
                            selected = settings.homeAppsFontWeight,
                            onSelectedChange = onHomeAppsFontWeightChanged,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 13.dp)
                                .testTag("appearance_font_weight_slider"),
                            sliderTestTag = "appearance_font_weight_slider_control",
                        )
                        CardDivider()
                        LabeledDropdownRow(
                            title = "App label color",
                            options = ClockColorOption.entries,
                            selected = settings.appLabelColorOption,
                            label = { it.displayName },
                            onSelect = onAppLabelColorOptionChanged,
                            testTag = "appearance_app_label_color_row",
                        )
                    }
                }
            }
        },
    )
}

/**
 * Shows how this screen's own settings actually render on Home/Dock, using real production
 * components ([AppRow]/[DockIcon] — the latter already `internal` for the profile carousel's own
 * live preview, the former promoted `internal` for exactly this reuse too) rather than a
 * hand-drawn mockup. Needs no preview-only state: [AppearanceSettingsViewModel.settings] already
 * updates immediately after every setter call, so this just reads the same live values the rest
 * of the screen's controls write to. Backed by [Wallpaper] (Home's own real backdrop tone, not
 * [Surface]/[SurfaceContainer]) so [AppRow]/[DockIcon]'s text-shadow treatment — tuned for
 * legibility over an arbitrary wallpaper, not a flat card — renders exactly as it would on Home,
 * not washed out against a plain light card. The two sample apps are synthetic (`icon = null`,
 * same placeholder-icon convention already used by this file's own `@Preview`s) — Theme/Accent/
 * Launcher Font/Font weight/App label color all render correctly from that alone, since none of
 * them need a real icon bitmap to demonstrate; Icon render mode specifically has nothing to tint
 * without one, so it isn't visually demonstrated here (this preview's own icons always fall back
 * to the placeholder, regardless of that setting).
 */
@Composable
private fun AppearancePreviewCard(
    appRowPosition: AppRowPosition,
    appRowPresentation: AppRowPresentation,
    appLabelColorOption: ClockColorOption,
    homeAppsFontWeight: FontWeightOption,
    dockDisplayMode: DockDisplayMode,
    modifier: Modifier = Modifier,
) {
    val labelColor = appLabelColorOption.resolve()
    val labelFontWeight = homeAppsFontWeight.resolve()
    val shape = MaterialTheme.shapes.medium
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = shape)
            .background(Wallpaper, shape)
            .border(1.dp, Hairline, shape)
            .padding(16.dp),
    ) {
        Text(
            text = "PREVIEW",
            style = MaterialTheme.typography.labelSmall,
            color = Muted,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        PreviewApps.forEach { app ->
            AppRow(
                app = app,
                onClick = {},
                badgeCount = null,
                badgeStyle = NotificationBadgeStyle.DOT,
                onRequestShortcuts = { emptyList() },
                onLaunchShortcut = {},
                position = appRowPosition,
                presentation = appRowPresentation,
                labelColor = labelColor,
                labelFontWeight = labelFontWeight,
                enableLongPressMenu = false,
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            DockIcon(
                app = PreviewDockApp,
                displayMode = dockDisplayMode,
                onClick = {},
                labelColor = labelColor,
                labelFontWeight = labelFontWeight,
                enableLongPressMenu = false,
            )
        }
    }
}

private val PreviewApps = listOf(
    AppInfo(packageName = "preview.appearance.one", activityName = ".Main", label = "Camera", icon = null),
    AppInfo(packageName = "preview.appearance.two", activityName = ".Main", label = "Messages", icon = null),
)
private val PreviewDockApp = AppInfo(packageName = "preview.appearance.dock", activityName = ".Main", label = "Phone", icon = null)

@Composable
private fun AppearanceSettingsHeader(onBack: () -> Unit, modifier: Modifier = Modifier) {
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
        Text(text = "Appearance", style = MaterialTheme.typography.headlineSmall, color = Ink)
    }
}

private fun ThemeMode.appearanceDisplayLabel(): String = when (this) {
    ThemeMode.LIGHT -> "Light"
    ThemeMode.DARK -> "Dark"
    ThemeMode.SYSTEM -> "System"
}

/**
 * Settings → Appearance → Accent color. Two sources, matching the system "Wallpaper & style"
 * picker's own interaction model: **Wallpaper colors** (Material You, the default) computes and
 * applies automatically — no picker shown; **Basic colors** reveals the curated [AccentSwatch]
 * grid below it. Each swatch renders both its light and dark half at once (see
 * [AccentSwatchCircle]) since picking one stores both — switching the theme mode restores the
 * right half for the *same* pick rather than needing to re-choose.
 */
@Composable
private fun AccentColorSection(
    accentFromSystem: Boolean,
    customAccentSwatch: String?,
    onAccentFromSystemChanged: (Boolean) -> Unit,
    onCustomAccentSwatchChanged: (AccentSwatch) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(vertical = 8.dp)) {
        Text(text = "Accent color", style = MaterialTheme.typography.bodyLarge, color = Ink, modifier = Modifier.padding(bottom = 10.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PillOption(
                label = "Wallpaper colors",
                selected = accentFromSystem,
                onClick = { onAccentFromSystemChanged(true) },
                testTag = "accent_source_wallpaper",
                modifier = Modifier.weight(1f),
            )
            PillOption(
                label = "Basic colors",
                selected = !accentFromSystem,
                onClick = { onAccentFromSystemChanged(false) },
                testTag = "accent_source_basic",
                modifier = Modifier.weight(1f),
            )
        }
        if (!accentFromSystem) {
            Spacer(modifier = Modifier.height(16.dp))
            AccentSwatchGrid(selected = customAccentSwatch, onSelect = onCustomAccentSwatchChanged)
        }
    }
}

@Composable
private fun PillOption(label: String, selected: Boolean, onClick: () -> Unit, testTag: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .testTag(testTag)
            // M3's SegmentedButton defaults to CornerFull — see CLAUDE.md's Material 3 shape section.
            .clip(CircleShape)
            .background(if (selected) Ink.copy(alpha = 0.06f) else Surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Ink for both — selection is already conveyed by the pill's own background tint above;
        // dimming the unselected label's text on top of that read as disabled (see chat history).
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = Ink)
    }
}

private const val ACCENT_SWATCH_GRID_COLUMNS = 5

@Composable
private fun AccentSwatchGrid(selected: String?, onSelect: (AccentSwatch) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.testTag("accent_swatch_grid"), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AccentSwatch.entries.chunked(ACCENT_SWATCH_GRID_COLUMNS).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                row.forEach { swatch ->
                    // Defaults to the first swatch once "Basic colors" is picked with nothing
                    // chosen yet, matching README's Accent stand-in (BLUE == the app's original
                    // default accent values).
                    AccentSwatchCircle(
                        swatch = swatch,
                        selected = selected?.let { AccentSwatch.valueOf(it) } == swatch ||
                            (selected == null && swatch == AccentSwatch.entries.first()),
                        onClick = { onSelect(swatch) },
                    )
                }
            }
        }
    }
}

@Composable
private fun AccentSwatchCircle(swatch: AccentSwatch, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .testTag("accent_swatch_${swatch.name}"),
        contentAlignment = Alignment.Center,
    ) {
        Row(modifier = Modifier.matchParentSize()) {
            Box(modifier = Modifier.weight(1f).fillMaxHeight().background(swatch.light))
            Box(modifier = Modifier.weight(1f).fillMaxHeight().background(swatch.dark))
        }
        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = swatch.label,
                tint = Surface,
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Ink.copy(alpha = 0.35f))
                    .padding(3.dp),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AppearanceSettingsScreenPreview() {
    LumenLauncherTheme {
        AppearanceSettingsContent(
            settings = LauncherSettings(),
            onBack = {},
            onThemeModeChanged = {},
            onAccentFromSystemChanged = {},
            onCustomAccentSwatchChanged = {},
            onIconRenderModeChanged = {},
            onLauncherFontOptionChanged = {},
            onAppLabelColorOptionChanged = {},
            onHomeAppsFontWeightChanged = {},
        )
    }
}
