package com.facetlauncher.app.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** The single-column list gives every row one width: the widest item's, at least half the list, never past the whole list. */
class HomeAppListColumnWidthTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val listWidth = 400.dp

    private fun app(index: Int, label: String) = PlacedItem.SingleApp(AppInfo(packageName = "com.example.app$index", activityName = ".Main", label = label, icon = null))

    private fun setList(items: List<PlacedItem.SingleApp>) {
        composeRule.setContent {
            FacetLauncherTheme {
                Box(Modifier.width(listWidth)) {
                    HomeAppListColumn(
                        content = HomeAppListContent(items, HomeItemStyle(labelColor = Color.White)),
                        actions = HomeItemActions(onAppClick = {}),
                        position = AppRowPosition.LEFT,
                        appRowPresentation = AppRowPresentation.ICON_AND_TEXT,
                        verticalPadding = 8.dp,
                        iconSize = 48.dp,
                        modifier = Modifier.width(listWidth),
                        minRowWidthFraction = 0.5f,
                    )
                }
            }
        }
    }

    private fun widthOf(item: PlacedItem.SingleApp): Dp =
        composeRule.onNodeWithTag("home_app_row_${item.stableKey()}", useUnmergedTree = true).getUnclippedBoundsInRoot().let { it.right - it.left }

    @Test
    fun `short labels give every row half the list`() {
        // Given a list of short names
        val items = listOf(app(1, "Mail"), app(2, "Maps"), app(3, "Notes"))
        setList(items)

        // Then every row is half the list wide
        items.forEach { assertEquals(200f, widthOf(it).value, 1f) }
    }

    @Test
    fun `a longer item widens every row together but not past the list`() {
        // Given one name too long for half the list
        val items = listOf(app(1, "Mail"), app(2, "A really long application name that needs more room than half"), app(3, "Notes"))
        setList(items)

        // Then all rows share one width, wider than half and within the list
        val widths = items.map { widthOf(it).value }
        assertTrue("rows differ: $widths", widths.all { kotlin.math.abs(it - widths.first()) < 1f })
        assertTrue("not wider than half: $widths", widths.first() > 200f)
        assertTrue("past the list: $widths", widths.first() <= listWidth.value + 1f)
    }
}
