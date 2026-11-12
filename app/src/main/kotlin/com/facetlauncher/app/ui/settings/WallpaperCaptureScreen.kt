package com.facetlauncher.app.ui.settings

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.facetlauncher.app.R
import com.facetlauncher.app.ui.components.PrimaryButton
import com.facetlauncher.app.ui.components.TextActionButton
import com.facetlauncher.app.ui.components.TonalButton
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.LocalStatusBarHidden
import com.facetlauncher.app.ui.theme.Surface

/**
 * An empty window onto the user's own wallpaper (the activity's window already shows it), with the system bars hidden, so a screenshot of it
 * is exactly the wallpaper. A card explains what to do and stays until the user taps *Take screenshot*, which clears it so the screenshot is
 * clean; tapping the empty screen brings it back. *Choose screenshot* goes on to the picker and *Return* goes back to the wallpaper screen
 * (so does Back).
 */
@Composable
fun WallpaperCaptureScreen(onChooseScreenshot: () -> Unit, onReturn: () -> Unit, modifier: Modifier = Modifier) {
    HideSystemBars()
    var cardVisible by remember { mutableStateOf(true) }
    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("wallpaper_capture")
            .pointerInput(Unit) { detectTapGestures { cardVisible = true } },
        contentAlignment = Alignment.Center,
    ) {
        AnimatedVisibility(visible = cardVisible, enter = fadeIn(), exit = fadeOut()) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .background(Surface, MaterialTheme.shapes.large)
                    .padding(20.dp)
                    .testTag("wallpaper_capture_hint"),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(text = stringResource(R.string.wallpaper_capture_title), style = MaterialTheme.typography.titleMedium, color = Ink)
                CaptureStep(number = 1, text = R.string.wallpaper_capture_step1)
                CaptureStep(number = 2, text = R.string.wallpaper_capture_step2)
                CaptureStep(number = 3, text = R.string.wallpaper_capture_step3)
                PrimaryButton(
                    text = stringResource(R.string.wallpaper_capture_take),
                    onClick = { cardVisible = false },
                    modifier = Modifier.fillMaxWidth().testTag("wallpaper_capture_take"),
                )
                TonalButton(
                    text = stringResource(R.string.wallpaper_capture_choose),
                    onClick = onChooseScreenshot,
                    modifier = Modifier.fillMaxWidth().testTag("wallpaper_capture_choose"),
                )
                TextActionButton(
                    text = stringResource(R.string.wallpaper_capture_return),
                    onClick = onReturn,
                    modifier = Modifier.fillMaxWidth().testTag("wallpaper_capture_return"),
                )
            }
        }
    }
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

/** Hides the status and navigation bars while shown, and puts back what was showing when it leaves. */
@Composable
private fun HideSystemBars() {
    val view = LocalView.current
    val statusBarHiddenBySetting = LocalStatusBarHidden.current
    DisposableEffect(view) {
        val window = view.context.findHostActivity()?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        controller?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller?.hide(WindowInsetsCompat.Type.systemBars())
        onDispose {
            controller?.show(WindowInsetsCompat.Type.navigationBars())
            if (!statusBarHiddenBySetting) controller?.show(WindowInsetsCompat.Type.statusBars())
        }
    }
}

private tailrec fun Context.findHostActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findHostActivity()
    else -> null
}
