package com.facetlauncher.app.ui.launcher

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Process
import android.os.UserHandle
import android.os.UserManager
import android.provider.CalendarContract
import com.facetlauncher.app.data.model.CalendarEvent
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.eq
import org.mockito.ArgumentMatchers.isNull
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CalendarEventOpenerTest {

    private val context = mock(Context::class.java)
    private val launcherApps = mock(LauncherApps::class.java)
    private val userManager = mock(UserManager::class.java)
    private val packageManager = mock(PackageManager::class.java)
    private val personal = Process.myUserHandle()
    private val work: UserHandle = mock(UserHandle::class.java)
    private val calendarComponent = ComponentName("com.example.calendar", "com.example.calendar.Main")

    private fun event(calendarId: String, id: Long = 42) =
        CalendarEvent(id = id, calendarId = calendarId, title = "Standup", startTimeMillis = 0, endTimeMillis = 1, isAllDay = false)

    private fun setUp(profiles: List<UserHandle>, workHasCalendar: Boolean = true, calendarPackage: String? = "com.example.calendar") {
        `when`(context.getSystemService(LauncherApps::class.java)).thenReturn(launcherApps)
        `when`(context.getSystemService(UserManager::class.java)).thenReturn(userManager)
        `when`(context.packageManager).thenReturn(packageManager)
        `when`(userManager.userProfiles).thenReturn(profiles)
        val resolved = calendarPackage?.let { ResolveInfo().apply { activityInfo = ActivityInfo().apply { packageName = it } } }
        `when`(packageManager.resolveActivity(any(Intent::class.java), anyInt())).thenReturn(resolved)
        val info = mock(LauncherActivityInfo::class.java)
        `when`(info.componentName).thenReturn(calendarComponent)
        `when`(launcherApps.getActivityList("com.example.calendar", work)).thenReturn(if (workHasCalendar) listOf(info) else emptyList())
    }

    @Test
    fun `a personal event opens that event in the personal calendar`() {
        // Given a personal event
        setUp(listOf(personal, work))

        // When it is tapped
        openCalendarEvent(context, event("3", id = 42))

        // Then the personal Calendar app is asked to view that event
        val intent = ArgumentCaptor.forClass(Intent::class.java)
        verify(context).startActivity(intent.capture())
        assertEquals(Intent.ACTION_VIEW, intent.value.action)
        assertEquals("${CalendarContract.Events.CONTENT_URI}/42", intent.value.data.toString())
        verify(launcherApps, never()).startMainActivity(any(), any(), any(), any())
    }

    @Test
    fun `a work event opens the work profile's calendar app and never the personal one`() {
        // Given a work event and a calendar app installed in the work profile
        setUp(listOf(personal, work))

        // When it is tapped
        openCalendarEvent(context, event("work:1"))

        // Then the work copy of the app is launched in the work profile, not a personal view intent
        verify(launcherApps).startMainActivity(eq(calendarComponent), eq(work), isNull(), isNull())
        verify(context, never()).startActivity(any(Intent::class.java))
    }

    @Test
    fun `a work event does nothing when the work profile has no calendar app`() {
        // Given a work profile without the calendar app
        setUp(listOf(personal, work), workHasCalendar = false)

        // When a work event is tapped
        openCalendarEvent(context, event("work:1"))

        // Then nothing launches and nothing crashes
        verify(launcherApps, never()).startMainActivity(any(), any(), any(), any())
        verify(context, never()).startActivity(any(Intent::class.java))
    }

    @Test
    fun `a work event does nothing when there is no work profile`() {
        // Given only the personal profile
        setUp(listOf(personal))

        // When a work event is tapped
        openCalendarEvent(context, event("work:1"))

        // Then nothing launches
        verify(launcherApps, never()).startMainActivity(any(), any(), any(), any())
        verify(context, never()).startActivity(any(Intent::class.java))
    }

    @Test
    fun `a work event does nothing when no default calendar app resolves`() {
        // Given the device resolving no calendar app at all
        setUp(listOf(personal, work), calendarPackage = null)

        // When a work event is tapped
        openCalendarEvent(context, event("work:1"))

        // Then nothing launches
        verify(launcherApps, never()).startMainActivity(any(), any(), any(), any())
    }

    @Test
    fun `a personal event whose app cannot be started does not crash`() {
        // Given no app to view the event
        setUp(listOf(personal, work))
        `when`(context.startActivity(any(Intent::class.java))).thenThrow(android.content.ActivityNotFoundException())

        // When it is tapped, then it is swallowed rather than thrown
        openCalendarEvent(context, event("3"))
    }
}
