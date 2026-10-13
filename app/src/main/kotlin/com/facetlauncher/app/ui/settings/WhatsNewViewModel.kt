package com.facetlauncher.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.ReleaseNotesRepository
import com.facetlauncher.app.data.model.ReleaseNotes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

data class WhatsNewUiState(
    val releases: List<ReleaseNotes> = emptyList(),
    val isLoading: Boolean = true,
)

/** Settings → About → What's new: every release's bundled notes, newest first. */
@HiltViewModel
class WhatsNewViewModel @Inject constructor(
    private val releaseNotesRepository: ReleaseNotesRepository,
) : ViewModel() {

    val uiState: StateFlow<WhatsNewUiState> = flow {
        emit(WhatsNewUiState(releases = releaseNotesRepository.releases(), isLoading = false))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WhatsNewUiState())
}
