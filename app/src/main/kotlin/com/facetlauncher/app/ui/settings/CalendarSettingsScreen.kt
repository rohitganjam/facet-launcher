package com.facetlauncher.app.ui.settings

import android.Manifest
import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.facetlauncher.app.data.model.CalendarInfo
import com.facetlauncher.app.ui.components.BackButton
import com.facetlauncher.app.ui.components.InheritOverrideCard
import com.facetlauncher.app.ui.components.dashedBorder
import com.facetlauncher.app.ui.components.SettingsCard
import com.facetlauncher.app.ui.components.StickyHeaderLayout
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.AccentSwatch
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.Surface
import com.facetlauncher.app.ui.theme.SurfaceContainer
import com.facetlauncher.app.ui.theme.resolvedColor

/**
 * "Calendars to display" (`4l`) — calendar *selection* only (which calendars, all-day events);
 * font/color/weight moved to `ClockStyleGalleryScreen` (see `CalendarSettingsViewModel`'s own doc
 * comment). Reached from Settings' "Calendars to display" row (no facet scope) or a facet's
 * "Calendars to display" row (scoped, with its own inherit/override header per `3f`'s pattern).
 * Real `READ_CALENDAR` runtime request + a live `CalendarContract` query back the "Calendars to
 * display" card once granted.
 */
@Composable
fun CalendarSettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CalendarSettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // READ_CALENDAR has no grant-change callback — re-check whenever the user returns to this
    // screen (e.g. from the system permission dialog, or having granted it via App info).
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshCalendarAccess()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val requestPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        viewModel.refreshCalendarAccess()
    }

    CalendarSettingsContent(
        uiState = uiState,
        onBack = onBack,
        onShowAllDayEventsChanged = viewModel::setShowAllDayEvents,
        onOverridingChanged = viewModel::setOverriding,
        onTurnOnClick = { requestPermission.launch(Manifest.permission.READ_CALENDAR) },
        onCalendarToggled = viewModel::setCalendarSelected,
        onSelectAllCalendarsChanged = viewModel::setAllCalendarsSelected,
        modifier = modifier,
    )
}

