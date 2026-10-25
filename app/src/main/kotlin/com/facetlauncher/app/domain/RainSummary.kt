package com.facetlauncher.app.domain

import com.facetlauncher.app.data.model.WeatherHour
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

enum class RainIntensity { LIGHT, MODERATE, HEAVY }

/** What the next day of rain amounts to, for the sheet's one-line summary. The UI turns it into words. */
sealed interface RainSummary {
    /** No hourly rain amounts in the window (the feed has none yet). */
    data object NoData : RainSummary

    /** No rain worth mentioning in the window. */
    data object Dry : RainSummary

    /** It is raining now, until [until] (`null`: past the end of the window). [totalMm] is what is still to fall. */
    data class RainNow(val until: Instant?, val totalMm: Double, val intensity: RainIntensity) : RainSummary

    /** Rain from [from] until [until] (`null`: past the end of the window). */
    data class RainLater(val from: Instant, val until: Instant?, val totalMm: Double, val intensity: RainIntensity) : RainSummary
}

private const val WET_HOUR_MM = 0.2
private const val MODERATE_PEAK_MM = 2.5
private const val HEAVY_PEAK_MM = 7.6
private const val MAX_DRY_GAP_HOURS = 2L
private const val CURRENT_RUN_SHARE = 0.5
private val WINDOW: Duration = Duration.ofHours(24)

private class WetRun(val slots: List<WeatherHour>) {
    val start: Instant get() = slots.first().time
    val end: Instant get() = slots.last().time.plus(1, ChronoUnit.HOURS)
    val totalMm: Double get() = slots.sumOf { it.precipitationMm ?: 0.0 }
    val peakMm: Double get() = slots.maxOf { it.precipitationMm ?: 0.0 }
}

/**
 * Finds the day's rain in the hourly points from the current hour on: wet hours (at least 0.2 mm) separated by at most
 * two dry ones form one spell. The spell it reports is the one with the most rain, or the current one when that holds at
 * least half as much. Intensity comes from the spell's wettest hour (light under 2.5 mm, moderate under 7.6 mm, else heavy).
 */
fun summarizeRain(hours: List<WeatherHour>, now: Instant, window: Duration = WINDOW): RainSummary {
    val from = now.truncatedTo(ChronoUnit.HOURS)
    val windowEnd = from.plus(window)
    val slots = hours.filter { it.precipitationMm != null && it.time >= from && it.time < windowEnd }.sortedBy { it.time }
    val runs = wetRuns(slots.filter { (it.precipitationMm ?: 0.0) >= WET_HOUR_MM })
    val best = runs.maxByOrNull { it.totalMm }
    return when {
        slots.isEmpty() -> RainSummary.NoData
        best == null -> RainSummary.Dry
        else -> describe(best, runs.firstOrNull { it.start <= now && now < it.end }, now, windowEnd)
    }
}

/** The spell to report: the current one when it holds at least half as much as the [best], else the [best]. */
private fun describe(best: WetRun, current: WetRun?, now: Instant, windowEnd: Instant): RainSummary {
    val chosen = if (current != null && current.totalMm >= best.totalMm * CURRENT_RUN_SHARE) current else best
    val until = chosen.end.takeIf { it < windowEnd }
    val intensity = intensityOf(chosen.peakMm)
    return if (chosen === current) {
        val remaining = chosen.slots.filter { it.time.plus(1, ChronoUnit.HOURS) > now }.sumOf { it.precipitationMm ?: 0.0 }
        RainSummary.RainNow(until, remaining, intensity)
    } else {
        RainSummary.RainLater(chosen.start, until, chosen.totalMm, intensity)
    }
}

private fun wetRuns(wet: List<WeatherHour>): List<WetRun> {
    val runs = mutableListOf<MutableList<WeatherHour>>()
    for (slot in wet) {
        val last = runs.lastOrNull()?.last()
        if (last != null && Duration.between(last.time, slot.time) <= Duration.ofHours(MAX_DRY_GAP_HOURS + 1)) runs.last() += slot else runs += mutableListOf(slot)
    }
    return runs.map(::WetRun)
}

private fun intensityOf(peakMm: Double): RainIntensity = when {
    peakMm >= HEAVY_PEAK_MM -> RainIntensity.HEAVY
    peakMm >= MODERATE_PEAK_MM -> RainIntensity.MODERATE
    else -> RainIntensity.LIGHT
}
