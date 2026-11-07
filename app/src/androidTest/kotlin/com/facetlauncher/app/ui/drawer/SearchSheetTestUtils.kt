package com.facetlauncher.app.ui.drawer

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick

/** Opens the search sheet by tapping the drawer's search bar, unless it is already open. */
fun ComposeTestRule.openSearchSheet() {
    if (onAllNodesWithTag("drawer_search_field").fetchSemanticsNodes().isEmpty()) {
        onNodeWithTag("drawer_search_open").performClick()
        waitUntil(timeoutMillis = 5_000) { onAllNodesWithTag("drawer_search_field").fetchSemanticsNodes().isNotEmpty() }
    }
}
