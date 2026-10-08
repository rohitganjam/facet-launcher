package com.facetlauncher.app.ui.pro

import android.app.Activity
import com.facetlauncher.app.data.BillingRepository
import com.facetlauncher.app.data.FakeEntitlementRepository
import com.facetlauncher.app.data.PurchaseLaunch
import com.facetlauncher.app.data.model.ProQueryResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

@OptIn(ExperimentalCoroutinesApi::class)
class FacetProViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)

    @After fun tearDown() = Dispatchers.resetMain()

    private val updates = MutableSharedFlow<ProQueryResult>(extraBufferCapacity = 4)
    private val activity: Activity = mock(Activity::class.java)

    private class Entitlement(initial: Boolean, private val restoreResult: ProQueryResult) : FakeEntitlementRepository(initial) {
        override suspend fun refresh(): ProQueryResult = restoreResult
    }

    private fun billing(price: String? = "$9.99", launch: PurchaseLaunch = PurchaseLaunch.Started) = mock(BillingRepository::class.java).also {
        runBlocking {
            `when`(it.priceText()).thenReturn(price)
            `when`(it.launchPurchase(activity)).thenReturn(launch)
        }
        `when`(it.updates).thenReturn(updates)
    }

    private fun TestScope.viewModel(entitlement: FakeEntitlementRepository = FakeEntitlementRepository(false), billing: BillingRepository = billing()): FacetProViewModel =
        FacetProViewModel(billing, entitlement).also { vm ->
            backgroundScope.launch { vm.uiState.collect {} }
            dispatcher.scheduler.advanceUntilIdle()
        }

    @Test
    fun `opening the screen loads the price from play`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.onScreenShown()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals("$9.99", vm.uiState.value.price)
    }

    @Test
    fun `no price from play leaves the price empty rather than wrong`() = runTest(dispatcher) {
        val vm = viewModel(billing = billing(price = null))

        vm.onScreenShown()
        dispatcher.scheduler.advanceUntilIdle()

        assertNull(vm.uiState.value.price)
    }

    @Test
    fun `the state follows the entitlement`() = runTest(dispatcher) {
        val entitlement = FakeEntitlementRepository(false)
        val vm = viewModel(entitlement)
        assertEquals(false, vm.uiState.value.isPro)

        entitlement.proFlow.value = true
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(true, vm.uiState.value.isPro)
    }

    @Test
    fun `restore reports a purchase that was found`() = runTest(dispatcher) {
        val vm = viewModel(Entitlement(false, ProQueryResult.Owned))

        vm.restore()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(FacetProMessage.RESTORED, vm.uiState.value.message)
        assertEquals(false, vm.uiState.value.busy)
    }

    @Test
    fun `restore says when this google account has no purchase`() = runTest(dispatcher) {
        val vm = viewModel(Entitlement(false, ProQueryResult.NotOwned))

        vm.restore()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(FacetProMessage.NO_PURCHASE_FOUND, vm.uiState.value.message)
    }

    @Test
    fun `restore says when the purchase is pending`() = runTest(dispatcher) {
        val vm = viewModel(Entitlement(false, ProQueryResult.Pending))

        vm.restore()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(FacetProMessage.PENDING, vm.uiState.value.message)
    }

    @Test
    fun `restore says when play cannot answer`() = runTest(dispatcher) {
        val vm = viewModel(Entitlement(false, ProQueryResult.Unavailable))

        vm.restore()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(FacetProMessage.PLAY_UNAVAILABLE, vm.uiState.value.message)
    }

    @Test
    fun `a purchase that play could not start says play is unavailable`() = runTest(dispatcher) {
        val vm = viewModel(billing = billing(launch = PurchaseLaunch.Unavailable))

        vm.buy(activity)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(FacetProMessage.PLAY_UNAVAILABLE, vm.uiState.value.message)
        assertEquals(false, vm.uiState.value.busy)
    }

    @Test
    fun `a started purchase shows no message because it finishes on plays own screen`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.buy(activity)
        dispatcher.scheduler.advanceUntilIdle()

        assertNull(vm.uiState.value.message)
        assertEquals(false, vm.uiState.value.busy)
    }

    @Test
    fun `a purchase waiting on payment shows as pending`() = runTest(dispatcher) {
        val vm = viewModel()

        updates.emit(ProQueryResult.Pending)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(FacetProMessage.PENDING, vm.uiState.value.message)
    }

    @Test
    fun `opening the screen again clears the last message`() = runTest(dispatcher) {
        val vm = viewModel(Entitlement(false, ProQueryResult.NotOwned))
        vm.restore()
        dispatcher.scheduler.advanceUntilIdle()

        vm.onScreenShown()
        dispatcher.scheduler.advanceUntilIdle()

        assertNull(vm.uiState.value.message)
    }
}
