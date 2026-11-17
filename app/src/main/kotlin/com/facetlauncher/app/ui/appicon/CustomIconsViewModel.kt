package com.facetlauncher.app.ui.appicon

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.domain.ChangeAppIconUseCase
import com.facetlauncher.app.domain.CustomIconRow
import com.facetlauncher.app.domain.ObserveCustomIconsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** The Custom icons screen's apps, and null until they have loaded. */
data class CustomIconsUiState(val rows: ImmutableList<CustomIconRow> = persistentListOf(), val isLoading: Boolean = true)

/** Settings → Icons → Custom icons: lists apps with a custom icon and resets them. Changing an icon is the app menu's job. */
@HiltViewModel
class CustomIconsViewModel @Inject constructor(
    observeCustomIcons: ObserveCustomIconsUseCase,
    private val changeAppIcon: ChangeAppIconUseCase,
) : ViewModel() {
    val uiState: StateFlow<CustomIconsUiState> = observeCustomIcons()
        .map { CustomIconsUiState(it.toImmutableList(), isLoading = false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CustomIconsUiState())

    fun reset(row: CustomIconRow) {
        viewModelScope.launch { changeAppIcon.reset(row.override) }
    }

    fun resetAll() {
        viewModelScope.launch { changeAppIcon.resetAll() }
    }
}
