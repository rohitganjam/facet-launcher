package com.facetlauncher.app.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.UserHandle
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

/** One live, positively-classified Work Profile — [handle] is its real identity, [isPaused] its current quiet-mode state, [label] a display name (disambiguated if more than one exists). */
data class WorkProfileInfo(val handle: UserHandle, val isPaused: Boolean, val label: String)

/**
 * Read-only visibility into every genuine Work Profile on this device and whether each is
 * currently paused ("quiet mode") — list-shaped rather than a single boolean. Stock Android only
 * ever provisions one real Work Profile per user, so this returns 0 or 1 entries in practice; the
 * list shape exists so a misclassification elsewhere (e.g. an OEM clone profile) can never
 * silently merge with a genuine one — [AppRepository.profileFor] only puts a handle here when
 * it's positively confirmed via `LauncherApps.getLauncherUserInfo`, never guessed. Deliberately
 * has no write path — Android already gives the user a system Settings toggle (and, on most
 * OEMs, a Quick Settings tile) for pausing/resuming a Work Profile, so this app reflects that
 * state rather than reimplementing the control surface, which also sidesteps needing to confirm
 * whether a plain default-launcher app (as opposed to a Device Policy Controller) is even
 * permitted to call the write API ([UserManager.requestQuietModeEnabled]) on this app's min SDK.
 */
@Singleton
class WorkProfileRepository @Inject constructor(
    private val userManager: UserManager,
    private val appRepository: AppRepository,
    @ApplicationContext private val context: Context,
) {

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

    /**
     * Every handle [AppRepository.profileFor] positively classifies as a real Work Profile, live
     * across enrollment/unenrollment/pause/resume. `isQuietModeEnabled` is wrapped in
     * `runCatching` — even a positively-classified handle can throw (e.g. this launcher isn't
     * recognized as eligible to query it on some OEM build), and that shouldn't crash the flow;
     * it's treated as "not paused" rather than propagating.
     */
    fun observeWorkProfiles(): Flow<List<WorkProfileInfo>> = observeProfileStateChanges().map {
        userManager.userProfiles
            .filter { appRepository.profileFor(it) == AppProfile.WORK }
            .mapIndexed { index, handle ->
                WorkProfileInfo(
                    handle = handle,
                    isPaused = runCatching { userManager.isQuietModeEnabled(handle) }.getOrDefault(false),
                    label = if (index == 0) "Work" else "Work ${index + 1}",
                )
            }
    }
}
