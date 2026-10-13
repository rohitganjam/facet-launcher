package com.facetlauncher.app.ui.settings

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.data.DockAppRepository
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppListColumnAlignment
import com.facetlauncher.app.data.model.AppListGridColumns
import com.facetlauncher.app.data.model.AppListGridDisplayMode
import com.facetlauncher.app.data.model.AppListLayout
import com.facetlauncher.app.data.model.AppListLimits
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.model.CalendarDisplayOptions
import com.facetlauncher.app.data.model.toColumnPositions
import com.facetlauncher.app.data.model.CalendarEvent
import com.facetlauncher.app.data.model.DockDisplayMode
import com.facetlauncher.app.data.model.DrawerPresentation
import com.facetlauncher.app.data.model.HomeWallpaper
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.NotificationBadgeStyle
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.ui.components.AppIcon
import com.facetlauncher.app.ui.components.AppIconSize
import com.facetlauncher.app.ui.components.AppMenuNotifications
import com.facetlauncher.app.ui.components.WallpaperBackground
import com.facetlauncher.app.ui.home.AppRow
import com.facetlauncher.app.ui.home.ClockBlock
import com.facetlauncher.app.ui.home.DockIcon
import com.facetlauncher.app.ui.home.FolderRow
import com.facetlauncher.app.ui.home.FolderTileGlyph
import com.facetlauncher.app.ui.theme.Hairline
import com.facetlauncher.app.ui.theme.resolve

/**
 * Not an arbitrary "glanceable strip" cap for the dock — [DockAppRepository.MAX_APPS] is the real
 * cap a dock list can never exceed, so `.take()`ing it is a no-op safety net, not a lossy
 * truncation. An earlier version of this preview capped at 2/3 regardless of how many favorites/
 * dock apps were actually configured — caught via direct user report: 4 configured favorites
 * showed only 2, 5 configured dock apps showed only 3 (see chat history).
 *
 * The favorites side is a real, decoupled cap instead of [AppListLimits.MAX_FAVORITES] directly —
 * this card's small fixed, scaled space was never sized for that many rows in single-column mode
 * — but the cap is layout-aware ([AppListLayout.previewItemCap]) rather than one flat number:
 * [AppListLayout.TWO_COLUMN]/[AppListLayout.GRID] pack more than one item per row, so the same
 * [PREVIEW_ROW_BUDGET] lets them show proportionally more of the real list without overflowing —
 * in practice every one of [AppListLimits.MAX_FAVORITES] renders for those two layouts, and only
 * single-column (the tightest case) actually truncates. On-device visual check (2026-09-28) caught
 * the previous flat cap leaving Grid's preview mostly empty despite 12 real favorites configured
 * (see chat history) — not just a design guess, a real gap once the cap raise shipped.
 */
private const val PREVIEW_ROW_BUDGET = 6
private fun AppListLayout.previewItemCap(gridColumns: Int): Int = when (this) {
    AppListLayout.GRID -> PREVIEW_ROW_BUDGET * gridColumns
    AppListLayout.TWO_COLUMN -> PREVIEW_ROW_BUDGET * 2
    AppListLayout.SINGLE_COLUMN, AppListLayout.LAUNCHER_DEFAULT -> PREVIEW_ROW_BUDGET
}
private const val PREVIEW_DOCK_APP_COUNT = DockAppRepository.MAX_APPS

/** Matches `FacetCarouselScreen`'s own `CAROUSEL_CARD_SCALE` — a genuine scale model of the
 * screen, not a full-width/full-height card. */
private const val APPEARANCE_PREVIEW_CARD_SCALE = 0.55f

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
 * it. In facet scope the clock shows the facet's own override when it has one, else the global clock.
 */
