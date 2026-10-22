package com.facetlauncher.app.ui.components

import android.os.Parcel
import android.os.UserHandle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppProfile
import com.facetlauncher.app.data.model.AppSortOption
import com.facetlauncher.app.data.model.SortDirection
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Rule
import org.junit.Test

/** A real, distinct [UserHandle] for [id] — see `AppDrawerScreenTest.fakeUserHandle` for why not a mock. */
private fun userHandle(id: Int): UserHandle {
    val parcel = Parcel.obtain()
    parcel.writeInt(id)
    parcel.setDataPosition(0)
    val handle = UserHandle(parcel)
    parcel.recycle()
    return handle
}

class AppPickerScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val personalCalendar = AppInfo(
        packageName = "com.google.android.calendar",
        activityName = "com.android.calendar.AllInOneActivity",
        label = "Calendar",
        icon = null,
        profile = AppProfile.PERSONAL,
        userHandle = userHandle(0),
    )
    private val workCalendar = personalCalendar.copy(profile = AppProfile.WORK, userHandle = userHandle(10))

    private fun setContent(selectedResults: List<AppInfo>, otherResults: List<AppInfo>) {
        composeRule.setContent {
            FacetLauncherTheme {
                AppPickerScreen(
                    title = "Pick apps",
                    query = "",
                    selectedResults = selectedResults,
                    otherResults = otherResults,
                    selectedComponents = emptySet(),
                    canAddMore = true,
                    canRemove = true,
                    sortOption = AppSortOption.ALPHABETICAL,
                    sortDirection = SortDirection.ASCENDING,
                    selectedSectionLabel = "Selected",
                    screenTestTag = "app_picker_test_screen",
                    tagPrefix = "app_picker_test",
                    onQueryChange = {},
                    onSortOptionChange = {},
                    onSortDirectionToggle = {},
                    onToggleApp = {},
                    onDone = {},
                )
            }
        }
    }

    @Test
    fun theSameAppInThePersonalAndWorkProfileShowsTwoRowsInAllApps() {
        // Given the same package and activity installed for two users, as on a device with a Work profile
        setContent(selectedResults = emptyList(), otherResults = listOf(personalCalendar, workCalendar))

        // Then both rows render, instead of the list crashing on a duplicate item key
        composeRule.onAllNodesWithText("Calendar").assertCountEquals(2)
    }

    @Test
    fun theSameAppInThePersonalAndWorkProfileShowsTwoRowsInSelected() {
        // Given both copies are already selected
        setContent(selectedResults = listOf(personalCalendar, workCalendar), otherResults = emptyList())

        // Then both rows render
        composeRule.onAllNodesWithText("Calendar").assertCountEquals(2)
    }

    @Test
    fun onlyTheWorkCopyOfADuplicatedAppCarriesTheWorkBadge() {
        // Given the same app for the personal user and a Work Profile
        setContent(selectedResults = emptyList(), otherResults = listOf(personalCalendar, workCalendar))

        // Then exactly one row, the Work copy's, shows the Work badge, so the two rows can be told apart
        // (a clickable row merges its children, so the badge is only visible in the unmerged tree)
        composeRule.onAllNodesWithTag("work_profile_badge", useUnmergedTree = true).assertCountEquals(1)
    }

    @Test
    fun anAppInOnlyThePersonalProfileHasNoWorkBadge() {
        setContent(selectedResults = emptyList(), otherResults = listOf(personalCalendar))

        composeRule.onAllNodesWithTag("work_profile_badge", useUnmergedTree = true).assertCountEquals(0)
    }

    @Test
    fun searchFieldAcceptsTypingAfterChangingTheSortOrder() {
        // Given a picker with live query and sort state, and the search field focused
        composeRule.setContent {
            var query by remember { mutableStateOf("") }
            var sortOption by remember { mutableStateOf(AppSortOption.ALPHABETICAL) }
            FacetLauncherTheme {
                AppPickerScreen(
                    title = "Pick apps",
                    query = query,
                    selectedResults = emptyList(),
                    otherResults = listOf(personalCalendar),
                    selectedComponents = emptySet(),
                    canAddMore = true,
                    canRemove = true,
                    sortOption = sortOption,
                    sortDirection = SortDirection.ASCENDING,
                    selectedSectionLabel = "Selected",
                    screenTestTag = "app_picker_test_screen",
                    tagPrefix = "app_picker_test",
                    onQueryChange = { query = it },
                    onSortOptionChange = { sortOption = it },
                    onSortDirectionToggle = {},
                    onToggleApp = {},
                    onDone = {},
                )
            }
        }
        composeRule.onNodeWithTag("app_picker_test_search").performClick()

        // When choosing another sort order from the dropdown
        val other = AppSortOption.entries.last().name
        composeRule.onNodeWithTag("app_picker_test_sort_menu_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("app_picker_test_sort_option_$other").performClick()
        composeRule.waitForIdle()

        // Then the field no longer holds stale focus, and tapping it again accepts typing
        composeRule.onNodeWithTag("app_picker_test_search").assertIsNotFocused()
        composeRule.onNodeWithTag("app_picker_test_search").performClick()
        composeRule.onNodeWithTag("app_picker_test_search").performTextInput("cal")
        composeRule.onNodeWithTag("app_picker_test_search").assertTextContains("cal")
    }
}
