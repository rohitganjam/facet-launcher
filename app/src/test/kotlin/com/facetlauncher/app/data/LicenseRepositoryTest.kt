package com.facetlauncher.app.data

import androidx.test.core.app.ApplicationProvider
import com.facetlauncher.app.data.model.LicenseCatalog
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Reads the real bundled licence files, so a missing or empty text fails here instead of showing a blank screen. */
@RunWith(RobolectricTestRunner::class)
class LicenseRepositoryTest {

    private val repository = LicenseRepository(ApplicationProvider.getApplicationContext())

    @Test
    fun `every entry with bundled files loads one non-empty section per file`() = runTest {
        // Given each catalogue entry that ships licence text
        val withText = (LicenseCatalog.librariesAndFonts + LicenseCatalog.dataSources).filter { it.files.isNotEmpty() }

        withText.forEach { info ->
            // When reading it
            val sections = repository.sections(info)

            // Then every file was found and has text
            assertEquals("${info.id} dropped a file", info.files.size, sections.size)
            assertTrue("${info.id} has an empty text", sections.all { it.text.isNotBlank() })
        }
    }

    @Test
    fun `an entry without bundled text links out instead`() {
        // Then every entry has either bundled text or a url to open
        (LicenseCatalog.librariesAndFonts + LicenseCatalog.dataSources).forEach { info ->
            assertTrue("${info.id} has neither text nor url", info.files.isNotEmpty() || info.url != null)
        }
    }

    @Test
    fun `the fonts entry carries each font's own copyright line`() = runTest {
        // When reading the fonts entry
        val sections = repository.sections(LicenseCatalog.fonts)

        // Then every section starts with its own copyright notice, under the font's name
        assertEquals(7, sections.size)
        sections.forEach { assertTrue("${it.heading} lacks a copyright line", it.text.startsWith("Copyright")) }
        assertNotNull(sections.firstOrNull { it.heading == "Poppins" })
    }

    @Test
    fun `entry ids are unique and resolvable`() {
        val all = LicenseCatalog.librariesAndFonts + LicenseCatalog.dataSources
        assertEquals(all.size, all.map { it.id }.toSet().size)
        all.forEach { assertEquals(it, LicenseCatalog.find(it.id)) }
    }
}
