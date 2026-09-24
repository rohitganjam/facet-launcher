package com.facetlauncher.app.ui.settings

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.facetlauncher.app.R
import com.facetlauncher.app.data.DockAppRepository
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppListLimits
import com.facetlauncher.app.data.model.AppListVerticalAlignment
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.model.CalendarEvent
import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.DockDisplayMode
import com.facetlauncher.app.data.model.DrawerPresentation
import com.facetlauncher.app.data.model.FontScaleOption
import com.facetlauncher.app.data.model.FontWeightOption
import com.facetlauncher.app.data.model.HomeWallpaper
import com.facetlauncher.app.data.model.IconRenderMode
import com.facetlauncher.app.data.model.LauncherFontOption
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.NotificationBadgeStyle
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.data.model.ThemeMode
import com.facetlauncher.app.data.model.WallpaperAccentRole
import com.facetlauncher.app.ui.components.BackButton
import com.facetlauncher.app.ui.components.CardDivider
import com.facetlauncher.app.ui.components.FontSizeSlider
import com.facetlauncher.app.ui.components.FontWeightSlider
import com.facetlauncher.app.ui.components.LabeledDropdownRow
import com.facetlauncher.app.ui.components.SettingsCard
import com.facetlauncher.app.ui.components.StickyHeaderLayout
import com.facetlauncher.app.ui.components.WallpaperBackground
import com.facetlauncher.app.ui.home.AppRow
import com.facetlauncher.app.ui.home.ClockBlock
import com.facetlauncher.app.ui.home.DockIcon
import com.facetlauncher.app.ui.home.FolderRow
import com.facetlauncher.app.ui.theme.AccentSwatch
import com.facetlauncher.app.ui.theme.Hairline
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.LocalDynamicColorRefreshSignal
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.Surface
import com.facetlauncher.app.ui.theme.SurfaceContainer
import com.facetlauncher.app.ui.theme.resolve
import com.facetlauncher.app.ui.theme.toneOf

/**
 * Settings → Appearance (`3c`) — theme, accent color, icons, launcher font/app-label-color, and
 * (per-field `LAUNCHER_DEFAULT`-sentinel overridable — see [AppearanceSettingsUiState]'s own doc)
 * dock display style / app-list presentation, position, and vertical alignment, split out of the
 * main Settings list into its own screen (see chat history: the main list was getting too long to
 * scan). Also hosts the "Clock style" nav row (global mode only — a facet's own Clock style has
 * its own separate row on `FacetSettingsScreen`), moved in from the main Settings list since
 * Appearance now governs the whole home-screen look, clock included.
 */
@Composable
fun AppearanceSettingsScreen(
    onBack: () -> Unit,
    onNavigateToClockStyleGallery: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AppearanceSettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val homeWallpaper by viewModel.homeWallpaper.collectAsState()
    AppearanceSettingsContent(
        settings = settings,
        uiState = uiState,
        homeWallpaper = homeWallpaper,
        onBack = onBack,
        onNavigateToClockStyleGallery = onNavigateToClockStyleGallery,
        onThemeModeChange = viewModel::setThemeMode,
        onAccentFromSystemChange = viewModel::setAccentFromSystem,
        onCustomAccentSwatchChange = viewModel::setCustomAccentSwatch,
        onWallpaperAccentRoleChange = viewModel::setWallpaperAccentRole,
        onIconRenderModeChange = viewModel::setIconRenderMode,
        onLauncherFontOptionChange = viewModel::setLauncherFontOption,
        onAppLabelColorOptionChange = viewModel::setAppLabelColorOption,
        onHomeAppsFontWeightChange = viewModel::setHomeAppsFontWeight,
        onFontScaleOptionChange = viewModel::setFontScaleOption,
        onDockDisplayModeChange = viewModel::setDockDisplayMode,
        onAppRowPositionChange = viewModel::setAppRowPosition,
        onAppRowPresentationChange = viewModel::setAppRowPresentation,
        onAppListVerticalAlignmentChange = viewModel::setAppListVerticalAlignment,
        modifier = modifier,
    )
}

