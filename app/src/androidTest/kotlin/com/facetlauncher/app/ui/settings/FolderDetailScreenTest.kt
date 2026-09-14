package com.facetlauncher.app.ui.settings

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.Folder
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Rule
import org.junit.Test

class FolderDetailScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val apps = (1..3).map { AppInfo(packageName = "com.example.app$it", activityName = ".Main", label = "App $it", icon = null) }
    private val folder = Folder(id = 1L, name = "Games", apps = apps)

    private fun setContent(
        folder: Folder? = this.folder,
        onBack: () -> Unit = {},
        onAddToFolder: () -> Unit = {},
        onRename: (String) -> Unit = {},
        onReorder: (List<AppInfo>) -> Unit = {},
        onRemoveApp: (AppInfo) -> Unit = {},
    ) {
        composeRule.setContent {
            FacetLauncherTheme {
                FolderDetailContent(
                    uiState = FolderDetailUiState(folder = folder),
                    onBack = onBack,
                    onAddToFolder = onAddToFolder,
                    onRename = onRename,
                    onReorder = onReorder,
                    onRemoveApp = onRemoveApp,
                )
            }
        }
    }

    @Test
    fun headerShowsTheFolderNameAndARenameLabel() {
        setContent()

        composeRule.onNodeWithText("Games").assertExists()
        composeRule.onNodeWithTag("folder_detail_rename").assertExists()
        composeRule.onNodeWithText("Rename").assertExists()
    }

    @Test
    fun addToFolderRendersAsALabeledButtonAboveTheList() {
        setContent()

        composeRule.onNodeWithTag("folder_detail_add_to_folder").assertExists()
        composeRule.onNodeWithText("Add to folder").assertExists()
    }

    @Test
    fun tappingAddToFolderInvokesTheCallback() {
        var tapped = false
        setContent(onAddToFolder = { tapped = true })

        composeRule.onNodeWithTag("folder_detail_add_to_folder").performClick()

        assert(tapped)
    }

    @Test
    fun everyAppRendersAsADraggableRow() {
        setContent()

        apps.forEach { app ->
            composeRule.onNodeWithTag("folder_detail_reorder_row_${app.packageName}").assertExists()
            composeRule.onNodeWithText(app.label).assertExists()
        }
    }

    @Test
    fun emptyFolderShowsAnEmptyStateMessage() {
        setContent(folder = Folder(id = 1L, name = "Empty", apps = emptyList()))

        composeRule.onNodeWithText("No apps in this folder yet. Tap \"Add to folder\" to add some.").assertExists()
    }

    @Test
    fun tappingRenameOpensDialogAndSavingCallsOnRename() {
        var renamedTo: String? = null
        setContent(onRename = { renamedTo = it })

        composeRule.onNodeWithTag("folder_detail_rename").performClick()
        composeRule.onNodeWithTag("rename_dialog_field").performTextReplacement("Fun")
        composeRule.onNodeWithTag("rename_dialog_save").performClick()

        assert(renamedTo == "Fun")
    }

    @Test
    fun everyAppRowHasARemoveButton() {
        setContent()

        apps.forEach { app ->
            composeRule.onNodeWithTag("folder_detail_remove_${app.packageName}").assertExists()
        }
    }

    @Test
    fun tappingARemoveButtonInvokesTheCallbackWithThatApp() {
        var removed: AppInfo? = null
        setContent(onRemoveApp = { removed = it })

        composeRule.onNodeWithTag("folder_detail_remove_${apps[1].packageName}").performClick()

        assert(removed == apps[1])
    }

}
