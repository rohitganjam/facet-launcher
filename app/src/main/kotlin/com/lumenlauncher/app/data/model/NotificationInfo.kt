package com.lumenlauncher.app.data.model

/**
 * One active notification, as [com.lumenlauncher.app.data.LumenNotificationListenerService] maps
 * it from the real `StatusBarNotification`/`Ranking` framework types — kept minimal and
 * framework-free so [com.lumenlauncher.app.data.NotificationBadgeRepository] stays trivially
 * testable without Robolectric.
 */
data class NotificationInfo(
    val packageName: String,
    /** True for a notification the system itself treats as silent (channel importance `IMPORTANCE_NONE`) — F13 never badges these. */
    val silent: Boolean,
)
