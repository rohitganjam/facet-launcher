package com.facetlauncher.app.data.model

import android.content.ComponentName
import android.graphics.Bitmap
import android.os.Process
import android.os.UserHandle

/** One addable widget, as surfaced by `AppWidgetManager.installedProviders` (every profile) — grouped by [provider]'s owning app in the picker. */
data class WidgetProviderOption(
    val provider: ComponentName,
    val appLabel: String,
    val widgetLabel: String,
    val columns: Int,
    val rows: Int,
    /** The provider's declared preview image, falling back to its app icon — `null` only if neither could be loaded. */
    val previewIcon: Bitmap? = null,
    /** Which profile this provider was enumerated from, for display (badge) purposes only. */
    val profile: AppProfile = AppProfile.PERSONAL,
    /** The real handle this provider was enumerated from — used to bind/rebind, never re-derived from [profile]. */
    val userHandle: UserHandle = Process.myUserHandle(),
)
