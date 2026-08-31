package com.lumenlauncher.app.data.model

import android.content.ComponentName

/** One addable widget, as surfaced by `AppWidgetManager.installedProviders` — grouped by [provider]'s owning app in the picker. */
data class WidgetProviderOption(
    val provider: ComponentName,
    val appLabel: String,
    val widgetLabel: String,
    val columns: Int,
    val rows: Int,
)
