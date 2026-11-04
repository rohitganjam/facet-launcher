package com.facetlauncher.app.data

import android.content.ContentResolver
import android.content.ContentUris
import android.net.Uri
import android.provider.CalendarContract
import com.facetlauncher.app.data.di.IoDispatcher
import com.facetlauncher.app.data.model.CalendarEvent
import com.facetlauncher.app.data.model.CalendarInfo
import com.facetlauncher.app.data.model.WORK_CALENDAR_ID_PREFIX
import com.facetlauncher.app.data.model.selectsCalendar
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

    /** Every calendar the user can pick from: the personal ones, then the Work Profile's that its admin shares. */
    open suspend fun getSelectableCalendars(): List<CalendarInfo> = getCalendars() + getWorkCalendars()

    /**
     * The Work Profile's calendars, when its admin lets this app read them (`setCrossProfileCalendarPackages`) and the user has turned on
     * showing work events: Android hides their names and accounts, so each is just an id, prefixed with [WORK_CALENDAR_ID_PREFIX], and
     * a colour. Empty when there is no Work Profile, no permission or no access.
     */
    open suspend fun getWorkCalendars(): List<CalendarInfo> = withContext(ioDispatcher) {
        runCatching {
            contentResolver.query(CalendarContract.Calendars.ENTERPRISE_CONTENT_URI, arrayOf(CalendarContract.Calendars._ID), null, null, null)?.use { cursor ->
                buildList {
                    while (cursor.moveToNext()) add(CalendarInfo(id = WORK_CALENDAR_ID_PREFIX + cursor.getLong(0), displayName = "", accountName = "", isWork = true))
                }
            }
        }.getOrNull().orEmpty()
    }

    /**
     * Events from local midnight today until noon tomorrow, ordered by start time. All-day events
     * count when dated today or tomorrow.
     * @param calendarIds restrict to these calendar ids; `null` means every personal calendar. A Work Profile calendar is read only
     * when its [WORK_CALENDAR_ID_PREFIX] id is listed.
     * @param includeAllDay whether all-day events are included at all.
     */
    open suspend fun getUpcomingEvents(calendarIds: Set<String>?, includeAllDay: Boolean): List<CalendarEvent> = withContext(ioDispatcher) {
        val (begin, end) = upcomingWindow()
        val today = LocalDate.now()
        val personal = queryInstances(CalendarContract.Instances.CONTENT_URI, begin, end, "", today, includeAllDay) { calendarIds.selectsCalendar(it) }
        val wanted = calendarIds.orEmpty().filter { it.startsWith(WORK_CALENDAR_ID_PREFIX) }.toSet()
        val work = if (wanted.isEmpty()) emptyList() else runCatching {
            queryInstances(CalendarContract.Instances.ENTERPRISE_CONTENT_URI, begin, end, WORK_CALENDAR_ID_PREFIX, today, includeAllDay) { it in wanted }
        }.getOrDefault(emptyList())
        (personal + work).sortedBy { it.startTimeMillis }
    }

    /** One provider's event instances in [begin]..[end], their calendar ids prefixed with [idPrefix] and kept when [accept] says so. */
    private fun queryInstances(
        baseUri: Uri,
        begin: Long,
        end: Long,
        idPrefix: String,
        today: LocalDate,
        includeAllDay: Boolean,
        accept: (calendarId: String) -> Boolean,
    ): List<CalendarEvent> {
        val projection = arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.CALENDAR_ID,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.ALL_DAY,
            CalendarContract.Instances.EVENT_LOCATION,
        )
        val uri = baseUri.buildUpon().also { ContentUris.appendId(it, begin) }.also { ContentUris.appendId(it, end) }.build()
        return contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            val eventIdCol = cursor.getColumnIndexOrThrow(CalendarContract.Instances.EVENT_ID)
            val calendarIdCol = cursor.getColumnIndexOrThrow(CalendarContract.Instances.CALENDAR_ID)
            val titleCol = cursor.getColumnIndexOrThrow(CalendarContract.Instances.TITLE)
            val beginCol = cursor.getColumnIndexOrThrow(CalendarContract.Instances.BEGIN)
            val endCol = cursor.getColumnIndexOrThrow(CalendarContract.Instances.END)
            val allDayCol = cursor.getColumnIndexOrThrow(CalendarContract.Instances.ALL_DAY)
            val locationCol = cursor.getColumnIndexOrThrow(CalendarContract.Instances.EVENT_LOCATION)
            buildList {
                while (cursor.moveToNext()) {
                    val calendarId = idPrefix + cursor.getLong(calendarIdCol)
                    val isAllDay = cursor.getInt(allDayCol) != 0
                    val inAllDayWindow = !isAllDay || (includeAllDay && isOnTodayOrTomorrow(cursor.getLong(beginCol), today))
                    if (!accept(calendarId) || !inAllDayWindow) continue
                    add(
                        CalendarEvent(
                            id = cursor.getLong(eventIdCol),
                            calendarId = calendarId,
                            title = cursor.getString(titleCol).orEmpty(),
                            startTimeMillis = cursor.getLong(beginCol),
                            endTimeMillis = cursor.getLong(endCol),
                            isAllDay = isAllDay,
                            location = cursor.getString(locationCol).orEmpty(),
                        ),
                    )
                }
            }
        }.orEmpty()
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
