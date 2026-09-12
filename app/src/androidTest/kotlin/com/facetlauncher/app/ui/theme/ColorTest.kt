package com.facetlauncher.app.ui.theme

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.test.junit4.createComposeRule
import com.facetlauncher.app.data.model.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Every design-token color resolves through [LocalIsDarkTheme] rather than being a plain
 * constant (see `Color.kt`) — that local is only ever computed and provided by
 * [FacetLauncherTheme] (from [ThemeMode], following an overridden [LocalConfiguration] when the
 * mode is [ThemeMode.SYSTEM] — real device/emulator dark-mode state would be slower and less
 * deterministic to flip per-test), so every case here renders through it rather than reading the
 * color properties bare. Checks the resolved value against the exact hex values in
 * `design_handoff_minimal_launcher/README.md`'s Light/Dark Design Tokens tables. Accent is
 * pinned to "Basic colors" / [AccentSwatch.BLUE] (whose own light/dark pair is exactly the
 * app's original stand-in values) rather than "Wallpaper colors", since Material You's real
 * dynamic color depends on whatever wallpaper happens to be set on the test device/emulator.
 *
 * [androidx.compose.ui.test.junit4.ComposeContentTestRule.setContent] can only be called once
 * per test — [resolveBoth] renders light and dark side by side in a single composition (two
 * independent [FacetLauncherTheme] subtrees, each with its own overridden [LocalConfiguration])
 * rather than calling `setContent` twice per test.
 */
class ColorTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun resolveBoth(color: @Composable () -> Color): Pair<Color, Color> {
        var light: Color? = null
        var dark: Color? = null
        composeRule.setContent {
            themed(dark = false) { light = color() }
            themed(dark = true) { dark = color() }
        }
        composeRule.waitForIdle()
        return requireNotNull(light) to requireNotNull(dark)
    }

    @Composable
    private fun themed(dark: Boolean, content: @Composable () -> Unit) {
        val configuration = Configuration(LocalConfiguration.current).apply {
            uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or
                if (dark) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO
        }
        CompositionLocalProvider(LocalConfiguration provides configuration) {
            FacetLauncherTheme(
                themeMode = ThemeMode.SYSTEM,
                accentFromSystem = false,
                customAccentSwatch = AccentSwatch.BLUE,
                content = content,
            )
        }
    }

    @Test
    fun `Wallpaper resolves to the light and dark tokens`() {
        val (light, dark) = resolveBoth { Wallpaper }
        assertEquals(0xFFE9ECF2.toInt(), light.toArgb())
        assertEquals(0xFF14171D.toInt(), dark.toArgb())
    }

    @Test
    fun `Surface resolves to the light and dark tokens`() {
        val (light, dark) = resolveBoth { Surface }
        assertEquals(0xFFFFFFFF.toInt(), light.toArgb())
        assertEquals(0xFF20242D.toInt(), dark.toArgb())
    }

    @Test
    fun `Ink resolves to the light and dark tokens`() {
        val (light, dark) = resolveBoth { Ink }
        assertEquals(0xFF020817.toInt(), light.toArgb())
        assertEquals(0xFFE7EAF0.toInt(), dark.toArgb())
    }

    @Test
    fun `IconTile resolves to the light and dark tokens`() {
        val (light, dark) = resolveBoth { IconTile }
        assertEquals(0xFF1E293B.toInt(), light.toArgb())
        assertEquals(0xFF39424F.toInt(), dark.toArgb())
    }

    @Test
    fun `ErrorColor resolves to the light and dark tokens`() {
        val (light, dark) = resolveBoth { ErrorColor }
        assertEquals(0xFFDC2626.toInt(), light.toArgb())
        assertEquals(0xFFF2857F.toInt(), dark.toArgb())
    }

    @Test
    fun `SuccessColor resolves to the light and dark tokens`() {
        val (light, dark) = resolveBoth { SuccessColor }
        assertEquals(0xFF16A34A.toInt(), light.toArgb())
        assertEquals(0xFF7FD493.toInt(), dark.toArgb())
    }

    @Test
    fun `Accent resolves to the Basic colors swatch light and dark values`() {
        val (light, dark) = resolveBoth { Accent }
        assertEquals(0xFF2563EB.toInt(), light.toArgb())
        assertEquals(0xFFA8C7FA.toInt(), dark.toArgb())
    }

    @Test
    fun `derived Home and Drawer text colors follow Ink and Muted into dark mode`() {
        var ink: Color? = null
        var muted: Color? = null
        var homeAppTextColor: Color? = null
        var drawerAppTextColor: Color? = null
        var drawerHeaderTextColor: Color? = null
        var drawerRailTextColor: Color? = null
        composeRule.setContent {
            themed(dark = true) {
                ink = Ink
                muted = Muted
                homeAppTextColor = HomeAppTextColor
                drawerAppTextColor = DrawerAppTextColor
                drawerHeaderTextColor = DrawerHeaderTextColor
                drawerRailTextColor = DrawerRailTextColor
            }
        }
        composeRule.waitForIdle()

        assertEquals(requireNotNull(ink).toArgb(), requireNotNull(homeAppTextColor).toArgb())
        assertEquals(requireNotNull(ink).toArgb(), requireNotNull(drawerAppTextColor).toArgb())
        // Muted, not Faint — both are real readable navigational text, not decorative (see
        // Color.kt's own doc comment on DrawerHeaderTextColor/DrawerRailTextColor).
        assertEquals(requireNotNull(muted).toArgb(), requireNotNull(drawerHeaderTextColor).toArgb())
        assertEquals(requireNotNull(muted).toArgb(), requireNotNull(drawerRailTextColor).toArgb())
    }
}
