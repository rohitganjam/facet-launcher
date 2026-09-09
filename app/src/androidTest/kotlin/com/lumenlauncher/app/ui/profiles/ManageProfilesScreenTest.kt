package com.lumenlauncher.app.ui.profiles

import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import com.lumenlauncher.app.data.ProfileRepository
import com.lumenlauncher.app.data.SettingsRepository
import com.lumenlauncher.app.data.local.LumenDatabase
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ManageProfilesScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setContent(
        onBack: () -> Unit = {},
        onEditProfile: (Long) -> Unit = {},
        seed: suspend (ProfileRepository, SettingsRepository) -> Unit = { _, _ -> },
    ) {
        composeRule.setContent {
            val context = LocalContext.current
            val viewModel = remember {
                val database = Room.inMemoryDatabaseBuilder(context, LumenDatabase::class.java).allowMainThreadQueries().build()
                val profileRepository = ProfileRepository(database.profileDao())
                val settingsRepository = SettingsRepository(
                    PreferenceDataStoreFactory.create(
                        produceFile = { File(context.cacheDir, "manage-profiles-test-${System.nanoTime()}.preferences_pb") },
                    ),
                )
                runBlocking { seed(profileRepository, settingsRepository) }
                ManageProfilesViewModel(profileRepository, settingsRepository)
            }
            LumenLauncherTheme {
                ManageProfilesScreen(onBack = onBack, onEditProfile = onEditProfile, viewModel = viewModel)
            }
        }
        composeRule.waitForIdle()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag("manage_profiles_screen").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun listShowsTheProfilesTheHintAndTheAddRow() {
        setContent(
            seed = { profileRepository, _ ->
                profileRepository.addProfile()
                profileRepository.addProfile()
            },
        )

        composeRule.onNodeWithText("Manage Profiles").assertExists()
        composeRule.onNodeWithText("Drag to re-order profiles").assertExists()
        composeRule.onNodeWithText("Profile 1").assertExists()
        composeRule.onNodeWithText("Profile 2").assertExists()
        composeRule.onNodeWithTag("profile_reorder_add_row").assertExists()
    }

    @Test
    fun addRowIsHiddenOnceTheMaximumIsReached() {
        setContent(
            seed = { profileRepository, _ ->
                repeat(ProfileRepository.MAX_PROFILES) { profileRepository.addProfile() }
            },
        )

        composeRule.onNodeWithTag("profile_reorder_add_row").assertDoesNotExist()
    }

    @Test
    fun deleteIsDisabledWithOnlyOneProfileRemaining() {
        var onlyId = 0L
        setContent(
            seed = { profileRepository, _ -> onlyId = profileRepository.addProfile().id },
        )

        composeRule.onNodeWithTag("profile_reorder_menu_$onlyId").performClick()
        composeRule.onNode(hasText("Delete")).assertIsNotEnabled()
    }

    @Test
    fun deletingFromTheOverflowMenuRemovesTheRow() {
        var secondId = 0L
        setContent(
            seed = { profileRepository, _ ->
                profileRepository.addProfile()
                secondId = profileRepository.addProfile().id
            },
        )
        composeRule.onNodeWithText("Profile 2").assertExists()

        composeRule.onNodeWithTag("profile_reorder_menu_$secondId").performClick()
        composeRule.onNode(hasText("Delete")).performClick()
        composeRule.onNode(hasText("Delete")).performClick() // confirm dialog

        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithText("Profile 2").assertDoesNotExist() }.isSuccess
        }
    }

    @Test
    fun profileSettingsFromTheOverflowMenuNavigatesToEditProfile() {
        var firstId = 0L
        var editedId = -1L
        setContent(
            onEditProfile = { editedId = it },
            seed = { profileRepository, _ ->
                firstId = profileRepository.addProfile().id
                profileRepository.addProfile()
            },
        )

        composeRule.onNodeWithTag("profile_reorder_menu_$firstId").performClick()
        composeRule.onNode(hasText("Profile settings")).performClick()

        assertEquals(firstId, editedId)
    }

    @Test
    fun backButtonInvokesTheCallback() {
        var backPressed = false
        setContent(
            onBack = { backPressed = true },
            seed = { profileRepository, _ -> profileRepository.addProfile() },
        )

        composeRule.onNodeWithTag("back_button").performClick()

        assertEquals(true, backPressed)
    }
}
