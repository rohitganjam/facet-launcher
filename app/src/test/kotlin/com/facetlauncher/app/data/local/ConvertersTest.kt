package com.facetlauncher.app.data.local

import com.facetlauncher.app.data.model.AppProfile
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.ClockFontOption
import com.facetlauncher.app.data.model.ClockTemplateId
import com.facetlauncher.app.data.model.ListContentMode
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * A real production crash (see chat history): [ClockColorOption]'s old `INK`/`WHITE`/`BLACK`
 * constants were renamed/removed, and every already-installed database still had those exact
 * strings stored — `toClockColorOption` used to return null for them, which Room's generated code
 * for these NOT NULL columns has no fallback for, crashing outright on every launch
 * (`IllegalStateException: Expected NON-NULL '...', but it was NULL`). Every `to*` converter must
 * fall back to a real value for any string it doesn't recognize, not just the constants that
 * happen to exist today — this test asserts that directly with a string no version of any of
 * these enums has ever used, not just the specific renamed ones from this incident.
 */
class ConvertersTest {

    private val converters = Converters()

    @Test
    fun `an unrecognized stored string falls back to the default instead of returning null`() {
        assertEquals(ListContentMode.FAVORITES, converters.toListContentMode("NOT_A_REAL_VALUE"))
        assertEquals(ClockTemplateId.LIGHT_STACK, converters.toClockTemplateId("NOT_A_REAL_VALUE"))
        assertEquals(ClockFontOption.LAUNCHER_DEFAULT, converters.toClockFontOption("NOT_A_REAL_VALUE"))
        assertEquals(ClockColorOption.THEME, converters.toClockColorOption("NOT_A_REAL_VALUE"))
        assertEquals(AppRowPosition.LEFT, converters.toAppRowPosition("NOT_A_REAL_VALUE"))
        assertEquals(AppRowPresentation.ICON_AND_TEXT, converters.toAppRowPresentation("NOT_A_REAL_VALUE"))
    }

    @Test
    fun `the exact old ClockColorOption constants renamed in this incident fall back cleanly`() {
        assertEquals(ClockColorOption.THEME, converters.toClockColorOption("INK"))
        assertEquals(ClockColorOption.THEME, converters.toClockColorOption("WHITE"))
        assertEquals(ClockColorOption.THEME, converters.toClockColorOption("BLACK"))
    }

    @Test
    fun `a null stored value falls back to the default too`() {
        assertEquals(ListContentMode.FAVORITES, converters.toListContentMode(null))
        assertEquals(ClockColorOption.THEME, converters.toClockColorOption(null))
    }

    @Test
    fun `a recognized value round-trips unchanged`() {
        assertEquals(ClockColorOption.ACCENT_SECONDARY, converters.toClockColorOption(converters.fromClockColorOption(ClockColorOption.ACCENT_SECONDARY)))
        assertEquals(AppRowPosition.RIGHT, converters.toAppRowPosition(converters.fromAppRowPosition(AppRowPosition.RIGHT)))
    }

    @Test
    fun `AppProfile PRIVATE round-trips unchanged, and an unrecognized value falls back to PERSONAL`() {
        assertEquals(AppProfile.PRIVATE, converters.toAppProfile(converters.fromAppProfile(AppProfile.PRIVATE)))
        assertEquals(AppProfile.WORK, converters.toAppProfile(converters.fromAppProfile(AppProfile.WORK)))
        assertEquals(AppProfile.PERSONAL, converters.toAppProfile("NOT_A_REAL_VALUE"))
    }
}
