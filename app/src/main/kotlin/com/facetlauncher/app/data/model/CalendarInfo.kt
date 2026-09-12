package com.facetlauncher.app.data.model

/** One row from [android.provider.CalendarContract.Calendars]. */
data class CalendarInfo(
    val id: String,
    val displayName: String,
    val accountName: String,
)

/** One instance from [android.provider.CalendarContract.Instances], scoped to today. */
data class CalendarEvent(
    val id: Long,
    val calendarId: String,
    val title: String,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val isAllDay: Boolean,
)
