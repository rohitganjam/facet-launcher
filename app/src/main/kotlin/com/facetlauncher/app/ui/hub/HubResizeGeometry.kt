package com.facetlauncher.app.ui.hub

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import kotlin.math.roundToInt

/** How a resized widget glides from where the finger left it into its cells: no bounce, so it never overshoots a cell. */
internal val RESIZE_SETTLE_SPEC = spring<Rect>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)

/** How long a released resize waits for the saved cells before going back to the old ones (a resize that can't fit saves nothing). */
internal const val RESIZE_SAVE_GRACE_MS = 600L

/** Which side of a widget a resize handle is on: START (left/top) moves that edge and with it the row/col, END (right/bottom) only the span. */
internal enum class ResizeEdge { START, END }

/** A widget's cells on the Hub grid. */
internal data class HubSpan(val row: Int, val col: Int, val colSpan: Int, val rowSpan: Int)

/** The grid a resize happens on, in px: cell size, the gap between cells, and how far a widget may reach. */
internal data class HubResizeGrid(
    val cellWidthPx: Float,
    val cellHeightPx: Float,
    val gapPx: Float,
    val columns: Int,
    val maxRows: Int,
) {
    private val pitchX get() = cellWidthPx + gapPx
    private val pitchY get() = cellHeightPx + gapPx

    /** Where [span] is drawn, relative to the grid's top-left. */
    fun rectOf(span: HubSpan): Rect {
        val left = pitchX * span.col
        val top = pitchY * span.row
        return Rect(left, top, left + spanSize(span.colSpan, cellWidthPx), top + spanSize(span.rowSpan, cellHeightPx))
    }

    private fun spanSize(span: Int, cellPx: Float) = cellPx * span + gapPx * (span - 1)

    /**
     * The box a resize drag draws: [base] with the dragged edges moved by exactly [deltaPx], so it follows the finger.
     * It never gets smaller than [minColSpan] × [minRowSpan] cells or leaves the grid.
     */
    fun freeRect(base: HubSpan, horizontal: ResizeEdge?, vertical: ResizeEdge?, deltaPx: Offset, minColSpan: Int, minRowSpan: Int): Rect {
        val rect = rectOf(base)
        val minWidth = spanSize(minColSpan, cellWidthPx)
        val minHeight = spanSize(minRowSpan, cellHeightPx)
        val maxRight = pitchX * columns - gapPx
        val maxBottom = pitchY * maxRows - gapPx
        var (left, top, right, bottom) = rect
        when (horizontal) {
            ResizeEdge.END -> right = (right + deltaPx.x).coerceIn(left + minWidth, maxOf(left + minWidth, maxRight))
            ResizeEdge.START -> left = (left + deltaPx.x).coerceIn(minOf(0f, right - minWidth), right - minWidth)
            null -> Unit
        }
        when (vertical) {
            ResizeEdge.END -> bottom = (bottom + deltaPx.y).coerceIn(top + minHeight, maxOf(top + minHeight, maxBottom))
            ResizeEdge.START -> top = (top + deltaPx.y).coerceIn(minOf(0f, bottom - minHeight), bottom - minHeight)
            null -> Unit
        }
        return Rect(left, top, right, bottom)
    }

    /** The cells the [freeRect] lands on: each dragged edge goes to the nearest cell boundary, within the same limits. */
    fun snappedSpan(base: HubSpan, horizontal: ResizeEdge?, vertical: ResizeEdge?, deltaPx: Offset, minColSpan: Int, minRowSpan: Int): HubSpan {
        val free = freeRect(base, horizontal, vertical, deltaPx, minColSpan, minRowSpan)
        var span = base
        when (horizontal) {
            ResizeEdge.END -> {
                val colSpan = ((free.width + gapPx) / pitchX).roundToInt()
                span = span.copy(colSpan = colSpan.coerceIn(minColSpan, maxOf(minColSpan, columns - base.col)))
            }
            ResizeEdge.START -> {
                val right = base.col + base.colSpan
                val col = (free.left / pitchX).roundToInt().coerceIn(0, maxOf(0, right - minColSpan))
                span = span.copy(col = col, colSpan = right - col)
            }
            null -> Unit
        }
        when (vertical) {
            ResizeEdge.END -> {
                val rowSpan = ((free.height + gapPx) / pitchY).roundToInt()
                span = span.copy(rowSpan = rowSpan.coerceIn(minRowSpan, maxOf(minRowSpan, maxRows - base.row)))
            }
            ResizeEdge.START -> {
                val bottom = base.row + base.rowSpan
                val row = (free.top / pitchY).roundToInt().coerceIn(0, maxOf(0, bottom - minRowSpan))
                span = span.copy(row = row, rowSpan = bottom - row)
            }
            null -> Unit
        }
        return span
    }
}

private operator fun Rect.component1() = left
private operator fun Rect.component2() = top
private operator fun Rect.component3() = right
private operator fun Rect.component4() = bottom
