package com.facetlauncher.app.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Dock
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.R
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/** Which list a placement action edits: the launcher-wide default when [facetName] is null, or that facet's own. */
internal data class PlacementScope(val facetName: String?)

/** One of the sheet's Favorites, Dock or Folder actions, in the shape both the tile and the row layout draw. */
internal data class PlacementActionUi(
    val tag: String,
    val icon: ImageVector,
    val label: String,
    /** Null for an action with no list to name, such as a folder. */
    val scope: PlacementScope?,
    /** True when the app is already there, so tapping takes it out. */
    val isRemove: Boolean,
    val onClick: () -> Unit,
)

/** A tile's label may wrap to this many lines; a third line means the tiles are too narrow and the rows take over. */
private const val MAX_TILE_LABEL_LINES = 2
private val TILE_GAP = 8.dp
private val TILE_HORIZONTAL_PADDING = 8.dp
private val ROW_MIN_HEIGHT = 56.dp
private val ROW_SCOPE_MAX_WIDTH = 140.dp

/**
 * Tiles fit only when every label wraps within [MAX_TILE_LABEL_LINES] without splitting a word and
 * every scope pill fits on one line. All widths are measured, in pixels.
 */
internal fun placementFitsTiles(
    labelLineCounts: List<Int>,
    longestWordWidths: List<Int>,
    pillWidths: List<Int>,
    textWidth: Int,
): Boolean =
    labelLineCounts.all { it <= MAX_TILE_LABEL_LINES } &&
        longestWordWidths.all { it <= textWidth } &&
        pillWidths.all { it <= textWidth }

/**
 * The sheet's bottom zone: Favorites, Dock and Folder as equal tiles, or as full-width rows once
 * the tiles can't hold their text. The choice is measured, not a text-size threshold, so a long
 * translation or facet name switches it at any size. All actions switch together.
 */
@Composable
internal fun PlacementActions(actions: ImmutableList<PlacementActionUi>, modifier: Modifier = Modifier) {
    if (actions.isEmpty()) return
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        if (rememberFitsTiles(actions, constraints.maxWidth)) PlacementTiles(actions) else PlacementRows(actions)
    }
}

@Composable
private fun rememberFitsTiles(actions: ImmutableList<PlacementActionUi>, availableWidth: Int): Boolean {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val labelStyle = MaterialTheme.typography.labelLarge
    val pillRatio = rememberChromeTextRatio()
    val pillStyle = MaterialTheme.typography.labelSmall.let { it.copy(fontSize = it.fontSize * pillRatio, lineHeight = it.lineHeight * pillRatio) }
    val globalLabel = stringResource(R.string.facet_scope_global)
    val inputs = actions.map { it.label to it.scope }
    return remember(inputs, availableWidth, density, labelStyle, pillStyle, globalLabel) {
        val gap = with(density) { TILE_GAP.roundToPx() }
        val padding = with(density) { TILE_HORIZONTAL_PADDING.roundToPx() }
        val pillPadding = with(density) { (ScopeBadgeHorizontalPadding * 2).roundToPx() }
        val textWidth = (availableWidth - gap * (actions.size - 1)) / actions.size - padding * 2
        fun naturalWidth(text: String, style: androidx.compose.ui.text.TextStyle) =
            measurer.measure(text, style, softWrap = false, maxLines = 1).size.width
        placementFitsTiles(
            labelLineCounts = actions.map { measurer.measure(it.label, labelStyle, constraints = Constraints(maxWidth = textWidth.coerceAtLeast(0))).lineCount },
            longestWordWidths = actions.map { action -> action.label.split(" ").maxOf { naturalWidth(it, labelStyle) } },
            pillWidths = actions.mapNotNull { action -> action.scope?.let { naturalWidth(it.facetName ?: globalLabel, pillStyle) + pillPadding } },
            textWidth = textWidth,
        )
    }
}

