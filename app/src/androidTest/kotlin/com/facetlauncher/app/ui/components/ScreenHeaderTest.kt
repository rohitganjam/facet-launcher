package com.facetlauncher.app.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.TextLayoutResult
import com.facetlauncher.app.R
import com.facetlauncher.app.domain.HUB_MAX_WIDGETS
import com.facetlauncher.app.ui.hub.HubHeader
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Muted
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

class ScreenHeaderTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun SemanticsNodeInteraction.textStyle() = run {
        val results = mutableListOf<TextLayoutResult>()
        fetchSemanticsNode().config[SemanticsActions.GetTextLayoutResult].action?.invoke(results)
        results.single().layoutInput.style
    }

    @Test
    fun titleIsInkAndSubtitleIsMutedBothWithAShadow() {
        // Given a plain header
        var ink = Color.Unspecified
        var muted = Color.Unspecified
        composeRule.setContent {
            FacetLauncherTheme {
                ink = Ink
                muted = Muted
                ScreenHeader(title = "Title text", subtitle = "Subtitle text")
            }
        }

        // Then the title is Ink and the subtitle is Muted, each with a shadow
        val title = composeRule.onNodeWithText("Title text").textStyle()
        val subtitle = composeRule.onNodeWithText("Subtitle text").textStyle()
        assertEquals(ink, title.color)
        assertEquals(muted, subtitle.color)
        assertNotNull(title.shadow)
        assertNotNull(subtitle.shadow)
    }

    @Test
    fun hubHeaderUsesTheSameInkMutedAndShadowLook() {
        // Given the Hub's header, which the Switch Facets carousel's header matches
        var ink = Color.Unspecified
        var muted = Color.Unspecified
        composeRule.setContent {
            FacetLauncherTheme {
                ink = Ink
                muted = Muted
                HubHeader(widgetCount = 3, columns = 5, isAtCapacity = false, onAddClick = {})
            }
        }

        // Then it renders Ink, Muted and shadowed text like ScreenHeader's own
        val context = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext
        val title = composeRule.onNodeWithText(context.getString(R.string.hub_header_title)).textStyle()
        val subtitle = composeRule.onNodeWithText(context.getString(R.string.hub_header_subtitle, 3, HUB_MAX_WIDGETS)).textStyle()
        assertEquals(ink, title.color)
        assertEquals(muted, subtitle.color)
        assertNotNull(title.shadow)
        assertNotNull(subtitle.shadow)
    }
}
