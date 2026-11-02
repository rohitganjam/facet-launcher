package com.facetlauncher.app.ui.theme

import com.facetlauncher.app.data.model.SystemBarIconStyle
import org.junit.Assert.assertEquals
import org.junit.Test

class SystemBarsTest {

    @Test
    fun `the status bar is hidden only over the wallpaper with the setting on and no reveal`() {
        for (backdrop in SystemBarsBackdrop.entries) {
            val overWallpaper = backdrop == SystemBarsBackdrop.WALLPAPER
            assertEquals(overWallpaper, isStatusBarHidden(hideSetting = true, backdrop = backdrop, revealed = false))
            assertEquals(false, isStatusBarHidden(hideSetting = false, backdrop = backdrop, revealed = false))
            assertEquals(false, isStatusBarHidden(hideSetting = true, backdrop = backdrop, revealed = true))
        }
    }

    @Test
    fun `a swipe reveals a hidden status bar once and then leaves the next swipe for the shade`() {
        // Given the setting on over the wallpaper, with the system not showing the bar
        val state = SystemBarsState().apply {
            backdrop = SystemBarsBackdrop.WALLPAPER
            hideStatusBar = true
        }

        // When swiping down, then swiping again while it shows
        val first = state.revealStatusBar()
        val second = state.revealStatusBar()

        // Then the first reveals it and the second is not consumed
        assertEquals(true, first)
        assertEquals(true, state.statusBarRevealed)
        assertEquals(false, second)
    }

    @Test
    fun `a swipe does not reveal when the system already shows the bar or the setting is off`() {
        // Given the system showing the bar after an edge swipe
        val shown = SystemBarsState().apply {
            backdrop = SystemBarsBackdrop.WALLPAPER
            hideStatusBar = true
            systemStatusBarVisible = { true }
        }
        // And the setting off
        val off = SystemBarsState().apply { backdrop = SystemBarsBackdrop.WALLPAPER }

        // Then neither consumes the swipe
        assertEquals(false, shown.revealStatusBar())
        assertEquals(false, off.revealStatusBar())
        assertEquals(false, shown.statusBarRevealed)
    }

    @Test
    fun `over the wallpaper, match theme follows the theme`() {
        assertEquals(true, useLightSystemBarIcons(SystemBarIconStyle.MATCH_THEME, SystemBarsBackdrop.WALLPAPER, isDarkTheme = true))
        assertEquals(false, useLightSystemBarIcons(SystemBarIconStyle.MATCH_THEME, SystemBarsBackdrop.WALLPAPER, isDarkTheme = false))
    }

    @Test
    fun `over the wallpaper, light and dark override the theme either way`() {
        for (isDark in listOf(true, false)) {
            assertEquals(true, useLightSystemBarIcons(SystemBarIconStyle.LIGHT, SystemBarsBackdrop.WALLPAPER, isDark))
            assertEquals(false, useLightSystemBarIcons(SystemBarIconStyle.DARK, SystemBarsBackdrop.WALLPAPER, isDark))
        }
    }

    @Test
    fun `on a theme surface the icons follow the theme whatever the setting says`() {
        for (style in SystemBarIconStyle.entries) {
            assertEquals(true, useLightSystemBarIcons(style, SystemBarsBackdrop.THEME_SURFACE, isDarkTheme = true))
            assertEquals(false, useLightSystemBarIcons(style, SystemBarsBackdrop.THEME_SURFACE, isDarkTheme = false))
        }
    }

    @Test
    fun `on an always-dark surface the icons are always light`() {
        for (style in SystemBarIconStyle.entries) {
            for (isDark in listOf(true, false)) {
                assertEquals(true, useLightSystemBarIcons(style, SystemBarsBackdrop.DARK_SURFACE, isDark))
            }
        }
    }

    @Test
    fun `a dark top keeps status icons light but lets the navigation bar follow the theme`() {
        for (style in SystemBarIconStyle.entries) {
            for (isDark in listOf(true, false)) {
                assertEquals(true, useLightSystemBarIcons(style, SystemBarsBackdrop.DARK_TOP, isDark))
                assertEquals(isDark, useLightNavigationBarIcons(style, SystemBarsBackdrop.DARK_TOP, isDark))
            }
        }
    }

    @Test
    fun `every other backdrop uses the same icons for the navigation bar`() {
        for (backdrop in SystemBarsBackdrop.entries.filter { it != SystemBarsBackdrop.DARK_TOP }) {
            for (isDark in listOf(true, false)) {
                assertEquals(
                    useLightSystemBarIcons(SystemBarIconStyle.MATCH_THEME, backdrop, isDark),
                    useLightNavigationBarIcons(SystemBarIconStyle.MATCH_THEME, backdrop, isDark),
                )
            }
        }
    }
}
