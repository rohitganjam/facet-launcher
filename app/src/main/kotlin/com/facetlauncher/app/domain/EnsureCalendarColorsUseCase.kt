package com.facetlauncher.app.domain

import com.facetlauncher.app.data.CalendarPermissionRepository
import com.facetlauncher.app.data.CalendarRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.CalendarInfo
import com.facetlauncher.app.ui.theme.AccentSwatch
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Gives every calendar on the device, the Work Profile's included, its stable bar colour: the one place that writes
 * [com.facetlauncher.app.data.model.LauncherSettings.calendarColors] for a calendar nobody has opened Appearance for yet.
 */
@Singleton
class EnsureCalendarColorsUseCase @Inject constructor(
    private val permission: CalendarPermissionRepository,
    private val calendars: CalendarRepository,
    private val settings: SettingsRepository,
    private val assign: AssignCalendarColorsUseCase,
) {
    private val lock = Mutex()
    private val palette = AccentSwatch.entries.map { it.name }

    /** Assigns colours to [list], or to every selectable calendar when it is `null` (a no-op without calendar access). */
    suspend operator fun invoke(list: List<CalendarInfo>? = null) = lock.withLock {
        val known = list ?: if (permission.isGranted()) calendars.getSelectableCalendars() else return@withLock
        val existing = settings.settings.first().calendarColors
        val updated = assign(known.map { it.id }, existing, palette)
        if (updated != existing) settings.setCalendarColors(updated)
    }

    /** Runs [invoke] now and again on every calendar change on the device, so a new calendar has a colour the moment it appears. */
    fun keepAssigned(): Flow<Unit> = calendars.observeCalendarChanges().map { invoke() }
}
