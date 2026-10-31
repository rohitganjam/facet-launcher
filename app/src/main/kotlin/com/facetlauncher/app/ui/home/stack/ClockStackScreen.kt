package com.facetlauncher.app.ui.home.stack

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.facetlauncher.app.data.model.ClockStackItemKind
import com.facetlauncher.app.data.model.ProReason
import com.facetlauncher.app.domain.AddClockStackItemResult
import com.facetlauncher.app.ui.components.BackButton
import com.facetlauncher.app.ui.components.CardDivider
import com.facetlauncher.app.ui.components.ProPill
import com.facetlauncher.app.ui.components.ReorderRowDefaults
import com.facetlauncher.app.ui.components.SettingsCard
import com.facetlauncher.app.ui.components.SettingsIconBadge
import com.facetlauncher.app.ui.components.SettingsIconBadgeInset
import com.facetlauncher.app.ui.components.StickyHeaderLayout
import com.facetlauncher.app.ui.components.TextActionButton
import com.facetlauncher.app.ui.components.ThemedModalBottomSheet
import com.facetlauncher.app.ui.components.rememberDragReorderState
import com.facetlauncher.app.ui.home.widget.ClockWidgetPickerScreen
import com.facetlauncher.app.ui.home.widget.ClockWidgetPickerTarget
import com.facetlauncher.app.ui.settings.ClickableRow
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Faint
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.SettingsSectionHue
import com.facetlauncher.app.ui.theme.Surface
import com.facetlauncher.app.ui.theme.SurfaceContainer
import kotlinx.coroutines.launch

private val STACK_ROW_HEIGHT = 64.dp
private val STACK_ROW_SPACING = 10.dp

/**
 * Settings-style screen for the active facet's clock stack (reached from Home's sheet): a drag-to-reorder list of the
 * clock and widgets with Configure and Remove on each row, an Add row, and the plan's limit. Add opens a small chooser
 * (the launcher clock, or a custom widget) while the clock is not in the stack, and goes straight to the widget picker
 * when it is. Replacing a page is on Home's sheet, not here.
 */
@Composable
fun ClockStackScreen(
    onBack: () -> Unit,
    onOpenFacetPro: (ProReason) -> Unit,
    onOpenAppearance: (facetId: Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ClockStackViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var showPicker by remember { mutableStateOf(false) }
    var showChooser by remember { mutableStateOf(false) }
    // A widget's own configure screen: nothing to read back, the host view redraws itself.
    val configureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {}

    Box(modifier = modifier.fillMaxSize()) {
        ClockStackContent(
            uiState = uiState,
            onBack = onBack,
            onReorder = viewModel::reorder,
            onRemove = viewModel::remove,
            onConfigure = { row ->
                val sender = row.appWidgetId?.let { viewModel.configureSender(it) }
                when {
                    row.kind == ClockStackItemKind.CLOCK -> onOpenAppearance(uiState.facetId)
                    sender != null -> configureLauncher.launch(IntentSenderRequest.Builder(sender).build())
                }
            },
            onAdd = {
                scope.launch {
                    when {
                        !viewModel.hasRoom() -> onOpenFacetPro(ProReason.WIDGET_STACK)
                        uiState.hasClock -> showPicker = true
                        else -> showChooser = true
                    }
                }
            },
        )
        if (showPicker) {
            BackHandler { showPicker = false }
            ClockWidgetPickerScreen(
                target = ClockWidgetPickerTarget.Add(uiState.facetId),
                onDone = { showPicker = false },
                modifier = Modifier.fillMaxSize().background(SurfaceContainer),
            )
        }
    }

    if (showChooser) {
        AddChooserSheet(
            onDismiss = { showChooser = false },
            onLauncherClock = {
                showChooser = false
                scope.launch { if (viewModel.addClock() == AddClockStackItemResult.LIMIT_REACHED) onOpenFacetPro(ProReason.WIDGET_STACK) }
            },
            onCustomWidget = {
                showChooser = false
                showPicker = true
            },
        )
    }
}

@Composable
private fun AddChooserSheet(onDismiss: () -> Unit, onLauncherClock: () -> Unit, onCustomWidget: () -> Unit) {
    ThemedModalBottomSheet(onDismissRequest = onDismiss, skipPartiallyExpanded = true, containerColor = SurfaceContainer) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 16.dp).testTag("clock_stack_chooser"),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = stringResource(R.string.clock_stack_chooser_title), style = MaterialTheme.typography.titleMedium, color = Ink)
            SettingsCard(fullBleedRows = true) {
                ClickableRow(
                    title = stringResource(R.string.clock_stack_chooser_clock),
                    subtitle = stringResource(R.string.clock_stack_chooser_clock_subtitle),
                    onClick = onLauncherClock,
                    testTag = "clock_stack_chooser_clock",
                    leadingIcon = { SettingsIconBadge(Icons.Outlined.AccessTime, SettingsSectionHue.APPEARANCE) },
                )
                CardDivider(startInset = SettingsIconBadgeInset)
                ClickableRow(
                    title = stringResource(R.string.clock_stack_chooser_widget),
                    subtitle = stringResource(R.string.clock_stack_chooser_widget_subtitle),
                    onClick = onCustomWidget,
                    testTag = "clock_stack_chooser_widget",
                    leadingIcon = { SettingsIconBadge(Icons.Outlined.Widgets, SettingsSectionHue.APPEARANCE) },
                )
            }
        }
    }
}

