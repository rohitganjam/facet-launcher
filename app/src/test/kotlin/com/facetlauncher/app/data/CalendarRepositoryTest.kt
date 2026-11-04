package com.facetlauncher.app.data

import android.content.ContentResolver
import android.database.MatrixCursor
import android.net.Uri
import android.provider.CalendarContract
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Calendar
import java.util.TimeZone
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.ArgumentMatchers.isNull
import org.mockito.ArgumentMatchers.nullable
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner

/**
 * [CalendarContract.Instances.query] is real framework code that builds its own URI internally
 * (no stable public contract for exactly what it looks like) before delegating to
 * [ContentResolver.query] — so the mocked resolver matches on the query's other args
 * ([Any] for the URI) and returns a [MatrixCursor] built with the exact columns the repository
 * requests, rather than trying to predict/replicate that internal URI.
 */
@RunWith(RobolectricTestRunner::class)
class CalendarRepositoryTest {

    private val contentResolver = mock(ContentResolver::class.java)
    private val repository = CalendarRepository(contentResolver)

    private fun calendarsCursor(rows: List<Triple<Long, String, String>>): MatrixCursor {
        val cursor = MatrixCursor(
            arrayOf(CalendarContract.Calendars._ID, CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, CalendarContract.Calendars.ACCOUNT_NAME),
        )
        rows.forEach { (id, name, account) -> cursor.addRow(arrayOf<Any>(id, name, account)) }
        return cursor
    }

    private fun instancesCursor(rows: List<InstanceRow>): MatrixCursor {
        val cursor = MatrixCursor(
            arrayOf(
                CalendarContract.Instances.EVENT_ID,
                CalendarContract.Instances.CALENDAR_ID,
                CalendarContract.Instances.TITLE,
                CalendarContract.Instances.BEGIN,
                CalendarContract.Instances.END,
                CalendarContract.Instances.ALL_DAY,
                CalendarContract.Instances.EVENT_LOCATION,
            ),
        )
        rows.forEach { row ->
            cursor.addRow(arrayOf<Any?>(row.eventId, row.calendarId, row.title, row.beginMillis, row.endMillis, if (row.allDay) 1 else 0, row.location))
        }
        return cursor
    }

    private data class InstanceRow(
        val eventId: Long,
        val calendarId: Long,
        val title: String,
        val beginMillis: Long,
        val endMillis: Long = beginMillis + 1_000L,
        val allDay: Boolean,
        val location: String? = null,
    )

    private fun stubInstances(rows: List<InstanceRow>) {
        // CalendarContract.Instances.query() builds its own URI/selection internally before
        // delegating to ContentResolver.query() — match loosely on everything but the URI/projection.
        `when`(
            contentResolver.query(
                any(Uri::class.java),
                any(Array<String>::class.java),
                nullable(String::class.java),
                nullable(Array<String>::class.java),
                nullable(String::class.java),
            ),
        ).thenReturn(instancesCursor(rows))
    }

    @Test
    fun `getCalendars returns every row from the provider`() = runTest {
        // Given two calendars in the provider
        `when`(
            contentResolver.query(eq(CalendarContract.Calendars.CONTENT_URI), any(Array<String>::class.java), isNull(), isNull(), isNull()),
        ).thenReturn(calendarsCursor(listOf(Triple(1L, "Work", "work@example.com"), Triple(2L, "Personal", "personal@example.com"))))

        // When fetching the calendar list
        val result = repository.getCalendars()

        // Then both come back, ids stringified
        assertEquals(listOf("1", "2"), result.map { it.id })
        assertEquals(listOf("Work", "Personal"), result.map { it.displayName })
        assertEquals(listOf("work@example.com", "personal@example.com"), result.map { it.accountName })
    }

    @Test
    fun `getUpcomingEvents returns every instance, sorted by start time`() = runTest {
        // Given two instances out of chronological order
        stubInstances(
            listOf(
                InstanceRow(eventId = 2, calendarId = 1, title = "Later", beginMillis = 2_000L, endMillis = 2_500L, allDay = false),
                InstanceRow(eventId = 1, calendarId = 1, title = "Earlier", beginMillis = 1_000L, endMillis = 1_500L, allDay = false),
            ),
        )

        // When fetching today's events, unfiltered
        val result = repository.getUpcomingEvents(calendarIds = null, includeAllDay = true)

        // Then they come back ordered by start time, end time carried through unchanged
        assertEquals(listOf("Earlier", "Later"), result.map { it.title })
        assertEquals(listOf(1_500L, 2_500L), result.map { it.endTimeMillis })
    }

