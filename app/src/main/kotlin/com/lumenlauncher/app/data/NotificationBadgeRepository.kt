package com.lumenlauncher.app.data

import com.lumenlauncher.app.data.model.NotificationInfo
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * F13's live notification-badge state — a plain in-memory singleton, not persisted (repopulated
 * from [android.service.notification.NotificationListenerService.getActiveNotifications] every
 * time the listener reconnects, which is the correct source of truth; nothing here survives
 * process death, nor should it). [com.lumenlauncher.app.data.LumenNotificationListenerService] is
 * the only writer; every other consumer (Home, Drawer) only ever reads [badgeCounts].
 */
@Singleton
class NotificationBadgeRepository @Inject constructor() {

    private val _badgeCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val badgeCounts: StateFlow<Map<String, Int>> = _badgeCounts.asStateFlow()

    /**
     * Replaces the whole snapshot — always recomputed wholesale from the listener's current
     * `activeNotifications`/`Ranking`, never incremented/decremented per event. A package can
     * have several notifications; only non-[NotificationInfo.silent] ones count, deduped by
     * package (F13 is dot/count, never per-notification detail).
     */
    fun setActiveNotifications(notifications: List<NotificationInfo>) {
        _badgeCounts.value = notifications
            .filterNot { it.silent }
            .groupingBy { it.packageName }
            .eachCount()
    }
}
