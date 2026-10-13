package com.facetlauncher.app.data

import com.facetlauncher.app.data.model.ReleaseNotes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReleaseNotesParserTest {

    @Test
    fun `parses the new and improved sections into bullets`() {
        // Given a file with both sections
        val markdown = """
            # 0.1.26

            ## New
            - A media player row.
            - Calendar options.

            ## Improved
            - Faster search.
        """.trimIndent()

        // When parsed
        val notes = parseReleaseNotes("0.1.26", markdown)

        // Then the bullets land in their sections
        assertEquals(
            ReleaseNotes("0.1.26", newFeatures = listOf("A media player row.", "Calendar options."), improvements = listOf("Faster search.")),
            notes,
        )
    }

    @Test
    fun `a file with only one section leaves the other empty`() {
        val notes = parseReleaseNotes("0.1.25", "## Improved\n- Tapping the row opens the app.")

        assertEquals(emptyList<String>(), notes?.newFeatures)
        assertEquals(listOf("Tapping the row opens the app."), notes?.improvements)
    }

    @Test
    fun `none placeholders other headings and stray text are ignored`() {
        // Given placeholders, an unrelated heading, prose and an asterisk bullet
        val markdown = """
            # 0.1.24
            Some intro text.
            ## New
            - (none)
            ## Fixes
            - This section is not shown.
            ## Improved
            * Uses an asterisk bullet.
            -
        """.trimIndent()

        val notes = parseReleaseNotes("0.1.24", markdown)

        assertEquals(emptyList<String>(), notes?.newFeatures)
        assertEquals(listOf("Uses an asterisk bullet."), notes?.improvements)
    }

    @Test
    fun `section headings are matched case-insensitively`() {
        val notes = parseReleaseNotes("0.1.1", "## NEW\n- One.\n## improved\n- Two.")

        assertEquals(listOf("One."), notes?.newFeatures)
        assertEquals(listOf("Two."), notes?.improvements)
    }

    @Test
    fun `a file without any bullets returns null`() {
        assertNull(parseReleaseNotes("0.1.2", "# 0.1.2\n\n## New\n- (none)\n\n## Improved\n"))
        assertNull(parseReleaseNotes("0.1.3", ""))
    }

    @Test
    fun `versions sort newest first comparing each number not the text`() {
        // Given versions where text order and numeric order disagree
        val releases = listOf("0.1.9", "0.1.10", "0.1.2", "0.2.0", "0.1.21").map { ReleaseNotes(it, emptyList(), listOf("x")) }

        // When sorted newest first
        val sorted = releases.sortedWith(NewestVersionFirst).map { it.version }

        // Then 0.1.10 is newer than 0.1.9
        assertEquals(listOf("0.2.0", "0.1.21", "0.1.10", "0.1.9", "0.1.2"), sorted)
    }
}
