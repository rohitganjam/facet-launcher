package com.facetlauncher.app.domain

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.facetlauncher.app.data.ClockStackRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.FakeEntitlementRepository
import com.facetlauncher.app.data.local.FacetDatabase
import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.ClockStackItemKind
import com.facetlauncher.app.data.model.NativeWidgetType
import com.facetlauncher.app.data.widget.AppWidgetRepository
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CopyFacetWidgetsUseCaseTest {

    private lateinit var database: FacetDatabase
    private lateinit var stack: ClockStackRepository
    private lateinit var facets: FacetRepository
    private lateinit var appWidgets: AppWidgetRepository
    private val entitlement = FakeEntitlementRepository(initial = true)
    private lateinit var useCase: CopyFacetWidgetsUseCase
    private var source = 0L
    private var target = 0L

    @Before
    fun setUp() = runTest {
        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), FacetDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        stack = ClockStackRepository(database, database.clockStackItemDao())
        facets = FacetRepository(database.facetDao(), database.clockStackItemDao())
        appWidgets = mock(AppWidgetRepository::class.java)
        useCase = CopyFacetWidgetsUseCase(stack, facets, appWidgets, entitlement)
        source = database.facetDao().insert(FacetEntity(name = "Work", position = 0, clockPlacementOwn = true, clockAlignment = ClockAlignment.RIGHT))
        target = database.facetDao().insert(FacetEntity(name = "Play", position = 1))
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun `prepare clears the target, releases its widget ids and copies the clock placement`() = runTest {
        // Given a target holding a hosted widget, and a source with its own clock placement
        stack.addWidget(target, 41)
        stack.addClock(source)

        // When the copy is prepared
        val plan = useCase.prepare(source, target)

        // Then the target stack is empty, its widget id was released and the placement came over
        assertEquals(emptyList<Int>(), stack.getAll(target).map { it.id })
        verify(appWidgets).deleteAppWidgetId(41)
        assertEquals(ClockAlignment.RIGHT, facets.getById(target)?.clockAlignment)
        assertEquals(true, facets.getById(target)?.clockPlacementOwn)
        assertEquals(listOf(ClockStackItemKind.CLOCK), plan.pages.map { it.kind })
        assertEquals(0, plan.omitted)
    }

    @Test
    fun `native pages keep their size, alignment and settings`() = runTest {
        // Given a source weather page with a saved size and alignment
        val id = stack.addNativeWidget(source, NativeWidgetType.WEATHER)!!
        stack.setSize(id, 200, 120)
        database.clockStackItemDao().setAlignment(id, ClockAlignment.RIGHT)
        database.clockStackItemDao().updateConfig(id, "cfg")
        val plan = useCase.prepare(source, target)

        // When the page is appended to the target
        assertTrue(useCase.appendNative(target, plan.pages.single()))

        // Then it is a separate row carrying the same values
        val copy = stack.getAll(target).single()
        assertEquals(NativeWidgetType.WEATHER, copy.nativeWidgetType)
        assertEquals(200 to 120, copy.widthDp to copy.heightDp)
        assertEquals(ClockAlignment.RIGHT, copy.alignment)
        assertEquals("cfg", copy.nativeWidgetConfig)
        assertFalse(copy.id == id)
    }

    @Test
    fun `a hosted page is stored with the new widget id and its source size`() = runTest {
        // Given a source hosted widget with a saved size
        stack.addWidget(source, 7)
        stack.setSize(stack.getAll(source).single().id, 300, 150)
        val plan = useCase.prepare(source, target)

        // When it is appended with the id just bound for the target
        assertTrue(useCase.appendHosted(target, plan.pages.single(), appWidgetId = 99))

        // Then the target row uses the new id and the source's size, and the source keeps its own id
        val copy = stack.getAll(target).single()
        assertEquals(99, copy.appWidgetId)
        assertEquals(300 to 150, copy.widthDp to copy.heightDp)
        assertEquals(7, stack.getAll(source).single().appWidgetId)
    }

    @Test
    fun `a free plan copies only the first page and reports the rest as omitted`() = runTest {
        // Given a free plan and a source with a clock and two native widgets
        entitlement.proFlow.value = false
        stack.addClock(source)
        stack.addNativeWidget(source, NativeWidgetType.WEATHER)
        stack.addNativeWidget(source, NativeWidgetType.AGENDA)

        // When the copy is prepared
        val plan = useCase.prepare(source, target)

        // Then one page is planned and two are omitted
        assertEquals(1, plan.pages.size)
        assertEquals(2, plan.omitted)
    }
}
