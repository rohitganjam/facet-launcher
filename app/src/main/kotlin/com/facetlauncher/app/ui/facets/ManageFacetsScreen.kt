package com.facetlauncher.app.ui.facets

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.facetlauncher.app.R
import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.ui.components.BackButton
import com.facetlauncher.app.ui.components.ConfirmDialog
import com.facetlauncher.app.ui.components.ReorderRowDefaults
import com.facetlauncher.app.ui.components.StickyHeaderLayout
import com.facetlauncher.app.ui.components.ThemedDropdownMenu
import com.facetlauncher.app.ui.components.ThemedDropdownMenuItem
import com.facetlauncher.app.ui.components.rememberDragReorderState
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.Faint
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.Surface
import com.facetlauncher.app.ui.theme.SurfaceContainer

private val REORDER_ROW_HEIGHT = 64.dp
private val REORDER_ROW_SPACING = 10.dp

/**
 * Settings → Facets (`3b`): a normal settings screen — [StickyHeaderLayout] header + a
 * drag-to-reorder list of facets with add / per-facet-settings / delete. Reached both from
 * Settings and from the Switch Facets carousel's "Reorder" button.
 */
@Composable
fun ManageFacetsScreen(
    onBack: () -> Unit,
    onEditFacet: (facetId: Long) -> Unit,
    onFacetApply: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ManageFacetsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ManageFacetsContent(
        uiState = uiState,
        onBack = onBack,
        onReorder = viewModel::reorderFacets,
        onEditFacet = onEditFacet,
        onApplyFacet = { facetId -> viewModel.applyFacet(facetId); onFacetApply() },
        onDeleteFacet = viewModel::deleteFacet,
        onAddFacet = viewModel::addFacet,
        modifier = modifier,
    )
}

@Composable
private fun ManageFacetsContent(
    uiState: ManageFacetsUiState,
    onBack: () -> Unit,
    onReorder: (List<FacetEntity>) -> Unit,
    onEditFacet: (Long) -> Unit,
    onApplyFacet: (Long) -> Unit,
    onDeleteFacet: (FacetEntity) -> Unit,
    onAddFacet: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var deletingFacet by remember { mutableStateOf<FacetEntity?>(null) }
    // remember(facets): a committed reorder re-emits the list from Room; re-seeding the working
    // copy on identity keeps the drag state from fighting the fresh list (same pattern the
    // carousel used).
    var workingList by remember(uiState.facets) { mutableStateOf(uiState.facets) }

    StickyHeaderLayout(
        modifier = modifier,
        header = { ManageFacetsHeader(onBack = onBack) },
        content = { headerHeight ->
            FacetReorderList(
                facets = workingList,
                activeFacetId = uiState.activeFacetId,
                canAddFacet = uiState.canAddFacet,
                canDeleteFacet = uiState.canDeleteFacet,
                onOrderChange = { workingList = it },
                onCommit = { onReorder(workingList) },
                onEditFacet = onEditFacet,
                onApplyFacet = onApplyFacet,
                onDeleteRequest = { deletingFacet = it },
                onAddFacet = onAddFacet,
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = headerHeight + 12.dp, bottom = 24.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .background(SurfaceContainer)
                    .testTag("manage_facets_screen")
                    .windowInsetsPadding(WindowInsets.systemBars),
            )
        },
    )

    deletingFacet?.let { facet ->
        ConfirmDialog(
            title = stringResource(R.string.facet_carousel_delete_title, facet.name),
            message = stringResource(R.string.manage_facets_delete_message),
            confirmLabel = stringResource(R.string.action_delete),
            onConfirm = { onDeleteFacet(facet); deletingFacet = null },
            onDismiss = { deletingFacet = null },
        )
    }
}

@Composable
private fun ManageFacetsHeader(onBack: () -> Unit, modifier: Modifier = Modifier) {
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
        Text(text = stringResource(R.string.manage_facets_header_title), style = MaterialTheme.typography.headlineSmall, color = Ink)
    }
}

/**
 * A plain vertical list (never scrolls in practice — capped at
 * [com.facetlauncher.app.data.FacetRepository.MAX_FACETS] rows) of drag-handle + name +
 * overflow rows, with a non-reorderable "Add facet" row pinned at the bottom. Only the handle
 * on the left has a drag gesture attached, so there's no long-press/axis-conflict to arbitrate.
 */
