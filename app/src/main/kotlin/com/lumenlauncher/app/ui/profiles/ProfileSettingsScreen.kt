package com.lumenlauncher.app.ui.profiles

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.data.model.ListContentMode
import com.lumenlauncher.app.ui.components.AppIcon
import com.lumenlauncher.app.ui.components.BackButton
import com.lumenlauncher.app.ui.components.CardDivider
import com.lumenlauncher.app.ui.components.InheritOverrideCard
import com.lumenlauncher.app.ui.components.LabeledDropdownRow
import com.lumenlauncher.app.ui.components.RenameDialog
import com.lumenlauncher.app.ui.components.SettingsCard
import com.lumenlauncher.app.ui.components.StickyHeaderLayout
import com.lumenlauncher.app.ui.theme.Faint
import com.lumenlauncher.app.ui.theme.Ink
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme
import com.lumenlauncher.app.ui.theme.Muted
import com.lumenlauncher.app.ui.theme.Surface
import kotlin.math.roundToInt

private fun ListContentMode.displayLabel(): String = when (this) {
    ListContentMode.FAVORITES -> "Favorites"
    ListContentMode.RECENTS -> "Recents"
    ListContentMode.MOST_USED -> "Most used"
}

/** README `3d`'s 4…8 range. */
private val APPS_TO_SHOW_OPTIONS = (4..8).toList()

/** Per-profile settings (`3d`), reached from the long-press sheet's "Edit profile" or the carousel's gear. */
@Composable
fun ProfileSettingsScreen(
    onBack: () -> Unit,
    onNavigateToFavorites: (profileId: Long) -> Unit,
    onNavigateToCalendarSettings: (profileId: Long) -> Unit,
    onNavigateToClockStyleGallery: (profileId: Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileSettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ProfileSettingsContent(
        uiState = uiState,
        onBack = onBack,
        onFavoritesClick = { onNavigateToFavorites(viewModel.profileId) },
        onCalendarClick = { onNavigateToCalendarSettings(viewModel.profileId) },
        onClockStyleClick = { onNavigateToClockStyleGallery(viewModel.profileId) },
        onRename = viewModel::renameProfile,
        onOverridingClockChanged = viewModel::setOverridingClock,
        onUse24HourTimeOverrideChanged = viewModel::setUse24HourTimeOverride,
        onOverridingAppsChanged = viewModel::setOverridingApps,
        onListContentModeChanged = viewModel::setListContentMode,
        onAppsToShowCountChanged = viewModel::setAppsToShowCount,
        onReorderFavorites = viewModel::reorderFavorites,
        modifier = modifier,
    )
}

@Composable
private fun ProfileSettingsContent(
    uiState: ProfileSettingsUiState,
    onBack: () -> Unit,
    onFavoritesClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onClockStyleClick: () -> Unit,
    onRename: (String) -> Unit,
    onOverridingClockChanged: (Boolean) -> Unit,
    onUse24HourTimeOverrideChanged: (Boolean) -> Unit,
    onOverridingAppsChanged: (Boolean) -> Unit,
    onListContentModeChanged: (ListContentMode) -> Unit,
    onAppsToShowCountChanged: (Int) -> Unit,
    onReorderFavorites: (List<AppInfo>) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showRenameDialog by remember { mutableStateOf(false) }
    val profile = uiState.profile

    StickyHeaderLayout(
        modifier = modifier,
        header = { ProfileSettingsHeader(title = profile?.name ?: "Profile", onBack = onBack) },
        content = { headerHeight ->
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
            .testTag("profile_settings_screen")
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(top = headerHeight),
    ) {
        item {
            SettingsCard {
                ClickableRow(
                    title = "Rename profile",
                    subtitle = profile?.name,
                    onClick = { showRenameDialog = true },
                    testTag = "profile_settings_rename_row",
                    trailing = { NavigationChevron() },
                )
            }
        }

        item { Spacer(modifier = Modifier.height(18.dp)) }
        item { SectionHeader("APPS") }
        item {
            AppsSection(
                uiState = uiState,
                onOverridingAppsChanged = onOverridingAppsChanged,
                onListContentModeChanged = onListContentModeChanged,
                onAppsToShowCountChanged = onAppsToShowCountChanged,
                onFavoritesClick = onFavoritesClick,
                onReorderFavorites = onReorderFavorites,
            )
        }

        item { Spacer(modifier = Modifier.height(18.dp)) }
        item { SectionHeader("CLOCK") }
        item {
            InheritOverrideCard(
                overriding = uiState.isOverridingClock,
                onOverridingChanged = onOverridingClockChanged,
                testTagPrefix = "profile_clock",
                inheritSubtitle = "${uiState.globalClockTemplateId.name.replace("_", " ")} · ${if (uiState.globalUse24HourTime) "24-hour" else "12-hour"} time",
            )
        }
        item { Spacer(modifier = Modifier.height(12.dp)) }
        item {
            SettingsCard {
                ClickableRow(
                    title = "Clock style",
                    subtitle = if (uiState.isOverridingClock) "Overriding defaults · tap to edit" else "Inherits default · Light stack · tap to preview",
                    onClick = onClockStyleClick,
                    testTag = "profile_clock_style_gallery_row",
                    trailing = { NavigationChevron() },
                )
                CardDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 13.dp)
                        .alpha(if (uiState.isOverridingClock) 1f else 0.4f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = "24-hour time", style = MaterialTheme.typography.bodyLarge, color = Ink)
                    Switch(
                        checked = uiState.effectiveUse24HourTime,
                        onCheckedChange = onUse24HourTimeOverrideChanged,
                        enabled = uiState.isOverridingClock,
                        modifier = Modifier.testTag("profile_use_24_hour_time_toggle"),
                    )
                }
                CardDivider()
                ClickableRow(
                    title = "Calendar",
                    subtitle = null,
                    onClick = onCalendarClick,
                    testTag = "profile_calendar_settings_row",
                    trailing = { NavigationChevron() },
                )
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
        },
    )

    if (showRenameDialog && profile != null) {
        RenameDialog(
            title = "Rename profile",
            explanation = "Rename this profile.",
            initialValue = profile.name,
            onSave = { newName -> onRename(newName); showRenameDialog = false },
            onDismiss = { showRenameDialog = false },
        )
    }
}

@Composable
private fun ProfileSettingsHeader(title: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Surface)
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BackButton(onClick = onBack)
        Text(text = title, style = MaterialTheme.typography.headlineSmall, color = Ink)
    }
}

