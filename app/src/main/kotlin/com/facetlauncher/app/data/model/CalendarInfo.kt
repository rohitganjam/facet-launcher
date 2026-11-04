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
    val location: String = "",
) {
    /** The location shortened for a narrow line: Workspace rooms read "BLR-Tower-2-Marie Curie (4)", so keep the part after the last dash. */
    val roomLabel: String
        get() {
            val text = location.trim()
            return if (text.count { it == '-' } >= ROOM_PATH_DASHES) text.substringAfterLast('-').trim().ifEmpty { text } else text
        }

    private companion object {
        const val ROOM_PATH_DASHES = 3
    }
}
