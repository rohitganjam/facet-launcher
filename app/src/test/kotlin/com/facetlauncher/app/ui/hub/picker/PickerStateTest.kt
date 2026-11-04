package com.facetlauncher.app.ui.hub.picker

import android.content.ComponentName
import com.facetlauncher.app.data.model.AppProfile
import com.facetlauncher.app.data.model.WidgetProviderOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PickerStateTest {

    private fun option(app: String, profile: AppProfile = AppProfile.PERSONAL) =
        WidgetProviderOption(ComponentName("pkg.$app", ".W"), app, "Widget", 2, 2, profile = profile)

    private val personalClock = option("Clock")
    private val workCalendar = option("Calendar", AppProfile.WORK)
    private val personalCalendar = option("Calendar")

    @Test
    fun `with no work widgets there are no tabs and everything lists`() {
        // Given only personal widgets
        val state = pickerState(listOf(personalClock, personalCalendar), query = "", tab = WidgetPickerTab.PERSONAL, remaining = 3)

        // Then there is no tab row and both apps list
        assertTrue(state.tabs.isEmpty())
        assertEquals(listOf("Calendar", "Clock"), state.groups.map { it.appLabel })
    }

    @Test
    fun `personal and work widgets are kept on their own tabs`() {
        // Given a personal and a work copy of Calendar, and a personal Clock
        val options = listOf(personalClock, personalCalendar, workCalendar)

        // When each tab is shown
        val personal = pickerState(options, "", WidgetPickerTab.PERSONAL, 3)
        val work = pickerState(options, "", WidgetPickerTab.WORK, 3)

        // Then Personal lists its own two apps, Work only the work one, and both offer the two tabs
        assertEquals(listOf(WidgetPickerTab.PERSONAL, WidgetPickerTab.WORK), personal.tabs)
        assertEquals(listOf("Calendar", "Clock"), personal.groups.map { it.appLabel })
        assertEquals(listOf(AppProfile.PERSONAL), personal.groups.first().options.map { it.profile })
        assertEquals(listOf("Calendar"), work.groups.map { it.appLabel })
        assertEquals(listOf(AppProfile.WORK), work.groups.single().options.map { it.profile })
    }

    @Test
    fun `search only looks within the shown tab`() {
        // Given both tabs have a Calendar and only Personal has a Clock
        val options = listOf(personalClock, personalCalendar, workCalendar)

        // When searching "clock" on the Work tab
        val state = pickerState(options, "clock", WidgetPickerTab.WORK, 3)

        // Then nothing from the Personal tab leaks in
        assertTrue(state.groups.isEmpty())
    }

    @Test
    fun `a work tab that is not offered falls back to Personal`() {
        // Given only personal widgets but a stale Work selection
        val state = pickerState(listOf(personalClock), "", WidgetPickerTab.WORK, null)

        // Then Personal shows
        assertEquals(WidgetPickerTab.PERSONAL, state.tab)
        assertEquals(listOf("Clock"), state.groups.map { it.appLabel })
    }
}
