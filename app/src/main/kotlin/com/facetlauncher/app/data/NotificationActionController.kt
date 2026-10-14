package com.facetlauncher.app.data

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Lets the UI dismiss other apps' notifications. Only the bound
 * [android.service.notification.NotificationListenerService] may call `cancelNotification`, so
 * [FacetNotificationListenerService] attaches itself here while connected and detaches when it
 * isn't; a dismiss while detached is a no-op rather than an error. The snapshot refreshes itself
 * through the service's own `onNotificationRemoved`.
 */
@Singleton
class NotificationActionController @Inject constructor() {

    @Volatile
    private var canceller: ((List<String>) -> Unit)? = null

    fun attach(canceller: (List<String>) -> Unit) {
        this.canceller = canceller
    }

    fun detach() {
        canceller = null
    }

    /** Returns whether a connected listener took the request. */
    fun dismiss(keys: List<String>): Boolean {
        val current = canceller
        if (keys.isEmpty() || current == null) return false
        return runCatching { current(keys) }.isSuccess
    }
}
