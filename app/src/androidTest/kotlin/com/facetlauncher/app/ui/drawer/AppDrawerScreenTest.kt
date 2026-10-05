package com.facetlauncher.app.ui.drawer

import android.content.Intent
import android.net.Uri
import android.os.Parcel
import android.os.UserHandle
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.center
import androidx.compose.ui.test.down
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.moveTo
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.test.up
import com.facetlauncher.app.data.WorkProfileInfo
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppProfile
import com.facetlauncher.app.data.model.ConnectionDetail
import com.facetlauncher.app.data.model.ContactConnection
import com.facetlauncher.app.data.model.ContactConnectionType
import com.facetlauncher.app.data.model.ContactInfo
import com.facetlauncher.app.data.model.DrawerFolderDisplayMode
import com.facetlauncher.app.data.model.DrawerGridSize
import com.facetlauncher.app.data.model.DrawerListItemSize
import com.facetlauncher.app.data.model.DrawerPresentation
import com.facetlauncher.app.data.model.Folder
import com.facetlauncher.app.data.model.RecentlyInstalledPosition
import com.facetlauncher.app.data.model.SearchBarPosition
import com.facetlauncher.app.data.model.SettingsSearchEntry
import com.facetlauncher.app.ui.launcher.LocalHomePressedEvent
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * A real, distinct [UserHandle] for [id] — not `mock(UserHandle::class.java)`. Mockito bypasses
 * the real constructor, so a mocked `UserHandle`'s private `mHandle` field is left at Java's
 * default `0`, which is also the *real* primary user's own id — meaning `Process.myUserHandle()
 * .equals(mock(UserHandle::class.java))` spuriously returns `true` on a real device/emulator
 * (confirmed: this caused `AppDrawerScreen`'s Work-tab filter, which correctly compares real
 * `UserHandle`s via `==`, to match the personal app too). `UserHandle`'s public `Parcel`
 * constructor is the real, working way to get a genuinely distinct one for instrumented tests.
 */
private fun fakeUserHandle(id: Int): UserHandle {
    val parcel = Parcel.obtain()
    parcel.writeInt(id)
    parcel.setDataPosition(0)
    val handle = UserHandle(parcel)
    parcel.recycle()
    return handle
}

class AppDrawerScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    /** One app per letter A..Z, alphabetically ordered — as AppRepository would hand it over. */
    private val apps = ('A'..'Z').map { letter ->
        AppInfo(packageName = "com.example.$letter", activityName = ".Main", label = "$letter App", icon = null)
    }

    @Test
    fun groupsAppsUnderTheirLeadingLetterHeader() {
        // Given the drawer rendered with one app per letter
        composeRule.setContent {
            FacetLauncherTheme {
                AppDrawerScreen(apps = apps, onAppClick = {})
            }
        }

        // Then the first few letter headers and their apps are both present
        composeRule.onNodeWithTag("header_A").assertExists()
        composeRule.onNodeWithText("A App").assertExists()
        composeRule.onNodeWithTag("header_B").assertExists()
        composeRule.onNodeWithText("B App").assertExists()
    }

    @Test
    fun draggingTheAlphabetRailToTheBottomScrollsTheLastLetterIntoView() {
        // Given a drawer with 26 letter groups (more content than fits on screen)
        composeRule.setContent {
            FacetLauncherTheme {
                AppDrawerScreen(apps = apps, onAppClick = {})
            }
        }

        // When the alphabet rail is dragged from its top to its bottom
        composeRule.onNodeWithTag("alphabet_rail").performTouchInput { swipeDown() }

        // Then the last letter's header ("Z") is scrolled into view
        composeRule.onNodeWithTag("header_Z").assertExists()
        composeRule.onNodeWithText("Z App").assertExists()
    }

    @Test
    fun onlyRendersLettersPresentInTheAppList() {
        // Given a drawer with apps under only three letters
        val sparseApps = listOf('A', 'M', 'Z').map { letter ->
            AppInfo(packageName = "com.example.$letter", activityName = ".Main", label = "$letter App", icon = null)
        }
        composeRule.setContent {
            FacetLauncherTheme {
                AppDrawerScreen(apps = sparseApps, onAppClick = {})
            }
        }

        // Then the rail shows only those letters, not a fixed A-Z alphabet — "B" has no app
        // and shouldn't appear anywhere (as a rail letter or a header)
        composeRule.onNodeWithText("B").assertDoesNotExist()
    }

    @Test
    fun touchingAboveTheVisibleRailBandJumpsToTheFirstLetter() {
        // Given a sparse drawer, scrolled away from the top
        val sparseApps = listOf('A', 'M', 'Z').map { letter ->
            AppInfo(packageName = "com.example.$letter", activityName = ".Main", label = "$letter App", icon = null)
        }
        composeRule.setContent {
            FacetLauncherTheme {
                AppDrawerScreen(apps = sparseApps, onAppClick = {})
            }
        }
        composeRule.onNodeWithTag("drawer_list").performTouchInput { swipeUp() }

        // When tapping at the very top of the rail's activation zone — above the visible
        // 70% band, where no letter is actually drawn
        composeRule.onNodeWithTag("alphabet_rail").performTouchInput {
            down(topCenter)
            up()
        }

        // Then it still activates — clamped to the first letter, not ignored
        composeRule.onNodeWithTag("header_A").assertExists()
    }

    @Test
    fun theLargeLetterIndicatorAppearsWhileDraggingAndDisappearsOnRelease() {
        // Given the drawer rendered
        composeRule.setContent {
            FacetLauncherTheme {
                AppDrawerScreen(apps = apps, onAppClick = {})
            }
        }

        // When a drag on the rail starts but hasn't been released yet
        composeRule.onNodeWithTag("alphabet_rail").performTouchInput {
            down(topCenter)
            moveTo(center)
        }

        // Then the large letter indicator is showing
        composeRule.onNodeWithTag("alphabet_rail_indicator").assertExists()

        // When the drag is released
        composeRule.onNodeWithTag("alphabet_rail").performTouchInput { up() }

        // Then the indicator disappears
        composeRule.onNodeWithTag("alphabet_rail_indicator").assertDoesNotExist()
    }

    @Test
    fun leftEdgeDragProducesTheSameScrollAsRightEdgeDrag() {
        // Given the drawer — left-edge letter jump is always on, not a setting
        composeRule.setContent {
            FacetLauncherTheme {
                AppDrawerScreen(apps = apps, onAppClick = {})
            }
        }

        // When the left-edge zone is dragged from top to bottom — now requires a drag past
        // slop, which swipeDown() correctly provides
        composeRule.onNodeWithTag("left_edge_letter_jump_zone").performTouchInput { swipeDown() }

        // Then the last letter's header is scrolled into view, same as the right rail
        composeRule.onNodeWithTag("header_Z").assertExists()
        composeRule.onNodeWithText("Z App").assertExists()
    }

    @Test
    fun leftEdgeTouchWithoutDragDoesNotScroll() {
        // Given a drawer, scrolled away from the top
        composeRule.setContent {
            FacetLauncherTheme {
                AppDrawerScreen(apps = apps, onAppClick = {})
            }
        }
        composeRule.onNodeWithTag("drawer_list").performTouchInput { swipeUp() }
        composeRule.onNodeWithTag("header_A").assertIsNotDisplayed()

        // When tapping (touch down + up without movement) the left-edge zone
        composeRule.onNodeWithTag("left_edge_letter_jump_zone").performTouchInput {
            down(center)
            up()
        }

        // Then it does not scroll back to the top (letter A) — requires a drag
        composeRule.onNodeWithTag("header_A").assertIsNotDisplayed()
    }

    @Test
    fun rightRailTouchWithoutDragStillScrolls() {
        // Given a drawer, scrolled away from the top
        composeRule.setContent {
            FacetLauncherTheme {
                AppDrawerScreen(apps = apps, onAppClick = {})
            }
        }
        composeRule.onNodeWithTag("drawer_list").performTouchInput { swipeUp() }
        composeRule.onNodeWithTag("header_A").assertIsNotDisplayed()

        // When tapping the right rail
        composeRule.onNodeWithTag("alphabet_rail").performTouchInput {
            down(topCenter)
            up()
        }

        // Then it still activates immediately on touch, jumping back to the top
        composeRule.onNodeWithTag("header_A").assertIsDisplayed()
    }

    @Test
    fun typingInTheSearchBarFiltersTheAppList() {
        // Given the drawer rendered with the search bar (default position: top)
        var query by mutableStateOf("")
        composeRule.setContent {
            FacetLauncherTheme {
                AppDrawerScreen(apps = apps, onAppClick = {}, query = query, onQueryChange = { query = it })
            }
        }

        // When typing a query matching only "M App" — search mode has no letter headers (a
        // flat results list per app), so matches are checked via each app row's own testTag
        // rather than its label text, since the search field's own input text also contains
        // "M App" and would otherwise ambiguously match the same text query
        composeRule.onNodeWithTag("drawer_search_field").performTextInput("M App")

        // Then browse mode's list/rail are gone, replaced by the flat search-results view —
        // only the matching app's row is shown, others are filtered out
        composeRule.onNodeWithTag("drawer_list").assertDoesNotExist()
        composeRule.onNodeWithTag("drawer_search_results").assertExists()
        composeRule.onNodeWithTag("drawer_app_row_com.example.M").assertExists()
        composeRule.onNodeWithTag("drawer_app_row_com.example.A").assertDoesNotExist()
    }

    @Test
    fun clearingTheSearchQueryRestoresTheFullList() {
        // Given a drawer filtered down to one app by typing into the search field (going
        // through the real onQueryChange callback, not an external state write, keeps this
        // in sync with Compose's own event handling/snapshot timing)
        var query by mutableStateOf("")
        composeRule.setContent {
            FacetLauncherTheme {
                AppDrawerScreen(apps = apps, onAppClick = {}, query = query, onQueryChange = { query = it })
            }
        }
        composeRule.onNodeWithTag("drawer_search_field").performTextInput("M App")
        composeRule.onNodeWithTag("drawer_app_row_com.example.A").assertDoesNotExist()

        // When the query is cleared the same way
        composeRule.onNodeWithTag("drawer_search_field").performTextReplacement("")

        // Then browse mode (letter-grouped list) is back
        composeRule.onNodeWithTag("drawer_search_results").assertDoesNotExist()
        composeRule.onNodeWithTag("header_A").assertExists()
    }

    @Test
    fun searchResultsAreCappedAtFiveAppsAndHaveNoLetterHeaders() {
        // Given more than 5 apps that would all match a broad query
        val manyMatches = (1..8).map { AppInfo(packageName = "com.example.match$it", activityName = ".Main", label = "Match App $it", icon = null) }
        var query by mutableStateOf("")
        composeRule.setContent {
            FacetLauncherTheme {
                AppDrawerScreen(apps = manyMatches, onAppClick = {}, query = query, onQueryChange = { query = it })
            }
        }

        // When searching a query all 8 apps match
        composeRule.onNodeWithTag("drawer_search_field").performTextInput("Match")

        // Then only the first 5 (by the incoming list's own order) render, no letter headers at all
        for (i in 1..5) {
            composeRule.onNodeWithTag("drawer_app_row_com.example.match$i").assertExists()
        }
        for (i in 6..8) {
            composeRule.onNodeWithTag("drawer_app_row_com.example.match$i").assertDoesNotExist()
        }
        composeRule.onNodeWithTag("header_A").assertDoesNotExist()
    }

    @Test
    fun emptySearchShowsTheNothingMatchesStateWithAWorkingClearButton() {
        // Given a query that matches nothing
        var query by mutableStateOf("")
        composeRule.setContent {
            FacetLauncherTheme {
                AppDrawerScreen(apps = apps, onAppClick = {}, query = query, onQueryChange = { query = it })
            }
        }
        composeRule.onNodeWithTag("drawer_search_field").performTextInput("zqx")

        // Then the empty state renders with the query echoed back
        composeRule.onNodeWithText("No matches found for “zqx”").assertExists()

        // When tapping Clear search
        composeRule.onNodeWithTag("drawer_search_clear").performClick()

        // Then the query is cleared and browse mode is back
        composeRule.onNodeWithTag("drawer_search_results").assertDoesNotExist()
        composeRule.onNodeWithTag("header_A").assertExists()
    }

    @Test
    fun overflowMenuNavigatesToSettings() {
        // Given the drawer rendered
        var settingsClicked = false
        composeRule.setContent {
            FacetLauncherTheme {
                AppDrawerScreen(apps = apps, onAppClick = {}, onNavigateToSettings = { settingsClicked = true })
            }
        }

        // When opening the overflow menu and tapping "Launcher settings"
        composeRule.onNodeWithTag("drawer_search_overflow").performClick()
        composeRule.onNodeWithTag("drawer_search_overflow_settings").performClick()

        // Then it navigates to settings
        assertEquals(true, settingsClicked)
    }

    @Test
    fun overflowMenuHasNoSecureFolderRowWhenItIsNotInstalled() {
        // Given Secure Folder isn't installed (no launch intent available)
        composeRule.setContent {
            FacetLauncherTheme {
                AppDrawerScreen(apps = apps, onAppClick = {}, secureFolderIntent = null)
            }
        }

        // When opening the overflow menu
        composeRule.onNodeWithTag("drawer_search_overflow").performClick()

        // Then there's no row to open it
        composeRule.onNodeWithTag("drawer_search_overflow_secure_folder").assertDoesNotExist()
    }

    @Test
    fun overflowMenuOpensSecureFolderWhenInstalled() {
        // Given Secure Folder is installed
        var openedIntent: Intent? = null
        val secureFolderIntent = Intent(Intent.ACTION_MAIN)
        composeRule.setContent {
            FacetLauncherTheme {
                AppDrawerScreen(
                    apps = apps,
                    onAppClick = {},
                    secureFolderIntent = secureFolderIntent,
                    onOpenSecureFolder = { openedIntent = it },
                )
            }
        }

        // When opening the overflow menu and tapping "Open Secure Folder"
        composeRule.onNodeWithTag("drawer_search_overflow").performClick()
        composeRule.onNodeWithTag("drawer_search_overflow_secure_folder").performClick()

        // Then it launches Secure Folder's own intent
        assertEquals(secureFolderIntent, openedIntent)
    }

    @Test
    fun overflowMenuHasNoPrivateSpaceRowWhenItIsNotConfigured() {
        // Given Private Space isn't configured on this device
        composeRule.setContent {
            FacetLauncherTheme {
                AppDrawerScreen(apps = apps, onAppClick = {}, showPrivateSpaceRow = false)
            }
        }

        // When opening the overflow menu
        composeRule.onNodeWithTag("drawer_search_overflow").performClick()

        // Then there's no row for it
        composeRule.onNodeWithTag("drawer_search_overflow_private_space").assertDoesNotExist()
    }

    @Test
    fun overflowMenuTapsThroughToPrivateSpaceWhenShown() {
        // Given Private Space is configured (locked or unlocked — the row itself doesn't branch on which)
        var clicked = false
        composeRule.setContent {
            FacetLauncherTheme {
                AppDrawerScreen(
                    apps = apps,
                    onAppClick = {},
                    showPrivateSpaceRow = true,
                    onPrivateSpaceRowClick = { clicked = true },
                )
            }
        }

        // When opening the overflow menu and tapping "Private Space"
        composeRule.onNodeWithTag("drawer_search_overflow").performClick()
        composeRule.onNodeWithTag("drawer_search_overflow_private_space").performClick()

        // Then the caller's own click handler fires — it's the one that decides whether that
        // means opening the screen or triggering the OS unlock flow, based on the actual state
        assertEquals(true, clicked)
    }

    @Test
    fun topPositionedSearchBarSitsInTheUpperHalfOfTheScreen() {
        // Given the drawer rendered with the search bar at the top
        composeRule.setContent {
            FacetLauncherTheme {
                AppDrawerScreen(apps = apps, onAppClick = {}, searchBarPosition = SearchBarPosition.TOP)
            }
        }

        // Then it sits in the upper half of the screen
        val barTop = composeRule.onNodeWithTag("drawer_search_bar").fetchSemanticsNode().boundsInRoot.top
        val screenHeight = composeRule.onRoot().fetchSemanticsNode().boundsInRoot.height
        assert(barTop < screenHeight / 2) { "expected top-positioned bar (top=$barTop) in the upper half (< ${screenHeight / 2})" }
    }

    @Test
    fun bottomPositionedSearchBarSitsInTheLowerHalfOfTheScreen() {
        // Given the drawer rendered with the search bar at the bottom
        composeRule.setContent {
            FacetLauncherTheme {
                AppDrawerScreen(apps = apps, onAppClick = {}, searchBarPosition = SearchBarPosition.BOTTOM)
            }
        }

        // Then it sits in the lower half of the screen
        val barTop = composeRule.onNodeWithTag("drawer_search_bar").fetchSemanticsNode().boundsInRoot.top
        val screenHeight = composeRule.onRoot().fetchSemanticsNode().boundsInRoot.height
        assert(barTop > screenHeight / 2) { "expected bottom-positioned bar (top=$barTop) in the lower half (> ${screenHeight / 2})" }
    }

    @Test
    fun gridPresentationRendersTilesInsteadOfRowsForTheSameAppSet() {
        // Given the drawer in Grid presentation
        composeRule.setContent {
            FacetLauncherTheme {
                AppDrawerScreen(apps = apps, onAppClick = {}, presentation = DrawerPresentation.GRID)
            }
        }

        // Then it renders the grid container (not the list) with a tile per app — no letter
        // headers at all, Grid is a continuous grid, not visually grouped
        composeRule.onNodeWithTag("drawer_grid").assertExists()
        composeRule.onNodeWithTag("drawer_list").assertDoesNotExist()
        composeRule.onNodeWithTag("header_A").assertDoesNotExist()
        composeRule.onNodeWithTag("drawer_grid_tile_com.example.A").assertExists()
    }

    @Test
    fun draggingTheAlphabetRailInGridPresentationScrollsToTheLetterSFirstApp() {
        // Given the drawer in Grid presentation with 26 letters, no headers to scroll to
        composeRule.setContent {
            FacetLauncherTheme {
                AppDrawerScreen(apps = apps, onAppClick = {}, presentation = DrawerPresentation.GRID)
            }
        }

        // When the alphabet rail is dragged from its top to its bottom — same rail, same
        // gesture handling as List presentation, just scrolling a LazyGridState this time
        composeRule.onNodeWithTag("alphabet_rail").performTouchInput { swipeDown() }

        // Then the last letter's tile (its only app, since one app per letter here) is scrolled
        // into view — no header exists to assert on in Grid mode
        composeRule.onNodeWithTag("drawer_grid_tile_com.example.Z").assertExists()
    }

    @Test
    fun gridSizeRowsSettingControlsTileHeight() {
        // Given the drawer in Grid presentation with a small row count (tiles get more of the
        // viewport's height each) — a single setContent with reactive state driving [gridSize],
        // not two separate setContent calls on the same Activity (no longer permitted by the
        // current compose-ui-test/activity libraries — see chat history).
        var gridSize by mutableStateOf(DrawerGridSize.FOUR_BY_FOUR)
        composeRule.setContent {
            FacetLauncherTheme {
                AppDrawerScreen(apps = apps, onAppClick = {}, presentation = DrawerPresentation.GRID, gridSize = gridSize)
            }
        }
        val fourRowsHeight = composeRule.onNodeWithTag("drawer_grid_tile_com.example.A").fetchSemanticsNode().boundsInRoot.height

        // When the same drawer instead uses a larger row count (tiles get less height each,
        // since more rows must fit in the same viewport)
        gridSize = DrawerGridSize.FIVE_BY_SIX
        composeRule.waitForIdle()
        val sixRowsHeight = composeRule.onNodeWithTag("drawer_grid_tile_com.example.A").fetchSemanticsNode().boundsInRoot.height

        // Then tile height tracks the selected row count, not just column count — this is the
        // regression this test guards: before the fix, DrawerGridSize.rows was never read, so
        // tile height was purely content-intrinsic and identical regardless of row count.
        assert(fourRowsHeight > sixRowsHeight) {
            "expected fewer rows (4) to produce taller tiles than more rows (6): $fourRowsHeight vs $sixRowsHeight"
        }
    }

    @Test
    fun listItemSizeSettingControlsRowHeight() {
        // Given the drawer in List presentation at the Compact item size (today's unchanged
        // default height) — a single setContent with reactive state driving [itemSize], not two
        // separate setContent calls on the same Activity (see the identical note above).
        var itemSize by mutableStateOf(DrawerListItemSize.COMPACT)
        composeRule.setContent {
            FacetLauncherTheme {
                AppDrawerScreen(apps = apps, onAppClick = {}, listItemSize = itemSize)
            }
        }
        val compactHeight = composeRule.onNodeWithTag("drawer_app_row_com.example.A").fetchSemanticsNode().boundsInRoot.height

        // When the same drawer instead uses the Spacious item size
        itemSize = DrawerListItemSize.SPACIOUS
        composeRule.waitForIdle()
        val spaciousHeight = composeRule.onNodeWithTag("drawer_app_row_com.example.A").fetchSemanticsNode().boundsInRoot.height

        // Then Spacious rows are taller than Compact rows
        assert(spaciousHeight > compactHeight) {
            "expected Spacious rows ($spaciousHeight) to be taller than Compact rows ($compactHeight)"
        }
    }

    @Test
    fun contactsSectionIsOmittedEntirelyWhenThereAreNoMatches() {
        // Given a search with app matches but an empty contacts list (READ_CONTACTS denied, F6
        // off, or genuinely no contact matches — all three look identical from here, per the
        // `4p` mockup's own "denied → app results only" annotation)
        var query by mutableStateOf("")
        composeRule.setContent {
            FacetLauncherTheme {
                AppDrawerScreen(apps = apps, onAppClick = {}, query = query, onQueryChange = { query = it }, contacts = emptyList())
            }
        }

        // When searching
        composeRule.onNodeWithTag("drawer_search_field").performTextInput("M App")

        // Then no contacts section renders at all, only the apps section
        composeRule.onNodeWithText("CONTACTS").assertDoesNotExist()
        composeRule.onNodeWithTag("drawer_app_row_com.example.M").assertExists()
    }

    @Test
    fun contactsPermissionBannerShowsWhileSearchingAndDismissesOnItsOwnClick() {
        // Given the caller reports READ_CONTACTS isn't granted (mirrors DrawerViewModel.showContactsPermissionPrompt)
        var query by mutableStateOf("")
        var showPrompt by mutableStateOf(false)
        var clicked = false
        composeRule.setContent {
            FacetLauncherTheme {
                AppDrawerScreen(
                    apps = apps,
                    onAppClick = {},
                    query = query,
                    onQueryChange = { query = it },
                    contacts = emptyList(),
                    showContactsPermissionPrompt = showPrompt,
                    onContactsPermissionPromptClick = { clicked = true; showPrompt = false },
                )
            }
        }

        // Then it stays hidden while there's no query yet
        composeRule.onNodeWithTag("drawer_contacts_access_strip").assertDoesNotExist()

        // When searching while the prompt condition holds
        composeRule.onNodeWithTag("drawer_search_field").performTextInput("M App")
        showPrompt = true
        composeRule.waitForIdle()

        // Then the banner renders under a CONTACTS header alongside the app results
        composeRule.onNodeWithText("CONTACTS").assertExists()
        composeRule.onNodeWithTag("drawer_contacts_access_strip").assertExists()
        composeRule.onNodeWithTag("drawer_app_row_com.example.M").assertExists()

        // When tapping the banner
        composeRule.onNodeWithTag("drawer_contacts_access_strip").performClick()

        // Then its own click callback fires and (per the caller's own state, mirroring the dismiss-on-click contract) it disappears immediately
        assertEquals(true, clicked)
        composeRule.onNodeWithTag("drawer_contacts_access_strip").assertDoesNotExist()
    }

    @Test
    fun contactsSettingOffBannerShowsWhileSearchingAndDismissesOnItsOwnClick() {
        // Given the caller reports READ_CONTACTS is granted but the "Search contacts" setting is
        // off (mirrors DrawerViewModel.showContactsSettingPrompt)
        var query by mutableStateOf("")
        var showPrompt by mutableStateOf(false)
        var clicked = false
        composeRule.setContent {
            FacetLauncherTheme {
                AppDrawerScreen(
                    apps = apps,
                    onAppClick = {},
                    query = query,
                    onQueryChange = { query = it },
                    contacts = emptyList(),
                    showContactsSettingPrompt = showPrompt,
                    onContactsSettingPromptClick = { clicked = true; showPrompt = false },
                )
            }
        }

        // Then it stays hidden while there's no query yet
        composeRule.onNodeWithTag("drawer_contacts_setting_off_strip").assertDoesNotExist()

        // When searching while the prompt condition holds
        composeRule.onNodeWithTag("drawer_search_field").performTextInput("M App")
        showPrompt = true
        composeRule.waitForIdle()

        // Then the banner renders under a CONTACTS header alongside the app results
        composeRule.onNodeWithText("CONTACTS").assertExists()
        composeRule.onNodeWithTag("drawer_contacts_setting_off_strip").assertExists()
        composeRule.onNodeWithTag("drawer_app_row_com.example.M").assertExists()

        // When tapping the banner
        composeRule.onNodeWithTag("drawer_contacts_setting_off_strip").performClick()

        // Then its own click callback fires and (per the caller's own state, mirroring the dismiss-on-click contract) it disappears immediately
        assertEquals(true, clicked)
        composeRule.onNodeWithTag("drawer_contacts_setting_off_strip").assertDoesNotExist()
    }

    @Test
    fun tappingAContactRowOpensTheConnectionsSheetForThem() {
        // Given a search that also has a contact match, with a fake connections provider
        // standing in for ContactRepository.getConnections (Phase 9 — see chat history)
        var query by mutableStateOf("")
        val contact = ContactInfo(id = "1", displayName = "Jane Doe", phoneNumber = "555-1234")
        val callConnection = ContactConnection(
            ContactConnectionType.CALL,
            "Call",
            null,
            ConnectionDetail.Single("555-1234", "Mobile", Intent(Intent.ACTION_DIAL, Uri.parse("tel:555-1234"))),
        )
        composeRule.setContent {
            FacetLauncherTheme {
                AppDrawerScreen(
                    apps = apps,
                    onAppClick = {},
                    query = query,
                    onQueryChange = { query = it },
                    contacts = listOf(contact),
                    onRequestConnections = { listOf(callConnection) },
                )
            }
        }
        composeRule.onNodeWithTag("drawer_search_field").performTextInput("Jane")

        // Then the contact renders in the results, sheet not open yet
        composeRule.onNodeWithText("CONTACTS").assertExists()
        composeRule.onNodeWithText("Jane Doe").assertExists()
        composeRule.onNodeWithTag("contact_connections_sheet").assertDoesNotExist()

        // When tapping the contact row
        composeRule.onNodeWithTag("contact_row_1").performClick()

        // Then the connections sheet opens, showing their name and fetched Call connection
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("contact_connections_sheet").assertIsDisplayed() }.isSuccess
        }
        composeRule.onNodeWithTag("contact_connection_call_call").assertExists()

        // When tapping the scrim to dismiss
        composeRule.onNodeWithTag("contact_connections_scrim").performClick()

        // Then the sheet closes
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("contact_connections_sheet").assertDoesNotExist() }.isSuccess
        }
    }

    @Test
    fun homePressedEventClosesTheContactConnectionsSheet() {
        // Given the connections sheet is open for a contact
        var query by mutableStateOf("")
        val homePressedEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
        val contact = ContactInfo(id = "1", displayName = "Jane Doe", phoneNumber = "555-1234")
        val callConnection = ContactConnection(
            ContactConnectionType.CALL,
            "Call",
            null,
            ConnectionDetail.Single("555-1234", "Mobile", Intent(Intent.ACTION_DIAL, Uri.parse("tel:555-1234"))),
        )
        composeRule.setContent {
            FacetLauncherTheme {
                CompositionLocalProvider(LocalHomePressedEvent provides homePressedEvent) {
                    AppDrawerScreen(
                        apps = apps,
                        onAppClick = {},
                        query = query,
                        onQueryChange = { query = it },
                        contacts = listOf(contact),
                        onRequestConnections = { listOf(callConnection) },
                    )
                }
            }
        }
        composeRule.onNodeWithTag("drawer_search_field").performTextInput("Jane")
        composeRule.onNodeWithTag("contact_row_1").performClick()
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("contact_connections_sheet").assertIsDisplayed() }.isSuccess
        }

        // When Home is pressed
        homePressedEvent.tryEmit(Unit)

        // Then the sheet closes
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("contact_connections_sheet").assertDoesNotExist() }.isSuccess
        }
    }

    @Test
    fun settingsSectionIsOmittedEntirelyWhenThereAreNoMatches() {
        // Given a search with app matches but no settings entries (F "Search settings" off, or
        // genuinely no matches — both look identical from here)
        var query by mutableStateOf("")
        composeRule.setContent {
            FacetLauncherTheme {
                AppDrawerScreen(apps = apps, onAppClick = {}, query = query, onQueryChange = { query = it }, settingsEntries = emptyList())
            }
        }

        // When searching
        composeRule.onNodeWithTag("drawer_search_field").performTextInput("M App")

        // Then no settings section renders at all, only the apps section
        composeRule.onNodeWithText("SETTINGS").assertDoesNotExist()
        composeRule.onNodeWithTag("drawer_app_row_com.example.M").assertExists()
    }

    @Test
    fun tappingASettingsResultRowFiresItsOwnClickCallback() {
        // Given a search that also has a settings match
        var query by mutableStateOf("")
        val entry = SettingsSearchEntry(id = "wifi", label = "Wi-Fi", action = "android.settings.WIFI_SETTINGS")
        var clickedEntry: SettingsSearchEntry? = null
        composeRule.setContent {
            FacetLauncherTheme {
                AppDrawerScreen(
                    apps = apps,
                    onAppClick = {},
                    query = query,
                    onQueryChange = { query = it },
                    settingsEntries = listOf(entry),
                    onSettingsEntryClick = { clickedEntry = it },
                )
            }
        }
        composeRule.onNodeWithTag("drawer_search_field").performTextInput("wifi")

        // Then it renders under a SETTINGS header alongside the app results
        composeRule.onNodeWithText("SETTINGS").assertExists()
        composeRule.onNodeWithText("Wi-Fi").assertExists()

        // When tapping the settings row
        composeRule.onNodeWithTag("settings_result_row_wifi").performClick()

        // Then its own click callback fires with that entry
        assertEquals(entry, clickedEntry)
    }

    @Test
    fun searchResultsAreHiddenAndBrowseModeReturnsOnBackPress() {
        // Given a drawer mid-search
        var query by mutableStateOf("")
        composeRule.setContent {
            FacetLauncherTheme {
                AppDrawerScreen(apps = apps, onAppClick = {}, query = query, onQueryChange = { query = it })
            }
        }
        composeRule.onNodeWithTag("drawer_search_field").performTextInput("M App")
        composeRule.onNodeWithTag("drawer_search_results").assertExists()

        // When pressing back — the search field still holds focus with the IME visible, so the
        // first system back press is consumed by the keyboard dismissal (standard OS behavior,
        // handled below the Activity's back-callback stack, before our BackHandler ever sees it)
        // rather than by our own BackHandler. Close the keyboard first so this exercises our
        // handler specifically, matching what a real user sees after that first back press.
        androidx.test.espresso.Espresso.closeSoftKeyboard()
        composeRule.waitForIdle()
        androidx.test.espresso.Espresso.pressBack()
        composeRule.waitForIdle()

        // Then the query is cleared and browse mode is back — the drawer itself isn't closed
        // (that's HomeDrawerRoute's own BackHandler, one level up, not exercised here). The
        // search-results container exits via a real animation (per CLAUDE.md's testing policy,
        // never disabled), so wait it out rather than asserting the instant after the back press.
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("drawer_search_results").assertDoesNotExist() }.isSuccess
        }
        composeRule.onNodeWithTag("header_A").assertExists()
    }

    @Test
    fun longPressingAListRowOpensTheContextMenu() {
        // Given List presentation (the default)
        composeRule.setContent {
            FacetLauncherTheme {
                AppDrawerScreen(apps = apps, onAppClick = {})
            }
        }
        composeRule.onNodeWithTag("app_context_menu").assertDoesNotExist()

        // When a row is long-pressed (F12)
        composeRule.onNodeWithTag("drawer_app_row_com.example.A").performTouchInput { longClick() }

        // Then the context menu opens
        composeRule.onNodeWithTag("app_context_menu").assertExists()
    }

    @Test
    fun theMenuOpensAtTheLongPressThresholdWithoutWaitingForRelease() {
        // Given List presentation (the default)
        composeRule.setContent {
            FacetLauncherTheme {
                AppDrawerScreen(apps = apps, onAppClick = {})
            }
        }
        composeRule.onNodeWithTag("app_context_menu").assertDoesNotExist()

        // When a row is held past the long-press threshold without lifting the finger — pause
        // the test clock and drive it forward explicitly so the long-press coroutine timeout
        // fires deterministically, matching this codebase's `createComposeRule()` virtual-time host.
        composeRule.mainClock.autoAdvance = false
        composeRule.onNodeWithTag("drawer_app_row_com.example.A").performTouchInput { down(center) }
        composeRule.mainClock.advanceTimeBy(600)
        composeRule.mainClock.autoAdvance = true
        composeRule.waitForIdle()

        // Then the menu has already opened, without the finger having lifted
        composeRule.onNodeWithTag("app_context_menu").assertExists()

        composeRule.onNodeWithTag("drawer_app_row_com.example.A").performTouchInput { up() }
    }

    @Test
    fun longPressingAGridTileOpensTheContextMenu() {
        // Given Grid presentation
        composeRule.setContent {
            FacetLauncherTheme {
                AppDrawerScreen(apps = apps, onAppClick = {}, presentation = DrawerPresentation.GRID)
            }
        }
        composeRule.onNodeWithTag("app_context_menu").assertDoesNotExist()

        // When a tile is long-pressed (F12)
        composeRule.onNodeWithTag("drawer_grid_tile_com.example.A").performTouchInput { longClick() }

        // Then the context menu opens
        composeRule.onNodeWithTag("app_context_menu").assertExists()
    }

    @Test
    fun profileTabRowIsHiddenWithoutAWorkProfile() {
        // Given the default, no-Work-Profile case (hasWorkProfile defaults to false)
        composeRule.setContent {
            FacetLauncherTheme {
                AppDrawerScreen(apps = apps, onAppClick = {})
            }
        }

        // Then no Personal/Work switcher renders — a device without a Work Profile sees no change
        composeRule.onNodeWithTag("drawer_profile_tab_personal").assertDoesNotExist()
        composeRule.onNodeWithTag("drawer_profile_tab_work").assertDoesNotExist()
    }

    @Test
    fun switchingToTheWorkTabShowsOnlyWorkProfileApps() {
        // Given a mix of personal and Work Profile apps, with a Work Profile present — the two
        // copies differ by real UserHandle (not just display profile), since that's the actual
        // identity the Work tab filters on (AppDrawerScreen's tabScopedApps).
        val workHandle = fakeUserHandle(10)
        val mixedApps = listOf(
            AppInfo(packageName = "com.example.a", activityName = ".Main", label = "A App", icon = null, profile = AppProfile.PERSONAL),
            AppInfo(packageName = "com.example.a", activityName = ".Main", label = "A App", icon = null, profile = AppProfile.WORK, userHandle = workHandle),
        )
        val workProfiles = listOf(WorkProfileInfo(handle = workHandle, isPaused = false, label = "Work"))
        composeRule.setContent {
            FacetLauncherTheme {
                AppDrawerScreen(apps = mixedApps, onAppClick = {}, workProfiles = workProfiles)
            }
        }

        // Then the Personal tab is selected by default, showing just one "A App"
        composeRule.onNodeWithTag("drawer_profile_tab_personal").assertExists()
        composeRule.onAllNodesWithText("A App").assertCountEquals(1)

        // When switching to the Work tab
        composeRule.onNodeWithTag("drawer_profile_tab_work_${workHandle.hashCode()}").performClick()

        // Then it still shows one "A App" — the Work Profile copy, not the personal one collapsed together
        composeRule.onAllNodesWithText("A App").assertCountEquals(1)
    }

    @Test
    fun searchingShowsResultsFromBothProfilesRegardlessOfTheSelectedTab() {
        // Given the same package installed in both profiles, and the Personal tab selected — the
        // two copies differ by real UserHandle, not just display profile.
        val workHandle = fakeUserHandle(10)
        val mixedApps = listOf(
            AppInfo(packageName = "com.example.a", activityName = ".Main", label = "Chat", icon = null, profile = AppProfile.PERSONAL),
            AppInfo(packageName = "com.example.a", activityName = ".Main", label = "Chat", icon = null, profile = AppProfile.WORK, userHandle = workHandle),
        )
        val workProfiles = listOf(WorkProfileInfo(handle = workHandle, isPaused = false, label = "Work"))
        var query by mutableStateOf("")
        composeRule.setContent {
            FacetLauncherTheme {
                AppDrawerScreen(apps = mixedApps, onAppClick = {}, workProfiles = workProfiles, query = query, onQueryChange = { query = it })
            }
        }

        // When searching for it
        query = "Chat"

        // Then both the personal and Work Profile copies show up as separate result rows — search
        // isn't scoped to the tab. 3, not 2: the search field's own typed value also matches "Chat".
        composeRule.onAllNodesWithText("Chat").assertCountEquals(3)
    }

    @Test
    fun folderDisplayModeDoNotShowKeepsFoldersOutOfTheDrawerEntirely() {
        // Given a real folder exists but the setting defaults to DO_NOT_SHOW
        val folder = Folder(id = 1, name = "Zeta Folder", apps = emptyList())
        composeRule.setContent {
            FacetLauncherTheme {
                AppDrawerScreen(apps = apps, onAppClick = {}, folderCandidates = listOf(folder))
            }
        }

        // Then it never renders as a drawer entry
        composeRule.onNodeWithText("Zeta Folder").assertDoesNotExist()
        composeRule.onNodeWithTag("header_folders").assertDoesNotExist()
    }

    @Test
    fun folderDisplayModeShowFirstPinsAFoldersSectionAheadOfTheLetteredList() {
        // Given SHOW_FIRST with one folder
        val folder = Folder(id = 1, name = "Zeta Folder", apps = emptyList())
        composeRule.setContent {
            FacetLauncherTheme {
                AppDrawerScreen(
                    apps = apps,
                    onAppClick = {},
                    folderCandidates = listOf(folder),
                    folderDisplayMode = DrawerFolderDisplayMode.SHOW_FIRST,
                )
            }
        }

        // Then a dedicated "Folders" header and the folder row both exist, ahead of "A"'s header
        composeRule.onNodeWithTag("header_folders").assertExists()
        composeRule.onNodeWithText("Zeta Folder").assertExists()
        composeRule.onNodeWithTag("header_A").assertExists()
    }

    @Test
    fun folderDisplayModeShowLastPinsAFoldersSectionAfterTheLetteredList() {
        // Given SHOW_LAST with one folder
        val folder = Folder(id = 1, name = "Zeta Folder", apps = emptyList())
        composeRule.setContent {
            FacetLauncherTheme {
                AppDrawerScreen(
                    apps = apps,
                    onAppClick = {},
                    folderCandidates = listOf(folder),
                    folderDisplayMode = DrawerFolderDisplayMode.SHOW_LAST,
                )
            }
        }

        // Then the folder still renders, in its own pinned section after all 26 letter groups —
        // scrolled to first, since a LazyColumn only composes what's near the viewport and this
        // section sits well past the initially-visible "A"/"B" rows.
        composeRule.onNodeWithTag("drawer_list").performScrollToNode(hasTestTag("header_folders"))
        composeRule.onNodeWithTag("header_folders").assertExists()
        composeRule.onNodeWithText("Zeta Folder").assertExists()
    }

    @Test
    fun folderDisplayModeInlineSortsTheFolderIntoItsOwnLetterAmongTheApps() {
        // Given INLINE mode with a folder named to sort under "A", alongside the "A App"
        val folder = Folder(id = 1, name = "Ace Folder", apps = emptyList())
        composeRule.setContent {
            FacetLauncherTheme {
                AppDrawerScreen(
                    apps = apps,
                    onAppClick = {},
                    folderCandidates = listOf(folder),
                    folderDisplayMode = DrawerFolderDisplayMode.INLINE,
                )
            }
        }

        // Then the folder renders under the shared "A" header — no separate "Folders" section
        composeRule.onNodeWithTag("header_folders").assertDoesNotExist()
        composeRule.onNodeWithTag("header_A").assertExists()
        composeRule.onNodeWithText("Ace Folder").assertExists()
        composeRule.onNodeWithText("A App").assertExists()
    }

    /** All 26 [apps] default to `firstInstallTime = 0L` — none ever qualify as recently installed on their own, so every test below seeds its own recently-installed app explicitly. */
    private fun recentApp(letter: Char = 'Z', userHandle: UserHandle? = null) = AppInfo(
        packageName = "com.example.recent$letter",
        activityName = ".Main",
        label = "Recent $letter App",
        icon = null,
        firstInstallTime = System.currentTimeMillis(),
        userHandle = userHandle ?: android.os.Process.myUserHandle(),
    )

    @Test
    fun recentlyInstalledRowShowsWhenAnAppQualifiesAndTheSettingIsOn() {
        // Given the default setting (on) and one recently-installed app
        composeRule.setContent {
            FacetLauncherTheme {
                AppDrawerScreen(apps = apps + recentApp(), onAppClick = {})
            }
        }

        composeRule.onNodeWithTag("drawer_recently_installed_row").assertExists()
        // ...with its own icon-only section header, same as a letter or "Folders" header
        composeRule.onNodeWithTag("header_recently_installed").assertExists()
    }

    @Test
    fun recentlyInstalledRowIsHiddenWhenTheSettingIsOff() {
        // Given a qualifying app, but the setting turned off
        composeRule.setContent {
            FacetLauncherTheme {
                AppDrawerScreen(apps = apps + recentApp(), onAppClick = {}, recentlyInstalledPosition = RecentlyInstalledPosition.DO_NOT_SHOW)
            }
        }

        composeRule.onNodeWithTag("drawer_recently_installed_row").assertDoesNotExist()
        composeRule.onNodeWithTag("header_recently_installed").assertDoesNotExist()
    }

    @Test
    fun recentlyInstalledRowSitsAtTheBottomWhenPositionIsShowLast() {
        // Given a qualifying app and the setting set to show it last
        composeRule.setContent {
            FacetLauncherTheme {
                AppDrawerScreen(apps = apps + recentApp(), onAppClick = {}, recentlyInstalledPosition = RecentlyInstalledPosition.SHOW_LAST)
            }
        }

        // Then it still renders, past all 26 letter groups — scrolled to first, since a
        // LazyColumn only composes what's near the viewport and this row sits well past the
        // initially-visible "A"/"B" rows (mirrors folderDisplayModeShowLastPinsAFoldersSection...).
        composeRule.onNodeWithTag("drawer_list").performScrollToNode(hasTestTag("drawer_recently_installed_row"))
        composeRule.onNodeWithTag("drawer_recently_installed_row").assertExists()
        composeRule.onNodeWithTag("header_recently_installed").assertExists()
    }

    @Test
    fun recentlyInstalledRowIsHiddenWhenNoAppsQualify() {
        // Given the setting on, but no app installed recently (all firstInstallTime = 0L)
        composeRule.setContent {
            FacetLauncherTheme {
                AppDrawerScreen(apps = apps, onAppClick = {})
            }
        }

        composeRule.onNodeWithTag("drawer_recently_installed_row").assertDoesNotExist()
    }

    @Test
    fun recentlyInstalledRowSitsAboveAShowFirstFolderSection() {
        // Given a qualifying app and a SHOW_FIRST folder
        val folder = Folder(id = 1, name = "Zeta Folder", apps = emptyList())
        composeRule.setContent {
            FacetLauncherTheme {
                AppDrawerScreen(
                    apps = apps + recentApp(),
                    onAppClick = {},
                    folderCandidates = listOf(folder),
                    folderDisplayMode = DrawerFolderDisplayMode.SHOW_FIRST,
                )
            }
        }

        // Then both exist, with the recently-installed row above the folder in root-relative position
        val rowTop = composeRule.onNodeWithTag("drawer_recently_installed_row").fetchSemanticsNode().boundsInRoot.top
        val folderTop = composeRule.onNodeWithTag("drawer_folder_row_1").fetchSemanticsNode().boundsInRoot.top
        assert(rowTop < folderTop)
    }

    @Test
    fun tappingRecentlyInstalledRowOpensTheSheetWithTheExpectedApp() {
        // Given one recently-installed app
        composeRule.setContent {
            FacetLauncherTheme {
                AppDrawerScreen(apps = apps + recentApp('Z'), onAppClick = {})
            }
        }

        // When tapping the row
        composeRule.onNodeWithTag("drawer_recently_installed_row").performClick()

        // Then the sheet opens showing that app
        composeRule.onNodeWithTag("folder_contents_sheet").assertExists()
        composeRule.onNodeWithText("Recent Z App").assertExists()
        // ...with no "Rename" action, since this isn't a real folder
        composeRule.onNodeWithTag("folder_contents_sheet_rename").assertDoesNotExist()
        // ...and a recency badge on the contained app's icon
        composeRule.onNodeWithTag("recency_badge_com.example.recentZ", useUnmergedTree = true).assertExists()
    }

    @Test
    fun recentlyInstalledRowIsHiddenInGridPresentation() {
        // Given Grid presentation and one recently-installed app — the feature is List-only
        composeRule.setContent {
            FacetLauncherTheme {
                AppDrawerScreen(apps = apps + recentApp(), onAppClick = {}, presentation = DrawerPresentation.GRID)
            }
        }

        composeRule.onNodeWithTag("drawer_recently_installed_row").assertDoesNotExist()
    }

    @Test
    fun recentlyInstalledAppsAreScopedToTheActiveTabUnlikePinnedFolders() {
        // Given a Personal recently-installed app and a Work recently-installed app, Personal
        // selected (the default tab)
        val workHandle = fakeUserHandle(7)
        val personalRecent = recentApp('P')
        val workRecent = recentApp('W', userHandle = workHandle).copy(profile = AppProfile.WORK)
        val workProfiles = listOf(WorkProfileInfo(handle = workHandle, isPaused = false, label = "Work"))
        composeRule.setContent {
            FacetLauncherTheme {
                AppDrawerScreen(apps = apps + personalRecent + workRecent, onAppClick = {}, workProfiles = workProfiles)
            }
        }

        // Then the sheet shows only the Personal app — the Work Profile's own recently-installed
        // app is excluded, unlike a pinned folder (deliberately NOT tab-filtered — see chat history)
        composeRule.onNodeWithTag("drawer_recently_installed_row").performClick()
        composeRule.onNodeWithText("Recent P App").assertExists()
        composeRule.onNodeWithText("Recent W App").assertDoesNotExist()
    }
}
