package com.lumenlauncher.app.ui.hub.picker

import android.content.Intent
import com.lumenlauncher.app.data.model.WidgetProviderOption

data class WidgetProviderGroup(
    val appLabel: String,
    val options: List<WidgetProviderOption>,
)

data class HubWidgetPickerUiState(
    val query: String = "",
    val groups: List<WidgetProviderGroup> = emptyList(),
    val remaining: Int = 0,
)

/** One-off effects the composable must act on (system Activity-result detours) — not folded into [HubWidgetPickerUiState]. */
sealed interface HubAddWidgetEvent {
    data class LaunchBindPermission(val intent: Intent) : HubAddWidgetEvent
    data class LaunchConfigure(val intent: Intent) : HubAddWidgetEvent
    data object WidgetAdded : HubAddWidgetEvent
    data object AddFailed : HubAddWidgetEvent
}
