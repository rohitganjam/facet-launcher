package com.lumenlauncher.app.ui.hub

import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetProviderInfo
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.TouchInjectionScope
import androidx.compose.ui.test.center
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.room.Room
import com.lumenlauncher.app.data.WidgetPlacementRepository
import com.lumenlauncher.app.data.local.LumenDatabase
import com.lumenlauncher.app.data.local.WidgetPlacementEntity
import com.lumenlauncher.app.data.widget.AppWidgetRepository
import com.lumenlauncher.app.domain.DeleteWidgetUseCase
import com.lumenlauncher.app.domain.ObserveHubStateUseCase
import com.lumenlauncher.app.domain.ResizeWidgetUseCase
import com.lumenlauncher.app.domain.CompactWidgetsUseCase
import com.lumenlauncher.app.domain.ResolveWidgetDropUseCase
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

/**
 * Milestone 6 — long-press grab (move) and release-in-place (context menu) gestures.
 *
 * [AppWidgetRepository] is mocked because resize mode requires a *resolved* (non-orphaned)
 * widget — [ObserveHubStateUseCase] derives that from [AppWidgetRepository.getAppWidgetInfo].
 */
class HubGestureTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var repository: WidgetPlacementRepository

    private fun placement(id: Int, row: Int, col: Int, colSpan: Int = 1, rowSpan: Int = 1) = WidgetPlacementEntity(
        appWidgetId = id,
        providerPackageName = "com.example.widgets",
        providerClassName = ".Provider",
        row = row,
        col = col,
        colSpan = colSpan,
        rowSpan = rowSpan,
    )

    private fun <T> eqNonNull(value: T): T = eq(value) ?: value

    private fun resolvableInfo() = AppWidgetProviderInfo()

    private fun setContent(placements: List<WidgetPlacementEntity>, resolvable: Set<Int> = emptySet()) {
        composeRule.setContent {
            val context = LocalContext.current
            val viewModel = remember {
                val database = Room.inMemoryDatabaseBuilder(context, LumenDatabase::class.java).allowMainThreadQueries().build()
                val widgetPlacementRepository = WidgetPlacementRepository(database.widgetPlacementDao())
                repository = widgetPlacementRepository
                runBlocking { placements.forEach { widgetPlacementRepository.upsert(it) } }
                val appWidgetRepository = mock(AppWidgetRepository::class.java)
                `when`(appWidgetRepository.observeProviderChanges()).thenReturn(emptyFlow())
                resolvable.forEach { id ->
                    val info = resolvableInfo()
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
                    ResizeWidgetUseCase(),
                    CompactWidgetsUseCase(),
                )
            }
            LumenLauncherTheme {
                HubScreen(onAddClick = {}, onManageClick = {}, viewModel = viewModel)
            }
        }
        composeRule.waitForIdle()
    }

    private fun cellWidthPxOf(appWidgetId: Int): Float =
        composeRule.onNodeWithTag("hub_widget_tile_$appWidgetId").fetchSemanticsNode().size.width.toFloat()

    private fun cellHeightPxOf(appWidgetId: Int): Float =
        composeRule.onNodeWithTag("hub_widget_tile_$appWidgetId").fetchSemanticsNode().size.height.toFloat()

    private fun awaitPlacement(id: Int, predicate: (WidgetPlacementEntity?) -> Boolean) {
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { predicate(repository.getById(id)) }
        }
    }

    private fun longPress(tag: String) {
        composeRule.onNodeWithTag(tag).performTouchInput { down(center) }
        composeRule.mainClock.advanceTimeBy(600)
        composeRule.waitForIdle()
    }

    private fun TouchInjectionScope.stepMoveBy(totalDelta: Offset, steps: Int = 12) {
        repeat(steps) { moveBy(totalDelta / steps.toFloat()) }
    }

    @Test
    fun draggingOntoAnEmptyCellMovesTheWidgetThere() {
        setContent(listOf(placement(id = 1, row = 0, col = 0)))
        val cellWidth = cellWidthPxOf(1)

        longPress("hub_widget_tile_1")
        composeRule.onNodeWithTag("hub_widget_tile_1").performTouchInput {
            stepMoveBy(Offset(cellWidth, 0f))
            up()
        }

        awaitPlacement(1) { it?.col == 1 && it.row == 0 }
    }

    @Test
    fun draggingOntoAnOccupiedCellDisplacesTheOccupantRight() {
        setContent(listOf(placement(id = 1, row = 0, col = 0), placement(id = 2, row = 0, col = 1)))
        val cellWidth = cellWidthPxOf(1)

        longPress("hub_widget_tile_1")
        composeRule.onNodeWithTag("hub_widget_tile_1").performTouchInput {
            stepMoveBy(Offset(cellWidth, 0f))
            up()
        }
        composeRule.waitForIdle()

        awaitPlacement(1) { it?.col == 1 && it.row == 0 }
        awaitPlacement(2) { it?.col == 2 && it.row == 0 }
    }

    @Test
    fun releasingALongPressWithoutMovingOpensTheContextMenu() {
        setContent(listOf(placement(id = 10, row = 0, col = 0)), resolvable = setOf(10))
        composeRule.onNodeWithText("Resize widget").assertDoesNotExist()
        composeRule.onNodeWithText("Remove widget").assertDoesNotExist()

        longPress("hub_widget_tile_10")
        composeRule.onNodeWithTag("hub_widget_tile_10").performTouchInput { up() }

        composeRule.onNodeWithText("Resize widget").assertExists()
        composeRule.onNodeWithText("Remove widget").assertExists()
    }

    @Test
    fun selectingResizeWidgetFromMenuEntersResizeMode() {
        setContent(listOf(placement(id = 10, row = 0, col = 0)), resolvable = setOf(10))
        longPress("hub_widget_tile_10")
        composeRule.onNodeWithTag("hub_widget_tile_10").performTouchInput { up() }

        composeRule.onNodeWithText("Resize widget").performClick()

        composeRule.onNodeWithTag("hub_resize_handle_right").assertExists()
        composeRule.onNodeWithTag("hub_resize_handle_bottom").assertExists()
    }

    @Test
    fun selectingRemoveWidgetFromMenuDeletesTheWidget() {
        setContent(listOf(placement(id = 10, row = 0, col = 0)), resolvable = setOf(10))
        longPress("hub_widget_tile_10")
        composeRule.onNodeWithTag("hub_widget_tile_10").performTouchInput { up() }

        composeRule.onNodeWithText("Remove widget").performClick()

        awaitPlacement(10) { it == null }
    }

    @Test
    fun draggingTheRightEdgeHandleGrowsWidth() {
        setContent(listOf(placement(id = 10, row = 0, col = 0)), resolvable = setOf(10))
        val cellWidth = cellWidthPxOf(10)
        longPress("hub_widget_tile_10")
        composeRule.onNodeWithTag("hub_widget_tile_10").performTouchInput { up() }
        composeRule.onNodeWithText("Resize widget").performClick()

        composeRule.onNodeWithTag("hub_resize_handle_right").performTouchInput {
            down(center)
            stepMoveBy(Offset(cellWidth, 0f))
            up()
        }

        awaitPlacement(10) { it?.colSpan == 2 && it.rowSpan == 1 }
    }

    @Test
    fun draggingTheBottomEdgeHandleGrowsHeight() {
        setContent(listOf(placement(id = 10, row = 0, col = 0)), resolvable = setOf(10))
        val cellHeight = cellHeightPxOf(10)
        longPress("hub_widget_tile_10")
        composeRule.onNodeWithTag("hub_widget_tile_10").performTouchInput { up() }
        composeRule.onNodeWithText("Resize widget").performClick()

        composeRule.onNodeWithTag("hub_resize_handle_bottom").performTouchInput {
            down(center)
            stepMoveBy(Offset(0f, cellHeight))
            up()
        }

        awaitPlacement(10) { it?.colSpan == 1 && it.rowSpan == 2 }
    }

    @Test
    fun movingAWidgetAfterResizingItPreservesTheNewSpan() {
        setContent(listOf(placement(id = 10, row = 0, col = 0)), resolvable = setOf(10))
        val cellWidth = cellWidthPxOf(10)
        longPress("hub_widget_tile_10")
        composeRule.onNodeWithTag("hub_widget_tile_10").performTouchInput { up() }
        composeRule.onNodeWithText("Resize widget").performClick()
        composeRule.onNodeWithTag("hub_resize_handle_right").performTouchInput {
            down(center)
            stepMoveBy(Offset(cellWidth, 0f))
            up()
        }
        awaitPlacement(10) { it?.colSpan == 2 }

        longPress("hub_widget_tile_10")
        composeRule.onNodeWithTag("hub_widget_tile_10").performTouchInput {
            stepMoveBy(Offset(cellWidth, 0f))
            up()
        }

        awaitPlacement(10) { it?.row == 0 && it.col == 1 && it.colSpan == 2 && it.rowSpan == 1 }
    }

    @Test
    fun shrinkingAWidgetCommits() {
        setContent(listOf(placement(id = 10, row = 0, col = 0, colSpan = 2, rowSpan = 2)), resolvable = setOf(10))
        val cellWidth = cellWidthPxOf(10) / 2
        longPress("hub_widget_tile_10")
        composeRule.onNodeWithTag("hub_widget_tile_10").performTouchInput { up() }
        composeRule.onNodeWithText("Resize widget").performClick()

        composeRule.onNodeWithTag("hub_resize_handle_right").performTouchInput {
            down(center)
            stepMoveBy(Offset(-cellWidth, 0f))
            up()
        }

        awaitPlacement(10) { it?.colSpan == 1 && it.rowSpan == 2 }
    }

    @Test
    fun growingIntoANeighboringWidgetDoesNotCommit() {
        setContent(
            listOf(placement(id = 10, row = 0, col = 0), placement(id = 11, row = 0, col = 1)),
            resolvable = setOf(10),
        )
        val cellWidth = cellWidthPxOf(10)
        longPress("hub_widget_tile_10")
        composeRule.onNodeWithTag("hub_widget_tile_10").performTouchInput { up() }
        composeRule.onNodeWithText("Resize widget").performClick()

        composeRule.onNodeWithTag("hub_resize_handle_right").performTouchInput {
            down(center)
            stepMoveBy(Offset(cellWidth, 0f))
            up()
        }

        awaitPlacement(10) { it?.colSpan == 1 }
        awaitPlacement(11) { it?.col == 1 }
    }

    @Test
    fun tappingOutsideAWidgetWhileResizingDismissesResizeMode() {
        setContent(listOf(placement(id = 10, row = 0, col = 0)), resolvable = setOf(10))
        longPress("hub_widget_tile_10")
        composeRule.onNodeWithTag("hub_widget_tile_10").performTouchInput { up() }
        composeRule.onNodeWithText("Resize widget").performClick()

        composeRule.onNodeWithTag("hub_grid").performTouchInput {
            down(Offset(centerX, bottom - 20f))
            up()
        }

        composeRule.onNodeWithTag("hub_resize_handle_right").assertDoesNotExist()
        composeRule.onNodeWithTag("hub_resize_handle_bottom").assertDoesNotExist()
    }

    @Test
    fun resizeHandleAtEdgeIsStillInteractive() {
        setContent(listOf(placement(id = 10, row = 0, col = 4)), resolvable = setOf(10))
        val cellWidth = cellWidthPxOf(10)
        longPress("hub_widget_tile_10")
        composeRule.onNodeWithTag("hub_widget_tile_10").performTouchInput { up() }
        composeRule.onNodeWithText("Resize widget").performClick()
        
        composeRule.onNodeWithTag("hub_resize_handle_right").assertExists()

        composeRule.onNodeWithTag("hub_resize_handle_right").performTouchInput {
            down(center)
            stepMoveBy(Offset(-cellWidth / 2, 0f))
            up()
        }
    }
}
