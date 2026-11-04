package com.facetlauncher.app.ui.launcher

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.os.Process
import android.os.UserManager
import android.provider.CalendarContract
import com.facetlauncher.app.data.model.CalendarEvent
import com.facetlauncher.app.data.model.WORK_CALENDAR_ID_PREFIX

/** Opens [event] in the Calendar app of the profile it lives in: a work event's id means nothing to the personal app. */
internal fun openCalendarEvent(context: Context, event: CalendarEvent) {
    if (event.calendarId.startsWith(WORK_CALENDAR_ID_PREFIX)) {
        runCatching { openWorkCalendarApp(context) }
        return
    }
    val uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, event.id)
    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
}

private fun openWorkCalendarApp(context: Context) {
    val launcherApps = context.getSystemService(LauncherApps::class.java)
    val calendarPackage = context.packageManager
        .resolveActivity(Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_CALENDAR), 0)
        ?.activityInfo?.packageName ?: return
    val work = context.getSystemService(UserManager::class.java).userProfiles.firstOrNull { it != Process.myUserHandle() } ?: return
    val activity = launcherApps.getActivityList(calendarPackage, work).firstOrNull() ?: return
    launcherApps.startMainActivity(activity.componentName, work, null, null)
}
