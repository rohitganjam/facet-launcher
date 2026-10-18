package com.facetlauncher.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

/** The app-wide typography with the Font size option applied — what Home, the drawer and the previews render with. */
@Suppress("CompositionLocalAllowlist") // Read by the Settings previews through a wrapper; threading it would touch every preview call site.
internal val LocalLauncherTypography = staticCompositionLocalOf { Typography() }

/** The same typography at scale 1 and regular weight: only the font family follows the user's choice. */
@Suppress("CompositionLocalAllowlist") // Read only by [UnscaledTypography], once, at the nav host.
internal val LocalUnscaledTypography = staticCompositionLocalOf { Typography() }

/** Settings screens keep their own text size and weight; only Home and the previews follow the Font size and weight options. */
@Composable
internal fun UnscaledTypography(content: @Composable () -> Unit) = WithTypography(LocalUnscaledTypography.current, content)

/** Restores the Font size and weight options inside [UnscaledTypography] — for Settings previews that show how Home will look. */
@Composable
internal fun LauncherScaledTypography(content: @Composable () -> Unit) = WithTypography(LocalLauncherTypography.current, content)

@Composable
private fun WithTypography(typography: Typography, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme,
        shapes = MaterialTheme.shapes,
        typography = typography,
        content = content,
    )
}
