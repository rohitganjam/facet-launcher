package com.facetlauncher.app.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.model.DockDisplayMode
import com.facetlauncher.app.data.model.HomeWallpaper
import com.facetlauncher.app.data.model.NotificationBadgeStyle
import com.facetlauncher.app.ui.home.AppRow
import com.facetlauncher.app.ui.home.DockIcon
import com.facetlauncher.app.ui.theme.Hairline
import com.facetlauncher.app.ui.theme.Muted

/**
 * "This is how Home looks" preview card, shared by Settings → Appearance / Home Apps List / Dock
 * and by a facet's own Apps-list / Dock sub-screens. Uses the real production [AppRow] and
 * [DockIcon] over the device's actual wallpaper ([WallpaperBackground]) rather than a mockup, so
 * the position/presentation/display-style/label choices render exactly as they would on Home.
 *
 * Pass an empty [appList] to omit the app rows, an empty [dockApps] to omit the dock — callers
 * that only govern one of the two surfaces show only that one. Both empty renders a short
 * placeholder rather than an empty card.
 */
@Composable
fun HomeSurfacePreview(
    appList: List<AppInfo>,
    dockApps: List<AppInfo>,
    labelColor: Color,
    labelFontWeight: FontWeight,
    homeWallpaper: HomeWallpaper,
    modifier: Modifier = Modifier,
    appRowPosition: AppRowPosition = AppRowPosition.LEFT,
    appRowPresentation: AppRowPresentation = AppRowPresentation.ICON_AND_TEXT,
    dockDisplayMode: DockDisplayMode = DockDisplayMode.ICONS,
    maxAppRows: Int = 3,
) {
    val rows = appList.take(maxAppRows)
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = shape)
            .clip(shape)
            .border(1.dp, Hairline, shape),
    ) {
        // The real system wallpaper — same backdrop Home renders these components over, so their
        // text-shadow treatment reads exactly as it would there. Bottom-anchored: this card is a
        // short band, so it shows the wallpaper's lower part (where Home's dock sits).
        WallpaperBackground(homeWallpaper, Modifier.matchParentSize(), alignment = Alignment.BottomCenter)

        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = "PREVIEW",
                style = MaterialTheme.typography.labelSmall,
                color = Muted,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            if (rows.isEmpty() && dockApps.isEmpty()) {
                Text(
                    text = "Nothing to preview yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Muted,
                )
            }
            rows.forEach { app ->
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
                    verticalPadding = 6.dp,
                    // Distinguishes this preview card's rows from Home's own real AppRow for the
                    // same app, in case both are ever present in one semantics tree.
                    testTagPrefix = "home_surface_preview_",
                )
            }
            if (dockApps.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    dockApps.forEach { app ->
                        DockIcon(
                            app = app,
                            displayMode = dockDisplayMode,
                            onClick = {},
                            labelColor = labelColor,
                            labelFontWeight = labelFontWeight,
                            enableLongPressMenu = false,
                        )
                    }
                }
            }
        }
    }
}