    @Test
    fun `getUpcomingEvents carries the location through, blank when there is none`() = runTest {
        // Given one event with a location and one without
        stubInstances(
            listOf(
                InstanceRow(eventId = 1, calendarId = 1, title = "Scrum", beginMillis = 1_000L, allDay = false, location = "BLR-Innovate-2-Marie Curie (4)"),
                InstanceRow(eventId = 2, calendarId = 1, title = "Demo", beginMillis = 2_000L, allDay = false, location = null),
            ),
        )

        // When fetching events
        val result = repository.getUpcomingEvents(calendarIds = null, includeAllDay = true)

        // Then the location is mapped and a missing one is empty
        assertEquals(listOf("BLR-Innovate-2-Marie Curie (4)", ""), result.map { it.location })
    }

    @Test
    fun `getUpcomingEvents excludes events from calendars not in the given set`() = runTest {
        // Given instances on two different calendars
        stubInstances(
            listOf(
                InstanceRow(eventId = 1, calendarId = 1, title = "Standup", beginMillis = 1_000L, allDay = false),
                InstanceRow(eventId = 2, calendarId = 2, title = "Dentist", beginMillis = 1_000L, allDay = false),
            ),
        )

        // When fetching restricted to just calendar 1
        val result = repository.getUpcomingEvents(calendarIds = setOf("1"), includeAllDay = true)

        // Then only that calendar's event is returned
        assertEquals(listOf("Standup"), result.map { it.title })
    }

    @Test
    fun `getUpcomingEvents excludes all-day events when includeAllDay is false`() = runTest {
        // Given a timed event and an all-day event
        stubInstances(
            listOf(
                InstanceRow(eventId = 1, calendarId = 1, title = "Standup", beginMillis = 1_000L, allDay = false),
                InstanceRow(eventId = 2, calendarId = 1, title = "Company holiday", beginMillis = 1_000L, allDay = true),
            ),
        )

        // When fetching with includeAllDay = false
        val result = repository.getUpcomingEvents(calendarIds = null, includeAllDay = false)

        // Then only the timed event is returned
        assertEquals(listOf("Standup"), result.map { it.title })
    }

    @Test
    fun `getUpcomingEvents excludes an all-day event whose real UTC date is yesterday`() = runTest {
        // Given a device timezone ahead of UTC (IST, +5:30), where the Instances query's
        // local-midnight window overlaps into yesterday's UTC-anchored all-day event
        val originalTimeZone = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"))
        try {
            val today = LocalDate.now()
            val yesterdayAllDayStart = today.minusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
            val todayAllDayStart = today.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
            stubInstances(
                listOf(
                    InstanceRow(eventId = 1, calendarId = 1, title = "Stale holiday", beginMillis = yesterdayAllDayStart, allDay = true),
                    InstanceRow(eventId = 2, calendarId = 1, title = "Today holiday", beginMillis = todayAllDayStart, allDay = true),
                ),
            )

            // When fetching today's events
            val result = repository.getUpcomingEvents(calendarIds = null, includeAllDay = true)

            // Then only the all-day event actually dated today survives
            assertEquals(listOf("Today holiday"), result.map { it.title })
        } finally {
            TimeZone.setDefault(originalTimeZone)
        }
    }

