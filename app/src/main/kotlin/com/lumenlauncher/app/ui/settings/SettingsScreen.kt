package com.lumenlauncher.app.ui.settings

import android.Manifest
import android.content.Intent
import android.content.res.Configuration
import android.provider.Settings as AndroidSettings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumenlauncher.app.data.DefaultFavoriteAppRepository
import com.lumenlauncher.app.data.DockAppRepository
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.data.model.DockDisplayMode
import com.lumenlauncher.app.data.model.DrawerGridSize
import com.lumenlauncher.app.data.model.DrawerListItemSize
import com.lumenlauncher.app.data.model.DrawerPresentation
import com.lumenlauncher.app.data.model.IconRenderMode
import com.lumenlauncher.app.data.model.ListContentMode
import com.lumenlauncher.app.data.model.NotificationBadgeStyle
import com.lumenlauncher.app.data.model.SearchBarPosition
import com.lumenlauncher.app.data.model.ThemeMode
import com.lumenlauncher.app.ui.components.BackButton
import com.lumenlauncher.app.ui.components.AppIcon
import com.lumenlauncher.app.ui.components.CardDivider
import com.lumenlauncher.app.ui.components.LabeledDropdownRow
import com.lumenlauncher.app.ui.components.SettingsCard
import com.lumenlauncher.app.ui.theme.Accent
import com.lumenlauncher.app.ui.theme.AccentSwatch
import com.lumenlauncher.app.ui.theme.Faint
import com.lumenlauncher.app.ui.theme.Hairline
import com.lumenlauncher.app.ui.theme.Ink
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme
import com.lumenlauncher.app.ui.theme.Muted
import com.lumenlauncher.app.ui.theme.Surface
import kotlin.math.roundToInt

/** Canonical Launcher Settings screen (`3c`). Sections tied to not-yet-built capabilities render disabled. */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onAddDockApp: () -> Unit,
    onViewProfiles: () -> Unit,
    onNavigateToCalendarSettings: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToNotificationSettings: () -> Unit,
    onEditDefaultFavorites: () -> Unit,
    onNavigateToClockStyleGallery: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val requestContactsPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        viewModel.setSearchContactsEnabled(granted)
    }

    SettingsContent(
        uiState = uiState,
        onBack = onBack,
        onAddDockApp = onAddDockApp,
        onViewProfiles = onViewProfiles,
        onNavigateToCalendarSettings = onNavigateToCalendarSettings,
        onNavigateToPermissions = onNavigateToPermissions,
        onNavigateToNotificationSettings = onNavigateToNotificationSettings,
        onEditDefaultFavorites = onEditDefaultFavorites,
        onNavigateToClockStyleGallery = onNavigateToClockStyleGallery,
        onDockDisplayModeChanged = viewModel::setDockDisplayMode,
        onShowDrawerIconsChanged = viewModel::setShowDrawerIcons,
        onShowDrawerLabelsChanged = viewModel::setShowDrawerLabels,
        onDrawerPresentationChanged = viewModel::setDrawerPresentation,
        onDrawerGridSizeChanged = viewModel::setDrawerGridSize,
        onDrawerListItemSizeChanged = viewModel::setDrawerListItemSize,
        onDrawerOpacityChanged = viewModel::setDrawerOpacity,
        onSearchBarPositionChanged = viewModel::setSearchBarPosition,
        onSearchContactsToggled = { enabled ->
            // Turning it on triggers the real request; the launcher's own callback (above) is
            // what actually persists the new value once the user responds. Turning it off needs
            // no permission dance — just write the value directly.
            if (enabled) requestContactsPermission.launch(Manifest.permission.READ_CONTACTS) else viewModel.setSearchContactsEnabled(false)
        },
        onReorderDockApps = viewModel::reorderDockApps,
        onReorderDefaultFavorites = viewModel::reorderDefaultFavorites,
        onListContentModeChanged = viewModel::setListContentMode,
        onAppsToShowCountChanged = viewModel::setAppsToShowCount,
        onThemeModeChanged = viewModel::setThemeMode,
        onAccentFromSystemChanged = viewModel::setAccentFromSystem,
        onCustomAccentSwatchChanged = viewModel::setCustomAccentSwatch,
        onIconRenderModeChanged = viewModel::setIconRenderMode,
        modifier = modifier,
    )
}