@Composable
private fun AppearanceSettingsContent(
    settings: LauncherSettings,
    uiState: AppearanceSettingsUiState,
    homeWallpaper: HomeWallpaper,
    onBack: () -> Unit,
    onNavigateToClockStyleGallery: () -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onAccentFromSystemChange: (Boolean) -> Unit,
    onCustomAccentSwatchChange: (AccentSwatch) -> Unit,
    onWallpaperAccentRoleChange: (WallpaperAccentRole) -> Unit,
    onIconRenderModeChange: (IconRenderMode) -> Unit,
    onLauncherFontOptionChange: (LauncherFontOption) -> Unit,
    onAppLabelColorOptionChange: (ClockColorOption) -> Unit,
    onHomeAppsFontWeightChange: (FontWeightOption) -> Unit,
    onFontScaleOptionChange: (FontScaleOption) -> Unit,
    onDockDisplayModeChange: (DockDisplayMode) -> Unit,
    onAppRowPositionChange: (AppRowPosition) -> Unit,
    onAppRowPresentationChange: (AppRowPresentation) -> Unit,
    onAppListVerticalAlignmentChange: (AppListVerticalAlignment) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isFacetScoped = uiState.isFacetScoped
    // LAUNCHER_DEFAULT only makes sense where there's a global value above to inherit from —
    // offering it in global mode would be circular (see ClockFontOption's own precedent, which
    // this mirrors).
    val dockDisplayModeOptions = if (isFacetScoped) DockDisplayMode.entries else DockDisplayMode.entries - DockDisplayMode.LAUNCHER_DEFAULT
    val appRowPositionOptions = if (isFacetScoped) AppRowPosition.entries else AppRowPosition.entries - AppRowPosition.LAUNCHER_DEFAULT
    val appRowPresentationOptions = if (isFacetScoped) AppRowPresentation.entries else AppRowPresentation.entries - AppRowPresentation.LAUNCHER_DEFAULT
    val appListVerticalAlignmentOptions = if (isFacetScoped) AppListVerticalAlignment.entries else AppListVerticalAlignment.entries - AppListVerticalAlignment.LAUNCHER_DEFAULT
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
                        settings = settings,
                        isFacetScoped = isFacetScoped,
                        appRowPosition = uiState.appRowPosition,
                        appRowPresentation = uiState.appRowPresentation,
                        dockDisplayMode = uiState.dockDisplayMode,
                        favorites = uiState.effectiveFavorites,
                        dockItems = uiState.effectiveDockItems,
                        calendarEvents = uiState.calendarEvents,
                        homeWallpaper = homeWallpaper,
                        modifier = Modifier.padding(bottom = 20.dp).testTag("appearance_preview_card"),
                    )
                }
                item { AppearanceSectionHeader(stringResource(R.string.appearance_section_dock_home)) }
                item {
                    // "How things look/are placed" for Dock and the Home apps list — moved in from
                    // Dock's/Home-Apps-List's own screens (see chat history), always shown at both
                    // scopes: LAUNCHER_DEFAULT itself (filtered from the option list in global mode)
                    // is what lets a facet revert to inheriting, so there's no separate
                    // Inherit/Override switch gating this card the way Clock/Calendar/Apps/Dock's
                    // own content screens have one.
                    SettingsCard {
                        LabeledDropdownRow(
                            title = stringResource(R.string.dock_display_style),
                            options = dockDisplayModeOptions,
                            selected = uiState.dockDisplayMode,
                            label = { stringResource(it.displayNameRes) },
                            onSelect = onDockDisplayModeChange,
                            testTag = "appearance_dock_display_style_row",
                        )
                        CardDivider()
                        LabeledDropdownRow(
                            title = stringResource(R.string.home_apps_list_position),
                            options = appRowPositionOptions,
                            selected = uiState.appRowPosition,
                            label = { stringResource(it.displayNameRes) },
                            onSelect = onAppRowPositionChange,
                            testTag = "appearance_app_row_position_row",
                        )
                        CardDivider()
                        LabeledDropdownRow(
                            title = stringResource(R.string.home_apps_list_presentation),
                            options = appRowPresentationOptions,
                            selected = uiState.appRowPresentation,
                            label = { stringResource(it.displayNameRes) },
                            onSelect = onAppRowPresentationChange,
                            testTag = "appearance_app_row_presentation_row",
                        )
                        CardDivider()
                        LabeledDropdownRow(
                            title = stringResource(R.string.home_apps_list_list_position),
                            options = appListVerticalAlignmentOptions,
                            selected = uiState.appListVerticalAlignment,
                            label = { stringResource(it.displayNameRes) },
                            onSelect = onAppListVerticalAlignmentChange,
                            testTag = "appearance_app_list_vertical_alignment_row",
                        )
                    }
                }
                // Every card below is global only — no per-facet override exists for these fields
                // yet (see this screen's own doc comment) — so they're simply absent in facet mode,
                // same reasoning Calendar/Clock use for their own global-only fields.
                if (!isFacetScoped) {
                    item { AppearanceSectionHeader(stringResource(R.string.appearance_section_clock)) }
                    item {
                        SettingsCard {
                            ClickableRow(
                                title = stringResource(R.string.settings_clock_calendar_title),
                                subtitle = stringResource(
                                    R.string.dot_join_2,
                                    stringResource(settings.clockTemplateId.displayNameRes),
                                    stringResource(if (settings.use24HourTime) R.string.settings_time_format_24h else R.string.settings_time_format_12h),
                                ),
                                onClick = onNavigateToClockStyleGallery,
                                testTag = "clock_style_gallery_row",
                                trailing = { NavigationChevron() },
                            )
                        }
                    }
                    item { AppearanceSectionHeader(stringResource(R.string.appearance_section_general)) }
                    item {
                        SettingsCard {
                            LabeledDropdownRow(
                                title = stringResource(R.string.appearance_launcher_theme),
                                options = ThemeMode.entries,
                                selected = settings.themeMode,
                                label = { stringResource(it.displayNameRes) },
                                onSelect = onThemeModeChange,
                                testTag = "theme_mode_dropdown",
                            )
                            CardDivider()
                            AccentColorSection(
                                accentFromSystem = settings.accentFromSystem,
                                customAccentSwatch = settings.customAccentSwatch,
                                wallpaperAccentRole = settings.wallpaperAccentRole,
                                onAccentFromSystemChange = onAccentFromSystemChange,
                                onCustomAccentSwatchChange = onCustomAccentSwatchChange,
                                onWallpaperAccentRoleChange = onWallpaperAccentRoleChange,
                            )
                            CardDivider()
                            LabeledDropdownRow(
                                title = stringResource(R.string.appearance_icon_style),
                                options = IconRenderMode.entries,
                                selected = settings.iconRenderMode,
                                label = { stringResource(it.displayNameRes) },
                                onSelect = onIconRenderModeChange,
                                testTag = "appearance_icons_row",
                            )
                            CardDivider()
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
                        }
                    }
                }
            }
        },
    )
}

