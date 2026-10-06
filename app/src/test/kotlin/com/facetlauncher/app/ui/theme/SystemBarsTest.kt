package com.facetlauncher.app.ui.theme

import com.facetlauncher.app.data.model.SystemBarIconStyle
import org.junit.Assert.assertEquals
import org.junit.Test

class SystemBarsTest {

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
}
