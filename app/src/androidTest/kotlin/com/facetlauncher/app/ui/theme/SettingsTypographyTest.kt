package com.facetlauncher.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import com.facetlauncher.app.data.model.FontScaleOption
import com.facetlauncher.app.data.model.FontWeightOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SettingsTypographyTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun unscaledTypographyIgnoresTheFontSizeAndWeightOptionsWhileLauncherTypographyFollowsThem() {
        // Given the largest Font size option and the thinnest Font weight
        var themed = TextUnit.Unspecified
        var unscaled = TextUnit.Unspecified
        var restored = TextUnit.Unspecified
        var themedWeight: FontWeight? = null
        var unscaledWeight: FontWeight? = null
        var restoredWeight: FontWeight? = null
        composeRule.setContent {
            FacetLauncherTheme(fontScaleOption = FontScaleOption.HUGE, homeAppsFontWeight = FontWeightOption.THIN) {
                themed = MaterialTheme.typography.bodyLarge.fontSize
                themedWeight = MaterialTheme.typography.bodyLarge.fontWeight
                UnscaledTypography {
                    unscaled = MaterialTheme.typography.bodyLarge.fontSize
                    unscaledWeight = MaterialTheme.typography.bodyLarge.fontWeight
                    LauncherScaledTypography {
                        restored = MaterialTheme.typography.bodyLarge.fontSize
                        restoredWeight = MaterialTheme.typography.bodyLarge.fontWeight
                    }
                }
            }
        }

        // Then Settings text stays at the base size, and a preview inside it gets the chosen size back
        assertEquals(16f, unscaled.value, 0.01f)
        assertTrue(themed.value > unscaled.value)
        assertEquals(themed.value, restored.value, 0.01f)
        assertEquals(FontWeight.Normal, unscaledWeight)
        assertEquals(FontWeight.Thin, themedWeight)
        assertEquals(FontWeight.Thin, restoredWeight)
    }
}
