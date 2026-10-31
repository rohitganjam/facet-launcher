package com.facetlauncher.app.domain

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.local.FacetDatabase
import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.LandscapeOverrides
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.landscape
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CommitClockPlacementUseCaseTest {

    private lateinit var database: FacetDatabase
    private lateinit var facetRepository: FacetRepository
    private var facetId = 0L

    /** The launcher's fallback: right-aligned, scale 1.2, divider at 300, and a landscape of its own. */
    private val launcher = LauncherSettings(
        clockAlignment = ClockAlignment.RIGHT,
        clockScale = 1.2f,
        clockZoneHeightDp = 300f,
        landscape = LandscapeOverrides(clockAlignment = ClockAlignment.CENTER, clockScale = 1.5f, clockZoneHeightDp = 410f),
    )

    @Before
    fun setUp() = runTest {
        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), FacetDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        facetRepository = FacetRepository(database.facetDao(), database.clockStackItemDao())
        facetId = database.facetDao().insert(FacetEntity(name = "Home", position = 0))
    }

    @After
    fun tearDown() = database.close()

    private fun useCase(settings: LauncherSettings = launcher): CommitClockPlacementUseCase {
        val settingsRepository = mock(SettingsRepository::class.java)
        `when`(settingsRepository.settings).thenReturn(flowOf(settings))
        return CommitClockPlacementUseCase(facetRepository, settingsRepository)
    }

    @Test
    fun `the first adjustment copies the launcher fallback into the facet and applies the change`() = runTest {
        // Given a facet that still shows the launcher's fallback placement
        assertEquals(false, facetRepository.getById(facetId)!!.clockPlacementOwn)

        // When only the divider is dragged in portrait
        useCase()(facetId, landscape = false) { it.copy(zoneHeightDp = 220f) }

        // Then the facet owns a placement with the new divider and the launcher's alignment and scale, not the defaults
        val stored = facetRepository.getById(facetId)!!
        assertEquals(true, stored.clockPlacementOwn)
        assertEquals(220f, stored.clockZoneHeightDp)
        assertEquals(ClockAlignment.RIGHT, stored.clockAlignment)
        assertEquals(1.2f, stored.clockScale, 0.001f)
    }

    @Test
    fun `the first adjustment also keeps the landscape placement the facet was showing`() = runTest {
        // When the divider is dragged in portrait
        useCase()(facetId, landscape = false) { it.copy(zoneHeightDp = 220f) }

        // Then landscape still shows what it showed: the launcher's landscape values, now the facet's own
        val stored = facetRepository.getById(facetId)!!
        assertEquals(ClockAlignment.CENTER, stored.landscape.clockAlignment)
        assertEquals(1.5f, stored.landscape.clockScale!!, 0.001f)
        assertEquals(410f, stored.landscape.clockZoneHeightDp)
    }

    @Test
    fun `a landscape adjustment changes landscape and leaves portrait as it was shown`() = runTest {
        // When the clock is rescaled in landscape
        useCase()(facetId, landscape = true) { it.copy(scale = 0.6f) }

        // Then landscape has the new scale and portrait keeps the launcher's values
        val stored = facetRepository.getById(facetId)!!
        assertEquals(0.6f, stored.landscape.clockScale!!, 0.001f)
        assertEquals(1.2f, stored.clockScale, 0.001f)
        assertEquals(ClockAlignment.RIGHT, stored.clockAlignment)
    }

    @Test
    fun `a landscape value equal to portrait stays unset, so it follows portrait`() = runTest {
        // Given a launcher whose landscape alignment and divider are the same as portrait's
        val same = LauncherSettings(clockAlignment = ClockAlignment.CENTER, clockScale = 1f, clockZoneHeightDp = null)

        // When only the portrait scale is changed
        useCase(same)(facetId, landscape = false) { it.copy(scale = 0.9f) }

        // Then the equal alignment and divider store no landscape twin, and landscape keeps the scale it showed
        val stored = facetRepository.getById(facetId)!!
        assertNull(stored.landscape.clockAlignment)
        assertNull(stored.landscape.clockZoneHeightDp)
        assertEquals(1f, stored.landscape.clockScale!!, 0.001f)
    }

    @Test
    fun `later adjustments change only what they name and keep the facet's own values`() = runTest {
        // Given a facet that already owns its placement
        useCase()(facetId, landscape = false) { it.copy(alignment = ClockAlignment.LEFT, scale = 0.7f) }

        // When the divider is dragged later (the launcher fallback is irrelevant now)
        useCase(LauncherSettings())(facetId, landscape = false) { it.copy(zoneHeightDp = 150f) }

        // Then alignment and scale are still the facet's own
        val stored = facetRepository.getById(facetId)!!
        assertEquals(ClockAlignment.LEFT, stored.clockAlignment)
        assertEquals(0.7f, stored.clockScale, 0.001f)
        assertEquals(150f, stored.clockZoneHeightDp)
    }

    @Test
    fun `an unknown facet is ignored`() = runTest {
        useCase()(9999L, landscape = false) { it.copy(scale = 2f) }

        assertEquals(false, facetRepository.getById(facetId)!!.clockPlacementOwn)
    }
}
