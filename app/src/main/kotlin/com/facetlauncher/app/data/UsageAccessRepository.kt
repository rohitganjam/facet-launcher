package com.facetlauncher.app.data

import android.app.AppOpsManager
import android.content.Context
import android.os.Process
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * F2's `PACKAGE_USAGE_STATS` special-access grant check. This permission never surfaces via
 * `checkSelfPermission` — it's a user-toggled entry in system Settings, checked instead via
 * [AppOpsManager.checkOpNoThrow]. There's no grant-change callback, so callers re-check this
 * (typically on `onResume`, since the only way to grant it is the system Settings redirect).
 */
@Singleton
class UsageAccessRepository @Inject constructor(
    private val appOpsManager: AppOpsManager,
    @ApplicationContext private val context: Context,
) {
    fun isGranted(): Boolean {
        val mode = appOpsManager.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName,
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }
}
