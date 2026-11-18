package com.facetlauncher.app.ui.settings

import android.app.Activity
import android.content.Intent
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.ScreenCaptureRepository
import com.facetlauncher.app.domain.CaptureWallpaperUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** What the capture screen is showing. [Capturing] shows nothing, so the frame is just the wallpaper. */
enum class CapturePhase { Prompt, Capturing, Failed, Manual }

@Immutable
data class WallpaperCaptureUiState(val phase: CapturePhase = CapturePhase.Prompt)

sealed interface WallpaperCaptureUiEvent {
    /** The copy was stored. */
    data object Captured : WallpaperCaptureUiEvent
}

/** The capture screen: asking Android for one frame of the screen, or falling back to the manual screenshot route. */
@HiltViewModel
class WallpaperCaptureViewModel @Inject constructor(
    private val screenCapture: ScreenCaptureRepository,
    private val captureWallpaper: CaptureWallpaperUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WallpaperCaptureUiState())
    val uiState: StateFlow<WallpaperCaptureUiState> = _uiState.asStateFlow()

    private val events = Channel<WallpaperCaptureUiEvent>(Channel.BUFFERED)
    val uiEvents = events.receiveAsFlow()

    fun consentIntent(): Intent = screenCapture.consentIntent()

    /** The user asked to capture: clear the card before Android's dialog opens, so the frame is clean. */
    fun onCaptureRequested() = setPhase(CapturePhase.Capturing)

    /**
     * Android's dialog closed with [resultCode] and [consent]; [barsHidden] says whether the screen has hidden the system bars, and [rotateTo] turns the
     * screen (returning once it has) so the same approval also captures the other orientation.
     */
    fun onConsentResult(resultCode: Int, consent: Intent?, barsHidden: () -> Boolean = { false }, rotateTo: (suspend (landscape: Boolean) -> Boolean)? = null) {
        if (resultCode != Activity.RESULT_OK || consent == null) {
            setPhase(CapturePhase.Failed)
            return
        }
        viewModelScope.launch {
            if (captureWallpaper(resultCode, consent, barsHidden, rotateTo)) events.send(WallpaperCaptureUiEvent.Captured) else setPhase(CapturePhase.Failed)
        }
    }

    fun onManualRoute() = setPhase(CapturePhase.Manual)

    fun onBackToPrompt() = setPhase(CapturePhase.Prompt)

    private fun setPhase(phase: CapturePhase) = _uiState.update { it.copy(phase = phase) }
}