@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    onBack: () -> Unit,
    onAddDockApp: () -> Unit,
    onViewProfiles: () -> Unit,
    onNavigateToCalendarSettings: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToNotificationSettings: () -> Unit,
    onEditDefaultFavorites: () -> Unit,
    onNavigateToClockStyleGallery: () -> Unit,
    onDockDisplayModeChanged: (DockDisplayMode) -> Unit,
    onShowDrawerIconsChanged: (Boolean) -> Unit,
    onShowDrawerLabelsChanged: (Boolean) -> Unit,
    onDrawerPresentationChanged: (DrawerPresentation) -> Unit,
    onDrawerGridSizeChanged: (DrawerGridSize) -> Unit,
    onDrawerListItemSizeChanged: (DrawerListItemSize) -> Unit,
    onDrawerOpacityChanged: (Float) -> Unit,
    onSearchBarPositionChanged: (SearchBarPosition) -> Unit,
    onSearchContactsToggled: (Boolean) -> Unit,
    onReorderDockApps: (List<AppInfo>) -> Unit,
    onReorderDefaultFavorites: (List<AppInfo>) -> Unit,
    onListContentModeChanged: (ListContentMode) -> Unit,
    onAppsToShowCountChanged: (Int) -> Unit,
    onThemeModeChanged: (ThemeMode) -> Unit,
    onAccentFromSystemChanged: (Boolean) -> Unit,
    onCustomAccentSwatchChanged: (AccentSwatch) -> Unit,
    onIconRenderModeChanged: (IconRenderMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val presentation = uiState.settings.drawerPresentation

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Surface)
            .testTag("settings_screen")
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(horizontal = 24.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                BackButton(onClick = onBack)
                Text(text = "Settings", style = MaterialTheme.typography.headlineSmall, color = Ink)
            }
        }

        item { SectionHeader("PROFILES") }
        item {
            SettingsCard {
                ClickableRow(
                    title = "Profiles",
                    subtitle = "Create and manage profile specific settings",
                    onClick = onViewProfiles,
                    testTag = "view_profiles_row",
                    trailing = { NavigationChevron() },
                )
            }
        }

        item { SectionHeader("CLOCK") }
        item {
            SettingsCard {
                ClickableRow(
                    title = "Default clock style",
                    subtitle = uiState.settings.clockTemplateId.displayName + " · " +
                        if (uiState.settings.use24HourTime) "24-hour time" else "12-hour time",
                    onClick = onNavigateToClockStyleGallery,
                    testTag = "clock_style_gallery_row",
                    trailing = { NavigationChevron() },
                )
                CardDivider()
                ClickableRow(
                    title = "Calendar",
                    subtitle = if (uiState.settings.showAllDayEvents) "All-day events shown" else "All-day events hidden",
                    onClick = onNavigateToCalendarSettings,
                    testTag = "calendar_settings_row",
                    trailing = { NavigationChevron() },
                )
            }
        }

        item { SectionHeader("NOTIFICATIONS") }
        item {
            SettingsCard {
                ClickableRow(
                    title = "Notifications",
                    subtitle = if (uiState.settings.notificationDotsEnabled) {
                        "On · ${uiState.settings.notificationBadgeStyle.displayLabel()}"
                    } else {
                        "Off"
                    },
                    onClick = onNavigateToNotificationSettings,
                    testTag = "notification_settings_row",
                    trailing = { NavigationChevron() },
                )
            }
        }

        item { SectionHeader("DOCK") }
        item {
            SettingsCard {
                LabeledDropdownRow(
                    title = "Display style",
                    options = DockDisplayMode.entries,
                    selected = uiState.settings.dockDisplayMode,
                    label = { it.displayLabel() },
                    onSelect = onDockDisplayModeChanged,
                    testTag = "dock_display_style_row",
                )
                CardDivider()
                ClickableRow(
                    title = "Select Dock Apps",
                    subtitle = "${uiState.dockApps.size} of ${DockAppRepository.MAX_APPS}",
                    onClick = onAddDockApp,
                    testTag = "add_dock_app_row",
                    trailing = { NavigationChevron() },
                )
                if (uiState.dockApps.isNotEmpty()) {
                    CardDivider()
                    DockSection(dockApps = uiState.dockApps, onReorder = onReorderDockApps)
                }
            }
        }

        item { SectionHeader("APPS LIST") }
        item {
            SettingsCard {
                LabeledDropdownRow(
                    title = "Default App list content",
                    options = ListContentMode.entries,
                    selected = uiState.settings.listContentMode,
                    label = { it.displayLabel() },
                    onSelect = onListContentModeChanged,
                    testTag = "default_list_content_row",
                )
                if (uiState.settings.listContentMode != ListContentMode.FAVORITES) {
                    CardDivider()
                    LabeledDropdownRow(
                        title = "Apps to show",
                        options = DEFAULT_APPS_TO_SHOW_OPTIONS,
                        selected = uiState.settings.appsToShowCount,
                        label = { it.toString() },
                        onSelect = onAppsToShowCountChanged,
                        testTag = "default_apps_to_show_row",
                    )
                }
                CardDivider()
                ClickableRow(
                    title = "Default favorites",
                    subtitle = "${uiState.defaultFavorites.size} of ${DefaultFavoriteAppRepository.MAX_FAVORITES}",
                    onClick = onEditDefaultFavorites,
                    testTag = "default_favorites_row",
                    trailing = { NavigationChevron() },
                )
                if (uiState.defaultFavorites.isNotEmpty()) {
                    CardDivider()
                    DefaultFavoritesReorderList(favorites = uiState.defaultFavorites, onReorder = onReorderDefaultFavorites)
                }
            }
        }

        item { SectionHeader("APP DRAWER") }
        item {
            SettingsCard {
                LabeledDropdownRow(
                    title = "Presentation",
                    options = DrawerPresentation.entries,
                    selected = presentation,
                    label = { it.displayLabel() },
                    onSelect = onDrawerPresentationChanged,
                    testTag = "drawer_presentation_row",
                )
                if (presentation == DrawerPresentation.GRID) {
                    CardDivider()
                    LabeledDropdownRow(
                        title = "Grid size",
                        options = DrawerGridSize.entries,
                        selected = uiState.settings.drawerGridSize,
                        label = { it.displayLabel() },
                        onSelect = onDrawerGridSizeChanged,
                        testTag = "drawer_grid_size_row",
                    )
                }
                if (presentation == DrawerPresentation.LIST) {
                    CardDivider()
                    LabeledDropdownRow(
                        title = "List item size",
                        options = DrawerListItemSize.entries,
                        selected = uiState.settings.drawerListItemSize,
                        label = { it.displayLabel() },
                        onSelect = onDrawerListItemSizeChanged,
                        testTag = "drawer_list_item_size_row",
                    )
                    CardDivider()
                    ToggleRow(
                        title = "Show icons",
                        subtitle = "Shows icons with the application name",
                        checked = uiState.settings.showDrawerIcons,
                        onCheckedChange = onShowDrawerIconsChanged,
                        testTag = "show_drawer_icons_toggle",
                    )
                }
                if (presentation == DrawerPresentation.GRID) {
                    CardDivider()
                    ToggleRow(
                        title = "Show labels",
                        subtitle = "Shows application name below the icon",
                        checked = uiState.settings.showDrawerLabels,
                        onCheckedChange = onShowDrawerLabelsChanged,
                        testTag = "show_drawer_labels_toggle",
                    )
                }
                CardDivider()
                ToggleRow(
                    title = "Search contacts",
                    subtitle = "Call, message, WhatsApp from results",
                    checked = uiState.settings.searchContactsEnabled,
                    onCheckedChange = onSearchContactsToggled,
                    testTag = "search_contacts_toggle",
                )
                CardDivider()
                LabeledDropdownRow(
                    title = "Search bar position",
                    options = SearchBarPosition.entries,
                    selected = uiState.settings.searchBarPosition,
                    label = { it.displayLabel() },
                    onSelect = onSearchBarPositionChanged,
                    testTag = "search_bar_position_row",
                )
                CardDivider()
                DrawerOpacitySlider(opacity = uiState.settings.drawerOpacity, onChange = onDrawerOpacityChanged)
            }
        }

        item { SectionHeader("APPEARANCE") }
        item {
            SettingsCard {
                LabeledDropdownRow(
                    title = "Launcher theme",
                    options = ThemeMode.entries,
                    selected = uiState.settings.themeMode,
                    label = { it.displayLabel() },
                    onSelect = onThemeModeChanged,
                    testTag = "theme_mode_dropdown",
                )
                CardDivider()
                AccentColorSection(
                    accentFromSystem = uiState.settings.accentFromSystem,
                    customAccentSwatch = uiState.settings.customAccentSwatch,
                    onAccentFromSystemChanged = onAccentFromSystemChanged,
                    onCustomAccentSwatchChanged = onCustomAccentSwatchChanged,
                )
                CardDivider()
                LabeledDropdownRow(
                    title = "Icons",
                    options = IconRenderMode.entries,
                    selected = uiState.settings.iconRenderMode,
                    label = { it.displayName },
                    onSelect = onIconRenderModeChanged,
                    testTag = "appearance_icons_row",
                )
            }
        }

        item { SectionHeader("PERMISSIONS") }
        item {
            SettingsCard {
                ClickableRow(
                    title = "Permissions",
                    subtitle = "See what Lumen can access and why",
                    onClick = onNavigateToPermissions,
                    testTag = "view_permissions_row",
                    trailing = { NavigationChevron() },
                )
            }
        }

        item { Spacer(modifier = Modifier.height(18.dp)) }
        item {
            SettingsCard {
                DisabledRow(
                    title = "Backup & restore",
                    subtitle = "Export settings as a file · widgets need re-adding on import",
                    modifier = Modifier.testTag("backup_restore_row"),
                )
                CardDivider()
                ClickableRow(
                    title = "Set as default launcher",
                    subtitle = if (uiState.isDefaultLauncher) "Active" else "Not set",
                    onClick = {
                        runCatching {
                            context.startActivity(Intent(AndroidSettings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS))
                        }
                    },
                    testTag = "set_default_launcher_row",
                    trailing = { NavigationChevron() },
                )
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

private fun DockDisplayMode.displayLabel(): String = when (this) {
    DockDisplayMode.ICONS -> "Icons"
    DockDisplayMode.TEXT -> "Text"
}

private fun DrawerPresentation.displayLabel(): String = when (this) {
    DrawerPresentation.LIST -> "List"
    DrawerPresentation.GRID -> "Grid"
}

private fun DrawerGridSize.displayLabel(): String = "$columns cols × $rows rows"

private fun DrawerListItemSize.displayLabel(): String = when (this) {
    DrawerListItemSize.COMPACT -> "Compact"
    DrawerListItemSize.REGULAR -> "Regular"
    DrawerListItemSize.SPACIOUS -> "Spacious"
}

private fun ThemeMode.displayLabel(): String = when (this) {
    ThemeMode.LIGHT -> "Light"
    ThemeMode.DARK -> "Dark"
    ThemeMode.SYSTEM -> "System"
}

private fun SearchBarPosition.displayLabel(): String = when (this) {
    SearchBarPosition.TOP -> "Top"
    SearchBarPosition.BOTTOM -> "Bottom"
}

private fun ListContentMode.displayLabel(): String = when (this) {
    ListContentMode.FAVORITES -> "Favorites"
    ListContentMode.RECENTS -> "Recents"
    ListContentMode.MOST_USED -> "Most used"
}

private fun NotificationBadgeStyle.displayLabel(): String = when (this) {
    NotificationBadgeStyle.DOT -> "Dot"
    NotificationBadgeStyle.COUNT -> "Count"
}

@Composable
private fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = Faint,
        modifier = modifier.padding(top = 18.dp, bottom = 6.dp),
    )
}

