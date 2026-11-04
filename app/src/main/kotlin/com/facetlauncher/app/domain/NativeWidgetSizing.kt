package com.facetlauncher.app.domain

import com.facetlauncher.app.data.model.NativeWidgetType

/** How much a widget shows. The thresholds below are tuned to the sizes the Hub's cells can produce, but Home reaches the same tiers from a plain dp size. */
enum class WidgetTier { SMALL, MEDIUM, LARGE }

/** The Hub's span for a widget of [type] when it is added. */
data class WidgetSpan(val cols: Int, val rows: Int)

/** The Hub's cell and gap on a typical phone, used to turn spans into dp for Home's defaults. */
private const val NOMINAL_CELL_DP = 72
private const val NOMINAL_GAP_DP = 4

private fun spanDp(count: Int) = NOMINAL_CELL_DP * count + NOMINAL_GAP_DP * (count - 1)

/** The smallest span the Hub lets [type] shrink to: the smallest size at which its [WidgetTier.SMALL] layout still reads. */
fun NativeWidgetType.minSpan(): WidgetSpan = when (this) {
    NativeWidgetType.WEATHER -> WidgetSpan(1, 1)
    NativeWidgetType.AGENDA -> WidgetSpan(2, 1)
    NativeWidgetType.NOW_PLAYING -> WidgetSpan(1, 1)
}

/** The span the Hub gives [type] when it is added. */
fun NativeWidgetType.defaultSpan(): WidgetSpan = when (this) {
    NativeWidgetType.WEATHER -> WidgetSpan(2, 2)
    NativeWidgetType.AGENDA -> WidgetSpan(3, 2)
    NativeWidgetType.NOW_PLAYING -> WidgetSpan(4, 1)
}

/** The smallest Home size in dp: the minimum span's size on a nominal Hub grid. */
fun NativeWidgetType.minSizeDp(): Pair<Int, Int> = minSpan().let { spanDp(it.cols) to spanDp(it.rows) }

/** The Home size in dp before the user resizes: the default span's size on a nominal Hub grid. */
fun NativeWidgetType.defaultSizeDp(): Pair<Int, Int> = defaultSpan().let { spanDp(it.cols) to spanDp(it.rows) }

/** Whether [this] has settings of its own to offer at this size: Weather and Agenda from [WidgetTier.MEDIUM] up, since the small layouts show too little for a choice to matter. */
fun NativeWidgetType.isConfigurable(widthDp: Int, heightDp: Int): Boolean =
    this != NativeWidgetType.NOW_PLAYING && tierFor(widthDp, heightDp) != WidgetTier.SMALL

/** The layout [this] draws in a box of [widthDp] by [heightDp]; Hub tiles and Home pages both ask this, so a size gives the same layout on either. */
fun NativeWidgetType.tierFor(widthDp: Int, heightDp: Int): WidgetTier = when (this) {
    NativeWidgetType.WEATHER -> weatherTier(widthDp, heightDp)
    NativeWidgetType.AGENDA -> agendaTier(widthDp, heightDp)
    NativeWidgetType.NOW_PLAYING -> nowPlayingTier(widthDp, heightDp)
}

private fun weatherTier(w: Int, h: Int) = when {
    w >= 220 && h >= 140 -> WidgetTier.LARGE
    w >= 140 || h >= 140 -> WidgetTier.MEDIUM
    else -> WidgetTier.SMALL
}

private fun agendaTier(w: Int, h: Int) = when {
    w >= 220 && h >= 220 -> WidgetTier.LARGE
    h >= 140 -> WidgetTier.MEDIUM
    else -> WidgetTier.SMALL
}

private fun nowPlayingTier(w: Int, h: Int) = when {
    w >= 300 && h >= 140 -> WidgetTier.LARGE
    w >= 220 || h >= 140 -> WidgetTier.MEDIUM
    else -> WidgetTier.SMALL
}
