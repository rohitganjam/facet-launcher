package com.lumenlauncher.app.data

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * F13's notification-listener special-access grant check. Like `PACKAGE_USAGE_STATS`
 * ([UsageAccessRepository]), this never surfaces via `checkSelfPermission` — it's a user-toggled
 * entry in system Settings (`ACTION_NOTIFICATION_LISTENER_SETTINGS`), checked instead via
 * [NotificationManagerCompat.getEnabledListenerPackages]. No grant-change callback exists, so
 * callers re-check this (typically on `onResume`, since the only way to grant it is that redirect).
 */
@Singleton
open class NotificationAccessRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    open fun isGranted(): Boolean =
        NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)
}