@Composable
private fun FacetReorderList(
    facets: List<FacetEntity>,
    activeFacetId: Long,
    canAddFacet: Boolean,
    canDeleteFacet: Boolean,
    onOrderChange: (List<FacetEntity>) -> Unit,
    onCommit: () -> Unit,
    onEditFacet: (Long) -> Unit,
    onApplyFacet: (Long) -> Unit,
    onDeleteRequest: (FacetEntity) -> Unit,
    onAddFacet: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val rowSlotHeightPx = with(LocalDensity.current) { (REORDER_ROW_HEIGHT + REORDER_ROW_SPACING).toPx() }
    val reorderState = rememberDragReorderState(
        items = facets,
        key = { it.id },
        axis = Orientation.Vertical,
        slotSizePx = rowSlotHeightPx,
        onOrderChange = onOrderChange,
        onDragCommit = { onCommit() },
    )

    LazyColumn(
        modifier = modifier.testTag("facet_reorder_list"),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(REORDER_ROW_SPACING),
    ) {
        item(key = "reorder_hint") {
            Text(
                text = stringResource(R.string.manage_facets_drag_hint),
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
                modifier = Modifier.padding(bottom = 4.dp).testTag("facet_reorder_hint"),
            )
        }
        items(facets, key = { it.id }) { facet ->
            val isDragged = reorderState.isDragging(facet)
            FacetReorderRow(
                facet = facet,
                isActive = facet.id == activeFacetId,
                canDelete = canDeleteFacet,
                onEditFacetClick = { onEditFacet(facet.id) },
                onApplyFacetClick = { onApplyFacet(facet.id) },
                onDeleteClick = { onDeleteRequest(facet) },
                dragHandleModifier = reorderState.dragModifier(facet),
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (isDragged) Modifier else Modifier.animateItem())
                    .graphicsLayer {
                        translationY = if (isDragged) reorderState.dragOffset else 0f
                        scaleX = if (isDragged) ReorderRowDefaults.DRAG_SCALE else 1f
                        scaleY = if (isDragged) ReorderRowDefaults.DRAG_SCALE else 1f
                        shadowElevation = if (isDragged) ReorderRowDefaults.DRAG_ELEVATION.toPx() else 0f
                        shape = RoundedCornerShape(12.dp)
                        clip = false
                    }
                    .zIndex(if (isDragged) 1f else 0f),
            )
        }
        item(key = "add_facet_row") {
            AddFacetRow(
                enabled = canAddFacet,
                onClick = onAddFacet,
                modifier = Modifier.fillMaxWidth().testTag("facet_reorder_add_row"),
            )
        }
    }
}

@Composable
private fun FacetReorderRow(
    facet: FacetEntity,
    isActive: Boolean,
    canDelete: Boolean,
    onEditFacetClick: () -> Unit,
    onApplyFacetClick: () -> Unit,
    onDeleteClick: () -> Unit,
    dragHandleModifier: Modifier,
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .height(REORDER_ROW_HEIGHT)
            // M3's Card default shape — see CLAUDE.md's Material 3 shape section.
            .clip(MaterialTheme.shapes.medium)
            .background(Surface)
            .padding(horizontal = 12.dp)
            .testTag("facet_reorder_row_${facet.id}"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Default.DragHandle,
            contentDescription = stringResource(R.string.drag_to_reorder_content_description),
            tint = Faint,
            modifier = Modifier
                .padding(end = 12.dp)
                .testTag("facet_reorder_handle_${facet.id}")
                .then(dragHandleModifier),
        )
        Text(text = facet.name, style = MaterialTheme.typography.bodyLarge, color = Ink, modifier = Modifier.weight(1f))
        Box {
            IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier.testTag("facet_reorder_menu_${facet.id}"),
            ) {
                Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.manage_facets_options), tint = Muted)
            }
            ThemedDropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                ThemedDropdownMenuItem(
                    label = stringResource(R.string.facet_carousel_facet_settings),
                    onClick = { menuExpanded = false; onEditFacetClick() },
                )
                ThemedDropdownMenuItem(
                    label = stringResource(R.string.manage_facets_apply_facet),
                    enabled = !isActive,
                    onClick = { menuExpanded = false; onApplyFacetClick() },
                )
                ThemedDropdownMenuItem(
                    label = stringResource(R.string.action_delete),
                    enabled = canDelete,
                    destructive = true,
                    onClick = { menuExpanded = false; onDeleteClick() },
                )
            }
        }
    }
}

@Composable
private fun AddFacetRow(enabled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    if (!enabled) return
    Row(
        modifier = modifier
            .height(REORDER_ROW_HEIGHT)
            // M3's Card default shape — see CLAUDE.md's Material 3 shape section.
            .clip(MaterialTheme.shapes.medium)
            .background(Surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.Default.Add, contentDescription = null, tint = Accent)
        Text(text = stringResource(R.string.facet_carousel_add_facet), style = MaterialTheme.typography.bodyLarge, color = Accent, modifier = Modifier.padding(start = 8.dp))
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ManageFacetsScreenPreview() {
    FacetLauncherTheme {
        ManageFacetsContent(
            uiState = ManageFacetsUiState(
                facets = listOf(
                    FacetEntity(id = 1, name = "Personal", position = 0),
                    FacetEntity(id = 2, name = "Work", position = 1),
                ),
                activeFacetId = 1,
            ),
            onBack = {},
            onReorder = {},
            onEditFacet = {},
            onApplyFacet = {},
            onDeleteFacet = {},
            onAddFacet = {},
        )
    }
}
