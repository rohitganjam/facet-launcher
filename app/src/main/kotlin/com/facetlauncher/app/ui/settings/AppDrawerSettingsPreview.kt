package com.facetlauncher.app.ui.settings

import android.content.res.Configuration
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.DrawerFolderDisplayMode
import com.facetlauncher.app.data.model.DrawerPresentation
import com.facetlauncher.app.data.model.Folder
import com.facetlauncher.app.data.model.HomeWallpaper
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.ui.components.WallpaperBackground
import com.facetlauncher.app.ui.drawer.AppDrawerScreen
import com.facetlauncher.app.ui.drawer.DrawerAppearance
import com.facetlauncher.app.ui.drawer.DrawerSearchBarConfig
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Hairline
import com.facetlauncher.app.ui.theme.resolve
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

private val PREVIEW_VIEWPORT_HEIGHT = 400.dp
private const val RECENT_SAMPLE_COUNT = 2
private const val FOLDER_SAMPLE_APP_COUNT = 2
private const val HOUR_MILLIS = 60 * 60 * 1000L
private const val SAMPLE_FOLDER_ID = 1L

/** Sample apps and a folder for [AppDrawerSettingsPreview]; icons are null, so the drawer draws its tile placeholder. */
internal data class DrawerPreviewSample(val apps: ImmutableList<AppInfo>, val folders: ImmutableList<Folder>)

/** The first [RECENT_SAMPLE_COUNT] labels are installed an hour ago, so the "Recently installed" row has something in it. */
internal fun drawerPreviewSample(appLabels: List<String>, folderName: String, nowMillis: Long): DrawerPreviewSample {
    val apps = appLabels.mapIndexed { index, label ->
        AppInfo(
            packageName = "preview.sample.$index",
            activityName = ".Main",
            label = label,
            icon = null,
            firstInstallTime = if (index < RECENT_SAMPLE_COUNT) nowMillis - HOUR_MILLIS else 0L,
        )
    }.sortedBy { it.label.lowercase() }.toImmutableList()
    return DrawerPreviewSample(apps, persistentListOf(Folder(SAMPLE_FOLDER_ID, folderName, apps.takeLast(FOLDER_SAMPLE_APP_COUNT))))
}

/**
 * The real [AppDrawerScreen] on sample apps over the wallpaper, set up from [settings], so presentation, sizes, icons, labels,
 * folders, recently installed, search bar position and opacity all show their effect as they change. Taps do nothing; a vertical
 * drag scrolls the sample apps, and what it can't scroll passes on to the settings list.
 */
@Composable
internal fun AppDrawerSettingsPreview(settings: LauncherSettings, wallpaper: HomeWallpaper, modifier: Modifier = Modifier) {
    val labels = stringArrayResource(R.array.drawer_preview_app_labels).toList()
    val folderName = stringResource(R.string.drawer_preview_folder_name)
    val sample = remember(labels, folderName) { drawerPreviewSample(labels, folderName, System.currentTimeMillis()) }
    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()
    val shape = MaterialTheme.shapes.extraLarge
    val description = stringResource(R.string.app_drawer_preview_description)
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val isGrid = settings.drawerPresentation == DrawerPresentation.GRID
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        // The drawer is laid out at the real screen width and shrunk to the card, so its columns and rows are the ones a phone gets.
        val scale = maxWidth / screenWidth
        // A drag moves the card's pixels, the list scrolls its own, so convert.
        val scrollState = rememberScrollableState { delta ->
            val state: ScrollableState = if (isGrid) gridState else listState
            -state.dispatchRawDelta(-delta / scale) * scale
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(PREVIEW_VIEWPORT_HEIGHT * scale)
                .clip(shape)
                .border(1.dp, Hairline, shape)
                .testTag("app_drawer_preview")
                .clearAndSetSemantics { contentDescription = description },
        ) {
            WallpaperBackground(wallpaper, Modifier.matchParentSize())
            AppDrawerScreen(
                apps = sample.apps,
                modifier = Modifier
                    .requiredSize(screenWidth, PREVIEW_VIEWPORT_HEIGHT)
                    .graphicsLayer { scaleX = scale; scaleY = scale }
                    .consumeWindowInsets(WindowInsets.systemBars),
                listState = listState,
                gridState = gridState,
                appearance = DrawerAppearance(
                    presentation = settings.drawerPresentation,
                    gridDensity = settings.drawerGridDensity,
                    listItemSize = settings.drawerListItemSize,
                    showIcons = settings.showDrawerIcons,
                    showLabels = settings.showDrawerLabels,
                    labelFontWeight = settings.homeAppsFontWeight.resolve(),
                    opacity = settings.drawerOpacity,
                    notificationBadgeStyle = settings.notificationBadgeStyle,
                    folderDisplayMode = settings.drawerFolderDisplayMode,
                    recentlyInstalledPosition = settings.recentlyInstalledPosition,
                ),
                searchBar = DrawerSearchBarConfig(position = settings.searchBarPosition),
                folderCandidates = sample.folders,
            )
            // A later sibling takes the touches first, so taps never reach the drawer. Vertical drags scroll its list or grid, and
            // what they can't consume (the ends) passes on to the settings list.
            Box(
                Modifier
                    .matchParentSize()
                    .scrollable(state = scrollState, orientation = Orientation.Vertical)
                    .pointerInput(Unit) {},
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390)
@Preview(name = "Dark", showBackground = true, widthDp = 390, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AppDrawerSettingsPreviewListPreview() {
    FacetLauncherTheme {
        AppDrawerSettingsPreview(
            settings = LauncherSettings(drawerFolderDisplayMode = DrawerFolderDisplayMode.SHOW_FIRST),
            wallpaper = HomeWallpaper.Unavailable,
        )
    }
}

@Preview(showBackground = true, widthDp = 390)
@Preview(name = "Dark", showBackground = true, widthDp = 390, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AppDrawerSettingsPreviewGridPreview() {
    FacetLauncherTheme {
        AppDrawerSettingsPreview(
            settings = LauncherSettings(drawerPresentation = DrawerPresentation.GRID),
            wallpaper = HomeWallpaper.Unavailable,
        )
    }
}
