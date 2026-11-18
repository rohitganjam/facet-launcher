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

/** What the Home pill offers: the view [key] to capture. */
data class WallpaperCopyOffer(val key: String)

/**
 * The offer for the view showing now when Home should suggest capturing its wallpaper (its glass would otherwise be a plain tint), or
 * `null`; never while no glass is switched on anywhere. Each view is offered once per launch; [onShown] records that.
 */
@HiltViewModel
class WallpaperCopyPillViewModel @Inject constructor(
    observeScreens: ObserveWallpaperScreensUseCase,
    observeGlassInUse: ObserveGlassInUseUseCase,
) : ViewModel() {
    private val offered = mutableSetOf<String>()

    val offer: StateFlow<WallpaperCopyOffer?> = combine(observeScreens.copyNeed(), observeGlassInUse()) { need, glass -> need.takeIf { glass } }
        .map { need -> need?.takeIf { it.view.key !in offered }?.let { WallpaperCopyOffer(it.view.key) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS), null)

    /** The pill for [key] appeared: not shown again for that view until the launcher restarts. */
    fun onShown(key: String) {
        offered += key
    }

    private companion object {
        const val SUBSCRIPTION_TIMEOUT_MS = 5_000L
    }
}
