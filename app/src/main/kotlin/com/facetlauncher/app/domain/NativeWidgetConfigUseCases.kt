package com.facetlauncher.app.domain

import com.facetlauncher.app.data.ClockStackRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WidgetPlacementRepository
import com.facetlauncher.app.data.model.NativeWidgetConfig
import com.facetlauncher.app.data.model.NativeWidgetRef
import com.facetlauncher.app.data.model.NativeWidgetType
import com.facetlauncher.app.data.model.decodeNativeWidgetConfig
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

/** A placed native widget: what it is and what it is set to. */
data class NativeWidgetSettings(val type: NativeWidgetType, val config: NativeWidgetConfig)

/** What the widget [NativeWidgetRef] points at is set to, wherever it lives; `null` when it is gone. Spans the Hub and Home stores, so it is a use case. */
@OptIn(ExperimentalCoroutinesApi::class)
class ObserveNativeWidgetSettingsUseCase @Inject constructor(
    private val widgetPlacementRepository: WidgetPlacementRepository,
    private val clockStackRepository: ClockStackRepository,
    private val settingsRepository: SettingsRepository,
) {
    operator fun invoke(ref: NativeWidgetRef): Flow<NativeWidgetSettings?> = when (ref) {
        is NativeWidgetRef.Hub -> widgetPlacementRepository.observeAll().map { placements ->
            placements.firstOrNull { it.appWidgetId == ref.appWidgetId }?.let { settingsOf(it.nativeWidgetType, it.nativeWidgetConfig) }
        }
        is NativeWidgetRef.Home -> settingsRepository.settings.map { it.activeFacetId }.distinctUntilChanged().flatMapLatest { facetId ->
            clockStackRepository.observe(facetId).map { items -> items.firstOrNull { it.id == ref.itemId }?.let { settingsOf(it.nativeWidgetType, it.nativeWidgetConfig) } }
        }
    }

    private fun settingsOf(type: NativeWidgetType?, encoded: String?) = type?.let { NativeWidgetSettings(it, decodeNativeWidgetConfig(encoded)) }
}

/** Saves what the widget [NativeWidgetRef] points at is set to, wherever it lives. */
class SetNativeWidgetConfigUseCase @Inject constructor(
    private val widgetPlacementRepository: WidgetPlacementRepository,
    private val clockStackRepository: ClockStackRepository,
) {
    suspend operator fun invoke(ref: NativeWidgetRef, config: NativeWidgetConfig) = when (ref) {
        is NativeWidgetRef.Hub -> widgetPlacementRepository.setConfig(ref.appWidgetId, config)
        is NativeWidgetRef.Home -> clockStackRepository.setConfig(ref.itemId, config)
    }
}
