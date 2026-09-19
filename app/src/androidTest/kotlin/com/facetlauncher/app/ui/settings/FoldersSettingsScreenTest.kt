package com.facetlauncher.app.ui.settings

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.Folder
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Rule
import org.junit.Test

class FoldersSettingsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val app = AppInfo(packageName = "com.example.a", activityName = ".Main", label = "App A", icon = null)
    private val folder = Folder(id = 1L, name = "Games", apps = listOf(app))

    private fun setContent(
        folders: List<Folder> = listOf(folder),
        onOpenFolder: (Long) -> Unit = {},
        onCreate: (String) -> Unit = {},
        onDelete: (Long) -> Unit = {},
    ) {
        composeRule.setContent {
            FacetLauncherTheme {
                FoldersSettingsContent(
                    uiState = FoldersSettingsUiState(folders = folders),
                    onBack = {},
                    onOpenFolder = onOpenFolder,
                    onCreate = onCreate,
                    onDelete = onDelete,
                )
            }
        }
    }

    @Test
    fun emptyStateShowsNoFoldersYetMessage() {
        setContent(folders = emptyList())

        composeRule.onNodeWithText("No folders yet. Long-press an app and choose \"Add to folder\" to create one.").assertExists()
    }

    @Test
    fun folderRowRendersNameAndAppCount() {
        setContent()

        composeRule.onNodeWithText("Games").assertExists()
        // Singular "1 app", not "1 apps" — pluralStringResource, see chat history.
        composeRule.onNodeWithText("1 app").assertExists()
    }

    @Test
    fun tappingAFolderRowInvokesOnOpenFolderWithItsId() {
        var openedId: Long? = null
        setContent(onOpenFolder = { openedId = it })

        composeRule.onNodeWithTag("folder_row_1").performClick()

        assert(openedId == 1L)
    }

    @Test
    fun tappingDeleteOnARowShowsConfirmationWithoutOpeningTheFolder() {
        var openedId: Long? = null
        setContent(onOpenFolder = { openedId = it })

        composeRule.onNodeWithTag("folder_row_1_delete").performClick()

        // The destructive delete is gated by ConfirmDialog — tapping the row's own delete icon
        // shows the confirmation, it doesn't call onOpenFolder or onDelete directly.
        composeRule.onNodeWithTag("confirm_dialog").assertExists()
        assert(openedId == null)
    }

    @Test
    fun confirmingDeleteInvokesOnDelete() {
        var deletedId: Long? = null
        setContent(onDelete = { deletedId = it })

        composeRule.onNodeWithTag("folder_row_1_delete").performClick()
        composeRule.onNodeWithText("Delete").performClick()

        assert(deletedId == 1L)
    }

    @Test
    fun headerShowsAPlusFolderLabelAndOpensRenameDialogAndSavingCallsOnCreate() {
        var createdName: String? = null
        setContent(onCreate = { createdName = it })

        composeRule.onNodeWithText("Folder").assertExists()
        composeRule.onNodeWithTag("folders_settings_create_button").performClick()
        composeRule.onNodeWithTag("rename_dialog_field").performTextInput("Social")
        composeRule.onNodeWithTag("rename_dialog_save").performClick()

        assert(createdName == "Social")
    }
}