@Composable
private fun RowScaffold(
    title: String,
    subtitle: String?,
    modifier: Modifier = Modifier,
    titleAlpha: Float = 1f,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.alpha(titleAlpha)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, color = Ink)
            if (subtitle != null) {
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = Muted)
            }
        }
        trailing?.invoke()
    }
}

@Composable
private fun DisabledRow(title: String, subtitle: String?, modifier: Modifier = Modifier) {
    RowScaffold(title = title, subtitle = subtitle, titleAlpha = 0.4f, modifier = modifier)
}

@Composable
private fun ToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
) {
    RowScaffold(
        title = title,
        subtitle = subtitle,
        modifier = modifier,
        trailing = {
            Switch(checked = checked, onCheckedChange = onCheckedChange, modifier = Modifier.testTag(testTag))
        },
    )
}

@Composable
private fun ClickableRow(
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    RowScaffold(title = title, subtitle = subtitle, modifier = modifier.testTag(testTag).clickable(onClick = onClick), trailing = trailing)
}

/** Trailing affordance for a row that navigates to its own screen (Edit favorites, app pickers). */
@Composable
private fun NavigationChevron(modifier: Modifier = Modifier) {
    Icon(
        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
        contentDescription = null,
        tint = Muted,
        modifier = modifier,
    )
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
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = if (selected) Ink else Muted)
    }
}

