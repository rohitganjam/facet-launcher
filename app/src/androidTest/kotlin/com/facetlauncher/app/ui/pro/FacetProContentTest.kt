package com.facetlauncher.app.ui.pro

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.facetlauncher.app.data.model.ProReason
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class FacetProContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    private var bought = 0
    private var restored = 0
    private var wentBack = 0
    private var addedFacet = 0

    private fun show(
        state: FacetProUiState = FacetProUiState(),
        reason: ProReason = ProReason.FACET_LIMIT,
        canBuy: Boolean = true,
    ) {
        composeRule.setContent {
            FacetLauncherTheme {
                FacetProContent(
                    reason = reason,
                    state = state,
                    onBack = { wentBack++ },
                    onBuy = if (canBuy) ({ bought++ }) else null,
                    onRestore = if (canBuy) ({ restored++ }) else null,
                    onAddFacet = { addedFacet++ },
                )
            }
        }
    }

    // ---- Before purchase ----

    @Test
    fun theUpgradeScreenShowsTheHeadlineTheFeaturesAndWhyTheUserIsHere() {
        show(reason = ProReason.FACET_LIMIT)

        composeRule.onNodeWithTag("facet_pro_screen").assertExists()
        composeRule.onNodeWithText("More facets, more automation, more customization").assertExists()
        composeRule.onNodeWithTag("facet_pro_reason").assertExists()
        composeRule.onNodeWithText("Free includes 3. Pro goes up to 10", substring = true).assertExists()
        composeRule.onNodeWithText("Wi-Fi, Bluetooth, headphones and battery triggers").assertExists()
        composeRule.onNodeWithText("Future Pro features included").assertExists()
    }

    @Test
    fun theHeroShowsTheExampleFacetsAndTheTriggerPills() {
        show()

        composeRule.onNodeWithText("Office").assertExists()
        composeRule.onNodeWithText("Drive").assertExists()
    }

    @Test
    fun openingFromSettingsHasNoReasonLine() {
        show(reason = ProReason.ABOUT)

        composeRule.onNodeWithTag("facet_pro_reason").assertDoesNotExist()
    }

    @Test
    fun theUnlockButtonCarriesLifetimeAndThePriceWhenPlayKnowsIt() {
        show(state = FacetProUiState(price = "$9.99"))

        composeRule.onNodeWithText("Unlock Lifetime Pro · $9.99").assertExists()
    }

    @Test
    fun withoutAPriceTheUnlockButtonStandsAlone() {
        show(state = FacetProUiState(price = null))

        composeRule.onNodeWithText("Unlock Lifetime Pro").assertExists()
    }

    @Test
    fun theFeatureListNamesCustomWidgets() {
        show(state = FacetProUiState())

        composeRule.onNodeWithText("Custom Widgets on home screen").assertExists()
    }

    @Test
    fun unlockAndRestoreAndBackReportBack() {
        show(state = FacetProUiState(price = "$9.99"))

        composeRule.onNodeWithTag("facet_pro_buy").performClick()
        composeRule.onNodeWithTag("facet_pro_restore_link").performClick()
        composeRule.onNodeWithTag("back_button").performClick()

        assertEquals(1, bought)
        assertEquals(1, restored)
        assertEquals(1, wentBack)
    }

    @Test
    fun restorePurchasesIsAlsoOfferedUnderTheUnlockButton() {
        show(state = FacetProUiState(price = "$9.99"))

        composeRule.onNodeWithText("Restore purchases").assertExists()
        composeRule.onNodeWithTag("facet_pro_restore_link").performClick()

        assertEquals(1, restored)
    }

    @Test
    fun whileARequestIsInFlightUnlockAndRestoreAreDisabled() {
        show(state = FacetProUiState(busy = true))

        composeRule.onNodeWithTag("facet_pro_buy").assertIsNotEnabled()
        composeRule.onNodeWithTag("facet_pro_restore_link").assertIsNotEnabled()
    }

    @Test
    fun aMessageFromPlayIsShown() {
        show(state = FacetProUiState(message = FacetProMessage.NO_PURCHASE_FOUND))

        composeRule.onNodeWithTag("facet_pro_message").assertExists()
        composeRule.onNodeWithText("No Pro purchase found", substring = true).assertExists()
    }

    @Test
    fun withoutAPurchaseServiceThereIsNoUnlockOrRestore() {
        show(canBuy = false)

        composeRule.onNodeWithTag("facet_pro_buy").assertDoesNotExist()
        composeRule.onNodeWithTag("facet_pro_restore_link").assertDoesNotExist()
        composeRule.onNodeWithTag("facet_pro_screen").assertIsEnabled()
    }

    // ---- After purchase ----

    @Test
    fun aProUserSeesTheThankYouAndWhatIsUnlockedAndNoPriceOrUnlock() {
        show(state = FacetProUiState(isPro = true))

        composeRule.onNodeWithTag("facet_pro_owned").assertExists()
        composeRule.onNodeWithText("Unlocked on every phone signed in to this Google account.").assertExists()
        composeRule.onNodeWithText("Wi-Fi triggers").performScrollTo().assertExists()
        composeRule.onNodeWithText("Headphones and battery triggers").performScrollTo().assertExists()
        composeRule.onNodeWithTag("facet_pro_buy").assertDoesNotExist()
        composeRule.onNodeWithTag("facet_pro_reason").assertDoesNotExist()
    }

    @Test
    fun theUnlockedScreenHasAddAFacetAndRestoreAtTheEndOfTheContent() {
        show(state = FacetProUiState(isPro = true))

        composeRule.onNodeWithTag("facet_pro_set_up_trigger").assertDoesNotExist()
        composeRule.onNodeWithTag("facet_pro_add_facet").performScrollTo().performClick()
        composeRule.onNodeWithTag("facet_pro_restore_link").performScrollTo().performClick()
        composeRule.onNodeWithText("Done").performClick()

        assertEquals(1, addedFacet)
        assertEquals(1, restored)
        assertEquals(1, wentBack)
    }

    @Test
    fun theUnlockedScreenShowsTheResultOfARestoreAndDisablesItWhileBusy() {
        show(state = FacetProUiState(isPro = true, busy = true, message = FacetProMessage.RESTORED))

        composeRule.onNodeWithTag("facet_pro_message").assertExists()
        composeRule.onNodeWithTag("facet_pro_restore_link").assertIsNotEnabled()
    }
}
