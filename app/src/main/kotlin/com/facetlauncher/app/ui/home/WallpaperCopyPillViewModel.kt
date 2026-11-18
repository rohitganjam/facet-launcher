package com.facetlauncher.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.ScreenCaptureRepository
import com.facetlauncher.app.domain.ObserveGlassInUseUseCase
import com.facetlauncher.app.domain.ObserveWallpaperScreensUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** What the Home pill offers: the [key] of the view and wallpaper to capture. */
data class WallpaperCopyOffer(val key: String)

/**
 * The offer for the view showing now when Home should suggest capturing its wallpaper (its glass would otherwise be a plain tint), or
 * `null`; never while no glass is switched on anywhere. Each view is offered once per wallpaper per launch ([onShown] records that), and never while a capture is being made.
 */
@HiltViewModel
class WallpaperCopyPillViewModel @Inject constructor(
    observeScreens: ObserveWallpaperScreensUseCase,
    observeGlassInUse: ObserveGlassInUseUseCase,
    screenCapture: ScreenCaptureRepository,
) : ViewModel() {
    private val offered = MutableStateFlow(emptySet<String>())

    val offer: StateFlow<WallpaperCopyOffer?> = combine(observeScreens.copyNeed(), observeGlassInUse(), screenCapture.capturing, offered) { need, glass, capturing, done ->
        need?.takeIf { glass && !capturing }?.let { WallpaperCopyOffer("${it.view.key}@${it.wallpaperId}") }?.takeIf { it.key !in done }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS), null)

    /** The pill for [key] appeared: not shown again for that view and wallpaper until the launcher restarts. */
    fun onShown(key: String) {
        offered.value += key
    }

    private companion object {
        const val SUBSCRIPTION_TIMEOUT_MS = 5_000L
    }
}
