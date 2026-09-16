package com.facetlauncher.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Private Space's own "incognito" palette — fixed, not derived from Material You/wallpaper and
 * not following the user's own [com.facetlauncher.app.data.model.ThemeMode] setting, matching the
 * platform convention for incognito surfaces (e.g. a browser's private-tab chrome) staying a
 * constant look regardless of the surrounding app's theme. Distinct from every token in
 * `Color.kt` — this screen deliberately doesn't participate in the shared app-wide palette.
 */
private val PrivateSpaceSurface = Color(0xFF0B0B0F)
private val PrivateSpaceSurfaceContainer = Color(0xFF16161C)
private val PrivateSpaceInk = Color(0xFFF2F2F7)
private val PrivateSpaceMuted = Color(0xFFA6A6B3)
private val PrivateSpaceAccent = Color(0xFF9D7BFF)
private val PrivateSpaceHairline = Color(0x1FF2F2F7) // rgba(242,242,247,.12)

/** [PrivateSpaceScreen][com.facetlauncher.app.ui.drawer.PrivateSpaceScreen]'s own surface tone — its cards/rows sit one step lighter than the page background, same convention as [Surface] over [SurfaceContainer] elsewhere. */
val PrivateSpaceCardSurface: Color get() = PrivateSpaceSurfaceContainer
val PrivateSpaceTextColor: Color get() = PrivateSpaceInk
val PrivateSpaceMutedTextColor: Color get() = PrivateSpaceMuted
val PrivateSpaceAccentColor: Color get() = PrivateSpaceAccent
val PrivateSpaceHairlineColor: Color get() = PrivateSpaceHairline

/**
 * Wraps [content] in Private Space's own fixed-dark [MaterialTheme] — nested inside the app's own
 * [FacetLauncherTheme], which a nested `MaterialTheme` call simply shadows for its subtree, no new
 * `CompositionLocal` needed. There's no existing precedent elsewhere in `ui/` for a screen-local
 * theme override — this is a deliberately narrow, self-contained one for a single, distinctly
 * themed screen rather than a general mechanism.
 */
@Composable
fun PrivateSpaceTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = PrivateSpaceAccent,
            onPrimary = PrivateSpaceSurface,
            background = PrivateSpaceSurface,
            surface = PrivateSpaceSurfaceContainer,
            onSurface = PrivateSpaceInk,
            onSurfaceVariant = PrivateSpaceMuted,
            outline = PrivateSpaceHairline,
        ),
        content = content,
    )
}
