package com.lumenlauncher.app.data.widget

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** This app's single [AppWidgetHost] instance — never create a second one with this id. */
const val HUB_APP_WIDGET_HOST_ID = 1024

/**
 * Pushes [onProviderChanged] callbacks (a bound provider's app was updated) into a [Flow] so
 * [AppWidgetRepository.observeProviderChanges] can expose them without composables/ViewModels
 * touching this class directly — per `CLAUDE.md`, only a `Repository` touches a framework object
 * like this one.
 */
class LauncherAppWidgetHost(context: Context) : AppWidgetHost(context, HUB_APP_WIDGET_HOST_ID) {

    private val _providerChanges = MutableSharedFlow<Int>(extraBufferCapacity = 8)
    val providerChanges: SharedFlow<Int> = _providerChanges.asSharedFlow()

    override fun onProviderChanged(appWidgetId: Int, appWidget: AppWidgetProviderInfo) {
        super.onProviderChanged(appWidgetId, appWidget)
        _providerChanges.tryEmit(appWidgetId)
    }
}
