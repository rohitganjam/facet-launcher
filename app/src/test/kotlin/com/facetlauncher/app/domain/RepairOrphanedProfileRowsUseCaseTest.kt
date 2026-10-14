package com.facetlauncher.app.domain

import android.os.UserHandle
import com.facetlauncher.app.data.AppRepository
import com.facetlauncher.app.data.FolderRepository
import com.facetlauncher.app.data.local.AppPlacementEntity
import com.facetlauncher.app.data.model.PlacementList
import com.facetlauncher.app.data.model.AppProfile
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import com.facetlauncher.app.data.PlacementRepository

/**
 * Mirrors [CleanUpUninstalledAppsUseCaseTest]'s constructor-mocking style for the same
 * three-dependency shape (`AppRepository`, `PlacementRepository`, `FolderRepository`). Exercises
 * the resolution logic against [PlacementRepository]'s rows (see [RepairOrphanedProfileRowsUseCase]'s own doc). Every repo's
 * `getOrphanedRows()` is explicitly stubbed to an empty list by default — Mockito's own
 * "return empty collection" default answer doesn't apply to a `suspend fun`, since the method
 * Mockito actually sees (post Kotlin's continuation-passing compilation) returns `Object`, not
 * `List`, so an unstubbed call returns `null` and crashes the use case's `forEach` instead.
 */
@RunWith(RobolectricTestRunner::class)
class RepairOrphanedProfileRowsUseCaseTest {

    private class Fixture {
        val appRepository: AppRepository = mock(AppRepository::class.java)
        val placementRepository: PlacementRepository = mock(PlacementRepository::class.java)
        val folderRepository: FolderRepository = mock(FolderRepository::class.java)

        init {
            // runBlocking, not runTest — this init block runs inside each @Test's own runTest
            // block (via `Fixture()`), and runTest cannot be nested inside another runTest.
            runBlocking {
                `when`(placementRepository.getOrphanedRows()).thenReturn(emptyList())
                `when`(folderRepository.getOrphanedRows()).thenReturn(emptyList())
            }
        }

        fun useCase() = RepairOrphanedProfileRowsUseCase(
            appRepository, placementRepository, folderRepository,
        )
    }

    private fun orphanedRow(profile: AppProfile) =
        AppPlacementEntity(list = PlacementList.FAVORITES, facetId = 1, packageName = "com.example.a", activityName = ".Main", position = 0, profile = profile, userId = -1)

    @Test
    fun `backfills a single orphaned row to the one live handle matching its stored profile`() = runTest {
        // Given an orphaned row stored under WORK, and exactly one live handle classified WORK
        val fixture = Fixture()
        val row = orphanedRow(AppProfile.WORK)
        `when`(fixture.placementRepository.getOrphanedRows()).thenReturn(listOf(row))
        val liveWorkHandle = mock(UserHandle::class.java)
        `when`(fixture.appRepository.handlesFor(AppProfile.WORK)).thenReturn(listOf(liveWorkHandle))

        // When the repair pass runs
        fixture.useCase().invoke()

        // Then it's backfilled to that handle's real userId
        verify(fixture.placementRepository).backfillUserId(row, liveWorkHandle.hashCode())
    }

    @Test
    fun `leaves a row orphaned when zero live handles match its stored profile`() = runTest {
        // Given an orphaned row stored under WORK, but no live Work Profile exists anymore
        val fixture = Fixture()
        val row = orphanedRow(AppProfile.WORK)
        `when`(fixture.placementRepository.getOrphanedRows()).thenReturn(listOf(row))
        `when`(fixture.appRepository.handlesFor(AppProfile.WORK)).thenReturn(emptyList())

        // When the repair pass runs
        fixture.useCase().invoke()

        // Then it's left alone — no backfill attempted
        verify(fixture.placementRepository, never()).backfillUserId(any(), anyInt())
    }

    @Test
    fun `leaves a row orphaned when more than one live handle matches its stored profile`() = runTest {
        // Given an orphaned row stored under OTHER, and two distinct live handles both
        // classified OTHER (e.g. a genuine second profile plus a clone-profile collision) — which
        // one it really came from can't be known, so it's left alone rather than guessed
        val fixture = Fixture()
        val row = orphanedRow(AppProfile.OTHER)
        `when`(fixture.placementRepository.getOrphanedRows()).thenReturn(listOf(row))
        `when`(fixture.appRepository.handlesFor(AppProfile.OTHER)).thenReturn(
            listOf(mock(UserHandle::class.java), mock(UserHandle::class.java)),
        )

        // When the repair pass runs
        fixture.useCase().invoke()

        // Then no backfill is attempted — ambiguous, not guessed
        verify(fixture.placementRepository, never()).backfillUserId(any(), anyInt())
    }

    @Test
    fun `does not touch rows that already have a real userId`() = runTest {
        // Given every repository reporting no orphaned rows at all (the real-world steady state
        // once every row has already been backfilled or migrated with userId = 0)
        val fixture = Fixture()

        // When the repair pass runs
        fixture.useCase().invoke()

        // Then nothing is backfilled anywhere — a no-op pass
        verify(fixture.placementRepository, never()).backfillUserId(any(), anyInt())
        verify(fixture.folderRepository, never()).backfillUserId(any(), anyInt())
    }
}

private fun <T> any(): T = org.mockito.Mockito.any()
