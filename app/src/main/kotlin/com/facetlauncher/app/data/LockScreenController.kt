package com.facetlauncher.app.data

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Lets the UI lock the screen. Only a connected accessibility service can perform the lock global
 * action, so [FacetAccessibilityService] attaches itself here while connected and detaches when it
 * isn't; a lock while detached is a no-op rather than an error.
 */
@Singleton
class LockScreenController @Inject constructor() {

    @Volatile
    private var locker: (() -> Boolean)? = null

    fun attach(locker: () -> Boolean) {
        this.locker = locker
    }

    fun detach() {
        locker = null
    }

    /** Returns whether a connected service took the request. */
    fun lock(): Boolean {
        val current = locker ?: return false
        return runCatching { current() }.getOrDefault(false)
    }
}
