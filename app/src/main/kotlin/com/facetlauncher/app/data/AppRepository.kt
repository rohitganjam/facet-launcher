package com.facetlauncher.app.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.LauncherApps
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Process
import android.os.UserHandle
import android.os.UserManager
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppProfile
import dagger.hilt.android.qualifiers.ApplicationContext
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
 * built-in mask then visually fights [AppIcon][com.facetlauncher.app.ui.components.AppIcon]'s own
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

/**
 * Broadcasts that mean "the set of profiles itself changed" (a Work Profile was enrolled,
 * unenrolled, or its quiet-mode state flipped) rather than "a package within an already-known
 * profile changed" — [LauncherApps.Callback] only ever reports the latter, so these need their
 * own [BroadcastReceiver] to keep [AppRepository.observeInstalledApps] live across profile
 * add/remove, not just package add/remove within a profile that was already there.
 */
private val PROFILE_CHANGE_ACTIONS = IntentFilter().apply {
    addAction(Intent.ACTION_MANAGED_PROFILE_ADDED)
    addAction(Intent.ACTION_MANAGED_PROFILE_REMOVED)
    addAction(Intent.ACTION_MANAGED_PROFILE_AVAILABLE)
    addAction(Intent.ACTION_MANAGED_PROFILE_UNAVAILABLE)
}

