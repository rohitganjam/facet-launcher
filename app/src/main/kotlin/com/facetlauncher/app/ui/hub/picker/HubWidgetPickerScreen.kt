package com.facetlauncher.app.ui.hub.picker

import com.facetlauncher.app.ui.nativewidget.ALL_NATIVE_WIDGETS
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.NativeWidgetRef
import com.facetlauncher.app.data.model.AppProfile
import com.facetlauncher.app.data.model.WidgetProviderOption
import com.facetlauncher.app.ui.components.BackButton
import com.facetlauncher.app.ui.components.StickyHeaderLayout
import com.facetlauncher.app.ui.components.OtherScopeBadge
import com.facetlauncher.app.ui.components.ProfileTab
import com.facetlauncher.app.ui.components.ProfileTabRow
import com.facetlauncher.app.ui.components.SwipeTabs
import com.facetlauncher.app.data.model.NativeWidgetType
import com.facetlauncher.app.ui.nativewidget.NativeWidgetPickerSection
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
    /** Called, before [onDone], with a placed widget that has settings to choose first. */
    onConfigureNewWidget: (NativeWidgetRef) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val configureNewWidget by rememberUpdatedState(onConfigureNewWidget)
    var failureMessage by remember { mutableStateOf<String?>(null) }

    val configureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        viewModel.onConfigureResult(result.resultCode == android.app.Activity.RESULT_OK)
    }
    val bindPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        viewModel.onBindResult(result.resultCode == android.app.Activity.RESULT_OK)
    }

    val hubFullMessage = stringResource(R.string.hub_widget_picker_full)
    val setupCancelledMessage = stringResource(R.string.hub_widget_picker_setup_cancelled)
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is HubAddWidgetEvent.LaunchBindPermission -> bindPermissionLauncher.launch(event.intent)
                is HubAddWidgetEvent.LaunchConfigure -> configureLauncher.launch(event.intent)
                HubAddWidgetEvent.WidgetAdded -> onDone()
                is HubAddWidgetEvent.NativeWidgetAdded -> {
                    configureNewWidget(event.ref)
                    onDone()
                }
                is HubAddWidgetEvent.AddFailed -> {
                    failureMessage = when (event.reason) {
                        AddFailureReason.HUB_FULL -> hubFullMessage
                        AddFailureReason.SETUP_CANCELLED -> setupCancelledMessage
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
        onQueryChange = viewModel::onQueryChange,
        onTabChange = viewModel::onTabChange,
        onProviderSelect = viewModel::onProviderSelect,
        onBack = onDone,
        modifier = modifier,
        nativeWidgets = ALL_NATIVE_WIDGETS,
        onNativeWidgetSelect = viewModel::onNativeWidgetSelect,
    )
}

@Composable
internal fun HubWidgetPickerContent(
    uiState: HubWidgetPickerUiState,
    onQueryChange: (String) -> Unit,
    onProviderSelect: (WidgetProviderOption) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onTabChange: (WidgetPickerTab) -> Unit = {},
    failureMessage: String? = null,
    /** `null` (the default) keeps the Hub's own "Add widget" title — PRD F15's clock widget picker overrides it. */
    title: String? = null,
    /** The widgets Facet draws itself, offered first. */
    nativeWidgets: ImmutableList<NativeWidgetType> = persistentListOf(),
    onNativeWidgetSelect: (NativeWidgetType) -> Unit = {},
    /** The Home pickers offer the clock too, while the facet has none. */
    showClockOption: Boolean = false,
    onClockSelect: () -> Unit = {},
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
            header = { HubWidgetPickerHeader(remaining = uiState.remaining, onBack = onBack, title = title) },
            content = { headerHeight ->
                SwipeTabs(
                    keys = uiState.tabs.map { it.name },
                    selected = uiState.tab.name,
                    onSelect = { onTabChange(WidgetPickerTab.valueOf(it)) },
                    modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.systemBars),
                ) { key ->
                    val tab = WidgetPickerTab.valueOf(key)
                    WidgetPickerList(
                        uiState = uiState,
                        tab = tab,
                        headerHeight = headerHeight,
                        tileWidth = tileWidth,
                        failureMessage = failureMessage,
                        native = NativeWidgetOffer(nativeWidgets, showClockOption, onNativeWidgetSelect, onClockSelect),
                        onTabChange = onTabChange,
                        onQueryChange = onQueryChange,
                        onProviderSelect = onProviderSelect,
                    )
                }
            },
        )
    }
}

/** The widgets Facet draws itself, offered first on the personal tab. */
@Immutable
private class NativeWidgetOffer(
    val types: ImmutableList<NativeWidgetType>,
    val showClock: Boolean,
    val onSelect: (NativeWidgetType) -> Unit,
    val onClockSelect: () -> Unit,
)

