package com.facetlauncher.app.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.UserManager
import androidx.core.content.ContextCompat
import com.facetlauncher.app.data.model.AppProfile
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map

/** Broadcasts that mean a Work Profile's own state changed — enrolled, unenrolled, paused, or resumed. */
private val PROFILE_STATE_ACTIONS = IntentFilter().apply {
    addAction(Intent.ACTION_MANAGED_PROFILE_ADDED)
    addAction(Intent.ACTION_MANAGED_PROFILE_REMOVED)
    addAction(Intent.ACTION_MANAGED_PROFILE_AVAILABLE)
    addAction(Intent.ACTION_MANAGED_PROFILE_UNAVAILABLE)
}

/**
 * Read-only visibility into whether a Work Profile exists and whether it's currently paused
 * ("quiet mode"). Deliberately has no write path — Android already gives the user a system
 * Settings toggle (and, on most OEMs, a Quick Settings tile) for pausing/resuming a Work Profile,
 * so this app reflects that state rather than reimplementing the control surface, which also
 * sidesteps needing to confirm whether a plain default-launcher app (as opposed to a Device
 * Policy Controller) is even permitted to call the write API
 * ([UserManager.requestQuietModeEnabled]) on this app's min SDK.
 */
@Singleton
class WorkProfileRepository @Inject constructor(
    private val userManager: UserManager,
    private val appRepository: AppRepository,
    @ApplicationContext private val context: Context,
) {

    /**
     * Delegates to [AppRepository.resolveUserHandle] rather than its own "any non-primary handle"
     * check — verified live on a real API 36 emulator that `UserManager.getUserProfiles()` also
     * returns an Android 15+ Private Space's handle, work profile or not, so that naive check
     * would report `hasWorkProfile() == true` (and show an always-empty "Work" row/tab) on any
     * device with a Private Space configured. [AppRepository.resolveUserHandle] positively checks
     * for a real Work Profile via `LauncherApps.getLauncherUserInfo`.
     */
    private fun findWorkProfileHandle() = appRepository.resolveUserHandle(AppProfile.WORK)

    /** Re-emits once immediately, then again whenever a Work Profile is added, removed, paused, or resumed. */
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

    /** Whether a Work Profile currently exists on this device, live across enrollment/unenrollment. */
    fun hasWorkProfile(): Flow<Boolean> = observeProfileStateChanges().map { findWorkProfileHandle() != null }

    /**
     * Whether the Work Profile is paused ("quiet mode"), live across pause/resume — always
     * `false` when there is no Work Profile (nothing to be paused).
     */
    fun isWorkProfilePaused(): Flow<Boolean> = observeProfileStateChanges().map {
        findWorkProfileHandle()?.let { handle -> userManager.isQuietModeEnabled(handle) } ?: false
    }
}
