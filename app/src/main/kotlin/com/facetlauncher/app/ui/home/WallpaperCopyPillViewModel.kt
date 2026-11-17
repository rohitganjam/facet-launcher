package com.facetlauncher.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.domain.ObserveGlassInUseUseCase
import com.facetlauncher.app.domain.ObserveWallpaperScreensUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * The key of the screen showing now when Home should offer to update its wallpaper copy (its glass would otherwise fall back to a
 * stand-in or the tint), or `null`; never while no glass is switched on anywhere. Each screen is offered once per launch; [onShown] records that.
 */
@HiltViewModel
class WallpaperCopyPillViewModel @Inject constructor(
    observeScreens: ObserveWallpaperScreensUseCase,
    observeGlassInUse: ObserveGlassInUseUseCase,
) : ViewModel() {
    private val offered = mutableSetOf<String>()

    val screenKey: StateFlow<String?> = combine(observeScreens.screenNeedingCopy(), observeGlassInUse()) { screen, glass -> screen.takeIf { glass } }
        .map { screen -> screen?.key?.takeIf { it !in offered } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS), null)

    /** The pill for [key] appeared: not shown again for that screen until the launcher restarts. */
    fun onShown(key: String) {
        offered += key
    }

    private companion object {
        const val SUBSCRIPTION_TIMEOUT_MS = 5_000L
    }
}
