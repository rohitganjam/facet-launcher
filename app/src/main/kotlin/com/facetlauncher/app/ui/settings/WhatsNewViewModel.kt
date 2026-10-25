package com.facetlauncher.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.AppVersion
import com.facetlauncher.app.data.ReleaseNotesRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.ReleaseNotes
import com.facetlauncher.app.data.model.whatsNewSeenId
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class WhatsNewUiState(
    val releases: List<ReleaseNotes> = emptyList(),
    val isLoading: Boolean = true,
)

/** Settings → About → What's new: every release's bundled notes, newest first. */
@HiltViewModel
class WhatsNewViewModel @Inject constructor(
    private val releaseNotesRepository: ReleaseNotesRepository,
    private val settingsRepository: SettingsRepository,
    private val appVersion: AppVersion,
) : ViewModel() {

    init {
        // Opening the page, from Settings or from the Home pill, is seeing this version's notes: the pill has nothing left to announce.
        viewModelScope.launch { settingsRepository.markCoachMarkSeen(whatsNewSeenId(appVersion.name)) }
    }

    val uiState: StateFlow<WhatsNewUiState> = flow {
        emit(WhatsNewUiState(releases = releaseNotesRepository.releases(), isLoading = false))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WhatsNewUiState())
}
