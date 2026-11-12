package com.facetlauncher.app.ui.components

import androidx.compose.runtime.staticCompositionLocalOf

/** What the glass background pickers need to offer a wallpaper copy: whether to, and what its two buttons do. */
class GlassBackgroundPrompt(val needsCopy: Boolean, val onChooseImage: () -> Unit, val onDecline: () -> Unit)

/** Provided by the nav host; `null` (no prompt) in previews and tests. */
val LocalGlassBackgroundPrompt = staticCompositionLocalOf<GlassBackgroundPrompt?> { null }
