package com.facetlauncher.app.ui.theme

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
}
