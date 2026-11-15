package com.facetlauncher.app.domain

import android.os.UserHandle
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.SearchSignals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock

class RankBySearchRelevanceUseCaseTest {

    private val rank = RankBySearchRelevanceUseCase()

    private fun ranked(query: String, vararg labels: String): List<String> = rank(labels.toList(), query) { it }

    private val user: UserHandle = mock(UserHandle::class.java)

    private fun app(label: String) = AppInfo(packageName = "pkg.${label.lowercase()}", activityName = ".Main", label = label, icon = null, userHandle = user)

    @Test
    fun `a blank query returns the items unchanged`() {
        // Given labels in an arbitrary order
        // When the query is blank or only spaces
        // Then nothing is filtered or reordered
        assertEquals(listOf("b", "a"), ranked("", "b", "a"))
        assertEquals(listOf("b", "a"), ranked("   ", "b", "a"))
    }

    @Test
    fun `a query of only punctuation matches nothing`() {
        assertEquals(emptyList<String>(), ranked("--", "Wi-Fi", "Maps"))
    }

    @Test
    fun `tiers order exact then prefix then word start then initials then substring`() {
        // Given one label per tier for the query "ma"
        val labels = arrayOf("Formatter", "My Agenda", "Google Maps", "Mail", "Ma")

        // When ranked for "ma"
        val result = ranked("ma", *labels)

        // Then each tier comes before the next, whatever the input order
        assertEquals(listOf("Ma", "Mail", "Google Maps", "My Agenda", "Formatter"), result)
    }

    @Test
    fun `initials find a multi-word label`() {
        assertEquals(listOf("Google Maps"), ranked("gm", "Google Maps", "Game Center"))
    }

    @Test
    fun `a capital inside a word starts a new word`() {
        // "wa" is not a prefix of WhatsApp, but "app" is a word start and "wa" its initials
        assertEquals(listOf("WhatsApp"), ranked("wa", "WhatsApp", "Clock"))
        assertEquals(listOf("WhatsApp"), ranked("app", "WhatsApp", "Clock"))
    }

    @Test
    fun `accents and case are ignored on both sides`() {
        assertEquals(listOf("Café Finder"), ranked("cafe", "Café Finder", "Calendar"))
        assertEquals(listOf("Cafe Finder"), ranked("CAFÉ", "Cafe Finder", "Calendar"))
    }

    @Test
    fun `trailing or doubled spaces and punctuation don't change the match`() {
        assertEquals(listOf("Google Maps"), ranked("google  maps ", "Google Maps", "Google Drive"))
    }

    @Test
    fun `punctuation in the label doesn't block a match without it`() {
        // "wifi" matches "Wi-Fi" through its compact form, and ranks as exact
        assertEquals(listOf("Wi-Fi", "Wifi Analyzer"), ranked("wifi", "Wifi Analyzer", "Wi-Fi"))
    }

    @Test
    fun `a single letter matches only the start of the label or a word`() {
        // "m" is inside Camera but starts neither it nor any of its words
        assertEquals(listOf("Maps", "Google Messages"), ranked("m", "Camera", "Google Messages", "Maps"))
    }

    @Test
    fun `several query words match word starts in order`() {
        // Given "goo ma", which is in neither label as one piece
        // Then Google Maps matches, and Maps Google (words out of order) does not
        assertEquals(listOf("Google Maps"), ranked("goo ma", "Maps Google", "Google Maps"))
    }

    @Test
    fun `any key can match, and the best tier counts`() {
        // Given a setting whose label doesn't contain "internet" but whose keyword does
        val entries = listOf("Wi-Fi" to listOf("internet"), "Internet speed test" to emptyList())

        // When ranked on label and keywords
        val result = rank.rank(entries, "internet", keys = { listOf(it.first) + it.second }).map { it.first }

        // Then both match, the exact keyword ahead of the prefix label
        assertEquals(listOf("Wi-Fi", "Internet speed test"), result)
    }

    @Test
    fun `launches order apps within a tier but never across tiers`() {
        // Given two prefix matches and a substring match that is opened most
        val mail = app("Mail")
        val maps = app("Maps")
        val gmail = app("Gmail")
        val signals = SearchSignals(picks = emptyMap(), launchCounts = mapOf(maps.listKey to 3, gmail.listKey to 50))

        // When ranked for "ma"
        val result = rank.apps(listOf(mail, maps, gmail), "ma", signals)

        // Then Maps beats Mail on launches, and Gmail stays below both
        assertEquals(listOf(maps, mail, gmail), result)
    }

    @Test
    fun `an app picked twice for a query goes first for that query only`() {
        // Given Chrome picked twice when searching "c"
        val calendar = app("Calendar")
        val chrome = app("Chrome")
        val signals = SearchSignals(picks = mapOf("c" to mapOf(chrome.listKey to 2)), launchCounts = emptyMap())

        // When ranked for "c" (the query it was learned under, case and spaces aside) and for "ch"
        // Then it leads for "c" and the alphabetical order holds for the same tier otherwise
        assertEquals(listOf(chrome, calendar), rank.apps(listOf(calendar, chrome), " C ", signals))
        assertEquals(listOf(calendar, chrome), rank.apps(listOf(calendar, chrome), "c", SearchSignals.EMPTY))
    }

    @Test
    fun `a single pick is not enough to learn`() {
        val calendar = app("Calendar")
        val chrome = app("Chrome")
        val signals = SearchSignals(picks = mapOf("c" to mapOf(chrome.listKey to 1)), launchCounts = emptyMap())

        assertEquals(listOf(calendar, chrome), rank.apps(listOf(calendar, chrome), "c", signals))
    }

    @Test
    fun `a learned pick still has to match the query`() {
        // Given Camera picked for "c" (stale) but the query is now "cl"
        val camera = app("Camera")
        val clock = app("Clock")
        val signals = SearchSignals(picks = mapOf("cl" to mapOf(camera.listKey to 5)), launchCounts = emptyMap())

        // Then only matching apps are returned
        assertEquals(listOf(clock), rank.apps(listOf(camera, clock), "cl", signals))
    }

    @Test
    fun `normalize gives the key a pick is stored under`() {
        assertEquals("google maps", rank.normalize("  Google-Maps "))
    }

    @Test
    fun `matchesSearch uses the same model as ranking`() {
        assertTrue(matchesSearch("Google Maps", "gm"))
        assertTrue(matchesSearch("Wi-Fi", "wifi"))
        assertTrue(matchesSearch("Anything", " "))
        assertFalse(matchesSearch("Camera", "m"))
        assertFalse(matchesSearch("Camera", "--"))
    }
}
