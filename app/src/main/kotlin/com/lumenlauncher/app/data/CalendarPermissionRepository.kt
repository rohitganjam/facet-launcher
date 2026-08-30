package com.lumenlauncher.app.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * F1's `READ_CALENDAR` grant check — a normal runtime permission (unlike F2's special-access
 * `PACKAGE_USAGE_STATS`), so this *does* surface via `checkSelfPermission`/a real system dialog.
 *
 * `open` so instrumented tests can substitute a fake reporting a fixed granted/ungranted value
 * (mirrors [CalendarRepository]'s existing `open`-for-`FakeCalendarRepository` pattern) — testing
 * the granted-state UI this way needs no real [android.app.UiAutomation]/`GrantPermissionRule`
 * grant at all, avoiding the mandatory app-process kill a *real* permission revoke causes when
 * done from this app's own instrumented process (see `CalendarSettingsScreenTest.kt`'s history).
 */
@Singleton
open class CalendarPermissionRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    open fun isGranted(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED
}