@Composable
private fun CalendarSettingsContent(
    uiState: CalendarSettingsUiState,
    onBack: () -> Unit,
    onShowAllDayEventsChanged: (Boolean) -> Unit,
    onOverridingChanged: (Boolean) -> Unit,
    onTurnOnClick: () -> Unit,
    onCalendarToggled: (String, Boolean) -> Unit,
    onSelectAllCalendarsChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Every control below (all-day events, calendar selection) is read-only under Inherit and live under Override.
    val controlsEnabled = !uiState.isFacetScoped || uiState.isOverriding

    StickyHeaderLayout(
        modifier = modifier,
        header = { CalendarSettingsHeader(onBack = onBack) },
        content = { headerHeight ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SurfaceContainer)
                    .testTag("calendar_settings_screen")
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .padding(horizontal = 24.dp),
                contentPadding = PaddingValues(top = headerHeight),
            ) {
                if (uiState.isFacetScoped) {
                    item {
                        InheritOverrideCard(
                            overriding = uiState.isOverriding,
                            onOverridingChanged = onOverridingChanged,
                            testTagPrefix = "calendar",
                            inheritSubtitle = "Follows the launcher-wide Calendar setting",
                        )
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }

                item {
                    SettingsCard {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 13.dp)
                                .alpha(if (controlsEnabled) 1f else 0.4f),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(text = "Show all-day events", style = MaterialTheme.typography.bodyLarge, color = Ink)
                            Switch(
                                checked = uiState.effectiveShowAllDayEvents,
                                onCheckedChange = onShowAllDayEventsChanged,
                                enabled = controlsEnabled,
                                modifier = Modifier.testTag("show_all_day_events_toggle"),
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }

                item {
                    SettingsCard {
                        Column(modifier = Modifier.padding(vertical = 13.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(text = "Calendars to display", style = MaterialTheme.typography.bodyLarge, color = Ink)
                                // Selects/deselects every calendar in one tap instead of one-by-one — real
                                // pain with a lot of calendars (see chat history). Indeterminate when only
                                // some are selected, matching the standard "select all" checkbox convention.
                                if (uiState.isCalendarAccessGranted && uiState.calendars.isNotEmpty()) {
                                    val selectedCount = uiState.calendars.count { uiState.isCalendarSelected(it.id) }
                                    val allState = when (selectedCount) {
                                        uiState.calendars.size -> ToggleableState.On
                                        0 -> ToggleableState.Off
                                        else -> ToggleableState.Indeterminate
                                    }
                                    TriStateCheckbox(
                                        state = allState,
                                        onClick = { onSelectAllCalendarsChanged(allState != ToggleableState.On) },
                                        modifier = Modifier.testTag("calendar_select_all_checkbox"),
                                    )
                                }
                            }
                            if (uiState.isCalendarAccessGranted) {
                                if (uiState.calendars.isEmpty()) {
                                    Text(text = "No calendars found on this device.", style = MaterialTheme.typography.bodyMedium, color = Muted)
                                } else {
                                    uiState.calendars.forEach { calendar ->
                                        CalendarPickerRow(
                                            calendar = calendar,
                                            swatch = uiState.calendarColors[calendar.id]?.let { runCatching { AccentSwatch.valueOf(it) }.getOrNull() },
                                            checked = uiState.isCalendarSelected(calendar.id),
                                            onToggle = { onCalendarToggled(calendar.id, !uiState.isCalendarSelected(calendar.id)) },
                                        )
                                    }
                                }
                            } else {
                                PermissionDeniedStrip(
                                    message = "Calendar access off — events hidden.",
                                    actionLabel = "Turn on",
                                    testTag = "calendar_permission_strip",
                                    onClick = onTurnOnClick,
                                )
                            }
                        }
                    }
                }

            }
        },
    )
}

@Composable
private fun CalendarSettingsHeader(onBack: () -> Unit, modifier: Modifier = Modifier) {
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
        Text(text = "Calendar settings", style = MaterialTheme.typography.headlineSmall, color = Ink)
    }
}

@Composable
private fun CalendarPickerRow(calendar: CalendarInfo, swatch: AccentSwatch?, checked: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (swatch != null) {
            val swatchLabel = stringResource(swatch.labelRes)
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(swatch.resolvedColor())
                    .semantics { contentDescription = swatchLabel },
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = calendar.displayName, style = MaterialTheme.typography.bodyLarge, color = Ink)
            Text(text = calendar.accountName, style = MaterialTheme.typography.bodyMedium, color = Muted)
        }
        Checkbox(
            checked = checked,
            onCheckedChange = { onToggle() },
            modifier = Modifier.testTag("calendar_picker_row_${calendar.id}"),
        )
    }
}

/** README `4p`'s shared permission-denied/empty-state pattern — reused verbatim styling, with a real tap target. */
@Composable
private fun PermissionDeniedStrip(message: String, actionLabel: String, testTag: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag)
            .dashedBorder(color = Ink.copy(alpha = 0.16f), cornerRadius = 10.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = message, style = MaterialTheme.typography.bodyMedium, color = Muted, modifier = Modifier.weight(1f))
        Text(text = actionLabel, style = MaterialTheme.typography.bodyMedium, color = Accent)
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CalendarSettingsScreenPreview() {
    FacetLauncherTheme {
        CalendarSettingsContent(
            uiState = CalendarSettingsUiState(),
            onBack = {},
            onShowAllDayEventsChanged = {},
            onOverridingChanged = {},
            onTurnOnClick = {},
            onCalendarToggled = { _, _ -> },
            onSelectAllCalendarsChanged = {},
        )
    }
}
