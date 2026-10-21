package com.facetlauncher.app.data

import android.content.ContentResolver
import android.provider.CalendarContract
import com.facetlauncher.app.data.di.IoDispatcher
import com.facetlauncher.app.data.model.CalendarEvent
import com.facetlauncher.app.data.model.CalendarInfo
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Wraps the system Calendar Provider (`READ_CALENDAR`) for F1's calendar list and today's events. */
@Singleton
open class CalendarRepository @Inject constructor(
    private val contentResolver: ContentResolver,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    /** Every calendar visible to this device's Calendar Provider, for the "Calendars to display" picker. */
    open suspend fun getCalendars(): List<CalendarInfo> = withContext(ioDispatcher) {
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
     * Events from local midnight today until noon tomorrow, ordered by start time. All-day events
     * count when dated today or tomorrow.
     * @param calendarIds restrict to these calendar ids; `null` means every calendar.
     * @param includeAllDay whether all-day events are included at all.
     */
    open suspend fun getUpcomingEvents(calendarIds: Set<String>?, includeAllDay: Boolean): List<CalendarEvent> = withContext(ioDispatcher) {
        val (begin, end) = upcomingWindow()
        val today = LocalDate.now()

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
                    if (isAllDay && !isOnTodayOrTomorrow(cursor.getLong(beginCol), today)) continue
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

    /**
     * All-day events are stored as UTC-midnight boundaries independent of device timezone, so
     * [CalendarContract.Instances.query]'s overlap check against a *local* begin/end window can
     * hand back an all-day event from the wrong side of the window whenever the device is ahead of
     * or behind UTC. Re-derive the event's actual date from its UTC-anchored start and compare it
     * against today's and tomorrow's local dates instead of trusting the query window alone.
     */
    private fun isOnTodayOrTomorrow(allDayStartMillis: Long, today: LocalDate): Boolean {
        val date = Instant.ofEpochMilli(allDayStartMillis).atZone(ZoneOffset.UTC).toLocalDate()
        return date == today || date == today.plusDays(1)
    }

    /** Local midnight today to noon tomorrow; calendar arithmetic keeps it right across DST changes. */
    private fun upcomingWindow(): Pair<Long, Long> {
        val begin = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val end = (begin.clone() as Calendar).apply {
            add(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 12)
        }
        return begin.timeInMillis to end.timeInMillis
    }
}