/**
 * Not an arbitrary "glanceable strip" cap — [AppListLimits.MAX_FAVORITES]/[DockAppRepository.MAX_APPS]
 * are the real caps a favorites/dock list can never exceed, so `.take()`ing them is a no-op safety
 * net, not a lossy truncation. An earlier version of this preview capped at 2/3 regardless of how
 * many favorites/dock apps were actually configured — caught via direct user report: 4 configured
 * favorites showed only 2, 5 configured dock apps showed only 3 (see chat history).
 */
private const val PREVIEW_HOME_APP_COUNT = AppListLimits.MAX_FAVORITES
private const val PREVIEW_DOCK_APP_COUNT = DockAppRepository.MAX_APPS

/** Matches `FacetCarouselScreen`'s own `CAROUSEL_CARD_SCALE` — a genuine scale model of the
 * screen, not a full-width/full-height card. */
private const val APPEARANCE_PREVIEW_CARD_SCALE = 0.55f

@Composable
private fun AppearanceSectionHeader(title: String, modifier: Modifier = Modifier) {
    // Matches SettingsScreen's own SectionHeader spacing exactly (top = 18.dp, bottom = 6.dp) — a
    // missing top padding here left "CLOCK"/"GENERAL" sitting flush against the card above them,
    // with no breathing room, unlike every section header on the main Settings screen.
    Text(text = title, style = MaterialTheme.typography.labelSmall, color = Muted, modifier = modifier.padding(top = 18.dp, bottom = 6.dp))
}

/**
 * "This is how Home looks" — scaled to a real fraction of the device screen and density-scaled to
 * match (same `BoxWithConstraints` + scaled-`LocalDensity` technique
 * [com.facetlauncher.app.ui.facets.FacetCarouselScreen]'s own `FacetPreviewPage` uses for its
 * carousel cards — deliberately reimplemented here rather than shared, to avoid any regression
 * risk to that already-shipped, tested carousel; this composable owns no interactive/click-to-apply
 * behavior FacetPreviewPage has, since it's read-only), so there's room for clock, calendar, the
 * app list, and the dock together without any one of them crowding the others out. Every list here
 * is this scope's real, live content — [favorites]/[dockItems]/[calendarEvents] are the actual
 * resolved facet-or-global lists ([AppearanceSettingsUiState.effectiveFavorites]/
 * [AppearanceSettingsUiState.effectiveDockItems]/[AppearanceSettingsUiState.calendarEvents]), not
 * sample/mock data (see chat history) — global scope shows the launcher-wide defaults directly,
 * facet scope shows the resolved facet-or-default output, exactly like `HomeAppsListSettingsScreen`/
 * `DockSettingsScreen`'s own preview cards and Home itself. The clock uses the real system clock
 * (its own default), not a fixed reference instant, so real calendar events read sensibly against
 * it. Facet-scoped mode omits the clock+calendar entirely — a facet's own clock look/preview lives
 * on its separate "Clock style" screen instead.
 */
