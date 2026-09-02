package com.lumenlauncher.app.data.model

import android.content.ComponentName
import android.graphics.Bitmap

/** One addable widget, as surfaced by `AppWidgetManager.installedProviders` — grouped by [provider]'s owning app in the picker. */
data class WidgetProviderOption(
    val provider: ComponentName,
    val appLabel: String,
    val widgetLabel: String,
    val columns: Int,
    val rows: Int,
    /** The provider's declared preview image, falling back to its app icon — `null` only if neither could be loaded. */
    val previewIcon: Bitmap? = null,
)
