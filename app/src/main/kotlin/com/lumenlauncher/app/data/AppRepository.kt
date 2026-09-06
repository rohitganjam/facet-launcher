package com.lumenlauncher.app.data

import android.content.pm.LauncherApps
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.os.Process
import android.os.UserHandle
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import com.lumenlauncher.app.data.model.AppInfo
import java.text.Collator
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Adaptive icons (API 26+) reserve their guaranteed-visible content to a centered "safe zone" —
 * about 66% of the full 108dp canvas — and the OS's own default drawing masks the rest to a
 * fixed system-wide shape (e.g. a squircle) when the drawable is drawn/flattened directly. That
 * built-in mask then visually fights [AppIcon][com.lumenlauncher.app.ui.components.AppIcon]'s own
 * `clip(RoundedCornerShape(...))`, since the two curves don't line up — visible mismatched
 * corners, worse for icons whose safe-zone content doesn't fill a circle cleanly. [flattenIcon]
 * sidesteps this for adaptive icons by drawing their background/foreground layers itself, scaled
 * past their own safe-zone inset to fill the whole canvas, so the app's own OS-level mask is never
 * applied — [AppIcon]'s clip becomes the *only* shape in play, uniform across every icon
 * regardless of what shape the app itself (or the OS) would otherwise have masked it to. Non-adaptive
 * (legacy) icons already come back as whatever raw shape the app itself baked into its PNG — there's
 * no equivalent "unmask" possible for those, so they're flattened as-is.
 */
private const val ADAPTIVE_ICON_CANVAS_SIZE = 108

/** Undoes ~66%'s worth of built-in safe-zone inset (1 / 0.66 ≈ 1.5) — see [ADAPTIVE_ICON_CANVAS_SIZE]'s doc. */
private const val ADAPTIVE_ICON_SCALE = 1.5f

private fun flattenIcon(drawable: Drawable): Bitmap {
    if (drawable !is AdaptiveIconDrawable) return drawable.toBitmap()
    val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: ADAPTIVE_ICON_CANVAS_SIZE
    val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: ADAPTIVE_ICON_CANVAS_SIZE
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val insetX = (width * (ADAPTIVE_ICON_SCALE - 1f) / 2f).toInt()
    val insetY = (height * (ADAPTIVE_ICON_SCALE - 1f) / 2f).toInt()
    val layerBounds = Rect(-insetX, -insetY, width + insetX, height + insetY)
    drawable.background?.apply { bounds = layerBounds; draw(canvas) }
    drawable.foreground?.apply { bounds = layerBounds; draw(canvas) }
    return bitmap
}

/** Wraps [LauncherApps] to expose the launchable apps visible to this launcher. */
@Singleton
class AppRepository @Inject constructor(private val launcherApps: LauncherApps) {

    /**
     * All launchable activities for the current user, sorted alphabetically (locale-aware).
     * Each app's icon decode/flatten runs as its own [async] on [Dispatchers.Default]'s thread
     * pool rather than one after another — on a real device with 100+ installed apps, decoding
     * every icon sequentially before this ever emits once took multiple real seconds; spreading
     * that work across all available cores cuts it down to roughly the slowest single icon
     * instead of the sum of all of them (see chat history: this was invisible before Home started
     * gating its own first paint on this list, since default content rendered over it in the
     * meantime).
     */
    suspend fun getInstalledApps(): List<AppInfo> = withContext(Dispatchers.Default) {
        val collator = Collator.getInstance()
        launcherApps.getActivityList(null, Process.myUserHandle())
            .map { info ->
                async {
                    AppInfo(
                        packageName = info.applicationInfo.packageName,
                        activityName = info.componentName.className,
                        label = info.label.toString(),
                        icon = runCatching { flattenIcon(info.getIcon(0)).asImageBitmap() }.getOrNull(),
                    )
                }
            }
            .awaitAll()
            .sortedWith(Comparator { a, b -> collator.compare(a.label, b.label) })
    }

    /**
     * Live-updating installed-app list — re-emits whenever a package is added, removed, or
     * changed while the launcher is running, via [LauncherApps.registerCallback]. [getInstalledApps]
     * only fetches once; without this, any install/uninstall happening while Lumen is in the
     * foreground (including via the app long-press context menu's Uninstall action) wouldn't be
     * reflected anywhere — Home/Drawer/Dock would keep showing the stale entry until the process
     * restarts. [FavoriteAppRepository]/[DockAppRepository] both hydrate against this live flow
     * too, so an uninstall collapses Favorites/Dock/Drawer alike; one-shot callers (picker seed
     * steps) can still use [getInstalledApps] directly where a live subscription isn't needed.
     */
    fun observeInstalledApps(): Flow<List<AppInfo>> = callbackFlow {
        val refresh: () -> Unit = { launch { trySend(getInstalledApps()) } }

        val callback = object : LauncherApps.Callback() {
            override fun onPackageAdded(packageName: String?, user: UserHandle?) = refresh()
            override fun onPackageRemoved(packageName: String?, user: UserHandle?) = refresh()
            override fun onPackageChanged(packageName: String?, user: UserHandle?) = refresh()
            override fun onPackagesAvailable(packageNames: Array<out String>?, user: UserHandle?, replacing: Boolean) = refresh()
            override fun onPackagesUnavailable(packageNames: Array<out String>?, user: UserHandle?, replacing: Boolean) = refresh()
        }
        launcherApps.registerCallback(callback)
        refresh()
        awaitClose { launcherApps.unregisterCallback(callback) }
    }

    /**
     * Emits a package name each time it's genuinely, permanently uninstalled — unlike
     * [observeInstalledApps], which also re-emits (the *whole* list) for reasons an app might
     * only be momentarily missing (e.g. `onPackagesUnavailable` mid-update). Backs
     * [com.lumenlauncher.app.domain.CleanUpUninstalledAppsUseCase], which deletes the
     * corresponding Favorites/Dock rows outright rather than just filtering them from view.
     */
    fun observeUninstalledPackages(): Flow<String> = callbackFlow {
        val callback = object : LauncherApps.Callback() {
            override fun onPackageAdded(packageName: String?, user: UserHandle?) = Unit
            override fun onPackageRemoved(packageName: String?, user: UserHandle?) {
                packageName?.let { trySend(it) }
            }
            override fun onPackageChanged(packageName: String?, user: UserHandle?) = Unit
            override fun onPackagesAvailable(packageNames: Array<out String>?, user: UserHandle?, replacing: Boolean) = Unit
            override fun onPackagesUnavailable(packageNames: Array<out String>?, user: UserHandle?, replacing: Boolean) = Unit
        }
        launcherApps.registerCallback(callback)
        awaitClose { launcherApps.unregisterCallback(callback) }
    }
}
