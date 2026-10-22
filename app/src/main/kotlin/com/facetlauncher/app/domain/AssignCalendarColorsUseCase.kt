package com.facetlauncher.app.domain

import javax.inject.Inject

/**
 * Assigns each calendar id in [calendarIds] a color-name token from [palette], leaving every
 * entry already in [existingColors] untouched — a calendar keeps its bar color for good once
 * assigned, even if the calendar list is re-fetched in a different order on a later run. New
 * calendars are handed the first [palette] entry not already in use; once every entry is taken,
 * it cycles back through [palette] in order. Pure/stateless (no repository access) so the caller
 * owns persisting the result.
 */
class AssignCalendarColorsUseCase @Inject constructor() {
    operator fun invoke(
        calendarIds: List<String>,
        existingColors: Map<String, String>,
        palette: List<String>,
    ): Map<String, String> {
        if (palette.isEmpty()) return existingColors
        val used = existingColors.values.toMutableList()
        val result = existingColors.toMutableMap()
        calendarIds.forEach { id ->
            if (id !in result) {
                val next = palette.firstOrNull { it !in used } ?: palette[result.size % palette.size]
                result[id] = next
                used += next
            }
        }
        return result
    }
}
