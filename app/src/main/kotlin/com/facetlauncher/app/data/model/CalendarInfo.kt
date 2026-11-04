package com.facetlauncher.app.data.model

/** One row from [android.provider.CalendarContract.Calendars]. */
data class CalendarInfo(
    val id: String,
    val displayName: String,
    val accountName: String,
    /** A Work Profile calendar read through Android's cross-profile (enterprise) calendar API, which shows no name or account. */
    val isWork: Boolean = false,
)

/** The id prefix of a Work Profile calendar or event, so its ids never meet a personal calendar's. */
const val WORK_CALENDAR_ID_PREFIX = "work:"

/**
 * Whether the calendar [id] is selected by this set of chosen ids, the one rule the launcher's Calendar settings, an Agenda widget's
 * settings and the event query all share: `null` means every personal calendar, and a Work Profile calendar is only ever selected when listed.
 */
fun Set<String>?.selectsCalendar(id: String): Boolean =
    if (id.startsWith(WORK_CALENDAR_ID_PREFIX)) this?.contains(id) == true else this?.contains(id) ?: true

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
