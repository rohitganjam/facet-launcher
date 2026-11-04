package com.facetlauncher.app.domain

import com.facetlauncher.app.data.ClockStackRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WidgetPlacementRepository
import com.facetlauncher.app.data.model.NativeWidgetRef
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/** What the widgets read for their own calendars and locations; an interface so a test can pass a fixed answer. */
fun interface NativeWidgetOverrides {
    operator fun invoke(): Flow<Map<NativeWidgetRef, NativeWidgetOverride>>
}

/**
 * The data of every Agenda and Weather widget that is set to its own calendars or location, Hub tiles and the active facet's Home
 * pages alike, by [NativeWidgetRef]. A widget that follows the launcher has no entry, so the launcher-wide state is used for it.
 * Re-derived every [ObserveWeatherUseCase.TICK], which also lets a forecast refresh once MET's `Expires` has passed.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ObserveNativeWidgetOverridesUseCase @Inject constructor(
    private val widgetPlacementRepository: WidgetPlacementRepository,
    private val clockStackRepository: ClockStackRepository,
    private val settingsRepository: SettingsRepository,
    private val resolver: NativeWidgetOverrideResolver,
) : NativeWidgetOverrides {
    override fun invoke(): Flow<Map<NativeWidgetRef, NativeWidgetOverride>> = settingsRepository.settings.flatMapLatest { settings ->
        combine(
            widgetPlacementRepository.observeAll(),
            clockStackRepository.observe(settings.activeFacetId),
        ) { placements, items ->
            val hub = placements.filter { it.nativeWidgetType != null }.map { NativeWidgetSetting<NativeWidgetRef>(NativeWidgetRef.Hub(it.appWidgetId), it.nativeWidgetType, it.nativeWidgetConfig) }
            val home = items.filter { it.nativeWidgetType != null }.map { NativeWidgetSetting<NativeWidgetRef>(NativeWidgetRef.Home(it.id), it.nativeWidgetType, it.nativeWidgetConfig) }
            hub + home
        }.distinctUntilChanged().flatMapLatest { widgets ->
            flow {
                while (true) {
                    emit(resolver.resolve(widgets, settings))
                    delay(ObserveWeatherUseCase.TICK.toMillis())
                }
            }
        }
    }
}