    @Test
    fun `getUpcomingEvents queries from local midnight today until noon tomorrow`() = runTest {
        // Given an Instances query that returns nothing
        val uri = org.mockito.ArgumentCaptor.forClass(Uri::class.java)
        `when`(
            contentResolver.query(
                uri.capture() ?: Uri.EMPTY,
                any(Array<String>::class.java),
                nullable(String::class.java),
                nullable(Array<String>::class.java),
                nullable(String::class.java),
            ),
        ).thenReturn(instancesCursor(emptyList()))

        // When fetching upcoming events
        repository.getUpcomingEvents(calendarIds = null, includeAllDay = true)

        // Then the window runs from 00:00 today to 12:00 tomorrow in the device time zone
        val segments = uri.value.pathSegments
        val begin = Calendar.getInstance().apply { timeInMillis = segments[segments.size - 2].toLong() }
        val end = Calendar.getInstance().apply { timeInMillis = segments.last().toLong() }
        val today = Calendar.getInstance()
        assertEquals(listOf(0, 0, 0), listOf(begin.get(Calendar.HOUR_OF_DAY), begin.get(Calendar.MINUTE), begin.get(Calendar.SECOND)))
        assertEquals(today.get(Calendar.DAY_OF_YEAR), begin.get(Calendar.DAY_OF_YEAR))
        assertEquals(listOf(12, 0), listOf(end.get(Calendar.HOUR_OF_DAY), end.get(Calendar.MINUTE)))
        val tomorrow = (begin.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 1) }
        assertEquals(tomorrow.get(Calendar.DAY_OF_YEAR), end.get(Calendar.DAY_OF_YEAR))
    }

    @Test
    fun `getUpcomingEvents keeps tomorrow's all-day event but drops the day after`() = runTest {
        // Given all-day events dated yesterday, today, tomorrow and the day after (UTC-anchored)
        val today = LocalDate.now()
        fun allDay(id: Long, title: String, date: LocalDate) =
            InstanceRow(eventId = id, calendarId = 1, title = title, beginMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(), allDay = true)
        stubInstances(
            listOf(
                allDay(1, "Yesterday", today.minusDays(1)),
                allDay(2, "Today", today),
                allDay(3, "Tomorrow", today.plusDays(1)),
                allDay(4, "Day after", today.plusDays(2)),
            ),
        )

        // When fetching upcoming events
        val result = repository.getUpcomingEvents(calendarIds = null, includeAllDay = true)

        // Then only today's and tomorrow's all-day events remain
        assertEquals(listOf("Today", "Tomorrow"), result.map { it.title })
    }

    @Test
    fun `getUpcomingEvents drops tomorrow's all-day event when all-day events are off`() = runTest {
        // Given an all-day event dated tomorrow
        val tomorrow = LocalDate.now().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        stubInstances(listOf(InstanceRow(eventId = 1, calendarId = 1, title = "Tomorrow", beginMillis = tomorrow, allDay = true)))

        // When all-day events are excluded
        val result = repository.getUpcomingEvents(calendarIds = null, includeAllDay = false)

        // Then it is dropped
        assertEquals(emptyList<String>(), result.map { it.title })
    }

    @Test
    fun `getUpcomingEvents returns an empty list when the query returns null`() = runTest {
        // Given a resolver that returns null (e.g. a transient provider failure)
        `when`(
            contentResolver.query(
                any(Uri::class.java),
                any(Array<String>::class.java),
                nullable(String::class.java),
                nullable(Array<String>::class.java),
                nullable(String::class.java),
            ),
        ).thenReturn(null)

        // When fetching today's events
        val result = repository.getUpcomingEvents(calendarIds = null, includeAllDay = true)

        // Then it degrades to an empty list rather than throwing
        assertEquals(emptyList<String>(), result.map { it.title })
    }

    private fun isEnterprise(uri: Uri) = uri.toString().contains("/enterprise/")

    private fun stubPersonalAndWorkInstances(personal: List<InstanceRow>, work: List<InstanceRow>) {
        `when`(
            contentResolver.query(
                org.mockito.ArgumentMatchers.argThat<Uri> { it != null && !isEnterprise(it) },
                any(Array<String>::class.java),
                nullable(String::class.java),
                nullable(Array<String>::class.java),
                nullable(String::class.java),
            ),
        ).thenAnswer { instancesCursor(personal) }
        `when`(
            contentResolver.query(
                org.mockito.ArgumentMatchers.argThat<Uri> { it != null && isEnterprise(it) },
                any(Array<String>::class.java),
                nullable(String::class.java),
                nullable(Array<String>::class.java),
                nullable(String::class.java),
            ),
        ).thenAnswer { instancesCursor(work) }
    }

    @Test
    fun `getWorkCalendars lists the work profile's calendars with prefixed ids`() = runTest {
        // Given the cross-profile provider exposing two calendars
        val cursor = MatrixCursor(arrayOf(CalendarContract.Calendars._ID))
        cursor.addRow(arrayOf<Any>(1L))
        cursor.addRow(arrayOf<Any>(4L))
        `when`(
            contentResolver.query(eq(CalendarContract.Calendars.ENTERPRISE_CONTENT_URI), any(Array<String>::class.java), isNull(), isNull(), isNull()),
        ).thenReturn(cursor)

        // When listing them
        val result = repository.getWorkCalendars()

        // Then each is marked as work and its id cannot meet a personal calendar's
        assertEquals(listOf("work:1", "work:4"), result.map { it.id })
        assertEquals(listOf(true, true), result.map { it.isWork })
    }

    @Test
    fun `getWorkCalendars carries a few distinct upcoming event titles per calendar`() = runTest {
        // Given two work calendars, the first with a repeated title, a blank one and four others
        val calendars = MatrixCursor(arrayOf(CalendarContract.Calendars._ID))
        calendars.addRow(arrayOf<Any>(1L))
        calendars.addRow(arrayOf<Any>(2L))
        val rows = listOf(
            InstanceRow(eventId = 1, calendarId = 1, title = "Standup", beginMillis = 1_000L, allDay = false),
            InstanceRow(eventId = 2, calendarId = 1, title = "Standup", beginMillis = 2_000L, allDay = false),
            InstanceRow(eventId = 3, calendarId = 1, title = " ", beginMillis = 3_000L, allDay = false),
            InstanceRow(eventId = 4, calendarId = 1, title = "Design review", beginMillis = 4_000L, allDay = false),
            InstanceRow(eventId = 5, calendarId = 1, title = "1:1", beginMillis = 5_000L, allDay = false),
            InstanceRow(eventId = 6, calendarId = 1, title = "Retro", beginMillis = 6_000L, allDay = false),
            InstanceRow(eventId = 7, calendarId = 2, title = "Dentist", beginMillis = 7_000L, allDay = false),
        )
        `when`(
            contentResolver.query(
                any(Uri::class.java),
                any(Array<String>::class.java), nullable(String::class.java), nullable(Array<String>::class.java), nullable(String::class.java),
            ),
        ).thenAnswer { instancesCursor(rows) }
        // The calendar list is stubbed last, so it wins for its own URI over the catch-all above
        `when`(
            contentResolver.query(eq(CalendarContract.Calendars.ENTERPRISE_CONTENT_URI), any(Array<String>::class.java), isNull(), isNull(), isNull()),
        ).thenReturn(calendars)

        // When listing them
        val result = repository.getWorkCalendars()

        // Then each carries at most three distinct, non-blank titles in start order
        assertEquals(listOf("Standup", "Design review", "1:1"), result[0].sampleTitles)
        assertEquals(listOf("Dentist"), result[1].sampleTitles)
    }

    @Test
    fun `getWorkCalendars is empty when the work profile does not share its calendars`() = runTest {
        // Given the provider refusing cross-profile access
        `when`(
            contentResolver.query(eq(CalendarContract.Calendars.ENTERPRISE_CONTENT_URI), any(Array<String>::class.java), isNull(), isNull(), isNull()),
        ).thenThrow(SecurityException("not allowed"))

        // Then there are no work calendars rather than a crash
        assertEquals(emptyList<String>(), repository.getWorkCalendars().map { it.id })
    }

    @Test
    fun `getUpcomingEvents reads a work calendar only when it is listed`() = runTest {
        // Given a personal event and a work event
        stubPersonalAndWorkInstances(
            personal = listOf(InstanceRow(eventId = 1, calendarId = 1, title = "Personal", beginMillis = 1_000L, allDay = false)),
            work = listOf(InstanceRow(eventId = 9, calendarId = 1, title = "Standup", beginMillis = 2_000L, allDay = false)),
        )

        // When the work calendar is listed, then when no set is given
        val withWork = repository.getUpcomingEvents(calendarIds = setOf("1", "work:1"), includeAllDay = true)
        val everyPersonal = repository.getUpcomingEvents(calendarIds = null, includeAllDay = true)

        // Then the work event appears only in the first, under a prefixed calendar id that is not the personal calendar 1
        assertEquals(listOf("Personal", "Standup"), withWork.map { it.title })
        assertEquals(listOf("1", "work:1"), withWork.map { it.calendarId })
        assertEquals(listOf("Personal"), everyPersonal.map { it.title })
    }

    @Test
    fun `observeCalendarChanges emits now and again each time the provider notifies`() = runTest {
        // Given the observer the repository registers on the calendar provider
        val registered = org.mockito.ArgumentCaptor.forClass(android.database.ContentObserver::class.java)
        val emissions = mutableListOf<Unit>()
        val job = launch(kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler)) { repository.observeCalendarChanges().collect { emissions += it } }
        org.mockito.Mockito.verify(contentResolver).registerContentObserver(eq(CalendarContract.Calendars.CONTENT_URI), eq(true), registered.capture())
        assertEquals(1, emissions.size)

        // When a calendar is added on the phone
        registered.value.onChange(false)
        org.robolectric.shadows.ShadowLooper.idleMainLooper()

        // Then it emits again, and stops observing once collection ends
        assertEquals(2, emissions.size)
        job.cancel()
        org.mockito.Mockito.verify(contentResolver).unregisterContentObserver(registered.value)
    }

    @Test
    fun `observeCalendarChanges still observes the personal calendars when the work profile cannot be observed`() = runTest {
        // Given the cross-profile provider refusing an observer
        `when`(contentResolver.registerContentObserver(eq(CalendarContract.Calendars.ENTERPRISE_CONTENT_URI), eq(true), any(android.database.ContentObserver::class.java)))
            .thenThrow(SecurityException("not allowed"))
        val emissions = mutableListOf<Unit>()

        // When observing
        val job = launch(kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler)) { repository.observeCalendarChanges().collect { emissions += it } }

        // Then the personal provider is observed and it still emits
        org.mockito.Mockito.verify(contentResolver).registerContentObserver(eq(CalendarContract.Calendars.CONTENT_URI), eq(true), any(android.database.ContentObserver::class.java))
        assertEquals(1, emissions.size)
        job.cancel()
    }
}
