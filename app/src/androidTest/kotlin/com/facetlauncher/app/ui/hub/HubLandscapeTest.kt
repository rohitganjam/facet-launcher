package com.facetlauncher.app.ui.hub

import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetProviderInfo
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.activity.ComponentActivity
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.TouchInjectionScope
import androidx.compose.ui.test.center
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.facetlauncher.app.data.WidgetPlacementRepository
import com.facetlauncher.app.data.local.FacetDatabase
import com.facetlauncher.app.data.local.WidgetPlacementEntity
import com.facetlauncher.app.data.widget.AppWidgetRepository
import com.facetlauncher.app.domain.CompactWidgetsUseCase
import com.facetlauncher.app.domain.DeleteWidgetUseCase
import com.facetlauncher.app.domain.FlowPackWidgetsUseCase
import com.facetlauncher.app.domain.LandscapeHubUseCase
import com.facetlauncher.app.domain.ObserveHubStateUseCase
import com.facetlauncher.app.domain.OrderWidgetsForLandscapeUseCase
import com.facetlauncher.app.domain.ReorderLandscapeWidgetUseCase
import com.facetlauncher.app.domain.ResolveWidgetDropUseCase
import com.facetlauncher.app.domain.ResolveWidgetResizeUseCase
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

/**
 * The Hub in a genuinely landscape window (the test activity is rotated first): fixed-size cells,
 * widgets flowing down the columns, and dragging / resizing / removing in the landscape grid.
 */
class HubLandscapeTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var repository: WidgetPlacementRepository

    @Before
    fun rotateToLandscape() {
        composeRule.activityRule.scenario.onActivity { it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        }
        composeRule.waitForIdle()
    }

    private fun placement(id: Int, row: Int, col: Int = 0, colSpan: Int = 1, rowSpan: Int = 1, landscapeOrder: Int? = null) = WidgetPlacementEntity(
        appWidgetId = id,
        providerPackageName = "com.example.widgets",
        providerClassName = ".Provider",
        row = row,
        col = col,
        colSpan = colSpan,
        rowSpan = rowSpan,
        landscapeOrder = landscapeOrder,
    )

    private fun <T> eqNonNull(value: T): T = eq(value) ?: value

    private fun setContent(placements: List<WidgetPlacementEntity>) {
        composeRule.setContent {
            val context = LocalContext.current
            val viewModel = remember {
                val database = Room.inMemoryDatabaseBuilder(context, FacetDatabase::class.java).allowMainThreadQueries().build()
                val widgetPlacementRepository = WidgetPlacementRepository(database.widgetPlacementDao())
                repository = widgetPlacementRepository
                runBlocking { placements.forEach { widgetPlacementRepository.upsert(it) } }
                val appWidgetRepository = mock(AppWidgetRepository::class.java)
                `when`(appWidgetRepository.observeProviderChanges()).thenReturn(emptyFlow())
                placements.forEach { placement ->
                    val info = AppWidgetProviderInfo()
                    val id = placement.appWidgetId
                    `when`(appWidgetRepository.getAppWidgetInfo(id)).thenReturn(info)
                    `when`(appWidgetRepository.getProviderLabel(id)).thenReturn("Widget")
                    `when`(appWidgetRepository.createHostView(eqNonNull(context), eqNonNull(id), eqNonNull(info))).thenReturn(AppWidgetHostView(context))
                }
                HubViewModel(
                    ObserveHubStateUseCase(widgetPlacementRepository, appWidgetRepository),
                    appWidgetRepository,
                    widgetPlacementRepository,
                    DeleteWidgetUseCase(widgetPlacementRepository, appWidgetRepository),
                    ResolveWidgetDropUseCase(),
                    ResolveWidgetResizeUseCase(),
                    CompactWidgetsUseCase(),
                    LandscapeHubUseCase(OrderWidgetsForLandscapeUseCase(), FlowPackWidgetsUseCase(), ReorderLandscapeWidgetUseCase()),
                )
            }
            FacetLauncherTheme {
                HubScreen(onAddClick = {}, onManageClick = {}, viewModel = viewModel)
            }
        }
        composeRule.waitForIdle()
    }

    private fun boundsOf(id: Int) = composeRule.onNodeWithTag("hub_widget_tile_$id").fetchSemanticsNode().boundsInRoot

    private fun awaitPlacement(id: Int, predicate: (WidgetPlacementEntity?) -> Boolean) {
        composeRule.waitUntil(timeoutMillis = 3_000) { runBlocking { predicate(repository.getById(id)) } }
    }

    private fun longPress(tag: String) {
        composeRule.onNodeWithTag(tag).performTouchInput { down(center) }
        composeRule.mainClock.advanceTimeBy(600)
        composeRule.waitForIdle()
    }

    private fun TouchInjectionScope.stepMoveBy(totalDelta: Offset, steps: Int = 12) {
        repeat(steps) { moveBy(totalDelta / steps.toFloat()) }
    }

    /** One cell plus its gap, in px. */
    private fun pitchPx(): Float = with(composeRule.density) { (HUB_LANDSCAPE_CELL_SIZE + HUB_GRID_GAP).toPx() }

    @Test
    fun landscapeShowsTheLandscapeGridNotThePortraitOne() {
        setContent(listOf(placement(id = 1, row = 0)))

        composeRule.onNodeWithTag("hub_landscape_grid").assertExists()
        composeRule.onNodeWithTag("hub_grid").assertDoesNotExist()
    }

    @Test
    fun aWidgetKeepsTheFixedCellSizeAndASizeInCells() {
        setContent(listOf(placement(id = 1, row = 0), placement(id = 2, row = 1, colSpan = 3)))

        val cellPx = with(composeRule.density) { HUB_LANDSCAPE_CELL_SIZE.toPx() }
        val gapPx = with(composeRule.density) { HUB_GRID_GAP.toPx() }
        assert(kotlin.math.abs(boundsOf(1).width - cellPx) < 1.5f) { "one cell should be $cellPx px wide, was ${boundsOf(1).width}" }
        assert(kotlin.math.abs(boundsOf(2).width - (3 * cellPx + 2 * gapPx)) < 1.5f) { "three cells should span ${3 * cellPx + 2 * gapPx}px, was ${boundsOf(2).width}" }
    }

    @Test
    fun widgetsFlowDownTheColumnsInPortraitReadingOrderUntilReordered() {
        // Given widgets stacked in portrait rows 0 and 1 (with no landscape order saved)
        setContent(listOf(placement(id = 1, row = 0), placement(id = 2, row = 1)))

        // Then in landscape the second sits directly below the first (there are at least two rows)
        composeRule.waitForIdle()
        assert(boundsOf(2).top > boundsOf(1).top) { "widget 2 ${boundsOf(2)} should be below widget 1 ${boundsOf(1)}" }
        assert(kotlin.math.abs(boundsOf(2).left - boundsOf(1).left) < 1.5f) { "widgets 1 and 2 should share a column" }
    }

    @Test
    fun savedLandscapeOrdersDecideTheFlowNotPortraitPositions() {
        // Given portrait rows 0 and 1, but landscape orders that put widget 2 first
        setContent(listOf(placement(id = 1, row = 0, landscapeOrder = 1), placement(id = 2, row = 1, landscapeOrder = 0)))

        composeRule.waitForIdle()
        assert(boundsOf(2).top < boundsOf(1).top) { "widget 2 ${boundsOf(2)} should now come first, above widget 1 ${boundsOf(1)}" }
    }

    @Test
    fun draggingAWidgetToALaterColumnMovesItLastAndSavesOnlyLandscapeOrders() {
        setContent(listOf(placement(id = 1, row = 0), placement(id = 2, row = 1)))
        val pitch = pitchPx()

        // When widget 1 is lifted and dropped one column to the right
        longPress("hub_widget_tile_1")
        composeRule.onNodeWithTag("hub_widget_tile_1").performTouchInput {
            stepMoveBy(Offset(pitch, 0f))
            up()
        }

        // Then the landscape order puts widget 2 first and widget 1 last, and portrait rows are untouched
        awaitPlacement(1) { it?.landscapeOrder == 1 }
        awaitPlacement(2) { it?.landscapeOrder == 0 }
        assert(runBlocking { repository.getById(1)?.row } == 0 && runBlocking { repository.getById(2)?.row } == 1) { "portrait rows must not change" }
    }

    @Test
    fun theDropTargetPreviewShowsWhileAWidgetIsDragged() {
        setContent(listOf(placement(id = 1, row = 0)))

        longPress("hub_widget_tile_1")
        composeRule.onNodeWithTag("hub_widget_tile_1").performTouchInput { stepMoveBy(Offset(pitchPx(), 0f)) }

        composeRule.onNodeWithTag("hub_drop_target_preview").assertExists()
        composeRule.onNodeWithTag("hub_widget_tile_1").performTouchInput { up() }
    }

    @Test
    fun resizingFromTheMenuGrowsTheWidgetAndSavesTheSharedSize() {
        setContent(listOf(placement(id = 1, row = 0)))
        val pitch = pitchPx()
        longPress("hub_widget_tile_1")
        composeRule.onNodeWithTag("hub_widget_tile_1").performTouchInput { up() }
        composeRule.onNodeWithText("Resize widget").performClick()

        composeRule.onNodeWithTag("hub_resize_handle_right").performTouchInput {
            down(center)
            stepMoveBy(Offset(pitch, 0f))
            up()
        }

        awaitPlacement(1) { it?.colSpan == 2 && it.rowSpan == 1 }
    }

    @Test
    fun theLandscapeResizeOffersOnlyTheRightAndBottomHandles() {
        setContent(listOf(placement(id = 1, row = 0)))
        longPress("hub_widget_tile_1")
        composeRule.onNodeWithTag("hub_widget_tile_1").performTouchInput { up() }
        composeRule.onNodeWithText("Resize widget").performClick()

        composeRule.onNodeWithTag("hub_resize_handle_right").assertExists()
        composeRule.onNodeWithTag("hub_resize_handle_bottom").assertExists()
        composeRule.onNodeWithTag("hub_resize_handle_left").assertDoesNotExist()
        composeRule.onNodeWithTag("hub_resize_handle_top").assertDoesNotExist()
    }

    @Test
    fun removingFromTheMenuDeletesTheWidget() {
        setContent(listOf(placement(id = 1, row = 0)))
        longPress("hub_widget_tile_1")
        composeRule.onNodeWithTag("hub_widget_tile_1").performTouchInput { up() }

        composeRule.onNodeWithText("Remove widget").performClick()

        awaitPlacement(1) { it == null }
    }

    @Test
    fun aWidgetTallerThanTheRowsIsShownNoTallerThanTheGrid() {
        setContent(listOf(placement(id = 1, row = 0, rowSpan = 12)))

        val grid = composeRule.onNodeWithTag("hub_landscape_grid").fetchSemanticsNode().boundsInRoot
        assert(boundsOf(1).height <= grid.height) { "tile ${boundsOf(1)} should fit inside the grid $grid" }
    }

    @Test
    fun theGridScrollsSidewaysWhenWidgetsRunPastTheEdge() {
        // Given enough wide widgets that they run past the right edge
        setContent((1..8).map { placement(id = it, row = it - 1, colSpan = 3, rowSpan = 2) })
        composeRule.waitForIdle()

        // Then the last widget starts beyond the visible width until the grid is scrolled (unclipped
        // bounds: an offscreen node's ordinary bounds are clipped to nothing)
        val visibleRight = composeRule.onNodeWithTag("hub_landscape_grid").getUnclippedBoundsInRoot().right
        val lastLeft = composeRule.onNodeWithTag("hub_widget_tile_8").getUnclippedBoundsInRoot().left
        assert(lastLeft > visibleRight) { "widget 8 (left $lastLeft) should start past the visible width $visibleRight" }
    }
}
