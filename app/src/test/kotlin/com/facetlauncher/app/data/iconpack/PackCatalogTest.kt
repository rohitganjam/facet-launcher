package com.facetlauncher.app.data.iconpack

import android.util.Xml
import java.io.StringReader
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PackCatalogTest {

    /** Reads [xml] as both a compiled resource (the pull parser) and an asset (the text scan), which must agree. */
    private fun parse(xml: String): PackCatalog {
        val pulled = PackCatalog.parse(Xml.newPullParser().apply { setInput(StringReader(xml)) })
        val scanned = PackCatalog.parse(xml)
        assertEquals(pulled, scanned)
        return scanned
    }

    @Test
    fun `categories group the items that follow them, untitled items first`() {
        // Given a drawable.xml with an untitled lead-in and two categories
        val catalog = parse(
            """
            <?xml version="1.0" encoding="utf-8"?>
            <resources>
                <version>1</version>
                <item drawable="intro" />
                <category title="Music" />
                <item drawable="spotify" />
                <item drawable="radio" />
                <category title="Tools" />
                <item drawable="calculator" />
            </resources>
            """.trimIndent(),
        )

        // Then each category holds its own items, in order
        assertEquals(
            listOf(PackCategory(null, listOf("intro")), PackCategory("Music", listOf("spotify", "radio")), PackCategory("Tools", listOf("calculator"))),
            catalog.categories,
        )
    }

    @Test
    fun `an icon listed in two categories appears once in all icons`() {
        // Given a New category repeating an icon from Music
        val catalog = parse(
            """
            <resources>
                <category title="New" /><item drawable="spotify" />
                <category title="Music" /><item drawable="spotify" /><item drawable="radio" />
            </resources>
            """.trimIndent(),
        )

        // Then All lists it once
        assertEquals(listOf("spotify", "radio"), catalog.allDrawables)
    }

    @Test
    fun `comments, empty categories and blank drawables are skipped`() {
        // Given a commented item, an empty category and a blank item
        val catalog = parse(
            """
            <resources>
                <!-- <item drawable="old" /> -->
                <category title="Empty" />
                <category title="Apps" />
                <item drawable=" " />
                <item drawable='maps' />
            </resources>
            """.trimIndent(),
        )

        // Then only the live item is kept, under its category
        assertEquals(listOf(PackCategory("Apps", listOf("maps"))), catalog.categories)
    }

    @Test
    fun `search matches every word against drawable names`() {
        // Given icons named with underscores
        val catalog = PackCatalog(listOf(PackCategory(null, listOf("google_maps", "maps_go", "google_photos", "music"))))

        // Then words match in any order, ignoring case and separators
        assertEquals(listOf("google_maps", "maps_go"), catalog.search("Maps"))
        assertEquals(listOf("google_maps"), catalog.search("maps google"))
        assertEquals(listOf("google_photos"), catalog.search("google_photo"))
        assertEquals(catalog.allDrawables, catalog.search("  "))
    }

    @Test
    fun `a pack without drawable xml offers every drawable its appfilter names`() {
        // Given an appfilter naming two drawables, one twice, and a calendar
        val filter = AppFilter(
            components = mapOf("a/a.Main" to "alpha", "b/b.Main" to "beta", "c/c.Main" to "alpha"),
            calendars = mapOf("d/d.Main" to "calendar_"),
        )

        // Then the catalog lists each once, the calendar by its first day
        assertEquals(listOf("alpha", "beta", "calendar_1"), PackCatalog.fromAppFilter(filter).allDrawables)
    }
}
