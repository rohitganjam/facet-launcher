package com.facetlauncher.app.data.dailyphotos

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.facetlauncher.app.data.model.DailyPhotoSource
import com.facetlauncher.app.data.model.DailyWallpaperPhoto
import com.facetlauncher.app.data.model.DailyWallpaperProblem
import com.facetlauncher.app.data.model.DailyWallpaperState
import java.io.File
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class DailyWallpaperRepositoryTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun repository() = DailyWallpaperRepository(
        PreferenceDataStoreFactory.create(produceFile = { File(tempFolder.root, "daily.preferences_pb") }),
        RuntimeEnvironment.getApplication(),
        Dispatchers.Unconfined,
    )

    @Test
    fun `a new install is off, on bing, wi-fi only and home only`() = runTest {
        assertEquals(DailyWallpaperState(), repository().get())
    }

    @Test
    fun `every field survives being stored`() = runTest {
        // Given a state with every field set, including a credit with a line break
        val state = DailyWallpaperState(
            enabled = true,
            source = DailyPhotoSource.SPOTLIGHT,
            wifiOnly = false,
            includeLock = true,
            paused = true,
            checkedDay = LocalDate.of(2026, 11, 13),
            attemptsDay = LocalDate.of(2026, 11, 12),
            attempts = 2,
            pending = DailyWallpaperPhoto("https://img.peapix.com/p.jpg", "Pending", "© one\ntwo"),
            applying = true,
            current = DailyWallpaperPhoto("https://img.peapix.com/c.jpg", "Current", "© someone"),
            currentSetAtMillis = 1_700_000_000_000,
            systemId = 12,
            lockId = 34,
            history = listOf("https://img.peapix.com/c.jpg", "https://img.peapix.com/b.jpg"),
            problem = DailyWallpaperProblem.WAITING_FOR_WIFI,
        )
        val repository = repository()

        // When it is stored and read back
        repository.update { state }

        // Then it is the same
        assertEquals(state, repository.get())
    }

    @Test
    fun `history keeps only the most recent thirty`() = runTest {
        // Given forty addresses
        val repository = repository()

        // When stored
        repository.update { it.copy(history = (1..40).map { n -> "https://img.peapix.com/$n.jpg" }) }

        // Then the first thirty remain
        assertEquals((1..30).map { n -> "https://img.peapix.com/$n.jpg" }, repository.get().history)
    }
}
