package com.facetlauncher.app.data

import android.app.Notification
import android.os.Bundle

/** A card shows a line or two; there is no use holding a whole email body in memory. */
internal const val MAX_NOTIFICATION_TEXT_LENGTH = 300

/** The title and body to show for a notification's extras. Prefers the compact text over the expanded one, and trims both. */
internal fun notificationTitleAndText(extras: Bundle?): Pair<String?, String?> {
    if (extras == null) return null to null
    val title = extras.firstText(Notification.EXTRA_TITLE, Notification.EXTRA_TITLE_BIG)
    val text = extras.firstText(Notification.EXTRA_TEXT, Notification.EXTRA_BIG_TEXT)
    return title to text
}

private fun Bundle.firstText(vararg keys: String): String? =
    keys.firstNotNullOfOrNull { key -> getCharSequence(key)?.toString()?.trim()?.takeIf { it.isNotEmpty() } }
        ?.take(MAX_NOTIFICATION_TEXT_LENGTH)