@Composable
internal fun AppearancePreviewCard(
    settings: LauncherSettings,
    appRowPosition: AppRowPosition,
    appRowPresentation: AppRowPresentation,
    appListLayout: AppListLayout,
    appListColumnAlignment: AppListColumnAlignment,
    appListGridColumns: AppListGridColumns,
    appListGridDisplayMode: AppListGridDisplayMode,
    dockDisplayMode: DockDisplayMode,
    favorites: List<PlacedItem>,
    dockItems: List<PlacedItem>,
    calendarEvents: List<CalendarEvent>,
    calendarDisplay: CalendarDisplayOptions,
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
                    calendarDisplay = calendarDisplay,
                    homeAppsFontWeight = settings.homeAppsFontWeight,
                    appLabelColorOption = settings.appLabelColorOption,
                    launcherFontOption = settings.launcherFontOption,
                    clockScale = 1f,
                    modifier = Modifier.testTag("appearance_preview_clock"),
                )

                Spacer(modifier = Modifier.weight(1f))

                AppearancePreviewAppList(
                    favorites = favorites.take(appListLayout.previewItemCap(appListGridColumns.columns)),
                    appListLayout = appListLayout,
                    appListColumnAlignment = appListColumnAlignment,
                    appListGridColumns = appListGridColumns,
                    appListGridDisplayMode = appListGridDisplayMode,
                    appRowPosition = appRowPosition,
                    appRowPresentation = appRowPresentation,
                    settings = settings,
                )

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

