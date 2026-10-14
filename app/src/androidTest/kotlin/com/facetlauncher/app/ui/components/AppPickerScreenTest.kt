package com.facetlauncher.app.ui.components

import android.os.Parcel
import android.os.UserHandle
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
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
}