@Composable
internal fun ClockStackContent(
    uiState: ClockStackUiState,
    onBack: () -> Unit,
    onReorder: (List<Long>) -> Unit,
    onRemove: (Long) -> Unit,
    onConfigure: (ClockStackRow) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // A committed reorder re-emits the list from Room; re-seeding the working copy on identity keeps the drag state from
    // fighting the fresh list, the same pattern the facet list uses.
    var workingRows by remember(uiState.rows) { mutableStateOf(uiState.rows) }

    StickyHeaderLayout(
        modifier = modifier,
        header = { ClockStackHeader(rowCount = uiState.rows.size, limit = uiState.limit, onBack = onBack) },
        content = { headerHeight ->
            ClockStackList(
                uiState = uiState,
                rows = StackRows(workingRows),
                onOrderChange = { workingRows = it },
                onCommit = { onReorder(workingRows.map { it.id }) },
                onRemove = onRemove,
                onConfigure = onConfigure,
                onAdd = onAdd,
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = headerHeight + 12.dp, bottom = 24.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .background(SurfaceContainer)
                    .testTag("clock_stack_screen")
                    .windowInsetsPadding(WindowInsets.systemBars),
            )
        },
    )
}

@Composable
private fun ClockStackHeader(rowCount: Int, limit: Int, onBack: () -> Unit, modifier: Modifier = Modifier) {
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
        Text(text = stringResource(R.string.clock_stack_title), style = MaterialTheme.typography.headlineSmall, color = Ink, modifier = Modifier.weight(1f))
        Text(
            text = stringResource(R.string.clock_stack_counter, rowCount, limit),
            style = MaterialTheme.typography.bodyMedium,
            color = Muted,
            modifier = Modifier.testTag("clock_stack_counter"),
        )
    }
}

/** A `List` parameter is unstable to Compose, so [ClockStackList] takes its rows wrapped in this. */
@Immutable
private data class StackRows(val items: List<ClockStackRow>)

