package com.facetlauncher.app.data.iconpack

import android.util.Xml
import java.io.StringReader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AppFilterTest {

    /** Reads [xml] as both a compiled resource (the pull parser) and an asset (the text scan), which must agree. */
    private fun parse(xml: String, keepPackage: (String) -> Boolean = { true }): AppFilter {
        val pulled = AppFilter.parse(Xml.newPullParser().apply { setInput(StringReader(xml)) }, keepPackage)
        val scanned = AppFilter.parse(xml, keepPackage)
        assertEquals(pulled, scanned)
        return scanned
    }

    @Test
    fun `commented-out items are ignored and entities decoded`() {
        // Given an item inside a comment and one with an escaped character
        val filter = parse(
            """
            <?xml version="1.0" encoding="UTF-8"?>
            <resources>
                <!-- <item component="ComponentInfo{com.old/com.old.Main}" drawable="old" /> -->
                <item component="ComponentInfo{com.a/com.a.B&amp;C}" drawable='single_quoted' />
            </resources>
            """.trimIndent(),
        )

        // Then only the live item is read, with its value unescaped
        assertEquals(mapOf("com.a/com.a.B&C" to "single_quoted"), filter.components)
    }

    @Test
    fun `only the packages asked for are kept`() {
        // Given a pack listing an installed and an uninstalled app
        val filter = parse(
            """
            <resources>
                <item component="ComponentInfo{com.installed/com.installed.Main}" drawable="installed" />
                <item component="ComponentInfo{com.elsewhere/com.elsewhere.Main}" drawable="elsewhere" />
            </resources>
            """,
            keepPackage = { it == "com.installed" },
        )

        // Then only the installed one is held
        assertEquals(mapOf("com.installed/com.installed.Main" to "installed"), filter.components)
    }

    @Test
    fun `items map each activity to its drawable`() {
        // Given an appfilter with two apps
        val filter = parse(
            """
            <resources>
                <item component="ComponentInfo{com.android.chrome/com.google.android.apps.chrome.Main}" drawable="chrome" />
                <item component="ComponentInfo{org.mozilla.firefox/org.mozilla.firefox.App}" drawable="firefox" />
            </resources>
            """,
        )

        // Then each activity resolves to its drawable
        assertEquals("chrome", filter.drawableFor("com.android.chrome", "com.google.android.apps.chrome.Main"))
        assertEquals("firefox", filter.drawableFor("org.mozilla.firefox", "org.mozilla.firefox.App"))
    }

    @Test
    fun `a relative activity name is resolved against its package`() {
        // Given an item whose class starts with a dot
        val filter = parse("""<resources><item component="ComponentInfo{com.app/.MainActivity}" drawable="app" /></resources>""")

        // Then the full class name finds it
        assertEquals("app", filter.drawableFor("com.app", "com.app.MainActivity"))
    }

    @Test
    fun `an unlisted activity falls back to its package's first icon`() {
        // Given a pack that lists one activity of an app
        val filter = parse(
            """
            <resources>
                <item component="ComponentInfo{com.app/com.app.Main}" drawable="app_main" />
                <item component="ComponentInfo{com.app/com.app.Other}" drawable="app_other" />
            </resources>
            """,
        )

        // When asking for an activity it doesn't list
        val drawable = filter.drawableFor("com.app", "com.app.Settings")

        // Then the package's first drawable stands in, and other packages get nothing
        assertEquals("app_main", drawable)
        assertNull(filter.drawableFor("com.other", "com.other.Main"))
    }

    @Test
    fun `malformed items are skipped without dropping the rest`() {
        // Given a pack with broken entries among good ones
        val filter = parse(
            """
            <resources>
                <item component="ComponentInfo{}" drawable="empty" />
                <item component=":LAUNCHER_ACTION_APP_DRAWER" drawable="drawer" />
                <item component="ComponentInfo{com.app/com.app.Main}" drawable="" />
                <item drawable="no_component" />
                <item component="ComponentInfo{com.good/com.good.Main}" drawable="good" />
            </resources>
            """,
        )

        // Then only the good entry survives
        assertEquals(mapOf("com.good/com.good.Main" to "good"), filter.components)
    }

    @Test
    fun `fallback art and scale are read`() {
        // Given a pack with backs, a mask, an overlay and a scale
        val filter = parse(
            """
            <resources>
                <iconback img1="back_a" img2="back_b" />
                <iconmask img1="mask" />
                <iconupon img1="upon" />
                <scale factor="0.75" />
            </resources>
            """,
        )

        // Then all of it is kept
        assertEquals(listOf("back_a", "back_b"), filter.iconBacks)
        assertEquals("mask", filter.iconMask)
        assertEquals("upon", filter.iconUpon)
        assertEquals(0.75f, filter.scale, 0.0001f)
        assertTrue(filter.hasFallbackArt)
    }

    @Test
    fun `a pack without fallback art says so`() {
        // Given only items and a scale
        val filter = parse("""<resources><scale factor="0.8" /><item component="ComponentInfo{a/a.B}" drawable="b" /></resources>""")

        // Then it has no art to theme other apps with
        assertFalse(filter.hasFallbackArt)
    }
}
