package com.facetlauncher.app.ui.settings

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.HomeWallpaper
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.ui.home.HomeScreenContent
import com.facetlauncher.app.ui.home.ScaledHomeScreen
import com.facetlauncher.app.ui.theme.Hairline

/** Matches `FacetCarouselScreen`'s `CAROUSEL_CARD_SCALE` — a scale model of the screen, not a full-width card. */
private const val APPEARANCE_PREVIEW_CARD_SCALE = 0.55f

/**
 * "This is how Home looks" for this scope: the shared [ScaledHomeScreen] fed the scope's real,
 * facet-or-global resolved content ([AppearanceSettingsUiState]), so clock and app-list position
 * and alignment match the facet carousel's cards and Home itself.
 */
@Composable
internal fun AppearancePreviewCard(
    uiState: AppearanceSettingsUiState,
    homeWallpaper: HomeWallpaper,
    modifier: Modifier = Modifier,
) {
    val config = LocalConfiguration.current
    val cardShape = MaterialTheme.shapes.extraLarge
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        ScaledHomeScreen(
            style = uiState.style,
            settings = uiState.settings,
            content = HomeScreenContent(
                listItems = uiState.effectiveFavorites,
                dockItems = uiState.effectiveDockItems,
                calendarEvents = uiState.calendarEvents,
            ),
            homeWallpaper = homeWallpaper,
            tagPrefix = "appearance_preview_",
            modifier = Modifier
                .fillMaxWidth(APPEARANCE_PREVIEW_CARD_SCALE)
                .aspectRatio(config.screenWidthDp.toFloat() / config.screenHeightDp.toFloat())
                .shadow(elevation = 4.dp, shape = cardShape)
                .clip(cardShape)
                .border(width = 1.dp, color = Hairline, shape = cardShape),
        )
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
