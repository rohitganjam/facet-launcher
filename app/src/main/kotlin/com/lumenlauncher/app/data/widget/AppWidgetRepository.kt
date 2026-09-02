package com.lumenlauncher.app.data.widget

import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import com.lumenlauncher.app.data.model.WidgetProviderOption
import com.lumenlauncher.app.domain.HUB_COLUMNS
import com.lumenlauncher.app.domain.calculateHubCellWidth
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

/**
 * Android's own widget-sizing convention (developer.android.com/develop/ui/views/appwidgets/layouts#anatomy) —
 * a provider's minWidth/minHeight includes an assumed amount of padding that the host (launcher)
 * is expected to "add back" to find the target cell count.
 */
private const val WIDGET_CELL_PADDING_DP = 30

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

    /**
     * Converts a provider's min dimension (dp) into Hub grid cells. The Hub grid is square,
     * so the same [cellUnitDp] applies to both width and height.
     */
    private fun dpToCells(dp: Int, cellUnitDp: Int): Int =
        ((dp + WIDGET_CELL_PADDING_DP) / cellUnitDp).coerceAtLeast(1)

    fun getInstalledProviders(): List<AppWidgetProviderInfo> = appWidgetManager.installedProviders

    /** Stable UI options for the add-widget picker (README `4c`) — grouped by the picker's own owning-app logic, not here. */
    fun getWidgetProviderOptions(): List<WidgetProviderOption> {
        val packageManager = context.packageManager
        val cellUnitDp = calculateHubCellWidth(context)
        return getInstalledProviders().mapNotNull { info ->
            val appLabel = runCatching {
                packageManager.getApplicationLabel(packageManager.getApplicationInfo(info.provider.packageName, 0)).toString()
            }.getOrNull() ?: return@mapNotNull null
            
            // A widget's span must never exceed the grid's own column count, even if its
            // minWidth metadata is large (e.g. a 6-column requirement on a 5-column grid).
            val columns = dpToCells(info.minWidth, cellUnitDp).coerceAtMost(HUB_COLUMNS)
            val rows = dpToCells(info.minHeight, cellUnitDp)

            WidgetProviderOption(
                provider = info.provider,
                appLabel = appLabel,
                widgetLabel = info.loadLabel(packageManager),
                columns = columns,
                rows = rows,
                // Most providers declare a preview image; ones that don't fall back to the app's
                // own icon, matching AOSP's own launcher convention for the add-widget picker.
                previewIcon = (info.loadPreviewImage(context, 0) ?: info.loadIcon(context, 0))?.toBitmap(),
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

    /**
     * `null` when [provider] declares no configure activity — the add flow skips straight to
     * placing it. A configure Activity is frequently *not* exported (it's only meant to be
     * reachable through the widget host that owns the id), so this must launch through
     * [LauncherAppWidgetHost.configureIntentSender] rather than a bare `Intent` — see its doc
     * comment for why a raw `Intent` crashes on providers like Slack's.
     */
    fun createConfigureIntentSender(appWidgetId: Int, provider: AppWidgetProviderInfo): IntentSender? {
        provider.configure ?: return null
        return host.configureIntentSender(appWidgetId)
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

/** Drawables from `loadPreviewImage`/`loadIcon` aren't always a [BitmapDrawable] (vector/adaptive icons aren't) — rasterize explicitly. */
private fun Drawable.toBitmap(): Bitmap {
    (this as? BitmapDrawable)?.bitmap?.let { return it }
    val width = intrinsicWidth.coerceAtLeast(1)
    val height = intrinsicHeight.coerceAtLeast(1)
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    setBounds(0, 0, width, height)
    draw(Canvas(bitmap))
    return bitmap
}
