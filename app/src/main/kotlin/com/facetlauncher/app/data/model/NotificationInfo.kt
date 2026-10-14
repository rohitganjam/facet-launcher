package com.facetlauncher.app.data.model

import android.app.PendingIntent
import android.os.UserHandle

/**
 * One active notification, as [com.facetlauncher.app.data.FacetNotificationListenerService] maps
 * it from the real `StatusBarNotification`/`Ranking` framework types, so
 * [com.facetlauncher.app.data.NotificationBadgeRepository] stays trivially testable without
 * Robolectric. Everything past [isMedia] is only for the long-press sheet's notification list.
 */
data class NotificationInfo(
    val packageName: String,
    /** True for a notification the system itself treats as silent (channel importance `IMPORTANCE_NONE`) — F13 never badges these. */
    val silent: Boolean,
    /** True for a `MediaStyle` notification — what keeps Home's media player row on screen. */
    val isMedia: Boolean = false,
    /** The listener's own key — what dismissing a notification needs. */
    val key: String = "",
    /** The profile the notification was posted for, so a Work copy of an app never shows a personal app's notifications. */
    val userHandle: UserHandle? = null,
    val title: String? = null,
    val text: String? = null,
    val postTime: Long = 0L,
    /** False for ongoing and no-clear notifications, which the user can't swipe away. */
    val isClearable: Boolean = true,
    /** `VISIBILITY_SECRET`: the app asked for it never to be shown on a surface like this. */
    val isSecret: Boolean = false,
    /** The app asked for the notification to go away once it's opened. Sending its intent from here doesn't do that by itself. */
    val autoCancel: Boolean = false,
    val contentIntent: PendingIntent? = null,
)
