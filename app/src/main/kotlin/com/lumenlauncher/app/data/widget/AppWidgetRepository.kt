package com.lumenlauncher.app.data.widget

import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.lumenlauncher.app.data.model.WidgetProviderOption
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

/** Android's own widget-sizing convention (developer.android.com/develop/ui/views/appwidgets/layouts#anatomy) — a cell is ~70dp with ~30dp of combined margin/padding per cell. */
private const val WIDGET_CELL_UNIT_DP = 70
private const val WIDGET_CELL_PADDING_DP = 30

private fun dpToCells(dp: Int): Int = ((dp + WIDGET_CELL_PADDING_DP) / WIDGET_CELL_UNIT_DP).coerceAtLeast(1)

/**
 * Every `AppWidgetManager`/`AppWidgetHost` call for the Hub (F5) lives here — nothing else in
 * the app touches either class directly, per `CLAUDE.md`'s layering rule. Kept separate from
 * [com.lumenlauncher.app.data.WidgetPlacementRepository], which wraps Room only, mirroring this
 * codebase's existing split-by-Android-surface convention (e.g. `CalendarRepository` vs.
 * `CalendarPermissionRepository`).
 */
@Singleton
class AppWidgetRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appWidgetManager: AppWidgetManager,
    private val host: LauncherAppWidgetHost,
) {

    fun getInstalledProviders(): List<AppWidgetProviderInfo> = appWidgetManager.installedProviders

    /** Stable UI options for the add-widget picker (README `4c`) — grouped by the picker's own owning-app logic, not here. */
    fun getWidgetProviderOptions(): List<WidgetProviderOption> {
        val packageManager = context.packageManager
        return getInstalledProviders().mapNotNull { info ->
            val appLabel = runCatching {
                packageManager.getApplicationLabel(packageManager.getApplicationInfo(info.provider.packageName, 0)).toString()
            }.getOrNull() ?: return@mapNotNull null
            WidgetProviderOption(
                provider = info.provider,
                appLabel = appLabel,
                widgetLabel = info.loadLabel(packageManager),
                columns = dpToCells(info.minWidth),
                rows = dpToCells(info.minHeight),
            )
        }
    }

    fun allocateAppWidgetId(): Int = host.allocateAppWidgetId()

    /** `false` means the system needs to show its own bind-permission dialog — launch [createBindIntent] instead. */
    fun bindAppWidgetIdIfAllowed(appWidgetId: Int, provider: ComponentName): Boolean =
        appWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId, provider)

    fun createBindIntent(appWidgetId: Int, provider: ComponentName): Intent =
        Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, provider)
        }

    /** `null` when [provider] declares no configure activity — the add flow skips straight to placing it. */
    fun createConfigureIntent(appWidgetId: Int, provider: AppWidgetProviderInfo): Intent? {
        val configureComponent = provider.configure ?: return null
        return Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
            component = configureComponent
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
    }

    /** `null` means orphaned — the provider's app was uninstalled (or the id is otherwise unknown). */
    fun getAppWidgetInfo(appWidgetId: Int): AppWidgetProviderInfo? = appWidgetManager.getAppWidgetInfo(appWidgetId)

    /** `null` when orphaned — resolving the label needs `PackageManager`, so it stays in this Repository rather than domain/. */
    fun getProviderLabel(appWidgetId: Int): String? = getAppWidgetInfo(appWidgetId)?.loadLabel(context.packageManager)

    /** Releases the host id — always call this alongside removing the placement row, or the id leaks. */
    fun deleteAppWidgetId(appWidgetId: Int) = host.deleteAppWidgetId(appWidgetId)

    fun startListening() = host.startListening()

    fun stopListening() = host.stopListening()

    fun createHostView(context: Context, appWidgetId: Int, info: AppWidgetProviderInfo): AppWidgetHostView =
        host.createView(context, appWidgetId, info)

    fun observeProviderChanges(): Flow<Int> = host.providerChanges
}
