package com.facetlauncher.app.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppListColumnAlignment
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.model.GlassStyle
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** With glass on, the two-column list leaves the same gap between its columns as between its rows. */
class HomeAppTwoColumnGapTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val items = (1..4).map { PlacedItem.SingleApp(AppInfo(packageName = "com.example.app$it", activityName = ".Main", label = "App $it", icon = null)) }

    private fun setList(glass: GlassStyle) {
        composeRule.setContent {
            FacetLauncherTheme {
                Box(Modifier.width(400.dp)) {
                    HomeAppTwoColumnList(
                        content = HomeAppListContent(items, HomeItemStyle(labelColor = Color.White), glass),
                        actions = HomeItemActions(onAppClick = {}),
                        columnAlignment = AppListColumnAlignment.BOTH_LEFT,
                        appRowPresentation = AppRowPresentation.ICON_AND_TEXT,
                        verticalPadding = 8.dp,
                        iconSize = 48.dp,
                    )
                }
            }
        }
    }

    private fun panelOf(index: Int): DpRect =
        composeRule.onNodeWithTag("home_app_row_content_${items[index].stableKey()}", useUnmergedTree = true).getUnclippedBoundsInRoot()

    @Test
    fun `glass rows have the same gap across columns as between rows`() {
        // Given a glass two-column list: 0 and 1 on the first row, 2 and 3 on the second
        setList(GlassStyle.FROST)

        // Then the gap beside a row equals the gap below it
        val across = panelOf(1).left - panelOf(0).right
        val down = panelOf(2).top - panelOf(0).bottom
        assertEquals(GlassRowGap.value, down.value, 0.5f)
        assertEquals(down.value, across.value, 0.5f)
    }

    @Test
    fun `without glass the columns touch like the rows do`() {
        // Given a two-column list without glass
        setList(GlassStyle.OFF)

        // Then neither the columns nor the rows have a gap
        assertEquals(0f, (panelOf(1).left - panelOf(0).right).value, 0.5f)
        assertEquals(0f, (panelOf(2).top - panelOf(0).bottom).value, 0.5f)
    }
}
