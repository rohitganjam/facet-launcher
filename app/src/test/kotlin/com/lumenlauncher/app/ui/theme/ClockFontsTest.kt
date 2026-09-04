package com.lumenlauncher.app.ui.theme

import androidx.compose.ui.text.font.FontFamily
import com.lumenlauncher.app.data.model.ClockFontOption
import com.lumenlauncher.app.data.model.LauncherFontOption
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
}
