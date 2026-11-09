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
import androidx.compose.material.icons.outlined.SwapHoriz
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
import com.facetlauncher.app.data.model.NativeWidgetType
import com.facetlauncher.app.ui.components.CardDivider
import com.facetlauncher.app.ui.components.SettingsCard
import com.facetlauncher.app.ui.components.SettingsIconBadge
import com.facetlauncher.app.ui.components.SettingsIconBadgeInset
import com.facetlauncher.app.ui.nativewidget.hasSettings
import com.facetlauncher.app.ui.nativewidget.labelRes
import com.facetlauncher.app.ui.settings.ClickableRow
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.SettingsSectionHue
import com.facetlauncher.app.ui.theme.SurfaceContainer

/**
 * The Home sheet, opened by a long-press on free space: the facet's and the launcher's settings. With no widget in the stack there is
 * nothing to long-press, so the stack's own rows (add a widget, the widget stack) are here too; otherwise the sheet offers to adjust the
 * widget showing, so resizing never depends on finding the widget to long-press.
 */
@Composable
fun HomeMenuSheetContent(
    onSwitchFacetClick: () -> Unit,
    onFacetSettingsClick: () -> Unit,
    onLauncherSettingsClick: () -> Unit,
    onAddWidgetClick: () -> Unit,
    onWidgetStackClick: () -> Unit,
    onAdjustClick: () -> Unit,
    /** What the Widget stack row says is in the stack; only shown while the stack is empty. */
    stackSummary: String,
    modifier: Modifier = Modifier,
    stackEmpty: Boolean = false,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 16.dp)
            .testTag("home_menu_sheet"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (stackEmpty) {
            SettingsCard(fullBleedRows = true) {
                AdjustRow(
                    icon = Icons.Outlined.AddCircleOutline,
                    hue = SettingsSectionHue.APPEARANCE,
                    label = stringResource(R.string.home_sheet_add_widget),
                    subtitle = stringResource(R.string.home_sheet_add_widget_subtitle),
                    onClick = onAddWidgetClick,
                    testTag = "home_sheet_add_widget",
                )
                CardDivider(startInset = SettingsIconBadgeInset)
                WidgetStackRow(stackSummary, onWidgetStackClick)
            }
        } else {
            SettingsCard(fullBleedRows = true) { AdjustWidgetRow(onAdjustClick) }
        }
        SettingsRows(onSwitchFacetClick, onFacetSettingsClick, onLauncherSettingsClick)
    }
}

/**
 * The widget's own sheet, opened by a long-press on the widget showing (the clock, its calendar, a Facet widget or a hosted one). First
 * card: that widget's settings (the clock's styles and calendar; "<Widget> settings" for Weather and Agenda; none for the rest). Second:
 * adjust size and position (the only way into resize mode), replace or remove the widget, and the widget stack. Replacing swaps the page in
 * place and is never a Pro action.
 */
@Composable
fun WidgetSheetContent(
    target: WidgetSheetTarget,
    onClockStylesClick: () -> Unit,
    onCalendarSettingsClick: () -> Unit,
    onWidgetSettingsClick: () -> Unit,
    onAdjustClick: () -> Unit,
    onReplaceWidgetClick: () -> Unit,
    onRemoveWidgetClick: () -> Unit,
    onWidgetStackClick: () -> Unit,
    /** What the Widget stack row says is in the stack: "Clock", "3 pages". */
    stackSummary: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 16.dp)
            .testTag("widget_sheet"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when (target) {
            WidgetSheetTarget.Clock -> ClockSettingsCard(onClockStylesClick, onCalendarSettingsClick)
            is WidgetSheetTarget.Native -> if (target.type.hasSettings) {
                SettingsCard(fullBleedRows = true) {
                    AdjustRow(
                        icon = Icons.Outlined.Tune,
                        hue = SettingsSectionHue.APPEARANCE,
                        label = stringResource(R.string.widget_sheet_settings, stringResource(target.type.labelRes())),
                        subtitle = stringResource(
                            if (target.type == NativeWidgetType.WEATHER) R.string.widget_sheet_weather_settings_subtitle else R.string.widget_sheet_agenda_settings_subtitle,
                        ),
                        onClick = onWidgetSettingsClick,
                        testTag = "widget_sheet_settings",
                    )
                }
            }
            WidgetSheetTarget.Hosted -> Unit
        }
        SettingsCard(fullBleedRows = true) {
            AdjustWidgetRow(onAdjustClick, testTag = "widget_sheet_adjust")
            PageRows(onReplaceWidgetClick, onRemoveWidgetClick)
            CardDivider(startInset = SettingsIconBadgeInset)
            WidgetStackRow(stackSummary, onWidgetStackClick)
        }
    }
}

