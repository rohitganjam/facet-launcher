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
import androidx.compose.material.icons.outlined.AccessTime
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
import com.facetlauncher.app.ui.components.FacetScopeBadge
import com.facetlauncher.app.ui.components.SettingsCard
import com.facetlauncher.app.ui.components.SettingsIconBadge
import com.facetlauncher.app.ui.components.SettingsIconBadgeInset
import com.facetlauncher.app.ui.settings.ClickableRow
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.SettingsSectionHue
import com.facetlauncher.app.ui.theme.SurfaceContainer

/**
 * Long-press bottom sheet — opened both by long-pressing the clock and by long-pressing any other
 * empty space on Home (see [com.facetlauncher.app.ui.home.HomeScreen]'s own root long-press
 * detector). Lets the user enter the combined clock adjust mode (move handle + resize handles
 * together), jump to the clock style gallery, or jump to the active facet's own settings
 * or launcher-wide settings — each row carries a subheading, same pattern as
 * [com.facetlauncher.app.ui.facets.FacetCarouselScreen]'s own "Launcher settings" row and its
 * per-card "Facet settings" gear.
 *
 * Laid out like the Settings screens: two tonal [SettingsCard]s (the clock rows, then the two
 * settings destinations) of icon-badge rows on the dimmer sheet tone ([SurfaceContainer], set by
 * the host), with full-width press highlights.
 *
 * "Clock & calendar settings"'s own label text never changes; a trailing [FacetScopeBadge]
 * names which style set it edits instead — "Global" or the overriding facet's own real name — same
 * pattern as [com.facetlauncher.app.ui.components.QuickPlacementBadge] on the app context menu's
 * Favorites/Dock rows (decided explicitly by the user to apply that same badge logic here, over
 * folding the distinction into the sentence itself — see chat history).
 *
 * The widget rows follow the page showing (PRD F15's clock stack). On the clock page: its settings and "Replace
 * with custom widget". On a widget page: "Replace with clock" (hidden while [canReplaceWithClock] is false, as the
 * clock is already on another page) and "Replace custom widget". Replacing swaps the visible page in place and is
 * never a Pro action; "Adjust size & position" is there either way.
 */
@Composable
fun ClockAdjustSheet(
    onAdjustClick: () -> Unit,
    onClockSettingsClick: () -> Unit,
    onFacetSettingsClick: () -> Unit,
    onLauncherSettingsClick: () -> Unit,
    onReplaceWithWidgetClick: () -> Unit,
    onReplaceWithClockClick: () -> Unit,
    onWidgetStackClick: () -> Unit,
    /** What the Widget stack row says is in the stack: "Clock", "3 pages", "Empty…". */
    stackSummary: String,
    /** Non-null names the facet whose own clock styles are in effect right now; `null` means the launcher-wide default applies — see [FacetScopeBadge]. */
    overrideFacetName: String?,
    modifier: Modifier = Modifier,
    /** What the page showing is: the clock, a hosted `AppWidget` (PRD F15), or nothing (an empty stack) — see this composable's own doc. */
    page: ClockSheetPage = ClockSheetPage.CLOCK,
    /** Whether "Replace with clock" is offered on a widget page: not while the clock is already on another page of the stack. */
    canReplaceWithClock: Boolean = true,
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
            ClockPageRows(
                onClockSettingsClick = onClockSettingsClick,
                onReplaceWithWidgetClick = onReplaceWithWidgetClick,
                onReplaceWithClockClick = onReplaceWithClockClick,
                overrideFacetName = overrideFacetName,
                page = page,
                canReplaceWithClock = canReplaceWithClock,
            )
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
}

/**
 * The rows that depend on the page showing, factored out of [ClockAdjustSheet] itself purely to stay under this
 * codebase's max-composable-length lint rule. Each row is preceded by its divider, so the first card's last row has
 * none after it.
 */
@Composable
private fun ClockPageRows(
    onClockSettingsClick: () -> Unit,
    onReplaceWithWidgetClick: () -> Unit,
    onReplaceWithClockClick: () -> Unit,
    overrideFacetName: String?,
    page: ClockSheetPage,
    canReplaceWithClock: Boolean,
) = Column {
    if (page == ClockSheetPage.CLOCK) {
        CardDivider(startInset = SettingsIconBadgeInset)
        AdjustRow(
            icon = Icons.Outlined.Palette,
            hue = SettingsSectionHue.APPEARANCE,
            label = stringResource(R.string.clock_adjust_clock_settings),
            subtitle = stringResource(R.string.clock_adjust_clock_settings_subtitle),
            onClick = onClockSettingsClick,
            testTag = "clock_adjust_clock_settings",
            trailingContent = { FacetScopeBadge(facetName = overrideFacetName) },
        )
        CardDivider(startInset = SettingsIconBadgeInset)
        AdjustRow(
            icon = Icons.Outlined.Widgets,
            hue = SettingsSectionHue.APPEARANCE,
            label = stringResource(R.string.clock_adjust_replace_with_widget),
            subtitle = stringResource(R.string.clock_adjust_replace_with_widget_subtitle),
            onClick = onReplaceWithWidgetClick,
            testTag = "clock_adjust_replace_with_widget",
        )
    } else if (page == ClockSheetPage.WIDGET) {
        if (canReplaceWithClock) {
            CardDivider(startInset = SettingsIconBadgeInset)
            AdjustRow(
                icon = Icons.Outlined.AccessTime,
                hue = SettingsSectionHue.APPEARANCE,
                label = stringResource(R.string.clock_adjust_replace_with_clock),
                subtitle = stringResource(R.string.clock_adjust_replace_with_clock_subtitle),
                onClick = onReplaceWithClockClick,
                testTag = "clock_adjust_replace_with_clock",
            )
        }
        CardDivider(startInset = SettingsIconBadgeInset)
        AdjustRow(
            icon = Icons.Outlined.Widgets,
            hue = SettingsSectionHue.APPEARANCE,
            label = stringResource(R.string.clock_adjust_replace_widget),
            subtitle = stringResource(R.string.clock_adjust_replace_widget_subtitle),
            onClick = onReplaceWithWidgetClick,
            testTag = "clock_adjust_replace_widget",
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
    trailingContent: (@Composable () -> Unit)? = null,
) {
    ClickableRow(
        title = label,
        subtitle = subtitle,
        onClick = onClick,
        testTag = testTag,
        modifier = modifier,
        leadingIcon = { SettingsIconBadge(icon, hue) },
        trailing = trailingContent,
    )
}

@Preview(showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ClockAdjustSheetPreview() {
    FacetLauncherTheme {
        ClockAdjustSheet(
            onAdjustClick = {},
            onClockSettingsClick = {},
            onFacetSettingsClick = {},
            onLauncherSettingsClick = {},
            onReplaceWithWidgetClick = {},
            onReplaceWithClockClick = {},
            onWidgetStackClick = {},
            stackSummary = "Clock",
            overrideFacetName = null,
            modifier = Modifier.background(SurfaceContainer),
        )
    }
}

@Preview(name = "Custom widget active", showBackground = true)
@Composable
private fun ClockAdjustSheetCustomWidgetPreview() {
    FacetLauncherTheme {
        ClockAdjustSheet(
            onAdjustClick = {},
            onClockSettingsClick = {},
            onFacetSettingsClick = {},
            onLauncherSettingsClick = {},
            onReplaceWithWidgetClick = {},
            onReplaceWithClockClick = {},
            onWidgetStackClick = {},
            stackSummary = "1 widget",
            overrideFacetName = null,
            page = ClockSheetPage.WIDGET,
            modifier = Modifier.background(SurfaceContainer),
        )
    }
}
