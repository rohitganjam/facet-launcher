package com.facetlauncher.app.domain

import android.os.UserHandle
import com.facetlauncher.app.data.NotificationAccessRepository
import com.facetlauncher.app.data.NotificationBadgeRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.NotificationInfo
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/** What the long-press sheet's notification zone shows for one app. */
data class AppNotificationsState(
    val notifications: List<NotificationInfo> = emptyList(),
    /** The setting is on but notification access isn't granted, so there is nothing to list yet. */
    val needsAccess: Boolean = false,
    /** False when the user turned the setting off: the zone isn't shown at all. Otherwise it always is, even with nothing to list. */
    val enabled: Boolean = true,
)

/**
 * The notifications zone of an app's long-press sheet: the setting, the access grant and the live
 * list in one place. Access is read when the flow starts (there is no grant-change callback), which
 * is fine for a sheet that opens fresh each time.
 */
class ObserveAppNotificationsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val notificationBadgeRepository: NotificationBadgeRepository,
    private val notificationAccessRepository: NotificationAccessRepository,
) {
    operator fun invoke(packageName: String, userHandle: UserHandle): Flow<AppNotificationsState> =
        settingsRepository.settings
            .map { it.showNotificationsInAppMenu }
            .distinctUntilChanged()
            .flatMapLatest { enabled ->
                when {
                    !enabled -> flowOf(AppNotificationsState(enabled = false))
                    !notificationAccessRepository.isGranted() -> flowOf(AppNotificationsState(needsAccess = true))
                    else -> notificationBadgeRepository.notificationsFor(packageName, userHandle).map { AppNotificationsState(it) }
                }
            }
}
