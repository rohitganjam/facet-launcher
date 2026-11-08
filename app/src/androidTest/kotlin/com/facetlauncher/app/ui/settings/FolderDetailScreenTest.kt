package com.facetlauncher.app.ui.settings

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.Folder
import com.facetlauncher.app.data.model.PlacementList
import com.facetlauncher.app.domain.FolderPlacementState
import com.facetlauncher.app.domain.QuickAddState
import com.facetlauncher.app.domain.QuickPlacementAction
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
        placement: FolderPlacementState = FolderPlacementState(),
        onApplyPlacement: (PlacementList, QuickPlacementAction) -> Unit = { _, _ -> },
        onDelete: () -> Unit = {},
    ) {
        composeRule.setContent {
            FacetLauncherTheme {
                FolderDetailContent(
                    uiState = FolderDetailUiState(folder = folder, placement = placement),
                    onBack = onBack,
                    onAddToFolder = onAddToFolder,
                    onRename = onRename,
                    onReorder = onReorder,
                    onRemoveApp = onRemoveApp,
                    onApplyPlacement = onApplyPlacement,
                    onDelete = onDelete,
                )
            }
        }
    }

    @Test
    fun headerShowsTheFolderNameAndAMenuButton() {
        setContent()

        composeRule.onNodeWithText("Games").assertExists()
        composeRule.onNodeWithTag("folder_detail_menu").assertExists()
        composeRule.onNodeWithTag("folder_detail_rename").assertDoesNotExist()
    }

    @Test
    fun theMenuListsRenameAndDeleteAndOnlyThePlacementsThatApply() {
        // Given a Dock that is full, so only Favorites can be offered
        setContent(placement = FolderPlacementState(quickAdd = QuickAddState(favoritesAction = QuickPlacementAction.Add("Home"), dockAction = null)))

        // When the menu opens
        composeRule.onNodeWithTag("folder_detail_menu").performClick()

        // Then Rename, Add to Favorites and Delete show, and Dock does not
        composeRule.onNodeWithTag("folder_detail_rename").assertExists()
        composeRule.onNodeWithTag("folder_detail_favorites").assertExists()
        composeRule.onNodeWithTag("folder_detail_dock").assertDoesNotExist()
        composeRule.onNodeWithTag("folder_detail_delete").assertExists()
    }

    @Test
    fun theFacetBadgeNamesTheFacetOnlyWhenThereIsMoreThanOne() {
        // Given several facets
        setContent(placement = FolderPlacementState(QuickAddState(dockAction = QuickPlacementAction.Add("Work")), facetName = "Work", showFacetBadge = true))
        composeRule.onNodeWithTag("folder_detail_menu").performClick()
        composeRule.onNodeWithText("Work").assertExists()
    }

    @Test
    fun withASingleFacetTheMenuShowsNoBadge() {
        // Given one facet
        setContent(placement = FolderPlacementState(QuickAddState(dockAction = QuickPlacementAction.Add("Work")), facetName = "Work", showFacetBadge = false))
        composeRule.onNodeWithTag("folder_detail_menu").performClick()
        composeRule.onNodeWithText("Work").assertDoesNotExist()
    }

    @Test
    fun choosingADockActionReportsTheListAndTheAction() {
        var applied: Pair<PlacementList, QuickPlacementAction>? = null
        val remove = QuickPlacementAction.Remove("Home")
        setContent(
            placement = FolderPlacementState(QuickAddState(dockAction = remove), facetName = "Home"),
            onApplyPlacement = { list, action -> applied = list to action },
        )

        composeRule.onNodeWithTag("folder_detail_menu").performClick()
        composeRule.onNodeWithTag("folder_detail_dock").performClick()

        assert(applied == PlacementList.DOCK to remove)
    }

    @Test
    fun deleteAsksForConfirmationBeforeDeleting() {
        var deleted = false
        setContent(onDelete = { deleted = true })

        composeRule.onNodeWithTag("folder_detail_menu").performClick()
        composeRule.onNodeWithTag("folder_detail_delete").performClick()
        assert(!deleted)

        composeRule.onNodeWithTag("confirm_dialog_confirm").performClick()
        assert(deleted)
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

        composeRule.onNodeWithTag("folder_detail_menu").performClick()
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