/** Wraps [LauncherApps] to expose the launchable apps visible to this launcher, across every profile (personal + a Work Profile, if one exists). */
@Singleton
class AppRepository @Inject constructor(
    private val launcherApps: LauncherApps,
    private val userManager: UserManager,
    @ApplicationContext private val context: Context,
) {

    /**
     * The primary user's own handle is always [AppProfile.PERSONAL]. Any other handle is
     * positively checked against [LauncherApps.getLauncherUserInfo]'s `userType`
     * (`UserManager.USER_TYPE_PROFILE_MANAGED` = a real Work Profile) rather than assumed —
     * confirmed live on a real API 36 emulator (see chat history) that `UserManager.getUserProfiles()`
     * *also* returns an Android 15+ Private Space's handle even without holding
     * `ACCESS_HIDDEN_PROFILES`, and `getLauncherUserInfo` correctly comes back `null` for it in
     * that case (a Work Profile's comes back non-null, `userType = "...profile.MANAGED"`) — so a
     * naive "not primary = WORK" check would have shown a permanently-empty "Work" tab/Settings
     * row on any device with a Private Space configured, real Work Profile or not.
     * `getLauncherUserInfo` itself needs API 35 — on 33/34 (where Private Space can't exist yet
     * anyway) this falls back to the old, still-safe "not primary" check. Public — the single
     * source of truth for this check, reused by [com.facetlauncher.app.data.widget.AppWidgetRepository]
     * and (via [resolveUserHandle]) [WorkProfileRepository] rather than each keeping its own copy.
     */
    fun profileFor(handle: UserHandle): AppProfile {
        if (handle == Process.myUserHandle()) return AppProfile.PERSONAL
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) return AppProfile.WORK
        val isManaged = runCatching { launcherApps.getLauncherUserInfo(handle)?.userType }.getOrNull() == UserManager.USER_TYPE_PROFILE_MANAGED
        return if (isManaged) AppProfile.WORK else AppProfile.PERSONAL
    }

    /**
     * Resolves [profile] to its real Android user handle, for callers that need to launch an
     * activity in it (see [com.facetlauncher.app.LauncherActivity.launchApp]) — `null` only if a
     * Work Profile was expected but no longer exists (a race with unenrollment; the live app list
     * should already be dropping its apps around the same time via [observeProfileRemoved]).
     */
    fun resolveUserHandle(profile: AppProfile): UserHandle? = userManager.userProfiles.firstOrNull { profileFor(it) == profile }

    /**
     * All launchable activities across every profile (personal, plus a Work Profile if one
     * exists), sorted alphabetically (locale-aware) as one combined list. Each app's icon
     * decode/flatten runs as its own [async] on [Dispatchers.Default]'s thread pool rather than
     * one after another — on a real device with 100+ installed apps, decoding every icon
     * sequentially before this ever emits once took multiple real seconds; spreading that work
     * across all available cores cuts it down to roughly the slowest single icon instead of the
     * sum of all of them (see chat history: this was invisible before Home started gating its own
     * first paint on this list, since default content rendered over it in the meantime).
     */
    suspend fun getInstalledApps(): List<AppInfo> = withContext(Dispatchers.Default) {
        val collator = Collator.getInstance()
        userManager.userProfiles
            .flatMap { handle -> launcherApps.getActivityList(null, handle).map { it to profileFor(handle) } }
            .map { (info, profile) ->
                async {
                    AppInfo(
                        packageName = info.applicationInfo.packageName,
                        activityName = info.componentName.className,
                        label = info.label.toString(),
                        icon = runCatching { flattenIcon(info.getIcon(0)).asImageBitmap() }.getOrNull(),
                        profile = profile,
                    )
                }
            }
            .awaitAll()
            .sortedWith(Comparator { a, b -> collator.compare(a.label, b.label) })
    }

    /**
     * Live-updating installed-app list — re-emits whenever a package is added, removed, or
     * changed in any profile while the launcher is running, via [LauncherApps.registerCallback],
     * or whenever a Work Profile itself is added/removed/paused/resumed, via [PROFILE_CHANGE_ACTIONS]
     * (a profile appearing or disappearing isn't a per-package event, so [LauncherApps.Callback]
     * alone can't be relied on to trigger a refresh for it). [getInstalledApps] only fetches once;
     * without this, any install/uninstall happening while Facet is in the foreground (including
     * via the app long-press context menu's Uninstall action) wouldn't be reflected anywhere —
     * Home/Drawer/Dock would keep showing the stale entry until the process restarts.
     * [FavoriteAppRepository]/[DockAppRepository] both hydrate against this live flow too, so an
     * uninstall collapses Favorites/Dock/Drawer alike; one-shot callers (picker seed steps) can
     * still use [getInstalledApps] directly where a live subscription isn't needed.
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

        val profileReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) = refresh()
        }
        ContextCompat.registerReceiver(context, profileReceiver, PROFILE_CHANGE_ACTIONS, ContextCompat.RECEIVER_NOT_EXPORTED)

        refresh()
        awaitClose {
            launcherApps.unregisterCallback(callback)
            context.unregisterReceiver(profileReceiver)
        }
    }

    /**
     * Emits `(packageName, profile)` each time an app is genuinely, permanently uninstalled from
     * some profile — unlike [observeInstalledApps], which also re-emits (the *whole* list) for
     * reasons an app might only be momentarily missing (e.g. `onPackagesUnavailable` mid-update).
     * The profile is included because a personal and Work Profile app can share the exact same
     * package name (a duplicate-installed app) — without it, uninstalling one would look
     * indistinguishable from uninstalling the other to callers. Backs
     * [com.facetlauncher.app.domain.CleanUpUninstalledAppsUseCase], which deletes the
     * corresponding Favorites/Dock rows outright rather than just filtering them from view. Does
     * *not* cover a whole profile being removed (see [observeProfileRemoved] instead) — that's a
     * profile-level event, not a per-package one, and isn't guaranteed to fire a per-app callback
     * for every app on its way out.
     */
    fun observeUninstalledPackages(): Flow<Pair<String, AppProfile>> = callbackFlow {
        val callback = object : LauncherApps.Callback() {
            override fun onPackageAdded(packageName: String?, user: UserHandle?) = Unit
            override fun onPackageRemoved(packageName: String?, user: UserHandle?) {
                val packageAndProfile = packageName?.let { it to profileFor(user ?: Process.myUserHandle()) }
                packageAndProfile?.let { trySend(it) }
            }
            override fun onPackageChanged(packageName: String?, user: UserHandle?) = Unit
            override fun onPackagesAvailable(packageNames: Array<out String>?, user: UserHandle?, replacing: Boolean) = Unit
            override fun onPackagesUnavailable(packageNames: Array<out String>?, user: UserHandle?, replacing: Boolean) = Unit
        }
        launcherApps.registerCallback(callback)
        awaitClose { launcherApps.unregisterCallback(callback) }
    }

    /**
     * Emits once whenever the Work Profile itself is removed (unenrolled) — a coarser, bulk
     * signal deliberately kept separate from [observeUninstalledPackages]: removing a whole
     * profile tears down every app in it atomically, and there's no guarantee each one also fires
     * its own [LauncherApps.Callback.onPackageRemoved] on the way out. Callers should treat this
     * as "drop every stored row tagged [AppProfile.WORK] outright", not wait on per-package events.
     */
    fun observeProfileRemoved(): Flow<Unit> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_MANAGED_PROFILE_REMOVED) trySend(Unit)
            }
        }
        val filter = IntentFilter(Intent.ACTION_MANAGED_PROFILE_REMOVED)
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        awaitClose { context.unregisterReceiver(receiver) }
    }
}
