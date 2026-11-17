package com.facetlauncher.app.ui.launcher

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import com.facetlauncher.app.data.model.SwipeDirection
import androidx.compose.ui.test.assertLeftPositionInRootIsEqualTo
import androidx.compose.ui.test.assertTopPositionInRootIsEqualTo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test

class PanelPlacementTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setPanel(direction: SwipeDirection, progress: Float) {
        composeRule.setContent {
            Box(Modifier.size(200.dp, 400.dp)) {
                Box(Modifier.fillMaxSize().panelPlacement(direction) { progress }.testTag("panel"))
            }
        }
    }

    @Test
    fun `a closed panel is parked a full container below on the first frame`() {
        // Given a panel that opens upward, closed
        setPanel(SwipeDirection.UP, progress = 0f)

        // Then it sits exactly one container height down, from the size it was measured at, with nothing on screen
        composeRule.onNodeWithTag("panel").assertTopPositionInRootIsEqualTo(400.dp)
    }

    @Test
    fun `a closed panel from the left is parked a full container width away`() {
        // Given a panel pulled in from the left, closed
        setPanel(SwipeDirection.RIGHT, progress = 0f)

        // Then it sits one container width to the left
        composeRule.onNodeWithTag("panel").assertLeftPositionInRootIsEqualTo((-200).dp)
    }

    @Test
    fun `an open panel is flush with the container`() {
        // Given a panel fully open
        setPanel(SwipeDirection.UP, progress = 1f)

        // Then it covers the container
        composeRule.onNodeWithTag("panel").assertTopPositionInRootIsEqualTo(0.dp)
    }

    @Test
    fun `a half open panel is half a container down`() {
        // Given a panel half open
        setPanel(SwipeDirection.UP, progress = 0.5f)

        // Then it is half a container height down
        composeRule.onNodeWithTag("panel").assertTopPositionInRootIsEqualTo(200.dp)
    }
}
