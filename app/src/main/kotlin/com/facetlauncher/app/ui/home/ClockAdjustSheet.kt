package com.facetlauncher.app.ui.home

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.OpenInFull
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.R
import com.facetlauncher.app.ui.components.CardDivider
import com.facetlauncher.app.ui.components.SettingsCard
import com.facetlauncher.app.ui.components.SettingsIconBadge
import com.facetlauncher.app.ui.components.SettingsIconBadgeInset
import com.facetlauncher.app.ui.settings.ClickableRow
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.SettingsSectionHue
import com.facetlauncher.app.ui.theme.SurfaceContainer

/**
 * The Home sheet, opened by a long-press on free space on Home or on a widget page (the clock and its calendar have their own,
 * [ClockStyleSheetContent]). Its rows do not depend on what the page is: adjust size and position (the only way into resize
 * mode), replace or remove the widget showing (or add one when there is none), the widget stack, then the facet's and the
 * launcher's settings. Replacing swaps the visible page in place and is never a Pro action.
 */
@Composable
fun ClockAdjustSheet(
    onAdjustClick: () -> Unit,
    onFacetSettingsClick: () -> Unit,
    onLauncherSettingsClick: () -> Unit,
    onReplaceWidgetClick: () -> Unit,
    onRemoveWidgetClick: () -> Unit,
    onAddWidgetClick: () -> Unit,
    onWidgetStackClick: () -> Unit,
    /** What the Widget stack row says is in the stack: "Clock", "3 pages", "Empty…". */
    stackSummary: String,
    modifier: Modifier = Modifier,
    /** Whether the stack has a page showing: replace and remove need one, and add is for when there is none. */
    hasPage: Boolean = true,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 16.dp)
            .testTag("clock_adjust_sheet"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SettingsCard(fullBleedRows = true) {
            AdjustRow(
                icon = Icons.Outlined.OpenInFull,
                hue = SettingsSectionHue.APPEARANCE,
                label = stringResource(R.string.clock_adjust_adjust_size_position),
                subtitle = stringResource(R.string.clock_adjust_size_position_subtitle),
                onClick = onAdjustClick,
                testTag = "clock_adjust_open",
            )
            PageRows(hasPage, onReplaceWidgetClick, onRemoveWidgetClick, onAddWidgetClick)
            CardDivider(startInset = SettingsIconBadgeInset)
            AdjustRow(
                icon = Icons.Outlined.Layers,
                hue = SettingsSectionHue.APPEARANCE,
                label = stringResource(R.string.clock_adjust_widget_stack),
                subtitle = stackSummary,
                onClick = onWidgetStackClick,
                testTag = "clock_adjust_widget_stack",
            )
        }
        SettingsRows(onFacetSettingsClick, onLauncherSettingsClick)
    }
}

/**
 * The clock's own sheet, opened by a long-press on the clock or on the events under it: its styles, which open full screen,
 * and its calendar settings.
 */
@Composable
fun ClockStyleSheetContent(
    onClockStylesClick: () -> Unit,
    onCalendarSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 16.dp)
            .testTag("clock_style_sheet"),
    ) {
        SettingsCard(fullBleedRows = true) {
            AdjustRow(
                icon = Icons.Outlined.Palette,
                hue = SettingsSectionHue.APPEARANCE,
                label = stringResource(R.string.clock_style_sheet_styles),
                subtitle = stringResource(R.string.clock_style_sheet_styles_subtitle),
                onClick = onClockStylesClick,
                testTag = "clock_style_sheet_styles",
            )
            CardDivider(startInset = SettingsIconBadgeInset)
            AdjustRow(
                icon = Icons.Outlined.CalendarMonth,
                hue = SettingsSectionHue.APPEARANCE,
                label = stringResource(R.string.clock_style_sheet_calendar),
                subtitle = stringResource(R.string.clock_style_sheet_calendar_subtitle),
                onClick = onCalendarSettingsClick,
                testTag = "clock_style_sheet_calendar",
            )
        }
    }
}

@Composable
private fun AdjustRow(
    icon: ImageVector,
    hue: SettingsSectionHue,
    label: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
) {
    ClickableRow(
        title = label,
        subtitle = subtitle,
        onClick = onClick,
        testTag = testTag,
        modifier = modifier,
        leadingIcon = { SettingsIconBadge(icon, hue) },
    )
}

