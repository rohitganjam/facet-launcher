package com.facetlauncher.app.ui.home

import com.facetlauncher.app.data.ClockStackRepository
import com.facetlauncher.app.data.EntitlementRepository
import com.facetlauncher.app.data.FakeEntitlementRepository
import com.facetlauncher.app.domain.ReplaceClockStackItemUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

class ClockWidgetFacetControllerTest {

    private val clockStackRepository = mock(ClockStackRepository::class.java)
    private val replaceClockStackItem = mock(ReplaceClockStackItemUseCase::class.java)

    private fun controller(entitlement: EntitlementRepository = EntitlementRepository()) =
        ClockWidgetFacetController(clockStackRepository, replaceClockStackItem, entitlement)

    @Test
    fun `replaceWithClock delegates to the use case with the page id`() = runTest {
        // When the visible page is switched to the clock
        controller().replaceWithClock(itemId = 5L)

        // Then the use case does it
        verify(replaceClockStackItem).withClock(5L)
    }

    @Test
    fun `commitSize saves the portrait size against the stack item`() = runTest {
        // When a resize is committed in portrait
        controller().commitSize(itemId = 5L, widthDp = 180, heightDp = 90)

        // Then it is saved on that item and landscape's own size is left alone
        verify(clockStackRepository).setSize(5L, 180, 90)
        verify(clockStackRepository, never()).setLandscapeSize(5L, 180, 90)
    }

    @Test
    fun `commitSize in landscape saves landscape's own size`() = runTest {
        // When a resize is committed in landscape
        controller().commitSize(itemId = 5L, widthDp = 300, heightDp = 200, mode = HomeLayoutMode.LANDSCAPE)

        // Then it goes to the item's landscape size, not the portrait one
        verify(clockStackRepository).setLandscapeSize(5L, 300, 200)
        verify(clockStackRepository, never()).setSize(5L, 300, 200)
    }

    @Test
    fun `isPro follows the entitlement`() {
        val entitlement = FakeEntitlementRepository(initial = false)
        val controller = controller(entitlement)

        assertFalse(controller.isPro.value)
        entitlement.proFlow.value = true
        assertTrue(controller.isPro.value)
    }
}
