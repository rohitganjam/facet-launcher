package com.facetlauncher.app.domain

import com.facetlauncher.app.data.ClockStackRepository
import com.facetlauncher.app.data.widget.AppWidgetRepository
import javax.inject.Inject

/**
 * Removes one page from a facet's clock stack and releases the `AppWidgetHost` id it held, so none leaks. Removing is
 * never a Pro action, and it may leave the stack empty, which draws nothing in the clock area.
 */
class RemoveClockStackItemUseCase @Inject constructor(
    private val clockStackRepository: ClockStackRepository,
    private val appWidgetRepository: AppWidgetRepository,
) {
    suspend operator fun invoke(itemId: Long) {
        clockStackRepository.remove(itemId)?.let { appWidgetRepository.deleteAppWidgetId(it) }
    }
}
