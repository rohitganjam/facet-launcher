package com.facetlauncher.app.ui.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.LicenseRepository
import com.facetlauncher.app.data.model.LicenseCatalog
import com.facetlauncher.app.data.model.LicenseInfo
import com.facetlauncher.app.data.model.LicenseSection
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

data class LicenseTextUiState(
    val info: LicenseInfo? = null,
    val sections: ImmutableList<LicenseSection> = persistentListOf(),
    val isLoading: Boolean = true,
)

/** Settings → About → Open source licenses → one entry: its bundled licence text(s). */
@HiltViewModel
class LicenseTextViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val licenseRepository: LicenseRepository,
) : ViewModel() {

    private val info: LicenseInfo? = savedStateHandle.get<String>("licenseId")?.let(LicenseCatalog::find)

    val uiState: StateFlow<LicenseTextUiState> = flow {
        val sections = info?.let { licenseRepository.sections(it) }.orEmpty().toImmutableList()
        emit(LicenseTextUiState(info = info, sections = sections, isLoading = false))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LicenseTextUiState())
}
