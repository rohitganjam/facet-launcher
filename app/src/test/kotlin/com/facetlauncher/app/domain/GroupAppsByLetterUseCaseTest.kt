package com.facetlauncher.app.domain

import com.facetlauncher.app.data.model.AppInfo
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GroupAppsByLetterUseCaseTest {

    private val useCase = GroupAppsByLetterUseCase()

    private fun app(label: String) = AppInfo(packageName = "com.example.${label.hashCode()}", activityName = ".Main", label = label, icon = null)

    @Test
    fun `groups plain Latin labels under their leading letter`() {
        val result = useCase(listOf(app("Alpha"), app("Bravo"), app("Beta")), locale = Locale.US)

        assertEquals(listOf("A", "B"), result.letters)
        assertEquals(listOf("Alpha"), result.groups["A"]?.map { it.label })
        assertEquals(listOf("Bravo", "Beta"), result.groups["B"]?.map { it.label })
    }

    @Test
    fun `buckets locale-specific letters as their own distinct entries rather than collapsing them`() {
        // AlphabeticIndex (ICU, F7) is locale-correct — under Swedish collation, Å is a
        // distinct primary letter from A (unlike English collation, which folds it into A).
        // Passing the locale explicitly (rather than relying on the JVM's default) keeps this
        // deterministic regardless of what locale the test runner happens to be in.
        val result = useCase(listOf(app("Ångström"), app("Apple")), locale = Locale.forLanguageTag("sv"))

        assertTrue("expected Å to bucket separately from A under Swedish collation, got ${result.letters}", result.letters.contains("Å"))
        assertEquals(listOf("Ångström"), result.groups["Å"]?.map { it.label })
        assertEquals(listOf("Apple"), result.groups["A"]?.map { it.label })
    }

    @Test
    fun `computes a flat header index — one slot per header plus one per app in that group`() {
        val result = useCase(listOf(app("Alpha"), app("Apex"), app("Bravo")), locale = Locale.US)

        // "A" header at index 0, its 2 apps at 1-2, "B" header at index 3
        assertEquals(0, result.headerIndexForLetter["A"])
        assertEquals(3, result.headerIndexForLetter["B"])
    }

    @Test
    fun `computes a flat first-app index — no header offset, for Grid's header-less scrolling`() {
        val result = useCase(listOf(app("Alpha"), app("Apex"), app("Bravo")), locale = Locale.US)

        // "A"'s first app at index 0 (2 apps: indices 0-1), "B"'s first app at index 2 — no +1
        // per group, unlike headerIndexForLetter, since Grid renders no header items at all
        assertEquals(0, result.firstAppIndexForLetter["A"])
        assertEquals(2, result.firstAppIndexForLetter["B"])
    }

    @Test
    fun `an empty app list produces no letters and no groups`() {
        val result = useCase(emptyList(), locale = Locale.US)

        assertEquals(emptyList<String>(), result.letters)
        assertEquals(emptyMap<String, List<AppInfo>>(), result.groups)
    }
}
