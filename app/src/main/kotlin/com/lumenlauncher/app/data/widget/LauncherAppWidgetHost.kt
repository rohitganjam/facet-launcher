package com.lumenlauncher.app.data.widget

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.content.IntentSender
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

    /**
     * A provider's own configure Activity is very often *not* exported (Slack's, for one) — it's
     * only meant to be reachable through the widget host that actually owns the id, not by any
     * app that happens to know its `ComponentName`. Building a bare `Intent` and starting it
     * directly hits Android's normal exported-Activity check and throws a `SecurityException`
     * (uncaught, this crashes the whole launcher — confirmed via on-device logcat: "Permission
     * Denial ... not exported"). `getIntentSenderForConfigureActivity` (API 31+) instead has the
     * system mint an `IntentSender` scoped to this host/appWidgetId, which carries its own grant
     * to launch that Activity regardless of its exported flag.
     *
     * Note: `getIntentSenderForConfigureActivity` is a hidden (@hide) API in the Android SDK.
     * We access it via reflection to support non-exported configuration activities.
     */
    fun configureIntentSender(appWidgetId: Int): IntentSender? {
        return try {
            val method = AppWidgetHost::class.java.getDeclaredMethod(
                "getIntentSenderForConfigureActivity",
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType
            )
            method.isAccessible = true
            method.invoke(this, appWidgetId, 0) as? IntentSender
        } catch (e: Exception) {
            null
        }
    }
}
