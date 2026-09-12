package com.facetlauncher.app.data

import android.content.ContentResolver
import android.provider.CalendarContract
import com.facetlauncher.app.data.model.CalendarEvent
import com.facetlauncher.app.data.model.CalendarInfo
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Wraps the system Calendar Provider (`READ_CALENDAR`) for F1's calendar list and today's events. */
@Singleton
open class CalendarRepository @Inject constructor(
    private val contentResolver: ContentResolver,
) {

    /** Every calendar visible to this device's Calendar Provider, for the "Calendars to display" picker. */
    open suspend fun getCalendars(): List<CalendarInfo> = withContext(Dispatchers.IO) {
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.ACCOUNT_NAME,
        )
        contentResolver.query(CalendarContract.Calendars.CONTENT_URI, projection, null, null, null)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(CalendarContract.Calendars._ID)
            val nameCol = cursor.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)
            val accountCol = cursor.getColumnIndexOrThrow(CalendarContract.Calendars.ACCOUNT_NAME)
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        CalendarInfo(
                            id = cursor.getLong(idCol).toString(),
                            displayName = cursor.getString(nameCol).orEmpty(),
                            accountName = cursor.getString(accountCol).orEmpty(),
                        ),
                    )
                }
            }
        }.orEmpty()
    }

    /**
     * Today's events (device-local day boundaries), ordered by start time.
     * @param calendarIds restrict to these calendar ids; `null` means every calendar.
     * @param includeAllDay whether all-day events are included at all.
     */
    suspend fun getTodayEvents(calendarIds: Set<String>?, includeAllDay: Boolean): List<CalendarEvent> = withContext(Dispatchers.IO) {
        val begin = startOfToday()
        val end = begin + DAY_MILLIS - 1

        val projection = arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.CALENDAR_ID,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.ALL_DAY,
        )
        val events = CalendarContract.Instances.query(contentResolver, projection, begin, end)?.use { cursor ->
            val eventIdCol = cursor.getColumnIndexOrThrow(CalendarContract.Instances.EVENT_ID)
            val calendarIdCol = cursor.getColumnIndexOrThrow(CalendarContract.Instances.CALENDAR_ID)
            val titleCol = cursor.getColumnIndexOrThrow(CalendarContract.Instances.TITLE)
            val beginCol = cursor.getColumnIndexOrThrow(CalendarContract.Instances.BEGIN)
            val endCol = cursor.getColumnIndexOrThrow(CalendarContract.Instances.END)
            val allDayCol = cursor.getColumnIndexOrThrow(CalendarContract.Instances.ALL_DAY)
            buildList {
                while (cursor.moveToNext()) {
                    val calendarId = cursor.getLong(calendarIdCol).toString()
                    val isAllDay = cursor.getInt(allDayCol) != 0
                    if (calendarIds != null && calendarId !in calendarIds) continue
                    if (isAllDay && !includeAllDay) continue
                    add(
                        CalendarEvent(
                            id = cursor.getLong(eventIdCol),
                            calendarId = calendarId,
                            title = cursor.getString(titleCol).orEmpty(),
                            startTimeMillis = cursor.getLong(beginCol),
                            endTimeMillis = cursor.getLong(endCol),
                            isAllDay = isAllDay,
                        ),
                    )
                }
            }
        }.orEmpty()
        events.sortedBy { it.startTimeMillis }
    }

    private fun startOfToday(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private companion object {
        const val DAY_MILLIS = 24 * 60 * 60 * 1000L
    }
}
