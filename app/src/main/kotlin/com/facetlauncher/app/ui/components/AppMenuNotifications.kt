package com.facetlauncher.app.ui.components

import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.NotificationInfo
import com.facetlauncher.app.domain.AppNotificationsState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * What [AppContextMenu]'s notifications zone needs, bundled so it threads through Home and the
 * drawer as one parameter. [None] shows no zone.
 */
data class AppMenuNotifications(
    val observe: (AppInfo) -> Flow<AppNotificationsState> = { flowOf(AppNotificationsState(enabled = false)) },
    /** Opens the notification's own screen. */
    val onOpen: (NotificationInfo) -> Unit = {},
    /** Dismisses these notifications by key; false when nothing could be dismissed. */
    val onDismiss: (List<String>) -> Boolean = { false },
    /** Sends the user to grant notification access. */
    val onEnableAccess: () -> Unit = {},
) {
    companion object {
        val None = AppMenuNotifications()
    }
}
