package com.facetlauncher.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.facetlauncher.app.data.model.FontScaleOption
import com.facetlauncher.app.ui.theme.FacetLauncherTheme

/**
 * Runs [content] as if the system font scale were [systemFontScale] and the app's own Text size
 * were [appTextSize], without touching the device's real settings. The two multiply, as they do
 * for a user who has raised both.
 */
@Composable
internal fun WithTextScale(systemFontScale: Float, appTextSize: FontScaleOption, content: @Composable () -> Unit) {
    val base = LocalDensity.current
    CompositionLocalProvider(LocalDensity provides Density(base.density, systemFontScale)) {
        FacetLauncherTheme(fontScaleOption = appTextSize, content = content)
    }
}

/**
 * Sets the real system font scale on the test activity, which is what a sheet's own window reads —
 * a [WithTextScale] density override doesn't reach it. Call [restore] when the test ends, since the
 * resources can be shared with later tests.
 */
internal class SystemFontScale(private val activity: androidx.activity.ComponentActivity) {
    private val original = activity.resources.configuration.fontScale

    fun set(scale: Float) = apply(scale)

    fun restore() = apply(original)

    @Suppress("DEPRECATION")
    private fun apply(scale: Float) {
        val config = android.content.res.Configuration(activity.resources.configuration).apply { fontScale = scale }
        activity.resources.updateConfiguration(config, activity.resources.displayMetrics)
    }
}
