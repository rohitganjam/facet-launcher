package com.lumenlauncher.app.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Expands the system notification shade from Home's swipe-down gesture. `StatusBarManager` is
 * `@hide`d from the public SDK, so this goes through the same reflection call third-party
 * launchers have long relied on (`expandNotificationsPanel()`); `EXPAND_STATUS_BAR` is a
 * normal-level permission, auto-granted once declared in the manifest.
 */
@Singleton
class NotificationShadeRepository @Inject constructor(@ApplicationContext private val context: Context) {

    suspend fun expand() = withContext(Dispatchers.Default) {
        runCatching {
            val statusBarService = context.getSystemService("statusbar") ?: return@runCatching
            val method = Class.forName("android.app.StatusBarManager").getMethod("expandNotificationsPanel")
            method.invoke(statusBarService)
        }
        Unit
    }
}