/**
 * Settings → Theme → Accent color. Two sources, matching the system "Wallpaper & style" picker's
 * own interaction model: **Wallpaper colors** (Material You, the default) computes and applies
 * automatically — no picker shown; **Basic colors** reveals the curated [AccentSwatch] grid
 * below it. Each swatch renders both its light and dark half at once (see [AccentSwatchCircle])
 * since picking one stores both — switching the theme mode restores the right half for the
 * *same* pick rather than needing to re-choose.
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

/** README `3d`'s 4…8 range. */
private val DEFAULT_APPS_TO_SHOW_OPTIONS = (4..8).toList()

private const val DOCK_ICON_SIZE_DP = 44
private const val DOCK_ICON_SPACING_DP = 10
private val DOCK_TILE_SHAPE = RoundedCornerShape(14.dp)

/** Shared "picked up" drag-lift treatment for every reorderable row/tile in Settings. */
private val REORDER_DRAG_ELEVATION = 6.dp
private const val REORDER_DRAG_SCALE = 1.04f

@Composable
private fun DockSection(
    dockApps: List<AppInfo>,
    onReorder: (List<AppInfo>) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(vertical = 8.dp)) {
        // Keyed on component identity only, not the raw `dockApps` list — see FavoritesReorderList's
        // doc comment (ProfileSettingsScreen.kt) for why keying on the raw list resets drag state
        // (and restarts the gesture detector) mid-drag on incidental re-emissions.
        val componentsKey = dockApps.map { it.packageName to it.activityName }
        var order by remember(componentsKey) { mutableStateOf(dockApps) }
        var draggingComponent by remember { mutableStateOf<Pair<String, String>?>(null) }
        var dragOffsetX by remember { mutableFloatStateOf(0f) }
        val slotWidthPx = with(LocalDensity.current) { (DOCK_ICON_SIZE_DP + DOCK_ICON_SPACING_DP).dp.toPx() }
        val currentOrder = rememberUpdatedState(order)
        val currentOnReorder = rememberUpdatedState(onReorder)

        LazyRow(
            modifier = Modifier.height(DOCK_ICON_SIZE_DP.dp),
            horizontalArrangement = Arrangement.spacedBy(DOCK_ICON_SPACING_DP.dp),
            verticalAlignment = Alignment.CenterVertically,
            userScrollEnabled = false,
        ) {
            items(order, key = { it.packageName + it.activityName }) { app ->
                val component = app.packageName to app.activityName
                val isDragging = component == draggingComponent
                Box(
                    modifier = Modifier
                        .testTag("dock_app_${app.packageName}")
                        .zIndex(if (isDragging) 1f else 0f)
                        .graphicsLayer {
                            translationX = if (isDragging) dragOffsetX else 0f
                            scaleX = if (isDragging) REORDER_DRAG_SCALE else 1f
                            scaleY = if (isDragging) REORDER_DRAG_SCALE else 1f
                            shadowElevation = if (isDragging) REORDER_DRAG_ELEVATION.toPx() else 0f
                            shape = DOCK_TILE_SHAPE
                            clip = false
                        }
                        .then(if (isDragging) Modifier else Modifier.animateItem())
                        .pointerInput(component) {
                            detectDragGestures(
                                onDragStart = { draggingComponent = component; dragOffsetX = 0f },
                                onDragEnd = {
                                    val list = currentOrder.value
                                    val currentIndex = list.indexOfFirst { it.packageName == component.first && it.activityName == component.second }
                                    if (currentIndex != -1) {
                                        val targetIndex = (currentIndex + (dragOffsetX / slotWidthPx).roundToInt())
                                            .coerceIn(0, list.lastIndex)
                                        if (targetIndex != currentIndex) {
                                            order = list.toMutableList().apply { add(targetIndex, removeAt(currentIndex)) }
                                            currentOnReorder.value(order)
                                        }
                                    }
                                    draggingComponent = null
                                    dragOffsetX = 0f
                                },
                                onDragCancel = { draggingComponent = null; dragOffsetX = 0f },
                            ) { change, dragAmount ->
                                change.consume()
                                dragOffsetX += dragAmount.x
                            }
                        },
                ) {
                    DockAppTile(app)
                }
            }
        }
        Text(
            text = "Drag to reorder",
            style = MaterialTheme.typography.bodyMedium,
            color = Faint,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun DockAppTile(app: AppInfo, modifier: Modifier = Modifier) {
    AppIcon(icon = app.icon, size = DOCK_ICON_SIZE_DP.dp, cornerRadius = 14.dp, contentDescription = app.label, modifier = modifier)
}

private const val DEFAULT_FAVORITE_ROW_HEIGHT_DP = 52
private val DEFAULT_FAVORITE_ROW_SHAPE = RoundedCornerShape(12.dp)

/**
 * Drag-to-reorder for Settings' own "Default favorites" list — the same shape as
 * [com.lumenlauncher.app.ui.profiles.ProfileSettingsScreen]'s `FavoritesReorderList` (see that
 * file's own doc comment for why `remember`/`pointerInput` are keyed on stable component identity
 * rather than the raw [favorites] list). Adding/removing a favorite happens on the picker screen
 * reached via the "Default favorites" row above this list, never here.
 */
@Composable
private fun DefaultFavoritesReorderList(favorites: List<AppInfo>, onReorder: (List<AppInfo>) -> Unit, modifier: Modifier = Modifier) {
    val componentsKey = favorites.map { it.packageName to it.activityName }
    var order by remember(componentsKey) { mutableStateOf(favorites) }
    var draggingComponent by remember { mutableStateOf<Pair<String, String>?>(null) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    val rowHeightPx = with(LocalDensity.current) { DEFAULT_FAVORITE_ROW_HEIGHT_DP.dp.toPx() }
    val currentOrder = rememberUpdatedState(order)
    val currentOnReorder = rememberUpdatedState(onReorder)

    LazyColumn(
        modifier = modifier.fillMaxWidth().height((DEFAULT_FAVORITE_ROW_HEIGHT_DP * order.size).dp),
        userScrollEnabled = false,
    ) {
        items(order, key = { it.packageName + it.activityName }) { app ->
            val component = app.packageName to app.activityName
            val isDragging = component == draggingComponent
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(DEFAULT_FAVORITE_ROW_HEIGHT_DP.dp)
                    .testTag("default_favorite_reorder_row_${app.packageName}")
                    .zIndex(if (isDragging) 1f else 0f)
                    .graphicsLayer {
                        translationY = if (isDragging) dragOffsetY else 0f
                        scaleX = if (isDragging) REORDER_DRAG_SCALE else 1f
                        scaleY = if (isDragging) REORDER_DRAG_SCALE else 1f
                        shadowElevation = if (isDragging) REORDER_DRAG_ELEVATION.toPx() else 0f
                        shape = DEFAULT_FAVORITE_ROW_SHAPE
                        clip = false
                    }
                    .then(if (isDragging) Modifier.background(Surface, DEFAULT_FAVORITE_ROW_SHAPE) else Modifier)
                    .then(if (isDragging) Modifier else Modifier.animateItem())
                    .padding(horizontal = if (isDragging) 10.dp else 0.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Icon(
                    Icons.Default.DragHandle,
                    contentDescription = "Drag to reorder",
                    tint = Faint,
                    modifier = Modifier
                        .testTag("default_favorite_reorder_handle_${app.packageName}")
                        .pointerInput(component) {
                            detectDragGestures(
                                onDragStart = { draggingComponent = component; dragOffsetY = 0f },
                                onDragEnd = {
                                    val list = currentOrder.value
                                    val currentIndex = list.indexOfFirst { it.packageName == component.first && it.activityName == component.second }
                                    if (currentIndex != -1) {
                                        val targetIndex = (currentIndex + (dragOffsetY / rowHeightPx).roundToInt())
                                            .coerceIn(0, list.lastIndex)
                                        if (targetIndex != currentIndex) {
                                            order = list.toMutableList().apply { add(targetIndex, removeAt(currentIndex)) }
                                            currentOnReorder.value(order)
                                        }
                                    }
                                    draggingComponent = null
                                    dragOffsetY = 0f
                                },
                                onDragCancel = { draggingComponent = null; dragOffsetY = 0f },
                            ) { change, dragAmount ->
                                change.consume()
                                dragOffsetY += dragAmount.y
                            }
                        },
                )
                AppIcon(icon = app.icon, size = 32.dp, cornerRadius = 9.dp, contentDescription = null)
                Text(text = app.label, style = MaterialTheme.typography.bodyLarge, color = Ink, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun DrawerOpacitySlider(opacity: Float, onChange: (Float) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.testTag("drawer_opacity_slider").padding(vertical = 8.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Drawer opacity", style = MaterialTheme.typography.bodyLarge, color = Ink)
            Text(text = "${(opacity * 100).roundToInt()}%", style = MaterialTheme.typography.bodyMedium, color = Muted)
        }
        // Plain M3 Slider — default thumb/track shape, just this app's Accent/Hairline colors.
        Slider(
            value = opacity,
            onValueChange = onChange,
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(
                thumbColor = Accent,
                activeTrackColor = Accent,
                inactiveTrackColor = Hairline,
            ),
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SettingsScreenPreview() {
    LumenLauncherTheme {
        SettingsContent(
            uiState = SettingsUiState(
                dockApps = (1..4).map {
                    AppInfo(packageName = "com.example.$it", activityName = ".Main", label = "App $it", icon = null)
                },
            ),
            onBack = {},
            onAddDockApp = {},
            onViewProfiles = {},
            onNavigateToCalendarSettings = {},
            onNavigateToPermissions = {},
            onNavigateToNotificationSettings = {},
            onEditDefaultFavorites = {},
            onNavigateToClockStyleGallery = {},
            onDockDisplayModeChanged = {},
            onShowDrawerIconsChanged = {},
            onShowDrawerLabelsChanged = {},
            onDrawerPresentationChanged = {},
            onDrawerGridSizeChanged = {},
            onDrawerListItemSizeChanged = {},
            onDrawerOpacityChanged = {},
            onSearchBarPositionChanged = {},
            onSearchContactsToggled = {},
            onReorderDockApps = {},
            onReorderDefaultFavorites = {},
            onListContentModeChanged = {},
            onAppsToShowCountChanged = {},
            onThemeModeChanged = {},
            onAccentFromSystemChanged = {},
            onCustomAccentSwatchChanged = {},
            onIconRenderModeChanged = {},
        )
    }
}
