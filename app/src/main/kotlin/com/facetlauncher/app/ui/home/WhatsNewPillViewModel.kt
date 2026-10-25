package com.facetlauncher.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.AppVersion
import com.facetlauncher.app.data.ReleaseNotesRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.whatsNewSeenId
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Which version's What's new Home should announce with its pill, or `null` for none: the running version, once onboarding
 * is done, when it has bundled notes and the user hasn't seen them. A fresh install never qualifies (finishing onboarding
 * marks its version as seen), nor does anyone who already opened What's new for this version.
 */
@HiltViewModel
class WhatsNewPillViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    releaseNotesRepository: ReleaseNotesRepository,
    private val appVersion: AppVersion,
) : ViewModel() {

    private val versionsWithNotes = flow { emit(releaseNotesRepository.releases().map { it.version }.toSet()) }

    val version: StateFlow<String?> = combine(settingsRepository.settings, versionsWithNotes) { settings, withNotes ->
        appVersion.name.takeIf {
            settings.onboardingCompleted && whatsNewSeenId(it) !in settings.coachMarksSeen && it in withNotes
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS), null)

    /** The pill appeared: one announcement per version, so it is retired now rather than when it goes away. */
    fun onShown() {
        viewModelScope.launch { settingsRepository.markCoachMarkSeen(whatsNewSeenId(appVersion.name)) }
    }

    private companion object {
        const val SUBSCRIPTION_TIMEOUT_MS = 5_000L
    }
}