@Composable
private fun AppearancePreviewCard(
    settings: LauncherSettings,
    isFacetScoped: Boolean,
    appRowPosition: AppRowPosition,
    appRowPresentation: AppRowPresentation,
    dockDisplayMode: DockDisplayMode,
    favorites: List<PlacedItem>,
    dockItems: List<PlacedItem>,
    calendarEvents: List<CalendarEvent>,
    homeWallpaper: HomeWallpaper,
    modifier: Modifier = Modifier,
) {
    val config = LocalConfiguration.current
    val screenAspectRatio = config.screenWidthDp.toFloat() / config.screenHeightDp.toFloat()
    val cardShape = MaterialTheme.shapes.extraLarge
    // Scaled to a fraction of the real screen — same APPEARANCE_PREVIEW_CARD_SCALE the facet
    // carousel's own cards use (CAROUSEL_CARD_SCALE), not the full device width/height (see chat
    // history: an earlier draft locked this to fillMaxWidth() at the real aspect ratio, which on
    // a typical phone made the card nearly full-screen-tall and pushed every card below it off
    // the initial viewport).
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth(APPEARANCE_PREVIEW_CARD_SCALE)
                .aspectRatio(screenAspectRatio)
                .shadow(elevation = 4.dp, shape = cardShape)
                .clip(cardShape)
                .border(width = 1.dp, color = Hairline, shape = cardShape),
        ) {
        WallpaperBackground(homeWallpaper, Modifier.matchParentSize())

        val screenHeight = config.screenHeightDp.dp
        val contentScale = (maxHeight / screenHeight).coerceIn(0.4f, 1f)
        val baseDensity = LocalDensity.current
        CompositionLocalProvider(
            LocalDensity provides Density(baseDensity.density * contentScale, baseDensity.fontScale),
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (!isFacetScoped) {
                    ClockBlock(
                        use24HourTime = settings.use24HourTime,
                        templateId = settings.clockTemplateId,
                        fontOption = settings.clockFontOption,
                        colorOption = settings.clockColorOption,
                        accentColorOption = settings.clockAccentColorOption,
                        showMeridiem = settings.clockShowMeridiem,
                        dateStyle = settings.clockDateStyle,
                        clockAlignment = settings.clockAlignment,
                        events = calendarEvents,
                        calendarColors = settings.calendarColors,
                        homeAppsFontWeight = settings.homeAppsFontWeight,
                        appLabelColorOption = settings.appLabelColorOption,
                        launcherFontOption = settings.launcherFontOption,
                        clockScale = 1f,
                        modifier = Modifier.testTag("appearance_preview_clock"),
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Column(modifier = Modifier.fillMaxWidth()) {
                    favorites.take(PREVIEW_HOME_APP_COUNT).forEach { item ->
                        when (item) {
                            is PlacedItem.SingleApp -> AppRow(
                                app = item.app,
                                onClick = {},
                                badgeCount = null,
                                badgeStyle = NotificationBadgeStyle.DOT,
                                onRequestShortcuts = { emptyList() },
                                onLaunchShortcut = {},
                                onAppInfo = {},
                                position = appRowPosition,
                                presentation = appRowPresentation,
                                labelColor = settings.appLabelColorOption.resolve(),
                                labelFontWeight = settings.homeAppsFontWeight.resolve(),
                                enableLongPressMenu = false,
                                testTagPrefix = "appearance_preview_",
                            )
                            is PlacedItem.FolderItem -> FolderRow(
                                folder = item.folder,
                                onAppClick = {},
                                onRequestShortcuts = { emptyList() },
                                onLaunchShortcut = {},
                                onAppInfo = {},
                                onRemoveFromFolder = { _, _ -> },
                                onRenameFolder = { _, _ -> },
                                drawerPresentation = DrawerPresentation.LIST,
                                position = appRowPosition,
                                presentation = appRowPresentation,
                                labelColor = settings.appLabelColorOption.resolve(),
                                labelFontWeight = settings.homeAppsFontWeight.resolve(),
                                enableLongPressMenu = false,
                                onClick = {},
                                testTagPrefix = "appearance_preview_",
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    dockItems.take(PREVIEW_DOCK_APP_COUNT).forEach { item ->
                        DockIcon(
                            item = item,
                            displayMode = dockDisplayMode,
                            onClick = {},
                            labelColor = settings.appLabelColorOption.resolve(),
                            labelFontWeight = settings.homeAppsFontWeight.resolve(),
                            enableLongPressMenu = false,
                        )
                    }
                }
            }
        }
        }
    }
}

/** Design-time stand-ins for [AppearanceSettingsScreenPreview] — Android Studio's `@Preview`
 * rendering has no real repositories to query, unlike the screen's own live
 * [AppearancePreviewCard], which sources the real effective favorites/dock/calendar instead (see
 * its doc). */
private val DesignTimeFavorites = listOf(
    PlacedItem.SingleApp(AppInfo(packageName = "preview.appearance.one", activityName = ".Main", label = "Camera", icon = null)),
    PlacedItem.SingleApp(AppInfo(packageName = "preview.appearance.two", activityName = ".Main", label = "Messages", icon = null)),
)
private val DesignTimeDockItems = listOf(
    PlacedItem.SingleApp(AppInfo(packageName = "preview.appearance.dock.one", activityName = ".Main", label = "Phone", icon = null)),
    PlacedItem.SingleApp(AppInfo(packageName = "preview.appearance.dock.two", activityName = ".Main", label = "Browser", icon = null)),
    PlacedItem.SingleApp(AppInfo(packageName = "preview.appearance.dock.three", activityName = ".Main", label = "Mail", icon = null)),
)

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
        Text(text = stringResource(R.string.appearance_header_title), style = MaterialTheme.typography.headlineSmall, color = Ink)
    }
}

