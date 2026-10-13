package com.facetlauncher.app.data

import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Reads the real bundled whats_new release files, so a malformed release file fails here instead of showing a blank card. */
@RunWith(RobolectricTestRunner::class)
class ReleaseNotesRepositoryTest {

    private val repository = ReleaseNotesRepository(ApplicationProvider.getApplicationContext())

    @Test
    fun `bundled releases load newest first`() = runTest {
        // When reading the bundled notes
        val releases = repository.releases()

        // Then there is at least one release and they run newest to oldest
        assertTrue(releases.isNotEmpty())
        assertEquals(releases.sortedWith(NewestVersionFirst).map { it.version }, releases.map { it.version })
    }

    @Test
    fun `every bundled release has at least one bullet`() = runTest {
        repository.releases().forEach { release ->
            assertTrue("${release.version} has no bullets", release.newFeatures.isNotEmpty() || release.improvements.isNotEmpty())
        }
    }

    @Test
    fun `no bundled release ships an unfinished draft`() = runTest {
        repository.releases().forEach { release ->
            val bullets = release.newFeatures + release.improvements
            assertTrue("${release.version} still has a TODO", bullets.none { it.contains("TODO") })
        }
    }

    @Test
    fun `every bundled notes file is picked up`() = runTest {
        // Given the files in the assets folder
        val files = java.io.File("src/main/assets/whats_new").listFiles { file -> file.name.endsWith(".md") }.orEmpty().map { it.name.removeSuffix(".md") }.toSet()

        // Then each one parsed into a release (none was dropped as unreadable or empty)
        assertEquals(files, repository.releases().map { it.version }.toSet())
    }
}
