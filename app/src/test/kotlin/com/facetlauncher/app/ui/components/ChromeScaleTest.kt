package com.facetlauncher.app.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class ChromeScaleTest {

    @Test
    fun `icons keep their base size at default text`() {
        assertEquals(1f, chromeIconScale(systemFontScale = 1f, appFontScale = 1f), 0.001f)
    }

    @Test
    fun `icons never shrink below their base size at small text`() {
        assertEquals(1f, chromeIconScale(systemFontScale = 0.85f, appFontScale = 0.85f), 0.001f)
    }

    @Test
    fun `icons grow with the text size`() {
        assertEquals(1.15f, chromeIconScale(systemFontScale = 1.15f, appFontScale = 1f), 0.001f)
        assertEquals(1.15f, chromeIconScale(systemFontScale = 1f, appFontScale = 1.15f), 0.001f)
    }

    @Test
    fun `icons stop at one point three times from either scale or both`() {
        assertEquals(1.3f, chromeIconScale(systemFontScale = 2f, appFontScale = 1f), 0.001f)
        assertEquals(1.3f, chromeIconScale(systemFontScale = 1f, appFontScale = 1.45f), 0.001f)
        assertEquals(1.3f, chromeIconScale(systemFontScale = 2f, appFontScale = 1.45f), 0.001f)
    }

    @Test
    fun `the two scales multiply`() {
        assertEquals(1.3f, chromeIconScale(systemFontScale = 1.15f, appFontScale = 1.15f), 0.001f)
    }

    @Test
    fun `chrome labels are not shrunk while the combined scale is within the cap`() {
        assertEquals(1f, chromeTextRatio(systemFontScale = 1f, appFontScale = 1f), 0.001f)
        assertEquals(1f, chromeTextRatio(systemFontScale = 1.3f, appFontScale = 1f), 0.001f)
        assertEquals(1f, chromeTextRatio(systemFontScale = 0.85f, appFontScale = 0.85f), 0.001f)
    }

    @Test
    fun `chrome labels are shrunk so they land on one point three times past the cap`() {
        // 2x system: the label is asked for 2x, so it is scaled by 1.3 / 2 to end at 1.3x
        assertEquals(0.65f, chromeTextRatio(systemFontScale = 2f, appFontScale = 1f), 0.001f)
        // Both at their maximum: 1.3 / 2.9
        assertEquals(1.3f / 2.9f, chromeTextRatio(systemFontScale = 2f, appFontScale = 1.45f), 0.001f)
    }
}