/**
 * Settings → Appearance → Accent color. Two sources, matching the system "Wallpaper & style"
 * picker's own interaction model: **Wallpaper colors** (Material You, the default) reveals
 * [WallpaperAccentRoleGrid] — the wallpaper's own three derived tones, tappable just like Basic
 * colors' preset swatches; **Basic colors** reveals the curated [AccentSwatch] grid instead. Each
 * swatch (either grid) renders both its light and dark half at once (see [SplitColorCircle])
 * since picking one stores both — switching the theme mode restores the right half for the *same*
 * pick rather than needing to re-choose.
 */
@Composable
private fun AccentColorSection(
    accentFromSystem: Boolean,
    customAccentSwatch: String?,
    wallpaperAccentRole: WallpaperAccentRole,
    onAccentFromSystemChange: (Boolean) -> Unit,
    onCustomAccentSwatchChange: (AccentSwatch) -> Unit,
    onWallpaperAccentRoleChange: (WallpaperAccentRole) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(vertical = 8.dp)) {
        Text(text = stringResource(R.string.appearance_accent_color), style = MaterialTheme.typography.bodyLarge, color = Ink, modifier = Modifier.padding(bottom = 10.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PillOption(
                label = stringResource(R.string.appearance_wallpaper_colors),
                selected = accentFromSystem,
                onClick = { onAccentFromSystemChange(true) },
                testTag = "accent_source_wallpaper",
                modifier = Modifier.weight(1f),
            )
            PillOption(
                label = stringResource(R.string.appearance_basic_colors),
                selected = !accentFromSystem,
                onClick = { onAccentFromSystemChange(false) },
                testTag = "accent_source_basic",
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (accentFromSystem) {
            WallpaperAccentRoleGrid(selected = wallpaperAccentRole, onSelect = onWallpaperAccentRoleChange)
        } else {
            AccentSwatchGrid(selected = customAccentSwatch, onSelect = onCustomAccentSwatchChange)
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
    SplitColorCircle(
        light = swatch.light,
        dark = swatch.dark,
        selected = selected,
        checkedContentDescription = stringResource(swatch.labelRes),
        testTag = "accent_swatch_${swatch.name}",
        onClick = onClick,
        modifier = modifier,
    )
}

/**
 * The shared visual behind every accent-color swatch (Basic colors' presets, and the wallpaper
 * roles below) — both its light and dark half at once, side by side, with a checkmark overlay
 * when selected.
 */
@Composable
private fun SplitColorCircle(
    light: Color,
    dark: Color,
    selected: Boolean,
    checkedContentDescription: String,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center,
    ) {
        Row(modifier = Modifier.matchParentSize()) {
            Box(modifier = Modifier.weight(1f).fillMaxHeight().background(light))
            Box(modifier = Modifier.weight(1f).fillMaxHeight().background(dark))
        }
        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = checkedContentDescription,
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

/**
 * The wallpaper-derived counterpart to [AccentSwatchGrid] — instead of a fixed preset palette,
 * shows the wallpaper's own three Material You tonal roles ([WallpaperAccentRole.entries]) as
 * tappable swatches, each with a real light/dark pair read live off the OS's dynamic color scheme
 * (same [dynamicLightColorScheme]/[dynamicDarkColorScheme] + resume-signal-keyed `remember` +
 * defensive fallback pattern as `ui/theme/Color.kt`'s own `accentTonalExtremes()` — see that
 * function's doc for why the resume signal matters for a launcher specifically). No text labels,
 * unlike the named Basic-colors presets — these are algorithmically derived, unnamed hues; the
 * swatch itself is the only identifier a user needs to tell them apart.
 */
@Composable
private fun WallpaperAccentRoleGrid(selected: WallpaperAccentRole, onSelect: (WallpaperAccentRole) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val refreshSignal = LocalDynamicColorRefreshSignal.current
    val tones = remember(context, refreshSignal) {
        WallpaperAccentRole.entries.associateWith { role ->
            runCatching { role.toneOf(dynamicLightColorScheme(context)) }.getOrDefault(AccentSwatch.BLUE.light) to
                runCatching { role.toneOf(dynamicDarkColorScheme(context)) }.getOrDefault(AccentSwatch.BLUE.dark)
        }
    }
    Row(modifier = modifier.testTag("wallpaper_accent_role_grid"), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        WallpaperAccentRole.entries.forEach { role ->
            val (light, dark) = tones.getValue(role)
            SplitColorCircle(
                light = light,
                dark = dark,
                selected = role == selected,
                checkedContentDescription = "Wallpaper color ${role.ordinal + 1}",
                testTag = "wallpaper_accent_role_${role.name}",
                onClick = { onSelect(role) },
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AppearanceSettingsScreenPreview() {
    FacetLauncherTheme {
        AppearanceSettingsContent(
            settings = LauncherSettings(),
            uiState = AppearanceSettingsUiState(globalFavorites = DesignTimeFavorites, globalDockItems = DesignTimeDockItems),
            homeWallpaper = HomeWallpaper.Unavailable,
            onBack = {},
            onNavigateToClockStyleGallery = {},
            onThemeModeChange = {},
            onAccentFromSystemChange = {},
            onCustomAccentSwatchChange = {},
            onWallpaperAccentRoleChange = {},
            onIconRenderModeChange = {},
            onLauncherFontOptionChange = {},
            onAppLabelColorOptionChange = {},
            onHomeAppsFontWeightChange = {},
            onFontScaleOptionChange = {},
            onDockDisplayModeChange = {},
            onAppRowPositionChange = {},
            onAppRowPresentationChange = {},
            onAppListVerticalAlignmentChange = {},
        )
    }
}

@Preview(name = "Facet-scoped", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun AppearanceSettingsScreenFacetScopedPreview() {
    FacetLauncherTheme {
        AppearanceSettingsContent(
            settings = LauncherSettings(),
            uiState = AppearanceSettingsUiState(
                facet = com.facetlauncher.app.data.local.FacetEntity(id = 1L, name = "Work", position = 0),
                globalFavorites = DesignTimeFavorites,
                globalDockItems = DesignTimeDockItems,
            ),
            homeWallpaper = HomeWallpaper.Unavailable,
            onBack = {},
            onNavigateToClockStyleGallery = {},
            onThemeModeChange = {},
            onAccentFromSystemChange = {},
            onCustomAccentSwatchChange = {},
            onWallpaperAccentRoleChange = {},
            onIconRenderModeChange = {},
            onLauncherFontOptionChange = {},
            onAppLabelColorOptionChange = {},
            onHomeAppsFontWeightChange = {},
            onFontScaleOptionChange = {},
            onDockDisplayModeChange = {},
            onAppRowPositionChange = {},
            onAppRowPresentationChange = {},
            onAppListVerticalAlignmentChange = {},
        )
    }
}
