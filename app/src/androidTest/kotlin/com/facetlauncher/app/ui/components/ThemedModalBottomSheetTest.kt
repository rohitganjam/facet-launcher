package com.facetlauncher.app.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import com.facetlauncher.app.data.model.FontScaleOption
import com.facetlauncher.app.data.model.FontWeightOption
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ThemedModalBottomSheetTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun sheetTextIgnoresTheFontSizeAndWeightOptions() {
        // Given the largest Font size and the thinnest Font weight, with a sheet open
        var outsideSize = TextUnit.Unspecified
        var sheetSize = TextUnit.Unspecified
        var sheetWeight: FontWeight? = null
        composeRule.setContent {
            FacetLauncherTheme(fontScaleOption = FontScaleOption.HUGE, homeAppsFontWeight = FontWeightOption.THIN) {
                outsideSize = MaterialTheme.typography.bodyLarge.fontSize
                ThemedModalBottomSheet(onDismissRequest = {}) {
                    sheetSize = MaterialTheme.typography.bodyLarge.fontSize
                    sheetWeight = MaterialTheme.typography.bodyLarge.fontWeight
                }
            }
        }
        composeRule.waitForIdle()

        // Then the sheet keeps the base size and regular weight while the rest of the app follows the options
        assertTrue(outsideSize.value > sheetSize.value)
        assertEquals(16f, sheetSize.value, 0.01f)
        assertEquals(FontWeight.Normal, sheetWeight)
    }
}
