package com.facetlauncher.app.ui.hub.picker

import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.facetlauncher.app.data.model.WidgetProviderOption
import com.facetlauncher.app.ui.components.BackButton
import com.facetlauncher.app.ui.components.StickyHeaderLayout
import com.facetlauncher.app.ui.theme.ErrorColor
import com.facetlauncher.app.ui.theme.Hairline
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.Surface
import kotlinx.coroutines.delay

/**
 * Add-widget picker (README `4c`) — grouped by owning app, each option showing its declared
 * grid size. Selecting one kicks off [HubWidgetPickerViewModel]'s allocate/bind/configure/place
 * flow; [WidgetProviderGroup]s stay driven by the ViewModel's own query filtering.
 */
@Composable
fun HubWidgetPickerScreen(
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HubWidgetPickerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var failureMessage by remember { mutableStateOf<String?>(null) }

    val configureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        viewModel.onConfigureResult(result.resultCode == android.app.Activity.RESULT_OK)
    }
    val bindPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        viewModel.onBindResult(result.resultCode == android.app.Activity.RESULT_OK)
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is HubAddWidgetEvent.LaunchBindPermission -> bindPermissionLauncher.launch(event.intent)
                is HubAddWidgetEvent.LaunchConfigure -> configureLauncher.launch(IntentSenderRequest.Builder(event.intentSender).build())
                HubAddWidgetEvent.WidgetAdded -> onDone()
                is HubAddWidgetEvent.AddFailed -> {
                    failureMessage = when (event.reason) {
                        AddFailureReason.HUB_FULL -> "Full — remove a widget first"
                        AddFailureReason.SETUP_CANCELLED -> "Setup wasn't finished, so that widget wasn't added"
                    }
                }
            }
        }
    }

    LaunchedEffect(failureMessage) {
        if (failureMessage != null) {
            delay(3000)
            failureMessage = null
        }
    }

    HubWidgetPickerContent(
        uiState = uiState,
        failureMessage = failureMessage,
        onQueryChanged = viewModel::onQueryChanged,
        onProviderSelected = viewModel::onProviderSelected,
        onBack = onDone,
        modifier = modifier,
    )
}

@Composable
internal fun HubWidgetPickerContent(
    uiState: HubWidgetPickerUiState,
    onQueryChanged: (String) -> Unit,
    onProviderSelected: (WidgetProviderOption) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    failureMessage: String? = null,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Surface)
            .testTag("hub_widget_picker_screen"),
    ) {
        val tileWidth = widgetTileWidth(maxWidth - WIDGET_PICKER_HORIZONTAL_PADDING * 2)
        StickyHeaderLayout(
            modifier = Modifier.fillMaxSize(),
            header = { HubWidgetPickerHeader(remaining = uiState.remaining, onBack = onBack) },
            content = { headerHeight ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.systemBars)
                        .padding(horizontal = WIDGET_PICKER_HORIZONTAL_PADDING),
                    contentPadding = PaddingValues(top = headerHeight),
                ) {
                    item {
                        OutlinedTextField(
                            value = uiState.query,
                            onValueChange = onQueryChanged,
                            placeholder = { Text("Search widgets") },
                            modifier = Modifier.fillMaxWidth().testTag("hub_widget_picker_search").padding(bottom = 12.dp),
                            singleLine = true,
                        )
                    }
                    item {
                        AnimatedVisibility(visible = failureMessage != null, enter = fadeIn(), exit = fadeOut()) {
                            Text(
                                text = failureMessage.orEmpty(),
                                style = MaterialTheme.typography.labelMedium,
                                color = ErrorColor,
                                modifier = Modifier.fillMaxWidth().testTag("hub_widget_picker_error").padding(bottom = 12.dp),
                            )
                        }
                    }
                    items(uiState.groups, key = { it.appLabel }) { group ->
                        WidgetProviderGroupRow(group = group, tileWidth = tileWidth, onProviderSelected = onProviderSelected)
                    }
                    if (uiState.groups.isEmpty()) {
                        item {
                            Text(
                                text = "No widgets found",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Muted,
                                modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            },
        )
    }
}

private val WIDGET_PICKER_HORIZONTAL_PADDING = 24.dp
private val WIDGET_TILE_TARGET_WIDTH = 96.dp
private val WIDGET_TILE_SPACING = 8.dp

/**
 * Tile width sized to fill [availableWidth] with as many whole [WIDGET_TILE_TARGET_WIDTH]-ish
 * tiles as fit, rather than a fixed dp value — so the grid uses the screen's actual width
 * instead of assuming a phone-sized column count.
 */
@Composable
private fun widgetTileWidth(availableWidth: Dp): Dp {
    val columns = ((availableWidth.value + WIDGET_TILE_SPACING.value) / (WIDGET_TILE_TARGET_WIDTH.value + WIDGET_TILE_SPACING.value))
        .toInt()
        .coerceAtLeast(1)
    return ((availableWidth.value - WIDGET_TILE_SPACING.value * (columns - 1)) / columns).dp
}

@Composable
private fun HubWidgetPickerHeader(remaining: Int, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Surface)
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BackButton(onClick = onBack)
            Text(text = "Add widget", style = MaterialTheme.typography.headlineSmall, color = Ink)
        }
        Text(
            text = "$remaining left",
            style = MaterialTheme.typography.labelMedium,
            color = Muted,
            modifier = Modifier.testTag("hub_widget_picker_remaining"),
        )
    }
}

