package com.facetlauncher.app.ui.theme

import androidx.compose.ui.text.font.FontFamily
import com.facetlauncher.app.data.model.ClockFontOption
import com.facetlauncher.app.data.model.LauncherFontOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ClockFontsTest {

    @Test
    fun `LAUNCHER_DEFAULT resolves to whatever the launcher font option currently is`() {
        assertEquals(LauncherFontOption.POPPINS.fontFamily, ClockFontOption.LAUNCHER_DEFAULT.resolveFontFamily(LauncherFontOption.POPPINS))
        assertEquals(FontFamily.SansSerif, ClockFontOption.LAUNCHER_DEFAULT.resolveFontFamily(LauncherFontOption.SYSTEM))
    }

    @Test
    fun `a real clock font choice ignores the launcher font option entirely`() {
        assertEquals(FontFamily.SansSerif, ClockFontOption.SYSTEM.resolveFontFamily(LauncherFontOption.POPPINS))
        assertNotEquals(LauncherFontOption.POPPINS.fontFamily, ClockFontOption.MANROPE.resolveFontFamily(LauncherFontOption.POPPINS))
    }

    @Test
    fun `every launcher font option resolves to a distinct font family`() {
        val families = LauncherFontOption.entries.map { it.fontFamily }
        assertEquals(LauncherFontOption.entries.size, families.distinct().size)
    }

    @Test
    fun `new font options resolve independently of each other`() {
        assertNotEquals(LauncherFontOption.INTER.fontFamily, LauncherFontOption.MONTSERRAT.fontFamily)
        assertNotEquals(LauncherFontOption.MONTSERRAT.fontFamily, LauncherFontOption.LATO.fontFamily)
        assertEquals(LauncherFontOption.INTER.fontFamily, ClockFontOption.INTER.resolveFontFamily(LauncherFontOption.SYSTEM))
        assertEquals(LauncherFontOption.MONTSERRAT.fontFamily, ClockFontOption.MONTSERRAT.resolveFontFamily(LauncherFontOption.SYSTEM))
        assertEquals(LauncherFontOption.LATO.fontFamily, ClockFontOption.LATO.resolveFontFamily(LauncherFontOption.SYSTEM))
    }
}
