package com.facetlauncher.app.ui.settings

import androidx.lifecycle.ViewModel
import com.facetlauncher.app.data.AccessibilityAccessRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Accessibility access has no grant-change callback — [refresh] is called on `onResume`, after the Settings redirect. */
@HiltViewModel
class AccessibilityAccessExplanationViewModel @Inject constructor(
    private val accessibilityAccessRepository: AccessibilityAccessRepository,
) : ViewModel() {

    private val _isGranted = MutableStateFlow(accessibilityAccessRepository.isGranted())
    val isGranted: StateFlow<Boolean> = _isGranted.asStateFlow()

    fun refresh() {
        _isGranted.value = accessibilityAccessRepository.isGranted()
    }
}
