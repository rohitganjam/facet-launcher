package com.facetlauncher.app.ui.settings

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.View
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.facetlauncher.app.R
import com.facetlauncher.app.ui.components.PrimaryButton
import com.facetlauncher.app.ui.components.TextActionButton
import com.facetlauncher.app.ui.components.TonalButton
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.HideStatusBarEffect
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.LocalStatusBarHidden
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.Surface

/**
 * Captures the user's wallpaper: the window's own wallpaper shows through, with the system bars hidden, and one frame of it is taken once
 * the user agrees in Android's dialog. If they decline, or the capture fails, they can try again or take a screenshot themselves.
 */
@Composable
fun WallpaperCaptureScreen(
    onDone: () -> Unit,
    onChooseScreenshot: () -> Unit,
    onReturn: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WallpaperCaptureViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val view = LocalView.current
    val consent = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        viewModel.onConsentResult(result.resultCode, result.data) { view.systemBarsHidden() }
    }
    val currentOnCaptured by rememberUpdatedState(onDone)
    LaunchedEffect(viewModel) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                WallpaperCaptureUiEvent.Captured -> {
                    Toast.makeText(context, R.string.wallpaper_capture_done, Toast.LENGTH_SHORT).show()
                    currentOnCaptured()
                }
            }
        }
    }
    WallpaperCaptureContent(
        phase = uiState.phase,
        onCapture = {
            viewModel.onCaptureRequested()
            consent.launch(viewModel.consentIntent())
        },
        onManualRoute = viewModel::onManualRoute,
        onBackToPrompt = viewModel::onBackToPrompt,
        onChooseScreenshot = onChooseScreenshot,
        onReturn = onReturn,
        modifier = modifier,
    )
}

/**
 * An empty window onto the user's own wallpaper with the system bars hidden. While [CapturePhase.Capturing] nothing is drawn, so the frame
 * is exactly the wallpaper. In [CapturePhase.Manual] the card stays until *Take screenshot* is tapped, which clears it for the user's own
 * screenshot, and a tap on the empty screen brings it back.
 */
@Composable
fun WallpaperCaptureContent(
    phase: CapturePhase,
    onCapture: () -> Unit,
    onManualRoute: () -> Unit,
    onBackToPrompt: () -> Unit,
    onChooseScreenshot: () -> Unit,
    onReturn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HideSystemBars()
    HideStatusBarEffect()
    var manualCardVisible by remember(phase) { mutableStateOf(true) }
    val cardVisible = when (phase) {
        CapturePhase.Capturing -> false
        CapturePhase.Manual -> manualCardVisible
        else -> true
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("wallpaper_capture")
            .pointerInput(phase) { if (phase == CapturePhase.Manual) detectTapGestures { manualCardVisible = true } },
        contentAlignment = Alignment.Center,
    ) {
        AnimatedVisibility(visible = cardVisible, enter = fadeIn(), exit = fadeOut()) {
            when (phase) {
                CapturePhase.Manual -> ManualCard(
                    onTake = { manualCardVisible = false },
                    onChoose = onChooseScreenshot,
                    onBack = onBackToPrompt,
                )
                CapturePhase.Failed -> PromptCard(
                    title = R.string.wallpaper_capture_failed_title,
                    body = R.string.wallpaper_capture_failed_body,
                    startLabel = R.string.wallpaper_capture_retry,
                    startTag = "wallpaper_capture_retry",
                    onStart = onCapture,
                    onManualRoute = onManualRoute,
                    onReturn = onReturn,
                )
                else -> PromptCard(
                    title = R.string.wallpaper_capture_title,
                    body = R.string.wallpaper_capture_body,
                    startLabel = R.string.wallpaper_capture_start,
                    startTag = "wallpaper_capture_start",
                    onStart = onCapture,
                    onManualRoute = onManualRoute,
                    onReturn = onReturn,
                )
            }
        }
    }
}

/** The card every phase shows its content in. */
@Composable
private fun CaptureCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .padding(horizontal = 32.dp)
            .background(Surface, MaterialTheme.shapes.large)
            .padding(20.dp)
            .testTag("wallpaper_capture_hint"),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) { content() }
}

