package com.facetlauncher.app.data

import android.content.BroadcastReceiver
import android.content.ComponentName
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
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.os.UserHandle
import android.os.UserManager
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.facetlauncher.app.data.di.ApplicationScope
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppProfile
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.Collator
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
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

/** How long [AppRepository.installedApps] keeps its [LauncherApps] registration alive with zero collectors — long enough to survive a Home↔Drawer transition or a brief config change without a full re-scan, short enough to actually release it when the launcher is genuinely backgrounded. */
private const val SHARE_STOP_TIMEOUT_MILLIS = 5_000L

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
 * unenrolled, or its quiet-mode state flipped; or, via the generic `ACTION_PROFILE_*` actions, any
 * other profile type — e.g. an Android 15+ Private Space — was added, removed, locked, or
 * unlocked) rather than "a package within an already-known profile changed" —
 * [LauncherApps.Callback] only ever reports the latter, so these need their own
 * [BroadcastReceiver] to keep [AppRepository.observeInstalledApps] live across profile
 * add/remove, not just package add/remove within a profile that was already there. The generic
 * actions mirror [PrivateSpaceRepository]'s own `PROFILE_STATE_ACTIONS`, which only drives that
 * repository's lock-state tracking — without them here too, a Private Space added while Facet is
 * already running left [installedApps] cached from before it existed, showing zero apps until the
 * process was force-stopped and relaunched (confirmed live: Private Space correctly appeared in
 * the profile carousel, but its app list stayed empty until restart).
 */
private val PROFILE_CHANGE_ACTIONS = IntentFilter().apply {
    addAction(Intent.ACTION_MANAGED_PROFILE_ADDED)
    addAction(Intent.ACTION_MANAGED_PROFILE_REMOVED)
    addAction(Intent.ACTION_MANAGED_PROFILE_AVAILABLE)
    addAction(Intent.ACTION_MANAGED_PROFILE_UNAVAILABLE)
    addAction(Intent.ACTION_PROFILE_ADDED)
    addAction(Intent.ACTION_PROFILE_REMOVED)
    addAction(Intent.ACTION_PROFILE_AVAILABLE)
    addAction(Intent.ACTION_PROFILE_UNAVAILABLE)
}