/** [AppearancePreviewCard]'s app-list region — its own 3-way [AppListLayout] branch, extracted to a dedicated composable to keep the card's own nesting depth within this codebase's Compose-nesting lint threshold. */
@Composable
private fun AppearancePreviewAppList(
    favorites: List<PlacedItem>,
    appListLayout: AppListLayout,
    appListColumnAlignment: AppListColumnAlignment,
    appListGridColumns: AppListGridColumns,
    appListGridDisplayMode: AppListGridDisplayMode,
    appRowPosition: AppRowPosition,
    appRowPresentation: AppRowPresentation,
    settings: LauncherSettings,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        when (appListLayout) {
            AppListLayout.GRID -> {
                // Fixed-slot columns, not Arrangement.SpaceEvenly — a short last row must leave
                // its missing slots empty and pack real items to the left, matching HomeScreen's
                // own LazyVerticalGrid(GridCells.Fixed(n)) behavior exactly (caught via on-device
                // comparison: SpaceEvenly stretched an incomplete row's items across the full
                // width instead, see chat history).
                favorites.chunked(appListGridColumns.columns).forEach { rowItems ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        repeat(appListGridColumns.columns) { columnIndex ->
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                rowItems.getOrNull(columnIndex)?.let { item ->
                                    AppearancePreviewGridTile(
                                        item = item,
                                        displayMode = appListGridDisplayMode,
                                        labelColor = settings.appLabelColorOption.resolve(),
                                        labelFontWeight = settings.homeAppsFontWeight.resolve(),
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            AppListLayout.TWO_COLUMN -> {
                val (leftPosition, rightPosition) = appListColumnAlignment.toColumnPositions()
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf(
                        favorites.filterIndexed { index, _ -> index % 2 == 0 } to leftPosition,
                        favorites.filterIndexed { index, _ -> index % 2 == 1 } to rightPosition,
                    ).forEach { (items, position) ->
                        Column(modifier = Modifier.weight(1f)) {
                            items.forEach { item ->
                                AppearancePreviewRow(item = item, position = position, presentation = appRowPresentation, settings = settings)
                            }
                        }
                    }
                }
            }
            AppListLayout.SINGLE_COLUMN, AppListLayout.LAUNCHER_DEFAULT -> {
                favorites.forEach { item ->
                    AppearancePreviewRow(item = item, position = appRowPosition, presentation = appRowPresentation, settings = settings)
                }
            }
        }
    }
}

/** [AppearancePreviewCard]'s single/two-column row dispatch, extracted since both layouts need the identical [AppRow]/[FolderRow] call, just with a different [position] and item subset. */
@Composable
private fun AppearancePreviewRow(item: PlacedItem, position: AppRowPosition, presentation: AppRowPresentation, settings: LauncherSettings) {
    when (item) {
        is PlacedItem.SingleApp -> AppRow(
            app = item.app,
            onClick = {},
            badgeCount = null,
            badgeStyle = NotificationBadgeStyle.DOT,
            appMenuNotifications = AppMenuNotifications.None,
            onAppInfo = {},
            position = position,
            presentation = presentation,
            labelColor = settings.appLabelColorOption.resolve(),
            labelFontWeight = settings.homeAppsFontWeight.resolve(),
            enableLongPressMenu = false,
            testTagPrefix = "appearance_preview_",
        )
        is PlacedItem.FolderItem -> FolderRow(
            folder = item.folder,
            onAppClick = {},
            appMenuNotifications = AppMenuNotifications.None,
            onAppInfo = {},
            onRemoveFromFolder = { _, _ -> },
            onRenameFolder = { _, _ -> },
            drawerPresentation = DrawerPresentation.LIST,
            position = position,
            presentation = presentation,
            labelColor = settings.appLabelColorOption.resolve(),
            labelFontWeight = settings.homeAppsFontWeight.resolve(),
            enableLongPressMenu = false,
            onClick = {},
            testTagPrefix = "appearance_preview_",
        )
    }
}

/** [AppearancePreviewCard]'s grid-mode tile — read-only, so a plain centered icon-or-label [Box], not [HomeScreen][com.facetlauncher.app.ui.home.HomeScreen]'s own clickable/long-pressable grid tile. See [AppListGridDisplayMode]'s own doc for why this is a strict icon-or-text branch. */
@Composable
private fun AppearancePreviewGridTile(item: PlacedItem, displayMode: AppListGridDisplayMode, labelColor: Color, labelFontWeight: FontWeight) {
    val tagSuffix = when (item) {
        is PlacedItem.SingleApp -> item.app.packageName
        is PlacedItem.FolderItem -> "folder_${item.folder.id}"
    }
    Box(modifier = Modifier.padding(vertical = 4.dp), contentAlignment = Alignment.Center) {
        val label = when (item) {
            is PlacedItem.SingleApp -> item.app.label
            is PlacedItem.FolderItem -> item.folder.name
        }
        if (displayMode == AppListGridDisplayMode.TEXT) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = labelColor,
                maxLines = 1,
                fontWeight = labelFontWeight,
                modifier = Modifier.testTag("appearance_preview_grid_label_$tagSuffix"),
            )
        } else {
            when (item) {
                is PlacedItem.SingleApp -> AppIcon(icon = item.app.icon, size = AppIconSize.TILE, contentDescription = label, modifier = Modifier.testTag("appearance_preview_grid_icon_$tagSuffix"))
                is PlacedItem.FolderItem -> FolderTileGlyph(folder = item.folder)
            }
        }
    }
}

/** Design-time stand-ins for the Appearance screens' previews — Android Studio's `@Preview`
 * rendering has no real repositories to query, unlike the screen's own live
 * [AppearancePreviewCard], which sources the real effective favorites/dock/calendar instead (see
 * its doc). */
internal val DesignTimeFavorites = listOf(
    PlacedItem.SingleApp(AppInfo(packageName = "preview.appearance.one", activityName = ".Main", label = "Camera", icon = null)),
    PlacedItem.SingleApp(AppInfo(packageName = "preview.appearance.two", activityName = ".Main", label = "Messages", icon = null)),
)
internal val DesignTimeDockItems = listOf(
    PlacedItem.SingleApp(AppInfo(packageName = "preview.appearance.dock.one", activityName = ".Main", label = "Phone", icon = null)),
    PlacedItem.SingleApp(AppInfo(packageName = "preview.appearance.dock.two", activityName = ".Main", label = "Browser", icon = null)),
    PlacedItem.SingleApp(AppInfo(packageName = "preview.appearance.dock.three", activityName = ".Main", label = "Mail", icon = null)),
)
