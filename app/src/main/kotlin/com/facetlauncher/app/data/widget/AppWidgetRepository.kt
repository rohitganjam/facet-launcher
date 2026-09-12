package com.facetlauncher.app.data.widget

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
import android.os.Bundle
import android.util.SizeF
import com.facetlauncher.app.data.model.WidgetProviderOption
import com.facetlauncher.app.domain.HUB_COLUMNS
import com.facetlauncher.app.domain.calculateHubCellWidth
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
 * [com.facetlauncher.app.data.WidgetPlacementRepository], which wraps Room only, mirroring this
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

    /**
     * A provider's default span, in Hub grid cells (columns to rows). Prefers `targetCellWidth`/
     * `targetCellHeight` (API 31+): a modern clock/weather widget commonly declares `minWidth`
     * near 0 and its real intent as `4 x 2` cells — the legacy dp math would place it 1x1, far
     * too small for it to render. Falls back to `minWidth`/`minHeight` for older providers.
     */
    fun defaultSpanFor(info: AppWidgetProviderInfo): Pair<Int, Int> {
        val cellUnitDp = calculateHubCellWidth(context)
        val columns = (info.targetCellWidth.takeIf { it > 0 } ?: dpToCells(info.minWidth, cellUnitDp))
            .coerceIn(1, HUB_COLUMNS)
        val rows = (info.targetCellHeight.takeIf { it > 0 } ?: dpToCells(info.minHeight, cellUnitDp))
            .coerceAtLeast(1)
        return columns to rows
    }

    /** [defaultSpanFor] by id — `null` when the id is orphaned (its provider was uninstalled). */
    fun defaultSpan(appWidgetId: Int): Pair<Int, Int>? = getAppWidgetInfo(appWidgetId)?.let(::defaultSpanFor)

    fun getInstalledProviders(): List<AppWidgetProviderInfo> = appWidgetManager.installedProviders

    /** Stable UI options for the add-widget picker (README `4c`) — grouped by the picker's own owning-app logic, not here. */
    fun getWidgetProviderOptions(): List<WidgetProviderOption> {
        val packageManager = context.packageManager
        return getInstalledProviders().mapNotNull { info ->
            val appLabel = runCatching {
                packageManager.getApplicationLabel(packageManager.getApplicationInfo(info.provider.packageName, 0)).toString()
            }.getOrNull() ?: return@mapNotNull null

            val (columns, rows) = defaultSpanFor(info)

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

    /**
     * Pushes a hosted widget's on-screen size (dp) into its options. Sets `OPTION_APPWIDGET_SIZES`
     * (API 31+) explicitly: modern RemoteViews/Glance widgets (Samsung's clock, battery, ...) read
     * it to pick a responsive layout, and an *empty* list makes them throw `NoSuchElementException`
     * mid-recomposition so the host just shows "Can't show content". `AppWidgetHostView`'s own
     * `updateAppWidgetSize` leaves the list `[]` on One UI (confirmed via `dumpsys appwidget` —
     * every other launcher populates it), so the Hub has to set it here.
     */
    fun updateWidgetSize(appWidgetId: Int, widthDp: Int, heightDp: Int) {
        if (widthDp <= 0 || heightDp <= 0) return
        appWidgetManager.updateAppWidgetOptions(
            appWidgetId,
            Bundle().apply {
                putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, widthDp)
                putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, heightDp)
                putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, widthDp)
                putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, heightDp)
                putParcelableArrayList(
                    AppWidgetManager.OPTION_APPWIDGET_SIZES,
                    arrayListOf(SizeF(widthDp.toFloat(), heightDp.toFloat())),
                )
            },
        )
    }

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
