package com.facetlauncher.app.ui.home

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
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
import com.facetlauncher.app.ui.components.ProPill
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
 * "Edit clock styles"'s own label text never changes; a trailing [FacetScopeBadge]
 * names which style set it edits instead — "Global" or the overriding facet's own real name — same
 * pattern as [com.facetlauncher.app.ui.components.QuickPlacementBadge] on the app context menu's
 * Favorites/Dock rows (decided explicitly by the user to apply that same badge logic here, over
 * folding the distinction into the sentence itself — see chat history).
 *
 * PRD F15 — [hasCustomClockWidget] switches the sheet between two widget-choice rows rather than
 * offering a destructive "remove": "Use custom widget" (always shown — picks a widget, or a
 * different one if one's already active) and, only once [hasCustomClockWidget] is true, "Switch
 * to launcher clock widget" (reverts to this facet's own native clock). "Edit … styles" is hidden
 * while a custom widget is active — no [com.facetlauncher.app.data.model.ClockTemplateId] styling
 * applies to a hosted widget's own content — but "Adjust size & position" stays available either
 * way.
 */
@Composable
fun ClockAdjustSheet(
    onAdjustClick: () -> Unit,
    onEditStylesClick: () -> Unit,
    onFacetSettingsClick: () -> Unit,
    onLauncherSettingsClick: () -> Unit,
    onUseCustomWidgetClick: () -> Unit,
    onSwitchToLauncherClockClick: () -> Unit,
    /** Non-null names the facet whose own clock styles are in effect right now; `null` means the launcher-wide default applies — see [FacetScopeBadge]. */
    overrideFacetName: String?,
    modifier: Modifier = Modifier,
    /** Whether the active facet currently has a hosted `AppWidget` bound as its clock (PRD F15) — see this composable's own doc. */
    hasCustomClockWidget: Boolean = false,
    /** `false` marks "Use custom widget" as Pro; the click is still reported so the caller can open Facet Pro. */
    isPro: Boolean = true,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
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
            ClockWidgetChoiceRows(
                onEditStylesClick = onEditStylesClick,
                onUseCustomWidgetClick = onUseCustomWidgetClick,
                onSwitchToLauncherClockClick = onSwitchToLauncherClockClick,
                overrideFacetName = overrideFacetName,
                hasCustomClockWidget = hasCustomClockWidget,
                isPro = isPro,
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
 * PRD F15's "Edit … styles"/widget-choice rows — factored out of [ClockAdjustSheet] itself purely
 * to stay under this codebase's max-composable-length lint rule; behavior is exactly
 * [ClockAdjustSheet]'s own doc. Each row is preceded by its divider, so the first card's last row
 * has none after it.
 */
@Composable
private fun ClockWidgetChoiceRows(
    onEditStylesClick: () -> Unit,
    onUseCustomWidgetClick: () -> Unit,
    onSwitchToLauncherClockClick: () -> Unit,
    overrideFacetName: String?,
    hasCustomClockWidget: Boolean,
    isPro: Boolean,
) = Column {
    if (!hasCustomClockWidget) {
        CardDivider(startInset = SettingsIconBadgeInset)
        AdjustRow(
            icon = Icons.Outlined.Palette,
            hue = SettingsSectionHue.APPEARANCE,
            label = stringResource(R.string.clock_adjust_edit_styles),
            subtitle = stringResource(R.string.clock_adjust_edit_styles_subtitle),
            onClick = onEditStylesClick,
            testTag = "clock_adjust_edit_styles",
            trailingContent = { FacetScopeBadge(facetName = overrideFacetName) },
        )
    }
    CardDivider(startInset = SettingsIconBadgeInset)
    AdjustRow(
        icon = Icons.Outlined.Widgets,
        hue = SettingsSectionHue.APPEARANCE,
        label = stringResource(R.string.clock_adjust_use_custom_widget),
        subtitle = stringResource(R.string.clock_adjust_use_custom_widget_subtitle),
        onClick = onUseCustomWidgetClick,
        testTag = "clock_adjust_use_custom_widget",
        trailingContent = if (isPro) null else ({ ProPill() }),
    )
    if (hasCustomClockWidget) {
        CardDivider(startInset = SettingsIconBadgeInset)
        AdjustRow(
            icon = Icons.Outlined.AccessTime,
            hue = SettingsSectionHue.APPEARANCE,
            label = stringResource(R.string.clock_adjust_switch_to_launcher_clock),
            subtitle = stringResource(R.string.clock_adjust_switch_to_launcher_clock_subtitle),
            onClick = onSwitchToLauncherClockClick,
            testTag = "clock_adjust_switch_to_launcher_clock",
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
            onEditStylesClick = {},
            onFacetSettingsClick = {},
            onLauncherSettingsClick = {},
            onUseCustomWidgetClick = {},
            onSwitchToLauncherClockClick = {},
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
            onEditStylesClick = {},
            onFacetSettingsClick = {},
            onLauncherSettingsClick = {},
            onUseCustomWidgetClick = {},
            onSwitchToLauncherClockClick = {},
            overrideFacetName = null,
            hasCustomClockWidget = true,
            modifier = Modifier.background(SurfaceContainer),
        )
    }
}
