package com.lumenlauncher.app.data

import android.app.Notification
import android.app.NotificationManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.lumenlauncher.app.data.model.NotificationInfo
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * F13's `NotificationListenerService` — declared in the manifest with
 * `BIND_NOTIFICATION_LISTENER_SERVICE`, system-instantiated once the user grants access via
 * `ACTION_NOTIFICATION_LISTENER_SETTINGS` (see [NotificationAccessRepository]). Every callback
 * just recomputes the *whole* badge snapshot from [getActiveNotifications] rather than
 * incrementing/decrementing per event — simpler, and self-correcting if an event is ever missed.
 *
 * Silence is determined via [getCurrentRanking]/[NotificationListenerService.Ranking.getImportance],
 * **not** [NotificationManager.getNotificationChannel] — the latter only resolves channels
 * belonging to *this* app; a listener needs the ranking API to see another app's effective
 * (possibly channel-blocked) importance. Group-summary notifications are filtered out entirely —
 * counting both a summary and its child notifications would double-count one conversation.
 */
@AndroidEntryPoint
class LumenNotificationListenerService : NotificationListenerService() {

    @Inject
    lateinit var notificationBadgeRepository: NotificationBadgeRepository

    override fun onListenerConnected() {
        super.onListenerConnected()
        refresh()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        refresh()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        refresh()
    }

    private fun refresh() {
        val ranking = Ranking()
        val notifications = runCatching { activeNotifications }
            .getOrDefault(emptyArray())
            .filterNot { it.notification.flags and Notification.FLAG_GROUP_SUMMARY != 0 }
            .map { sbn ->
                val silent = currentRanking?.getRanking(sbn.key, ranking) == true &&
                    ranking.importance == NotificationManager.IMPORTANCE_NONE
                NotificationInfo(packageName = sbn.packageName, silent = silent)
            }
        notificationBadgeRepository.setActiveNotifications(notifications)
    }
}