@Composable
private fun ClockStackList(
    uiState: ClockStackUiState,
    rows: StackRows,
    onOrderChange: (List<ClockStackRow>) -> Unit,
    onCommit: () -> Unit,
    onRemove: (Long) -> Unit,
    onConfigure: (ClockStackRow) -> Unit,
    onAdd: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val slotHeightPx = with(LocalDensity.current) { (STACK_ROW_HEIGHT + STACK_ROW_SPACING).toPx() }
    val reorderState = rememberDragReorderState(
        items = rows.items,
        key = { it.id },
        axis = Orientation.Vertical,
        slotSizePx = slotHeightPx,
        onOrderChange = onOrderChange,
        onDragCommit = { onCommit() },
    )
    LazyColumn(
        modifier = modifier.testTag("clock_stack_list"),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(STACK_ROW_SPACING),
    ) {
        item(key = "hint") {
            Text(
                text = stringResource(if (rows.items.isEmpty()) R.string.clock_stack_empty else R.string.clock_stack_drag_hint),
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
                modifier = Modifier.padding(bottom = 4.dp).testTag("clock_stack_hint"),
            )
        }
        items(rows.items, key = { it.id }) { row ->
            val isDragged = reorderState.isDragging(row)
            ClockStackRowItem(
                row = row,
                onConfigure = { onConfigure(row) },
                onRemove = { onRemove(row.id) },
                dragHandleModifier = reorderState.dragModifier(row),
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
        if (uiState.showAdd) {
            item(key = "add") { AddRow(showProPill = uiState.addNeedsPro, onClick = onAdd, modifier = Modifier.fillMaxWidth().testTag("clock_stack_add_row")) }
        }
        if (uiState.addNeedsPro && !uiState.isPro && rows.items.isNotEmpty()) {
            item(key = "free_hint") {
                Text(
                    text = stringResource(R.string.clock_stack_full_free_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                    modifier = Modifier.testTag("clock_stack_free_hint"),
                )
            }
        }
    }
}

@Composable
private fun ClockStackRowItem(
    row: ClockStackRow,
    onConfigure: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    dragHandleModifier: Modifier = Modifier,
) {
    val name = when {
        row.kind == ClockStackItemKind.CLOCK -> stringResource(R.string.clock_stack_row_clock)
        else -> row.providerName ?: stringResource(R.string.clock_stack_row_unavailable)
    }
    Row(
        modifier = modifier
            .height(STACK_ROW_HEIGHT)
            // M3's Card default shape — see CLAUDE.md's Material 3 shape section.
            .clip(MaterialTheme.shapes.medium)
            .background(Surface)
            .padding(horizontal = 12.dp)
            .testTag("clock_stack_row_${row.id}"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Default.DragHandle,
            contentDescription = stringResource(R.string.drag_to_reorder_content_description),
            tint = Faint,
            modifier = Modifier.padding(end = 12.dp).testTag("clock_stack_handle_${row.id}").then(dragHandleModifier),
        )
        Text(text = name, style = MaterialTheme.typography.bodyLarge, color = Ink, modifier = Modifier.weight(1f))
        if (row.canConfigure) {
            TextActionButton(text = stringResource(R.string.clock_stack_configure), onClick = onConfigure, modifier = Modifier.testTag("clock_stack_configure_${row.id}"))
        }
        IconButton(onClick = onRemove, modifier = Modifier.testTag("clock_stack_remove_${row.id}")) {
            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.clock_stack_remove, name), tint = Muted)
        }
    }
}

@Composable
private fun AddRow(showProPill: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .height(STACK_ROW_HEIGHT)
            // M3's Card default shape — see CLAUDE.md's Material 3 shape section.
            .clip(MaterialTheme.shapes.medium)
            .background(Surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.Default.Add, contentDescription = null, tint = Accent)
        Text(text = stringResource(R.string.clock_stack_add), style = MaterialTheme.typography.bodyLarge, color = Accent, modifier = Modifier.padding(start = 8.dp))
        if (showProPill) ProPill(modifier = Modifier.padding(start = 8.dp))
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ClockStackScreenPreview() {
    FacetLauncherTheme {
        ClockStackContent(
            uiState = ClockStackUiState(
                facetId = 1,
                rows = listOf(
                    ClockStackRow(id = 1, kind = ClockStackItemKind.CLOCK, appWidgetId = null, providerName = null, canConfigure = true),
                    ClockStackRow(id = 2, kind = ClockStackItemKind.WIDGET, appWidgetId = 7, providerName = "Gmail", canConfigure = true),
                    ClockStackRow(id = 3, kind = ClockStackItemKind.WIDGET, appWidgetId = 8, providerName = "Calendar", canConfigure = false),
                ),
            ),
            onBack = {},
            onReorder = {},
            onRemove = {},
            onConfigure = {},
            onAdd = {},
        )
    }
}
