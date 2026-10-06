package com.facetlauncher.app.ui.theme

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.core.view.WindowCompat
import com.facetlauncher.app.data.model.SystemBarIconStyle
import com.facetlauncher.app.data.model.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Real window flags, not just the pure rule — `isAppearanceLight*Bars` means *dark* icons. */
class SystemBarsAppearanceTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun setContent(
        themeMode: ThemeMode,
        style: SystemBarIconStyle,
        backdrop: () -> SystemBarsBackdrop?,
    ) {
        composeRule.setContent {
            FacetLauncherTheme(themeMode = themeMode) {
                val state = remember { SystemBarsState() }
                ProvideSystemBars(style, state) {
                    backdrop()?.let { SystemBarsBackdropEffect(it) }
                }
            }
        }
        composeRule.waitForIdle()
    }

    private fun darkStatusIcons(): Boolean {
        val window = composeRule.activity.window
        return WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars
    }

    private fun darkNavIcons(): Boolean {
        val window = composeRule.activity.window
        return WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightNavigationBars
    }

    @Test
    fun `match theme over the wallpaper gives dark icons on a light theme and light icons on a dark one`() {
        setContent(ThemeMode.LIGHT, SystemBarIconStyle.MATCH_THEME) { SystemBarsBackdrop.WALLPAPER }
        assertEquals(true, darkStatusIcons())
        assertEquals(true, darkNavIcons())

        composeRule.activityRule.scenario.recreate()
        setContent(ThemeMode.DARK, SystemBarIconStyle.MATCH_THEME) { SystemBarsBackdrop.WALLPAPER }
        assertEquals(false, darkStatusIcons())
        assertEquals(false, darkNavIcons())
    }

    @Test
    fun `light override over the wallpaper forces light icons on both bars even on a light theme`() {
        setContent(ThemeMode.LIGHT, SystemBarIconStyle.LIGHT) { SystemBarsBackdrop.WALLPAPER }

        assertEquals(false, darkStatusIcons())
        assertEquals(false, darkNavIcons())
    }

    @Test
    fun `dark override over the wallpaper forces dark icons on both bars even on a dark theme`() {
        setContent(ThemeMode.DARK, SystemBarIconStyle.DARK) { SystemBarsBackdrop.WALLPAPER }

        assertEquals(true, darkStatusIcons())
        assertEquals(true, darkNavIcons())
    }

    @Test
    fun `on a theme surface the override is ignored and the theme wins`() {
        setContent(ThemeMode.LIGHT, SystemBarIconStyle.LIGHT) { SystemBarsBackdrop.THEME_SURFACE }

        assertEquals(true, darkStatusIcons())
        assertEquals(true, darkNavIcons())
    }

    @Test
    fun `an always-dark surface forces light icons even on a light theme`() {
        setContent(ThemeMode.LIGHT, SystemBarIconStyle.DARK) { SystemBarsBackdrop.DARK_SURFACE }

        assertEquals(false, darkStatusIcons())
        assertEquals(false, darkNavIcons())
    }

    @Test
    fun `leaving the wallpaper screen falls back to the theme`() {
        // Given a light theme with a forced-light override over the wallpaper
        var onHome by mutableStateOf(true)
        setContent(ThemeMode.LIGHT, SystemBarIconStyle.LIGHT) { if (onHome) SystemBarsBackdrop.WALLPAPER else null }
        assertEquals(false, darkStatusIcons())

        // When the Home screen leaves composition (navigating to Settings)
        composeRule.runOnUiThread { onHome = false }
        composeRule.waitForIdle()

        // Then the bars follow the light theme again
        assertEquals(true, darkStatusIcons())
        assertEquals(true, darkNavIcons())
    }
}