/** Wraps [LauncherApps] to expose the launchable apps visible to this launcher, across every profile (personal + a Work Profile, if one exists). */
@Singleton
class AppRepository @Inject constructor(
    private val launcherApps: LauncherApps,
    private val userManager: UserManager,
    @ApplicationContext private val context: Context,
    // Defaulted (rather than required) so the ~90 call sites across the test suite that
    // construct AppRepository directly, without going through Hilt, don't all need updating —
    // Hilt itself always supplies the real @ApplicationScope binding from AppModule regardless of
    // this default, since Dagger-generated code passes every constructor parameter explicitly.
    @ApplicationScope private val applicationScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {

    /**
     * The primary user's own handle is always [AppProfile.PERSONAL]. Any other handle is
     * positively checked against [LauncherApps.getLauncherUserInfo]'s `userType`
     * (`UserManager.USER_TYPE_PROFILE_MANAGED` = a real Work Profile, `USER_TYPE_PROFILE_PRIVATE`
     * = an Android 15+ Private Space) rather than assumed — confirmed live on a real API 36
     * emulator (see chat history) that `UserManager.getUserProfiles()` returns a Private Space's
     * handle even without holding `ACCESS_HIDDEN_PROFILES`, and `getLauncherUserInfo` correctly
     * comes back `null` for it without that permission. Anything that isn't positively WORK or
     * PRIVATE — an unrecognized `userType` (e.g. an OEM dual-app/clone profile, which reports its
     * own distinct type), or *any* non-primary handle below API 35 where `getLauncherUserInfo`
     * doesn't exist at all — becomes [AppProfile.OTHER], never [AppProfile.PERSONAL] and never
     * [AppProfile.WORK]: a naive "not primary = WORK" guess (this app's old behavior below API 35)
     * misclassified exactly this clone-profile case as a Work Profile, which both crashed (calling
     * `UserManager.isQuietModeEnabled` on a handle that isn't actually managed) and hid clone apps
     * behind a Work tab instead of the main list — see chat history. This is purely a display
     * classification, not an identity one (see [AppProfile]'s own doc / [AppInfo.userHandle]), so
     * guessing wrong here below API 35 only costs a badge/tab, never a misrouted launch or a data
     * collision. Public — the single source of truth for this check, reused by
     * [com.facetlauncher.app.data.widget.AppWidgetRepository] and (via [resolveUserHandle])
     * [WorkProfileRepository]/[PrivateSpaceRepository] rather than each keeping its own copy.
     */
    fun profileFor(handle: UserHandle): AppProfile {
        if (handle == Process.myUserHandle()) return AppProfile.PERSONAL
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) return AppProfile.OTHER
        val userType = runCatching { launcherApps.getLauncherUserInfo(handle)?.userType }.getOrNull()
        return when (userType) {
            UserManager.USER_TYPE_PROFILE_MANAGED -> AppProfile.WORK
            UserManager.USER_TYPE_PROFILE_PRIVATE -> AppProfile.PRIVATE
            else -> AppProfile.OTHER
        }
    }

    /**
     * Resolves [profile] to *a* live handle classified as that category — `null` if none exists
     * (e.g. a race with unenrollment). Only for callers that need "the" Work Profile/Private Space
     * without an [AppInfo] in hand (e.g. [PrivateSpaceRepository] checking lock state) — a
     * single-representative lookup, not an identity resolution: [AppProfile.WORK]/[AppProfile.PRIVATE]
     * are only ever assigned to a positively-classified handle, so in practice this is a genuine
     * 1:1 lookup, but it deliberately isn't used anywhere identity actually matters (launch
     * routing, Favorites/Dock/Folder keys) — those use [AppInfo.userHandle] directly instead,
     * since a display category is never guaranteed unique the way a real handle is.
     */
    fun resolveUserHandle(profile: AppProfile): UserHandle? = userManager.userProfiles.firstOrNull { profileFor(it) == profile }

    /**
     * Every currently live handle classified as [profile] — unlike [resolveUserHandle], doesn't
     * collapse to just the first one. [RepairOrphanedProfileRowsUseCase] needs the real count (not
     * just "does at least one exist") to know whether a pre-identity-column row can be
     * unambiguously backfilled.
     */
    fun handlesFor(profile: AppProfile): List<UserHandle> = userManager.userProfiles.filter { profileFor(it) == profile }

    /** [observeInstalledApps], scoped to a single [profile] — the raw source [PrivateSpaceRepository] reads its own app list from. */
    fun observeAppsForProfile(profile: AppProfile): Flow<List<AppInfo>> = observeInstalledApps().map { apps -> apps.filter { it.profile == profile } }

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
            // A device-specific LauncherApps quirk for one odd profile (e.g. an OEM clone profile
            // that doesn't fully honor the LauncherApps contract) shouldn't take down the whole
            // app list — skip that handle's apps rather than crashing the entire fetch.
            .flatMap { handle -> runCatching { launcherApps.getActivityList(null, handle) }.getOrDefault(emptyList()).map { it to handle } }
            .map { (info, handle) ->
                async {
                    // getActivityList() succeeding doesn't guarantee every entry it returns is
                    // safe to read — a single LauncherActivityInfo's embedded Parcelable fields
                    // (applicationInfo, componentName, label) can still throw BadParcelableException
                    // on first access if the OS/OEM's LauncherApps implementation for that profile
                    // (e.g. an OEM clone profile) hands back something that doesn't unmarshal
                    // cleanly in this process — confirmed via a real Techno Spark 20 Pro crash.
                    // One bad entry should drop just itself, not the whole list.
                    runCatching {
                        AppInfo(
                            packageName = info.applicationInfo.packageName,
                            activityName = info.componentName.className,
                            label = info.label.toString(),
                            icon = runCatching { flattenIcon(info.getIcon(0)).asImageBitmap() }.getOrNull(),
                            profile = profileFor(handle),
                            userHandle = handle,
                            firstInstallTime = info.firstInstallTime,
                            // PackageManager only resolves packages belonging to this process's own
                            // user — a Work Profile app isn't visible to it, so fall back to the
                            // (reliably cross-profile) install time rather than leaving it at 0L.
                            lastUpdateTime = runCatching {
                                context.packageManager.getPackageInfo(info.applicationInfo.packageName, 0).lastUpdateTime
                            }.getOrDefault(info.firstInstallTime),
                        )
                    }.getOrNull()
                }
            }
            .awaitAll()
            .filterNotNull()
            .sortedWith(Comparator { a, b -> collator.compare(a.label, b.label) })
    }

    /**
     * Live-updating installed-app list — re-emits whenever a package is added, removed, or
     * changed in any profile while the launcher is running, via [LauncherApps.registerCallback],
     * or whenever a profile itself is added/removed/paused/resumed (a Work Profile, or an
     * Android 15+ Private Space), via [PROFILE_CHANGE_ACTIONS] (a profile appearing or
     * disappearing isn't a per-package event, so [LauncherApps.Callback] alone can't be relied on
     * to trigger a refresh for it). [getInstalledApps] only fetches once;
     * without this, any install/uninstall happening while Facet is in the foreground (including
     * via the app long-press context menu's Uninstall action) wouldn't be reflected anywhere —
     * Home/Drawer/Dock would keep showing the stale entry until the process restarts.
     * [FavoriteAppRepository]/[DockAppRepository] both hydrate against this live flow too, so an
     * uninstall collapses Favorites/Dock/Drawer alike; one-shot callers (picker seed steps) can
     * still use [getInstalledApps] directly where a live subscription isn't needed.
     *
     * Shared via [shareIn] ([installedApps]) rather than re-subscribed per caller — with as many
     * as ~7 collectors live at once (Home's app list, dock, favorites, folders, each hydrating
     * against this), a plain cold `callbackFlow` here meant every one of them registered its own
     * [LauncherApps.Callback] and independently re-ran the full profile scan + per-icon decode on
     * every single package event. [kotlinx.coroutines.flow.SharingStarted.WhileSubscribed] keeps
     * exactly one registration alive across the whole app while at least one collector remains
     * (surviving brief gaps like a Home↔Drawer transition), and tears it down again once nothing
     * is listening — never a leak, never a duplicate scan.
     */
    fun observeInstalledApps(): Flow<List<AppInfo>> = installedApps

    private val installedApps: SharedFlow<List<AppInfo>> = observeInstalledAppsUncached()
        .shareIn(applicationScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = SHARE_STOP_TIMEOUT_MILLIS), replay = 1)

    private fun observeInstalledAppsUncached(): Flow<List<AppInfo>> = callbackFlow {
        val refresh: () -> Unit = { launch { trySend(getInstalledApps()) } }

        val callback = object : LauncherApps.Callback() {
            override fun onPackageAdded(packageName: String?, user: UserHandle?) = refresh()
            override fun onPackageRemoved(packageName: String?, user: UserHandle?) = refresh()
            override fun onPackageChanged(packageName: String?, user: UserHandle?) = refresh()
            override fun onPackagesAvailable(packageNames: Array<out String>?, user: UserHandle?, replacing: Boolean) = refresh()
            override fun onPackagesUnavailable(packageNames: Array<out String>?, user: UserHandle?, replacing: Boolean) = refresh()
        }
        launcherApps.registerCallback(callback, mainThreadHandler)

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
     * [LauncherApps.registerCallback]'s single-arg overload binds to the *calling* thread's own
     * [Looper] rather than main — fine from a main-dispatched caller, but this repository's
     * [callbackFlow]s run on [applicationScope] ([Dispatchers.Default], a plain worker thread with
     * no Looper at all), which crashed with "Can't create handler inside thread ... that has not
     * called Looper.prepare()" on a real API 36 run. Passing the main-thread handler explicitly
     * makes registration correct regardless of which thread calls it.
     */
    private val mainThreadHandler = Handler(Looper.getMainLooper())

    /**
     * Emits `(packageName, userId)` each time an app is genuinely, permanently uninstalled from
     * some profile — unlike [observeInstalledApps], which also re-emits (the *whole* list) for
     * reasons an app might only be momentarily missing (e.g. `onPackagesUnavailable` mid-update).
     * The real `UserHandle.hashCode()` is included (`getIdentifier()` itself is hidden API, not in
     * the public SDK), not [AppProfile] — a personal and a
     * clone-profile copy of the same package can both be [AppProfile.OTHER] below API 35, so the
     * enum alone can't tell them apart; without the real per-user id, uninstalling one would look
     * indistinguishable from uninstalling the other to callers, and could delete the wrong one's
     * Favorites/Dock rows. Backs [com.facetlauncher.app.domain.CleanUpUninstalledAppsUseCase],
     * which deletes the corresponding Favorites/Dock rows outright rather than just filtering them
     * from view. Does *not* cover a whole profile being removed (see [observeProfileRemoved]
     * instead) — that's a profile-level event, not a per-package one, and isn't guaranteed to fire
     * a per-app callback for every app on its way out.
     */
    fun observeUninstalledPackages(): Flow<Pair<String, Int>> = callbackFlow {
        val callback = object : LauncherApps.Callback() {
            override fun onPackageAdded(packageName: String?, user: UserHandle?) = Unit
            override fun onPackageRemoved(packageName: String?, user: UserHandle?) {
                val packageAndUserId = packageName?.let { it to (user ?: Process.myUserHandle()).hashCode() }
                packageAndUserId?.let { trySend(it) }
            }
            override fun onPackageChanged(packageName: String?, user: UserHandle?) = Unit
            override fun onPackagesAvailable(packageNames: Array<out String>?, user: UserHandle?, replacing: Boolean) = Unit
            override fun onPackagesUnavailable(packageNames: Array<out String>?, user: UserHandle?, replacing: Boolean) = Unit
        }
        launcherApps.registerCallback(callback, mainThreadHandler)
        awaitClose { launcherApps.unregisterCallback(callback) }
    }

    /**
     * Emits the removed [UserHandle] whenever a Work Profile is removed (unenrolled) — a coarser,
     * bulk signal deliberately kept separate from [observeUninstalledPackages]: removing a whole
     * profile tears down every app in it atomically, and there's no guarantee each one also fires
     * its own [LauncherApps.Callback.onPackageRemoved] on the way out. Callers should treat this
     * as "drop every stored row for this specific handle's `userId` outright" — precisely that
     * handle, not every row tagged the same [AppProfile] display category, since a colliding
     * second profile in the same category (e.g. a clone profile also [AppProfile.OTHER]) would
     * otherwise have its own still-live rows swept away too. `Intent.EXTRA_USER` is always present
     * on a real `ACTION_MANAGED_PROFILE_REMOVED` broadcast; a malformed one is simply dropped.
     */
    fun observeProfileRemoved(): Flow<UserHandle> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action != Intent.ACTION_MANAGED_PROFILE_REMOVED) return
                intent.getParcelableExtra(Intent.EXTRA_USER, UserHandle::class.java)?.let { trySend(it) }
            }
        }
        val filter = IntentFilter(Intent.ACTION_MANAGED_PROFILE_REMOVED)
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        awaitClose { context.unregisterReceiver(receiver) }
    }

    /**
     * Opens the OS's own App Info screen for [app], correctly targeted at the exact profile it
     * came from via [LauncherApps.startAppDetailsActivity] — unlike a plain
     * `Settings.ACTION_APPLICATION_DETAILS_SETTINGS` intent (this app's old behavior), which
     * carries no profile targeting at all and resolves ambiguously via ambient context rather than
     * [app]'s real [AppInfo.userHandle].
     */
    fun openAppDetails(app: AppInfo) {
        val component = ComponentName(app.packageName, app.activityName)
        runCatching { launcherApps.startAppDetailsActivity(component, app.userHandle, null, null) }
    }
}