@Composable
private fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(text = title, style = MaterialTheme.typography.labelSmall, color = Faint, modifier = modifier.padding(bottom = 6.dp))
}

/**
 * The "APPS" section — one Inherit/Override card, then one card with everything it governs
 * (list content mode, apps-to-show, favorites), mirroring the shape of Settings' own "APPS LIST"
 * card exactly rather than splitting list-content and favorites into two independent overrides.
 */
@Composable
private fun AppsSection(
    uiState: ProfileSettingsUiState,
    onOverridingAppsChanged: (Boolean) -> Unit,
    onListContentModeChanged: (ListContentMode) -> Unit,
    onAppsToShowCountChanged: (Int) -> Unit,
    onFavoritesClick: () -> Unit,
    onReorderFavorites: (List<AppInfo>) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        InheritOverrideCard(
            overriding = uiState.isOverridingApps,
            onOverridingChanged = onOverridingAppsChanged,
            testTagPrefix = "profile_apps",
            inheritSubtitle = uiState.globalListContentMode.displayLabel() +
                if (uiState.globalListContentMode != ListContentMode.FAVORITES) " · ${uiState.globalAppsToShowCount} apps" else "",
        )
        Spacer(modifier = Modifier.height(12.dp))
        SettingsCard {
            LabeledDropdownRow(
                title = "App list content",
                options = ListContentMode.entries,
                selected = uiState.listContentMode,
                label = { it.displayLabel() },
                onSelect = onListContentModeChanged,
                enabled = uiState.isOverridingApps,
                testTag = "app_list_content_row",
            )
            if (uiState.listContentMode != ListContentMode.FAVORITES) {
                CardDivider()
                LabeledDropdownRow(
                    title = "Apps to show",
                    options = APPS_TO_SHOW_OPTIONS,
                    selected = uiState.appsToShowCount,
                    label = { it.toString() },
                    onSelect = onAppsToShowCountChanged,
                    enabled = uiState.isOverridingApps,
                    testTag = "apps_to_show_row",
                )
            }
            CardDivider()
            ClickableRow(
                title = "Edit favorites",
                subtitle = uiState.favoritesLabel,
                onClick = onFavoritesClick,
                testTag = "profile_settings_favorites_row",
                enabled = uiState.isOverridingApps,
                trailing = { NavigationChevron() },
            )
            if (uiState.isOverridingApps && uiState.favorites.isNotEmpty()) {
                CardDivider()
                FavoritesReorderList(favorites = uiState.favorites, onReorder = onReorderFavorites)
            }
        }
    }
}

private const val FAVORITE_ROW_HEIGHT_DP = 52
private val REORDER_DRAG_ELEVATION = 6.dp
private const val REORDER_DRAG_SCALE = 1.04f
private val REORDER_ROW_SHAPE = RoundedCornerShape(12.dp)

