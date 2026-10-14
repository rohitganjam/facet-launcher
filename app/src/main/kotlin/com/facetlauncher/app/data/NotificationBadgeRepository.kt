package com.facetlauncher.app.data

import android.os.UserHandle
import com.facetlauncher.app.data.model.NotificationInfo
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * F13's live notification-badge state — a plain in-memory singleton, not persisted (repopulated
 * from [android.service.notification.NotificationListenerService.getActiveNotifications] every
 * time the listener reconnects, which is the correct source of truth; nothing here survives
 * process death, nor should it). [com.facetlauncher.app.data.FacetNotificationListenerService] is
 * the only writer; every other consumer (Home, Drawer) only ever reads [badgeCounts],
 * [mediaNotificationPackages] and [notificationsFor].
 */
@Singleton
class NotificationBadgeRepository @Inject constructor() {

    private val _badgeCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val badgeCounts: StateFlow<Map<String, Int>> = _badgeCounts.asStateFlow()

    private val _mediaNotificationPackages = MutableStateFlow<Set<String>>(emptySet())

    /** Packages that currently have a media notification posted, silent or not. */
    val mediaNotificationPackages: StateFlow<Set<String>> = _mediaNotificationPackages.asStateFlow()

    private val _listedNotifications = MutableStateFlow<List<NotificationInfo>>(emptyList())

    /**
     * Replaces the whole snapshot — always recomputed wholesale from the listener's current
     * `activeNotifications`/`Ranking`, never incremented/decremented per event. A package can
     * have several notifications; only non-[NotificationInfo.silent] ones count, deduped by
     * package (F13 is dot/count, never per-notification detail).
     */
    fun setActiveNotifications(notifications: List<NotificationInfo>) {
        _mediaNotificationPackages.value = notifications.filter { it.isMedia }.map { it.packageName }.toSet()
        _badgeCounts.value = notifications
            .filterNot { it.silent }
            .groupingBy { it.packageName }
            .eachCount()
        _listedNotifications.value = notifications.filter { it.isListable() }.sortedByDescending { it.postTime }
    }

    /**
     * The notifications worth listing for one app in one profile, newest first. Leaves out silent,
     * media (Home's player row owns those), secret and contentless ones. Persistent
     * (non-clearable) ones are listed: they can be opened but not dismissed.
     */
    fun notificationsFor(packageName: String, userHandle: UserHandle): Flow<List<NotificationInfo>> =
        _listedNotifications
            .map { all -> all.filter { it.packageName == packageName && it.userHandle == userHandle } }
            .distinctUntilChanged()
}

private fun NotificationInfo.isListable(): Boolean =
    !silent && !isMedia && !isSecret && (!title.isNullOrBlank() || !text.isNullOrBlank())
