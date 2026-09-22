package com.facetlauncher.app.ui.hub.picker

import android.content.Intent
import android.content.IntentSender
import com.facetlauncher.app.data.model.WidgetProviderOption

data class WidgetProviderGroup(
    val appLabel: String,
    val options: List<WidgetProviderOption>,
)

data class HubWidgetPickerUiState(
    val query: String = "",
    val groups: List<WidgetProviderGroup> = emptyList(),
    /** `null` hides the header's "N left" counter entirely — used by PRD F15's clock widget picker, which has no capacity concept (a facet has exactly 0 or 1 clock widget, not up to [com.facetlauncher.app.domain.HUB_MAX_WIDGETS]). */
    val remaining: Int? = 0,
)

enum class AddFailureReason { HUB_FULL, SETUP_CANCELLED }

/** One-off effects the composable must act on (system Activity-result detours) — not folded into [HubWidgetPickerUiState]. */
sealed interface HubAddWidgetEvent {
    data class LaunchBindPermission(val intent: Intent) : HubAddWidgetEvent
    data class LaunchConfigure(val intentSender: IntentSender) : HubAddWidgetEvent
    data object WidgetAdded : HubAddWidgetEvent
    data class AddFailed(val reason: AddFailureReason) : HubAddWidgetEvent
}