@Composable
private fun AdjustWidgetRow(onClick: () -> Unit, testTag: String = "home_sheet_adjust") {
    AdjustRow(
        icon = Icons.Outlined.OpenInFull,
        hue = SettingsSectionHue.APPEARANCE,
        label = stringResource(R.string.widget_sheet_adjust_label),
        subtitle = stringResource(R.string.widget_sheet_adjust_subtitle),
        onClick = onClick,
        testTag = testTag,
    )
}

@Composable
private fun WidgetStackRow(stackSummary: String, onWidgetStackClick: () -> Unit) {
    AdjustRow(
        icon = Icons.Outlined.Layers,
        hue = SettingsSectionHue.APPEARANCE,
        label = stringResource(R.string.widget_sheet_stack),
        subtitle = stackSummary,
        onClick = onWidgetStackClick,
        testTag = "widget_sheet_stack",
    )
}

/** The clock's two settings screens: its styles, and the calendar under it. Shared by Home's widget sheet and the widget stack's Configure. */
@Composable
internal fun ClockSettingsCard(onClockStylesClick: () -> Unit, onCalendarSettingsClick: () -> Unit) {
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
private fun HomeMenuSheetPreview() {
    FacetLauncherTheme {
        HomeMenuSheetContent({}, {}, {}, {}, {}, {}, stackSummary = "Clock", modifier = Modifier.background(SurfaceContainer))
    }
}

@Preview(name = "Empty stack", showBackground = true)
@Composable
private fun HomeMenuSheetEmptyPreview() {
    FacetLauncherTheme {
        HomeMenuSheetContent({}, {}, {}, {}, {}, {}, stackSummary = "Empty", stackEmpty = true, modifier = Modifier.background(SurfaceContainer))
    }
}

@Preview(name = "Clock widget sheet", showBackground = true)
@Composable
private fun ClockWidgetSheetPreview() {
    FacetLauncherTheme {
        WidgetSheetContent(WidgetSheetTarget.Clock, {}, {}, {}, {}, {}, {}, {}, stackSummary = "Clock", modifier = Modifier.background(SurfaceContainer))
    }
}

@Preview(name = "Agenda widget sheet", showBackground = true)
@Composable
private fun AgendaWidgetSheetPreview() {
    FacetLauncherTheme {
        WidgetSheetContent(WidgetSheetTarget.Native(NativeWidgetType.AGENDA), {}, {}, {}, {}, {}, {}, {}, stackSummary = "3 pages", modifier = Modifier.background(SurfaceContainer))
    }
}

@Composable
private fun SettingsRows(onSwitchFacetClick: () -> Unit, onFacetSettingsClick: () -> Unit, onLauncherSettingsClick: () -> Unit) {
    SettingsCard(fullBleedRows = true) {
        AdjustRow(
            icon = Icons.Outlined.SwapHoriz,
            hue = SettingsSectionHue.FACETS,
            label = stringResource(R.string.home_sheet_switch_facet),
            subtitle = stringResource(R.string.home_sheet_switch_facet_subtitle),
            onClick = onSwitchFacetClick,
            testTag = "home_sheet_switch_facet",
        )
        CardDivider(startInset = SettingsIconBadgeInset)
        AdjustRow(
            icon = Icons.Outlined.Settings,
            hue = SettingsSectionHue.FACETS,
            label = stringResource(R.string.facet_carousel_facet_settings),
            subtitle = stringResource(R.string.home_sheet_facet_settings_subtitle),
            onClick = onFacetSettingsClick,
            testTag = "home_sheet_facet_settings",
        )
        CardDivider(startInset = SettingsIconBadgeInset)
        AdjustRow(
            icon = Icons.Outlined.Tune,
            hue = SettingsSectionHue.SYSTEM,
            label = stringResource(R.string.facet_carousel_launcher_settings),
            subtitle = stringResource(R.string.facet_carousel_launcher_settings_subtitle),
            onClick = onLauncherSettingsClick,
            testTag = "home_sheet_launcher_settings",
        )
    }
}

/** Rows that act on the page showing. Called inside the second card, after the adjust row. */
@Composable
private fun PageRows(
    onReplaceWidgetClick: () -> Unit,
    onRemoveWidgetClick: () -> Unit,
) {
    Column {
        CardDivider(startInset = SettingsIconBadgeInset)
        AdjustRow(
            icon = Icons.Outlined.Widgets,
            hue = SettingsSectionHue.APPEARANCE,
            label = stringResource(R.string.widget_sheet_replace),
            subtitle = stringResource(R.string.widget_sheet_replace_subtitle),
            onClick = onReplaceWidgetClick,
            testTag = "widget_sheet_replace",
        )
        CardDivider(startInset = SettingsIconBadgeInset)
        AdjustRow(
            icon = Icons.Outlined.DeleteOutline,
            hue = SettingsSectionHue.APPEARANCE,
            label = stringResource(R.string.widget_sheet_remove),
            subtitle = stringResource(R.string.widget_sheet_remove_subtitle),
            onClick = onRemoveWidgetClick,
            testTag = "widget_sheet_remove",
        )
    }
}
