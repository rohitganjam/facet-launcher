package com.facetlauncher.app.ui.drawer

import android.os.UserHandle
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.ContactInfo
import com.facetlauncher.app.data.model.SettingsSearchEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.Mockito.mock

class TopSearchResultTest {

    private val app = AppInfo(packageName = "com.example.chat", activityName = ".Main", label = "Chat", icon = null, userHandle = mock(UserHandle::class.java))
    private val contact = ContactInfo(id = "1", displayName = "Mom", phoneNumber = "")
    private val setting = SettingsSearchEntry(id = "wifi", label = "Wi-Fi", action = "android.settings.WIFI_SETTINGS")

    @Test
    fun `an app match wins over a contact or a setting`() {
        // Given a query that matches an app, a contact and a setting
        val top = topSearchResult("m", listOf(app), listOf(contact), listOf(setting))

        // Then Enter opens the app, which is shown first
        assertEquals(TopSearchResult.App(app), top)
    }

    @Test
    fun `with no app the first contact is opened`() {
        // Given a query that matches a contact and a setting but no app
        val top = topSearchResult("mom", emptyList(), listOf(contact), listOf(setting))

        // Then Enter opens the contact
        assertEquals(TopSearchResult.Contact(contact), top)
    }

    @Test
    fun `with no app or contact the first setting is opened`() {
        // Given a query that matches only a setting
        val top = topSearchResult("wifi", emptyList(), emptyList(), listOf(setting))

        // Then Enter opens the setting
        assertEquals(TopSearchResult.Setting(setting), top)
    }

    @Test
    fun `a blank query or no match opens nothing`() {
        // Given results while the query is blank, and a query with no results at all
        // Then Enter opens nothing in both cases
        assertNull(topSearchResult("  ", listOf(app), listOf(contact), listOf(setting)))
        assertNull(topSearchResult("zzz", emptyList(), emptyList(), emptyList()))
    }
}
