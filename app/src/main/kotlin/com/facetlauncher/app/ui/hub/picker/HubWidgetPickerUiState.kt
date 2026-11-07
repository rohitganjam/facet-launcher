package com.facetlauncher.app.ui.hub.picker

import android.content.Intent
import com.facetlauncher.app.data.model.NativeWidgetRef
import com.facetlauncher.app.data.model.AppProfile
import com.facetlauncher.app.data.model.WidgetProviderOption
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class WidgetProviderGroup(
    val appLabel: String,
    val options: List<WidgetProviderOption>,
)

/** Groups by owning app, apps A-Z by name (case-insensitive) and each app's widgets A-Z by widget name. */
fun List<WidgetProviderOption>.toSortedGroups(): List<WidgetProviderGroup> =
    groupBy { it.appLabel }
        .map { (appLabel, options) -> WidgetProviderGroup(appLabel, options.sortedBy { it.widgetLabel.lowercase() }) }
        .sortedBy { it.appLabel.lowercase() }

/** The profile a picker list is split by: a Work Profile's widgets are kept apart from the personal ones. */
enum class WidgetPickerTab { PERSONAL, WORK }

private fun WidgetProviderOption.pickerTab() = if (profile == AppProfile.WORK) WidgetPickerTab.WORK else WidgetPickerTab.PERSONAL

/**
 * The picker's state for [options] as it is searched and tabbed: with no Work Profile widgets there are no tabs and everything lists;
 * otherwise the Personal and Work tabs each list only their own, and [tab] picks which shows (Personal if it is not offered).
 */
fun pickerState(options: List<WidgetProviderOption>, query: String, tab: WidgetPickerTab, remaining: Int?): HubWidgetPickerUiState {
    val tabs = if (options.any { it.pickerTab() == WidgetPickerTab.WORK }) persistentListOf(WidgetPickerTab.PERSONAL, WidgetPickerTab.WORK) else persistentListOf()
    val shownTab = tab.takeIf { it in tabs } ?: WidgetPickerTab.PERSONAL
    val inTab = if (tabs.isEmpty()) options else options.filter { it.pickerTab() == shownTab }
    // Search only by app name, not widget name, per user request.
    val matching = if (query.isBlank()) inTab else inTab.filter { it.appLabel.contains(query, ignoreCase = true) }
    return HubWidgetPickerUiState(query = query, groups = matching.toSortedGroups(), remaining = remaining, tabs = tabs, tab = shownTab)
}

data class HubWidgetPickerUiState(
    val query: String = "",
    val groups: List<WidgetProviderGroup> = emptyList(),
    /** Personal and Work, when a Work Profile has widgets to offer; empty (no tab row) otherwise. */
    val tabs: ImmutableList<WidgetPickerTab> = persistentListOf(),
    val tab: WidgetPickerTab = WidgetPickerTab.PERSONAL,
    /** `null` hides the header's "N left" counter entirely — used by PRD F15's clock widget picker, which has no capacity concept (a facet has exactly 0 or 1 clock widget, not up to [com.facetlauncher.app.domain.HUB_MAX_WIDGETS]). */
    val remaining: Int? = 0,
)

enum class AddFailureReason { HUB_FULL, SETUP_CANCELLED }

/** One-off effects the composable must act on (system Activity-result detours) — not folded into [HubWidgetPickerUiState]. */
sealed interface HubAddWidgetEvent {
    data class LaunchBindPermission(val intent: Intent) : HubAddWidgetEvent
    data class LaunchConfigure(val intent: Intent) : HubAddWidgetEvent
    data object WidgetAdded : HubAddWidgetEvent

    /** A widget Facet draws itself was placed and has settings to choose first: they open for [ref]. */
    data class NativeWidgetAdded(val ref: NativeWidgetRef) : HubAddWidgetEvent
    data class AddFailed(val reason: AddFailureReason) : HubAddWidgetEvent
}
