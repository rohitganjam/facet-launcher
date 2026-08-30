package com.lumenlauncher.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumenlauncher.app.data.UsageAccessRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** `PACKAGE_USAGE_STATS` has no grant-change callback — [refresh] is called on `onResume`, since the only way to grant it is the system Settings redirect this screen sends the user to. */
@HiltViewModel
class UsageAccessExplanationViewModel @Inject constructor(
    private val usageAccessRepository: UsageAccessRepository,
) : ViewModel() {

    private val _isGranted = MutableStateFlow(usageAccessRepository.isGranted())
    val isGranted: StateFlow<Boolean> = _isGranted.asStateFlow()

    fun refresh() {
        viewModelScope.launch { _isGranted.value = usageAccessRepository.isGranted() }
    }
}