@Preview(showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ClockAdjustSheetPreview() {
    FacetLauncherTheme {
        ClockAdjustSheet(
            onAdjustClick = {},
            onFacetSettingsClick = {},
            onLauncherSettingsClick = {},
            onReplaceWidgetClick = {},
            onRemoveWidgetClick = {},
            onAddWidgetClick = {},
            onWidgetStackClick = {},
            stackSummary = "Clock",
            modifier = Modifier.background(SurfaceContainer),
        )
    }
}

@Preview(name = "Empty stack", showBackground = true)
@Composable
private fun ClockAdjustSheetEmptyPreview() {
    FacetLauncherTheme {
        ClockAdjustSheet(
            onAdjustClick = {},
            onFacetSettingsClick = {},
            onLauncherSettingsClick = {},
            onReplaceWidgetClick = {},
            onRemoveWidgetClick = {},
            onAddWidgetClick = {},
            onWidgetStackClick = {},
            stackSummary = "Empty",
            hasPage = false,
            modifier = Modifier.background(SurfaceContainer),
        )
    }
}

@Preview(name = "Clock sheet", showBackground = true)
@Composable
private fun ClockStyleSheetPreview() {
    FacetLauncherTheme { ClockStyleSheetContent(onClockStylesClick = {}, onCalendarSettingsClick = {}, modifier = Modifier.background(SurfaceContainer)) }
}

@Composable
private fun SettingsRows(onFacetSettingsClick: () -> Unit, onLauncherSettingsClick: () -> Unit) {
    SettingsCard(fullBleedRows = true) {
        AdjustRow(
            icon = Icons.Outlined.Settings,
            hue = SettingsSectionHue.FACETS,
            label = stringResource(R.string.facet_carousel_facet_settings),
            subtitle = stringResource(R.string.clock_adjust_facet_settings_subtitle),
            onClick = onFacetSettingsClick,
            testTag = "clock_adjust_facet_settings",
        )
        CardDivider(startInset = SettingsIconBadgeInset)
        AdjustRow(
            icon = Icons.Outlined.Tune,
            hue = SettingsSectionHue.SYSTEM,
            label = stringResource(R.string.facet_carousel_launcher_settings),
            subtitle = stringResource(R.string.facet_carousel_launcher_settings_subtitle),
            onClick = onLauncherSettingsClick,
            testTag = "clock_adjust_launcher_settings",
        )
    }
}

/** Rows that act on the page showing, or add one when there is none. Called inside the first card. */
@Composable
private fun PageRows(
    hasPage: Boolean,
    onReplaceWidgetClick: () -> Unit,
    onRemoveWidgetClick: () -> Unit,
    onAddWidgetClick: () -> Unit,
) {
    if (hasPage) {
        CardDivider(startInset = SettingsIconBadgeInset)
        AdjustRow(
            icon = Icons.Outlined.Widgets,
            hue = SettingsSectionHue.APPEARANCE,
            label = stringResource(R.string.clock_adjust_replace_widget),
            subtitle = stringResource(R.string.clock_adjust_replace_widget_subtitle),
            onClick = onReplaceWidgetClick,
            testTag = "clock_adjust_replace_widget",
        )
        CardDivider(startInset = SettingsIconBadgeInset)
        AdjustRow(
            icon = Icons.Outlined.DeleteOutline,
            hue = SettingsSectionHue.APPEARANCE,
            label = stringResource(R.string.clock_adjust_remove_widget),
            subtitle = stringResource(R.string.clock_adjust_remove_widget_subtitle),
            onClick = onRemoveWidgetClick,
            testTag = "clock_adjust_remove_widget",
        )
    } else {
        CardDivider(startInset = SettingsIconBadgeInset)
        AdjustRow(
            icon = Icons.Outlined.AddCircleOutline,
            hue = SettingsSectionHue.APPEARANCE,
            label = stringResource(R.string.clock_adjust_add_widget),
            subtitle = stringResource(R.string.clock_adjust_add_widget_subtitle),
            onClick = onAddWidgetClick,
            testTag = "clock_adjust_add_widget",
        )
    }
}
