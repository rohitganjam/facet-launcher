package com.facetlauncher.app.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.UserManager
import androidx.core.content.ContextCompat
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppProfile
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map

/** Broadcasts that mean Private Space's own state changed — added, removed, locked, or unlocked. */
private val PROFILE_STATE_ACTIONS = IntentFilter().apply {
    addAction(Intent.ACTION_PROFILE_ADDED)
    addAction(Intent.ACTION_PROFILE_REMOVED)
    addAction(Intent.ACTION_PROFILE_AVAILABLE)
    addAction(Intent.ACTION_PROFILE_UNAVAILABLE)
}

/** Whether a Private Space exists on this device and, if so, whether it's currently locked. */
sealed class PrivateSpaceState {
    data object NotConfigured : PrivateSpaceState()
    data object Locked : PrivateSpaceState()
    data object Unlocked : PrivateSpaceState()
}

/**
 * Read-only visibility into Private Space (Android 15+), plus the one write action a launcher
 * genuinely needs — triggering the OS's own unlock prompt — unlike [WorkProfileRepository], which
 * deliberately has no write path at all: a locked Work Profile still shows its (dimmed) apps, but
 * a locked Private Space is unusable without unlocking first, so there's no "just reflect Settings
 * state" option here.
 */
@Singleton
class PrivateSpaceRepository @Inject constructor(
    private val userManager: UserManager,
    private val appRepository: AppRepository,
    @ApplicationContext private val context: Context,
) {

    /** Delegates to [AppRepository.resolveUserHandle] — see its own doc for why this is a positive check, not "any non-primary handle". */
    private fun findPrivateSpaceHandle() = appRepository.resolveUserHandle(AppProfile.PRIVATE)

    /** Re-emits once immediately, then again whenever Private Space is added, removed, locked, or unlocked. */
    private fun observeProfileStateChanges(): Flow<Unit> = callbackFlow {
        trySend(Unit)
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                trySend(Unit)
            }
        }
        ContextCompat.registerReceiver(context, receiver, PROFILE_STATE_ACTIONS, ContextCompat.RECEIVER_NOT_EXPORTED)
        awaitClose { context.unregisterReceiver(receiver) }
    }

    /**
     * Whether Private Space exists and, if so, whether it's locked — live across
     * add/remove/lock/unlock. A locked Private Space's handle stays resolvable via
     * [AppRepository.resolveUserHandle] (unlike a fully-removed one), so [UserManager.isQuietModeEnabled]
     * is what actually distinguishes [PrivateSpaceState.Locked] from [PrivateSpaceState.Unlocked].
     */
    fun observePrivateSpaceState(): Flow<PrivateSpaceState> = observeProfileStateChanges().map {
        val handle = findPrivateSpaceHandle() ?: return@map PrivateSpaceState.NotConfigured
        val isQuietModeEnabled = runCatching { userManager.isQuietModeEnabled(handle) }.getOrDefault(false)
        if (isQuietModeEnabled) PrivateSpaceState.Locked else PrivateSpaceState.Unlocked
    }

    /**
     * Private Space's own installed apps, live across install/uninstall. Its handle stays
     * enumerable while locked (per [observePrivateSpaceState]'s doc), so this list reflects
     * installed apps regardless of lock state — the caller (`PrivateSpaceViewModel`) gates
     * rendering on [observePrivateSpaceState] instead, since a locked space isn't meaningful to
     * browse even though its app list is technically still readable.
     */
    fun observePrivateSpaceApps(): Flow<List<AppInfo>> = appRepository.observeAppsForProfile(AppProfile.PRIVATE)

    /**
     * Requests the OS unlock Private Space — this is not "reimplementing a control surface" the
     * way a Work Profile pause/resume toggle would be (see [WorkProfileRepository]'s own doc):
     * unlike a paused Work Profile (still browsable, just not launchable), a locked Private Space
     * is not meaningfully browsable at all, so surfacing *some* way to unlock it from the overflow
     * menu is required for the feature to be usable, not an optional convenience. Android's own
     * documented mechanism for this is the same quiet-mode call Work Profile pause/resume uses;
     * for a security-sensitive profile the OS is expected to interpose its own biometric/PIN
     * prompt before actually disabling quiet mode. Returns `false` (a no-op) if Private Space
     * isn't configured at all.
     */
    fun requestUnlock(): Boolean {
        val handle = findPrivateSpaceHandle() ?: return false
        return runCatching { userManager.requestQuietModeEnabled(false, handle) }.getOrDefault(false)
    }
}
