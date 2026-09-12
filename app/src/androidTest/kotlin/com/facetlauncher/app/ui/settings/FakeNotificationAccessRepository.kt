package com.facetlauncher.app.ui.settings

import android.content.Context
import com.facetlauncher.app.data.NotificationAccessRepository

/**
 * Fakes the grant check with a fixed value instead of the real `NotificationManagerCompat`
 * lookup — same rationale as [FakeCalendarPermissionRepository]: notification listener access is
 * a special-access grant with no `GrantPermissionRule` equivalent, and this codebase's tests
 * never touch the real one, so a fixed value stands in for both the granted and ungranted cases.
 */
internal class FakeNotificationAccessRepository(
    context: Context,
    private val granted: Boolean,
) : NotificationAccessRepository(context) {
    override fun isGranted(): Boolean = granted
}