@Composable
private fun PlacementTiles(actions: ImmutableList<PlacementActionUi>) {
    // Folder has no pill, but its tile keeps the pill's height so every icon lines up.
    val reservesPillSlot = actions.any { it.scope != null }
    Row(
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(TILE_GAP),
    ) {
        actions.forEach { action ->
            PlacementTile(action, reservesPillSlot, Modifier.weight(1f).fillMaxHeight())
        }
    }
}

@Composable
private fun PlacementTile(action: PlacementActionUi, reservesPillSlot: Boolean, modifier: Modifier = Modifier) {
    val description = placementDescription(action)
    Column(
        modifier = modifier
            .testTag(action.tag)
            .placementContainer(action)
            .clickable(role = Role.Button, onClick = action.onClick)
            .semantics(mergeDescendants = true) { contentDescription = description }
            .padding(horizontal = TILE_HORIZONTAL_PADDING, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (reservesPillSlot) {
            val scope = action.scope
            if (scope != null) {
                FacetScopeBadge(facetName = scope.facetName, textScaleRatio = rememberChromeTextRatio())
            } else {
                // Same pill, invisible, so the slot is exactly as tall as a real one at any text size.
                FacetScopeBadge(facetName = null, modifier = Modifier.alpha(0f).clearAndSetSemantics {}, textScaleRatio = rememberChromeTextRatio())
            }
            Spacer(Modifier.height(8.dp))
        }
        PlacementIcon(action)
        Spacer(Modifier.height(4.dp))
        Text(
            text = action.label,
            style = MaterialTheme.typography.labelLarge,
            color = placementTextColor(action),
            textAlign = TextAlign.Center,
            maxLines = MAX_TILE_LABEL_LINES,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun PlacementRows(actions: ImmutableList<PlacementActionUi>) {
    Column(verticalArrangement = Arrangement.spacedBy(TILE_GAP)) {
        actions.forEach { action ->
            val description = placementDescription(action)
            Row(
                modifier = Modifier
                    .testTag(action.tag)
                    .fillMaxWidth()
                    .placementContainer(action)
                    .clickable(role = Role.Button, onClick = action.onClick)
                    .semantics(mergeDescendants = true) { contentDescription = description }
                    .heightIn(min = ROW_MIN_HEIGHT)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                PlacementIcon(action)
                Text(
                    text = action.label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = placementTextColor(action),
                    modifier = Modifier.weight(1f),
                )
                action.scope?.let { FacetScopeBadge(facetName = it.facetName, modifier = Modifier.widthIn(max = ROW_SCOPE_MAX_WIDTH), textScaleRatio = rememberChromeTextRatio()) }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Preview(name = "Dark", showBackground = true, widthDp = 360, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PlacementActionsPreview() {
    FacetLauncherTheme {
        Box(Modifier.padding(24.dp)) {
            PlacementActions(
                actions = persistentListOf(
                    PlacementActionUi("p_fav", Icons.Outlined.StarBorder, "Add to Favorites", PlacementScope(null), isRemove = false, onClick = {}),
                    PlacementActionUi("p_dock", Icons.Outlined.Dock, "Remove from Dock", PlacementScope("Work"), isRemove = true, onClick = {}),
                    PlacementActionUi("p_folder", Icons.Outlined.Folder, "Add to folder", scope = null, isRemove = false, onClick = {}),
                ),
            )
        }
    }
}

@Preview(name = "Rows", showBackground = true, widthDp = 360)
@Composable
private fun PlacementActionsRowsPreview() {
    FacetLauncherTheme {
        Box(Modifier.padding(24.dp)) {
            PlacementActions(
                actions = persistentListOf(
                    PlacementActionUi("p_fav", Icons.Outlined.StarBorder, "Add to Favorites with a very long label here", PlacementScope(null), isRemove = false, onClick = {}),
                    PlacementActionUi("p_dock", Icons.Outlined.Dock, "Remove from Dock", PlacementScope("Weekend reading"), isRemove = true, onClick = {}),
                    PlacementActionUi("p_folder", Icons.Outlined.Folder, "Add to folder", scope = null, isRemove = false, onClick = {}),
                ),
            )
        }
    }
}
