package com.facetlauncher.app.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity

/** Icons and small labels in the long-press sheet's chrome grow with text, but never past this multiple of their base size. */
internal const val MAX_CHROME_ICON_SCALE = 1.3f

/** `bodyLarge`'s size in M3's own type scale; the app's Text size option multiplies it. */
private const val BODY_LARGE_BASE_SP = 16f

/** The system font scale times the app's own Text size option, held between 1 and [MAX_CHROME_ICON_SCALE]. Icons never shrink below their base size. */
internal fun chromeIconScale(systemFontScale: Float, appFontScale: Float): Float =
    (systemFontScale * appFontScale).coerceIn(1f, MAX_CHROME_ICON_SCALE)

/** What to multiply a chrome label's size by so it stops growing at [MAX_CHROME_ICON_SCALE]: 1 until the combined text scale passes it, then smaller. */
internal fun chromeTextRatio(systemFontScale: Float, appFontScale: Float): Float =
    (MAX_CHROME_ICON_SCALE / (systemFontScale * appFontScale)).coerceAtMost(1f)

@Composable
private fun appFontScale(): Float = MaterialTheme.typography.bodyLarge.fontSize.value / BODY_LARGE_BASE_SP

@Composable
internal fun rememberChromeIconScale(): Float = chromeIconScale(LocalDensity.current.fontScale, appFontScale())

@Composable
internal fun rememberChromeTextRatio(): Float = chromeTextRatio(LocalDensity.current.fontScale, appFontScale())