@Composable
private fun WidgetProviderGroupRow(
    group: WidgetProviderGroup,
    tileWidth: Dp,
    onProviderSelected: (WidgetProviderOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(top = 14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = group.appLabel, style = MaterialTheme.typography.bodyLarge, color = Ink)
            Text(
                text = if (group.options.size == 1) "1 widget" else "${group.options.size} widgets",
                style = MaterialTheme.typography.labelSmall,
                color = Muted,
            )
        }
        // FlowRow, not Row — a group with more options than fit on one line (Clock's 9, say)
        // must wrap onto further lines rather than squeezing every tile into whatever width is
        // left, which is what a plain Row does (see chat history — this collapsed the last
        // tile's own label into a near-zero-width column that wrapped one letter per line).
        FlowRow(
            modifier = Modifier.padding(top = 11.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(WIDGET_TILE_SPACING),
            verticalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            group.options.forEach { option ->
                WidgetProviderOptionTile(option = option, tileWidth = tileWidth, onClick = { onProviderSelected(option) })
            }
        }
    }
}

@Composable
private fun WidgetProviderOptionTile(option: WidgetProviderOption, tileWidth: Dp, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .width(tileWidth)
            .testTag("hub_widget_option_${option.provider.flattenToShortString()}")
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, Hairline, RoundedCornerShape(12.dp))
                .padding(4.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val previewIcon = option.previewIcon
            if (previewIcon != null) {
                Image(
                    bitmap = previewIcon.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Text(
                    text = "${option.columns}×${option.rows}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Muted,
                )
            }
        }
        Text(
            text = "${option.widgetLabel} ${option.columns}×${option.rows}",
            style = MaterialTheme.typography.labelSmall,
            color = Muted,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 5.dp),
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HubWidgetPickerScreenPreview() {
    FacetLauncherTheme {
        HubWidgetPickerContent(
            uiState = HubWidgetPickerUiState(
                groups = listOf(
                    WidgetProviderGroup(
                        appLabel = "Calendar",
                        options = listOf(
                            WidgetProviderOption(android.content.ComponentName("com.example.cal", ".Month"), "Calendar", "Month", 2, 2),
                            WidgetProviderOption(android.content.ComponentName("com.example.cal", ".Agenda"), "Calendar", "Agenda", 4, 2),
                        ),
                    ),
                ),
                remaining = 14,
            ),
            onQueryChanged = {},
            onProviderSelected = {},
            onBack = {},
        )
    }
}
