package com.lumenlauncher.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.lumenlauncher.app.data.model.ClockColorOption

/** Resolves a [ClockColorOption] to a real [Color] — [ClockColorOption.INK] is theme-aware ([Ink]); the rest are fixed. */
@Composable
fun ClockColorOption.resolve(): Color = when (this) {
    ClockColorOption.INK -> Ink
    ClockColorOption.WHITE -> Color.White
    ClockColorOption.BLACK -> Color.Black
    ClockColorOption.ACCENT -> Accent
}