/**
 * Drag-to-reorder only — mirrors Settings' Dock card's own drag pattern (see `DockSection` in
 * `SettingsScreen.kt`), just vertical instead of horizontal, since favorites render as a vertical
 * list rather than a horizontal row. Adding/removing a favorite happens on the picker screen
 * ([FavoritesPickerScreen]), reached via the "Edit favorites" row above this list — never here.
 *
 * A real (non-scrolling — nested inside this card's own outer, already-scrolling LazyColumn)
 * [LazyColumn] rather than a plain [Column], so each row carries a stable [AppInfo] key and
 * [androidx.compose.foundation.lazy.LazyItemScope.animateItem] — the other rows slide smoothly
 * into their new slots instead of snapping, and while a row is actively dragged it lifts off the
 * list (scaled up, a real shadow, its own rounded surface) rather than just sliding in place.
 */
@Composable
private fun FavoritesReorderList(favorites: List<AppInfo>, onReorder: (List<AppInfo>) -> Unit, modifier: Modifier = Modifier) {
    // Keyed on component identity only, not the raw `favorites` list — that list's own AppInfo.icon
    // is a freshly-decoded ImageBitmap on every LauncherApps re-emission (even ones unrelated to
    // favorites), so it's structurally "new" far more often than the actual membership/order
    // changes. Keying remember()/pointerInput() on the raw list instead would reset drag state —
    // and restart the gesture detector — mid-drag on every such incidental re-emission, which is
    // exactly what feels like the dragged row "jumping"/losing the finger.
    val componentsKey = favorites.map { it.packageName to it.activityName }
    var order by remember(componentsKey) { mutableStateOf(favorites) }
    var draggingComponent by remember { mutableStateOf<Pair<String, String>?>(null) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    val rowHeightPx = with(LocalDensity.current) { FAVORITE_ROW_HEIGHT_DP.dp.toPx() }
    // The gesture-detection coroutine below is long-lived (one drag start-to-end) and its
    // pointerInput key is deliberately just the row's own stable component — it must read the
    // *current* order/callback through these rather than closing over a snapshot from whenever
    // the coroutine started, without needing to restart the coroutine to get a fresh one.
    val currentOrder = rememberUpdatedState(order)
    val currentOnReorder = rememberUpdatedState(onReorder)

    LazyColumn(
        modifier = modifier.fillMaxWidth().height((FAVORITE_ROW_HEIGHT_DP * order.size).dp),
        userScrollEnabled = false,
    ) {
        items(order, key = { it.packageName + it.activityName }) { app ->
            val component = app.packageName to app.activityName
            val isDragging = component == draggingComponent
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(FAVORITE_ROW_HEIGHT_DP.dp)
                    .testTag("profile_favorite_reorder_row_${app.packageName}")
                    .zIndex(if (isDragging) 1f else 0f)
                    .graphicsLayer {
                        translationY = if (isDragging) dragOffsetY else 0f
                        scaleX = if (isDragging) REORDER_DRAG_SCALE else 1f
                        scaleY = if (isDragging) REORDER_DRAG_SCALE else 1f
                        shadowElevation = if (isDragging) REORDER_DRAG_ELEVATION.toPx() else 0f
                        shape = REORDER_ROW_SHAPE
                        clip = false
                    }
                    .then(if (isDragging) Modifier.background(Surface, REORDER_ROW_SHAPE) else Modifier)
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
                        .testTag("profile_favorite_reorder_handle_${app.packageName}")
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
private fun RowScaffold(
    title: String,
    subtitle: String?,
    modifier: Modifier = Modifier,
    titleAlpha: Float = 1f,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 13.dp).alpha(titleAlpha),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
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
private fun ClickableRow(
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    trailing: (@Composable () -> Unit)? = null,
) {
    RowScaffold(
        title = title,
        subtitle = subtitle,
        titleAlpha = if (enabled) 1f else 0.4f,
        modifier = modifier.testTag(testTag).clickable(enabled = enabled, onClick = onClick),
        trailing = trailing,
    )
}

/** Trailing affordance for a row that navigates to its own screen (Edit favorites, Calendar). */
@Composable
private fun NavigationChevron(modifier: Modifier = Modifier) {
    Icon(
        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
        contentDescription = null,
        tint = Muted,
        modifier = modifier,
    )
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ProfileSettingsScreenPreview() {
    LumenLauncherTheme {
        ProfileSettingsContent(
            uiState = ProfileSettingsUiState(),
            onBack = {},
            onFavoritesClick = {},
            onCalendarClick = {},
            onClockStyleClick = {},
            onRename = {},
            onOverridingClockChanged = {},
            onUse24HourTimeOverrideChanged = {},
            onOverridingAppsChanged = {},
            onListContentModeChanged = {},
            onAppsToShowCountChanged = {},
            onReorderFavorites = {},
        )
    }
}
