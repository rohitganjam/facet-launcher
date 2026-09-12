package com.facetlauncher.app.data

import android.content.ContentResolver
import android.database.MatrixCursor
import android.net.Uri
import android.provider.CalendarContract
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
            ),
        )
        rows.forEach { row ->
            cursor.addRow(arrayOf<Any>(row.eventId, row.calendarId, row.title, row.beginMillis, row.endMillis, if (row.allDay) 1 else 0))
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
    fun `getTodayEvents returns every instance, sorted by start time`() = runTest {
        // Given two instances out of chronological order
        stubInstances(
            listOf(
                InstanceRow(eventId = 2, calendarId = 1, title = "Later", beginMillis = 2_000L, endMillis = 2_500L, allDay = false),
                InstanceRow(eventId = 1, calendarId = 1, title = "Earlier", beginMillis = 1_000L, endMillis = 1_500L, allDay = false),
            ),
        )

        // When fetching today's events, unfiltered
        val result = repository.getTodayEvents(calendarIds = null, includeAllDay = true)

        // Then they come back ordered by start time, end time carried through unchanged
        assertEquals(listOf("Earlier", "Later"), result.map { it.title })
        assertEquals(listOf(1_500L, 2_500L), result.map { it.endTimeMillis })
    }

    @Test
    fun `getTodayEvents excludes events from calendars not in the given set`() = runTest {
        // Given instances on two different calendars
        stubInstances(
            listOf(
                InstanceRow(eventId = 1, calendarId = 1, title = "Standup", beginMillis = 1_000L, allDay = false),
                InstanceRow(eventId = 2, calendarId = 2, title = "Dentist", beginMillis = 1_000L, allDay = false),
            ),
        )

        // When fetching restricted to just calendar 1
        val result = repository.getTodayEvents(calendarIds = setOf("1"), includeAllDay = true)

        // Then only that calendar's event is returned
        assertEquals(listOf("Standup"), result.map { it.title })
    }

    @Test
    fun `getTodayEvents excludes all-day events when includeAllDay is false`() = runTest {
        // Given a timed event and an all-day event
        stubInstances(
            listOf(
                InstanceRow(eventId = 1, calendarId = 1, title = "Standup", beginMillis = 1_000L, allDay = false),
                InstanceRow(eventId = 2, calendarId = 1, title = "Company holiday", beginMillis = 1_000L, allDay = true),
            ),
        )

        // When fetching with includeAllDay = false
        val result = repository.getTodayEvents(calendarIds = null, includeAllDay = false)

        // Then only the timed event is returned
        assertEquals(listOf("Standup"), result.map { it.title })
    }

    @Test
    fun `getTodayEvents returns an empty list when the query returns null`() = runTest {
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
        val result = repository.getTodayEvents(calendarIds = null, includeAllDay = true)

        // Then it degrades to an empty list rather than throwing
        assertEquals(emptyList<String>(), result.map { it.title })
    }
}