@Composable
private fun PromptCard(
    @StringRes title: Int,
    @StringRes body: Int,
    @StringRes startLabel: Int,
    startTag: String,
    onStart: () -> Unit,
    onManualRoute: () -> Unit,
    onReturn: () -> Unit,
) = CaptureCard {
    Text(text = stringResource(title), style = MaterialTheme.typography.titleMedium, color = Ink)
    Text(text = stringResource(body), style = MaterialTheme.typography.bodyMedium, color = Muted)
    PrimaryButton(text = stringResource(startLabel), onClick = onStart, modifier = Modifier.fillMaxWidth().testTag(startTag))
    TonalButton(
        text = stringResource(R.string.wallpaper_capture_manual),
        onClick = onManualRoute,
        modifier = Modifier.fillMaxWidth().testTag("wallpaper_capture_manual"),
    )
    TextActionButton(
        text = stringResource(R.string.wallpaper_capture_return),
        onClick = onReturn,
        modifier = Modifier.fillMaxWidth().testTag("wallpaper_capture_return"),
    )
}

@Composable
private fun ManualCard(onTake: () -> Unit, onChoose: () -> Unit, onBack: () -> Unit) = CaptureCard {
    Text(text = stringResource(R.string.wallpaper_capture_manual_title), style = MaterialTheme.typography.titleMedium, color = Ink)
    CaptureStep(number = 1, text = R.string.wallpaper_capture_step1)
    CaptureStep(number = 2, text = R.string.wallpaper_capture_step2)
    CaptureStep(number = 3, text = R.string.wallpaper_capture_step3)
    PrimaryButton(
        text = stringResource(R.string.wallpaper_capture_take),
        onClick = onTake,
        modifier = Modifier.fillMaxWidth().testTag("wallpaper_capture_take"),
    )
    TonalButton(
        text = stringResource(R.string.wallpaper_capture_choose),
        onClick = onChoose,
        modifier = Modifier.fillMaxWidth().testTag("wallpaper_capture_choose"),
    )
    TextActionButton(
        text = stringResource(R.string.wallpaper_capture_back),
        onClick = onBack,
        modifier = Modifier.fillMaxWidth().testTag("wallpaper_capture_back"),
    )
}

/** One numbered step; the string carries `<b>` around the words to act on. */
@Composable
private fun CaptureStep(number: Int, @StringRes text: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
        Text(text = number.toString(), style = MaterialTheme.typography.titleMedium, color = Accent, modifier = Modifier.width(STEP_NUMBER_WIDTH))
        Text(
            text = AnnotatedString.fromHtml(stringResource(text)),
            style = MaterialTheme.typography.bodyMedium,
            color = Ink,
            modifier = Modifier.testTag("wallpaper_capture_step$number"),
        )
    }
}

private val STEP_NUMBER_WIDTH = 14.dp

/** Hides the status and navigation bars while shown (again after Android's dialog), and puts back what was showing when it leaves. */
@Composable
private fun HideSystemBars() {
    val view = LocalView.current
    val statusBarHiddenBySetting = LocalStatusBarHidden.current
    LifecycleResumeEffect(view) {
        val window = view.context.findHostActivity()?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        controller?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller?.hide(WindowInsetsCompat.Type.systemBars())
        onPauseOrDispose { }
    }
    DisposableEffect(view) {
        val window = view.context.findHostActivity()?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        onDispose {
            controller?.show(WindowInsetsCompat.Type.navigationBars())
            if (!statusBarHiddenBySetting) controller?.show(WindowInsetsCompat.Type.statusBars())
        }
    }
}

/** Whether the status and navigation bars are both hidden right now. */
private fun View.systemBarsHidden(): Boolean {
    val insets = ViewCompat.getRootWindowInsets(this) ?: return false
    return !insets.isVisible(WindowInsetsCompat.Type.statusBars()) && !insets.isVisible(WindowInsetsCompat.Type.navigationBars())
}

private tailrec fun Context.findHostActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findHostActivity()
    else -> null
}

@Preview(showBackground = true)
@Composable
private fun WallpaperCaptureContentPreview() {
    FacetLauncherTheme {
        WallpaperCaptureContent(
            phase = CapturePhase.Prompt,
            onCapture = {},
            onManualRoute = {},
            onBackToPrompt = {},
            onChooseScreenshot = {},
            onReturn = {},
        )
    }
}