/** One tab's list: the tab row, search, any failure, the native widgets (personal tab) and the provider groups. */
@Composable
private fun WidgetPickerList(
    uiState: HubWidgetPickerUiState,
    tab: WidgetPickerTab,
    headerHeight: Dp,
    tileWidth: Dp,
    failureMessage: String?,
    native: NativeWidgetOffer,
    onTabChange: (WidgetPickerTab) -> Unit,
    onQueryChange: (String) -> Unit,
    onProviderSelect: (WidgetProviderOption) -> Unit,
) {
    val groups = if (tab == uiState.tab) uiState.groups else uiState.groupsByTab[tab].orEmpty()
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = WIDGET_PICKER_HORIZONTAL_PADDING),
        contentPadding = PaddingValues(top = headerHeight),
    ) {
        if (uiState.tabs.isNotEmpty()) {
            item { WidgetPickerTabRow(uiState = uiState, onTabChange = onTabChange) }
        }
        item {
            OutlinedTextField(
                value = uiState.query,
                onValueChange = onQueryChange,
                placeholder = { Text(stringResource(R.string.hub_widget_picker_search)) },
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
        // The widgets Facet draws itself belong to the personal side.
        if (uiState.query.isBlank() && tab == WidgetPickerTab.PERSONAL) {
            item { NativeWidgetPickerSection(types = native.types, tileWidth = tileWidth, onSelect = native.onSelect, showClock = native.showClock, onClockSelect = native.onClockSelect) }
        }
        items(groups, key = { it.appLabel }) { group ->
            WidgetProviderGroupRow(group = group, tileWidth = tileWidth, onProviderSelect = onProviderSelect)
        }
        if (groups.isEmpty()) item { NoWidgetsFound() }
    }
}

/** Personal/Work tabs above the list, shown only when a Work Profile has widgets: the same switcher as the app drawer's. */
@Composable
private fun WidgetPickerTabRow(uiState: HubWidgetPickerUiState, onTabChange: (WidgetPickerTab) -> Unit) {
    val personal = stringResource(R.string.drawer_tab_personal)
    val work = stringResource(R.string.facet_scope_work)
    val items = remember(uiState.tabs, personal, work) {
        uiState.tabs.map { ProfileTab(it.name, if (it == WidgetPickerTab.WORK) work else personal) }.toImmutableList()
    }
    ProfileTabRow(
        tabs = items,
        selectedKey = uiState.tab.name,
        onSelect = { key -> onTabChange(WidgetPickerTab.valueOf(key)) },
        tagPrefix = "hub_widget_picker_tab_",
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
    )
}

@Composable
private fun NoWidgetsFound() {
    Text(
        text = stringResource(R.string.hub_widget_picker_none_found),
        style = MaterialTheme.typography.bodyMedium,
        color = Muted,
        modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
        textAlign = TextAlign.Center,
    )
}

private val WIDGET_PICKER_HORIZONTAL_PADDING = 24.dp
internal val WIDGET_TILE_SPACING = 12.dp
internal val WIDGET_TILE_HEIGHT = 120.dp

/** Two tiles per row, so each preview is large enough to read. */
internal fun widgetTileWidth(availableWidth: Dp): Dp = (availableWidth - WIDGET_TILE_SPACING) / 2

@Composable
private fun HubWidgetPickerHeader(remaining: Int?, onBack: () -> Unit, modifier: Modifier = Modifier, title: String? = null) {
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
            Text(text = title ?: stringResource(R.string.hub_widget_picker_title), style = MaterialTheme.typography.headlineSmall, color = Ink)
        }
        if (remaining != null) {
            Text(
                text = stringResource(R.string.hub_widget_picker_remaining, remaining),
                style = MaterialTheme.typography.labelMedium,
                color = Muted,
                modifier = Modifier.testTag("hub_widget_picker_remaining"),
            )
        }
    }
}

@Composable
private fun WidgetProviderGroupRow(
    group: WidgetProviderGroup,
    tileWidth: Dp,
    onProviderSelect: (WidgetProviderOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(top = 14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = group.appLabel, style = MaterialTheme.typography.bodyLarge, color = Ink)
            Text(
                text = pluralStringResource(R.plurals.hub_widget_picker_option_count, group.options.size, group.options.size),
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
                WidgetProviderOptionTile(option = option, tileWidth = tileWidth, onClick = { onProviderSelect(option) })
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(WIDGET_TILE_HEIGHT)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, Hairline, RoundedCornerShape(12.dp)),
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(4.dp),
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
                        text = stringResource(R.string.hub_widget_dimensions, option.columns, option.rows),
                        style = MaterialTheme.typography.labelSmall,
                        color = Muted,
                    )
                }
            }
            // Work widgets have their own tab, so only another profile's get a badge.
            if (option.profile == AppProfile.OTHER) {
                OtherScopeBadge(modifier = Modifier.align(Alignment.BottomStart).padding(4.dp).testTag("hub_widget_option_other_badge"))
            }
        }
        Text(
            text = stringResource(R.string.hub_widget_label_with_dimensions, option.widgetLabel, option.columns, option.rows),
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
            onQueryChange = {},
            onProviderSelect = {},
            onBack = {},
        )
    }
}
