package com.facetlauncher.app.ui.home

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder as FolderIcon
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.model.Folder
import com.facetlauncher.app.ui.components.AppIcon
import com.facetlauncher.app.ui.components.AppIconSize
import com.facetlauncher.app.ui.components.longPressReleaseClickable
import com.facetlauncher.app.ui.home.FolderRow
import com.facetlauncher.app.ui.home.FolderTileGlyph
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.FolderGlyphBackground
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.homeAppLabelShadow
import kotlinx.coroutines.flow.map

/** A [AppIconSize.TILE]-sized, `medium`/12dp-shaped tile showing up to 4 of the folder's app icons — a 1x2 row for 1-2 apps, a 2x2 grid for 3-4 — CLAUDE.md's M3 shape table, tile scale. No inner padding: icons run to the tile's own edge so the outer rounded-corner clip crops their corners slightly, the same way a real folder preview reads as "full" rather than an icon floating in a frame. A 0-app folder (valid, persistent — never auto-deleted) renders a single centered muted folder glyph instead. */
@Composable
internal fun FolderTileGlyph(folder: Folder, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(AppIconSize.TILE)
            .clip(RoundedCornerShape(12.dp))
            .background(FolderGlyphBackground)
            .drawWithContent {
                drawContent()
                // Slight vignette for inset depth, rather than a flat plate.
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.18f)),
                        center = center,
                        radius = size.maxDimension * 0.75f,
                    ),
                )
            },
    ) {
        if (folder.apps.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.FolderIcon,
                    contentDescription = null,
                    tint = Muted,
                    modifier = Modifier.size(AppIconSize.SHORTCUT),
                )
            }
        } else {
            val previewApps = folder.apps.take(4)
            if (previewApps.size <= 2) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    for (colIndex in 0..1) {
                        FolderTileGlyphSlot(app = previewApps.getOrNull(colIndex))
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    for (rowIndex in 0..1) {
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally)) {
                            for (colIndex in 0..1) {
                                FolderTileGlyphSlot(app = previewApps.getOrNull(rowIndex * 2 + colIndex))
                            }
                        }
                    }
                }
            }
        }
    }
}

/** One cell of [FolderTileGlyph]'s grid/row — an app icon, or a blank spacer when the folder has fewer apps than slots. */
@Composable
private fun FolderTileGlyphSlot(app: AppInfo?) {
    if (app != null) {
        AppIcon(icon = app.icon, monochromeIcon = app.monochromeIcon, size = AppIconSize.SHORTCUT, contentDescription = null)
    } else {
        Box(modifier = Modifier.size(AppIconSize.SHORTCUT))
    }
}

/**
 * A Home row for a folder. A tap opens its contents sheet, a long-press its menu; with [enableLongPressMenu]
 * off (the facet carousel's read-only preview cards) a tap calls [onClick] instead and there is no menu.
 */
@Composable
internal fun FolderRow(
    folder: Folder,
    actions: HomeItemActions,
    style: HomeItemStyle,
    modifier: Modifier = Modifier,
    position: AppRowPosition = AppRowPosition.LEFT,
    presentation: AppRowPresentation = AppRowPresentation.ICON_AND_TEXT,
    verticalPadding: Dp = HOME_APP_ROW_REGULAR_VERTICAL_PADDING,
    iconSize: Dp = AppIconSize.ROW_REGULAR,
    testTagPrefix: String = "home_",
    enableLongPressMenu: Boolean = true,
    onClick: () -> Unit = {},
) {
    var sheetOpen by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    val clickModifier = if (enableLongPressMenu) {
        Modifier.longPressReleaseClickable(onClick = { sheetOpen = true }, onLongPress = { menuExpanded = true })
    } else {
        Modifier.clickable(onClick = onClick)
    }
    FolderRowContent(
        folder = folder,
        style = style,
        position = position,
        presentation = presentation,
        iconSize = iconSize,
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .then(clickModifier)
            .testTag(homeTag("${testTagPrefix}folder_row_${folder.id}"))
            .padding(horizontal = 8.dp, vertical = verticalPadding),
    )
    if (enableLongPressMenu && sheetOpen) FolderItemSheet(folder, { sheetOpen = false }, actions, style)
    if (enableLongPressMenu) FolderItemMenu(folder, menuExpanded, { menuExpanded = false }, actions)
}

/** A [FolderRow]'s glyph and name, packed by [position] as [AppRow] packs its own content. */
@Composable
private fun FolderRowContent(
    folder: Folder,
    style: HomeItemStyle,
    position: AppRowPosition,
    presentation: AppRowPresentation,
    iconSize: Dp,
    modifier: Modifier = Modifier,
) {
    val showIcon = presentation != AppRowPresentation.TEXT_ONLY
    val showLabel = presentation != AppRowPresentation.ICON_ONLY
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = appRowArrangement(position)) {
        val icon: @Composable () -> Unit = {
            if (showIcon) FolderTileGlyph(folder = folder, modifier = Modifier.size(iconSize))
        }
        val label: @Composable () -> Unit = {
            if (showLabel) {
                Text(
                    text = folder.name,
                    style = MaterialTheme.typography.titleMedium.copy(shadow = homeAppLabelShadow(style.labelColor), fontWeight = style.labelFontWeight),
                    color = style.labelColor,
                )
            }
        }
        if (position == AppRowPosition.RIGHT) {
            label()
            icon()
        } else {
            icon()
            label()
        }
    }
}

/** Every [FolderTileGlyph] slot count side by side: 0 (muted glyph fallback), 1-2 (1x2 row), 3-4 (2x2 grid). */
@Preview(name = "Folder glyph", showBackground = true)
@Preview(name = "Folder glyph - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FolderTileGlyphPreview() {
    FacetLauncherTheme {
        val apps = (1..4).map {
            AppInfo(packageName = "com.example.app$it", activityName = ".MainActivity", label = "App $it", icon = null)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(16.dp)) {
            for (count in 0..4) {
                FolderTileGlyph(folder = Folder(id = count.toLong(), name = "Folder", apps = apps.take(count)))
            }
        }
    }
}
