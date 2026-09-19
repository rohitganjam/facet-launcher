package com.facetlauncher.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight
import com.facetlauncher.app.data.model.FontWeightOption
import org.junit.Assert.assertEquals
import org.junit.Test

class TypeTest {

    @Test
    fun `each FontWeightOption resolves to its matching real FontWeight`() {
        assertEquals(FontWeight.Thin, FontWeightOption.THIN.resolve())
        assertEquals(FontWeight.ExtraLight, FontWeightOption.EXTRA_LIGHT.resolve())
        assertEquals(FontWeight.Light, FontWeightOption.LIGHT.resolve())
        assertEquals(FontWeight.Normal, FontWeightOption.REGULAR.resolve())
        assertEquals(FontWeight.Medium, FontWeightOption.MEDIUM.resolve())
        assertEquals(FontWeight.SemiBold, FontWeightOption.SEMI_BOLD.resolve())
    }

    @Test
    fun `facetTypography scales every role's fontSize and lineHeight by fontScale`() {
        val m3Defaults = Typography()
        val scaled = facetTypography(fontScale = 1.3f)

        assertEquals(m3Defaults.displayLarge.fontSize * 1.3f, scaled.displayLarge.fontSize)
        assertEquals(m3Defaults.displayLarge.lineHeight * 1.3f, scaled.displayLarge.lineHeight)
        assertEquals(m3Defaults.bodyLarge.fontSize * 1.3f, scaled.bodyLarge.fontSize)
        assertEquals(m3Defaults.bodyLarge.lineHeight * 1.3f, scaled.bodyLarge.lineHeight)
        assertEquals(m3Defaults.labelSmall.fontSize * 1.3f, scaled.labelSmall.fontSize)
    }

    @Test
    fun `facetTypography leaves fontSize untouched at the default 1x scale`() {
        val m3Defaults = Typography()
        val unscaled = facetTypography()

        assertEquals(m3Defaults.bodyLarge.fontSize, unscaled.bodyLarge.fontSize)
        assertEquals(m3Defaults.titleMedium.fontSize, unscaled.titleMedium.fontSize)
    }

    @Test
    fun `facetTypography applies fontWeight only to body roles, never title or headline`() {
        val m3Defaults = Typography()
        val weighted = facetTypography(fontWeight = FontWeight.Thin)

        assertEquals(FontWeight.Thin, weighted.bodyLarge.fontWeight)
        assertEquals(FontWeight.Thin, weighted.bodyMedium.fontWeight)
        assertEquals(FontWeight.Thin, weighted.bodySmall.fontWeight)
        // title/label/headline/display keep Material's own per-role weight — see facetTypography's
        // own doc for why (dialog titles, context menus, onboarding copy all read these roles).
        assertEquals(m3Defaults.titleMedium.fontWeight, weighted.titleMedium.fontWeight)
        assertEquals(m3Defaults.labelLarge.fontWeight, weighted.labelLarge.fontWeight)
        assertEquals(m3Defaults.headlineSmall.fontWeight, weighted.headlineSmall.fontWeight)
    }
}
