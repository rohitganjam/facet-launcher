package com.lumenlauncher.app.domain

import com.lumenlauncher.app.data.WidgetPlacementRepository
import com.lumenlauncher.app.data.widget.AppWidgetRepository
import javax.inject.Inject

/**
 * Deletes a placed widget: releases the `AppWidgetHost` id and removes the Room placement row
 * together — spans two repositories, so it's a UseCase rather than living in either repository
 * or a ViewModel directly. Neither call alone is correct: dropping only the Room row leaks the
 * host id; releasing only the id leaves a placement row pointing at a freed one.
 */
class DeleteWidgetUseCase @Inject constructor(
    private val widgetPlacementRepository: WidgetPlacementRepository,
    private val appWidgetRepository: AppWidgetRepository,
) {
    suspend operator fun invoke(appWidgetId: Int) {
        widgetPlacementRepository.deleteById(appWidgetId)
        appWidgetRepository.deleteAppWidgetId(appWidgetId)
    }
}
