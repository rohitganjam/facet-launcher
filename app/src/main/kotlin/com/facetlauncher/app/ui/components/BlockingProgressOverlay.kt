package com.facetlauncher.app.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * A spinner and [message] over the whole screen for something that takes a few seconds (setting a wallpaper, downloading a photo). It takes every
 * touch and the Back button, so nothing can be started or cancelled halfway.
 */
@Composable
fun BlockingProgressOverlay(message: String, modifier: Modifier = Modifier) {
    BackHandler { }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = OVERLAY_ALPHA))
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) awaitPointerEvent().changes.forEach { it.consume() }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(horizontal = 32.dp)) {
            CircularProgressIndicator(color = Color.White)
            Text(text = message, style = MaterialTheme.typography.bodyMedium, color = Color.White, textAlign = TextAlign.Center)
        }
    }
}

private const val OVERLAY_ALPHA = 0.55f
