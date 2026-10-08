package com.facetlauncher.app.ui.pro

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.BillingRepository
import com.facetlauncher.app.data.EntitlementRepository
import com.facetlauncher.app.data.PurchaseLaunch
import com.facetlauncher.app.data.model.ProQueryResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** What the last Buy or Restore attempt left to tell the user. */
enum class FacetProMessage { PLAY_UNAVAILABLE, NO_PURCHASE_FOUND, PENDING, RESTORED }

data class FacetProUiState(
    val isPro: Boolean = false,
    /** The price as Google Play formats it for this user; null until known or when Play can't say. */
    val price: String? = null,
    /** A Buy or Restore request is in flight. */
    val busy: Boolean = false,
    val message: FacetProMessage? = null,
)

private data class Progress(val price: String? = null, val busy: Boolean = false, val message: FacetProMessage? = null)

/**
 * Buy and Restore for the Facet Pro screen. It never decides who is Pro: that is [EntitlementRepository],
 * which this only asks to refresh.
 */
@HiltViewModel
class FacetProViewModel @Inject constructor(
    private val billingRepository: BillingRepository,
    private val entitlementRepository: EntitlementRepository,
) : ViewModel() {

    private val progress = MutableStateFlow(Progress())

    val uiState: StateFlow<FacetProUiState> = combine(entitlementRepository.isPro, progress) { isPro, progress ->
        FacetProUiState(isPro = isPro, price = progress.price, busy = progress.busy, message = progress.message)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), FacetProUiState())

    init {
        // A purchase that is waiting on payment completes later, outside the screen.
        viewModelScope.launch {
            billingRepository.updates.filter { it == ProQueryResult.Pending }.collect { progress.update { it.copy(busy = false, message = FacetProMessage.PENDING) } }
        }
    }

    /** Called each time the screen opens: forget the last message and (re)ask Play for the price. */
    fun onScreenShown() {
        progress.update { it.copy(message = null) }
        viewModelScope.launch { progress.update { it.copy(price = billingRepository.priceText()) } }
    }

    fun buy(activity: Activity) {
        progress.update { it.copy(busy = true, message = null) }
        viewModelScope.launch {
            val launch = billingRepository.launchPurchase(activity)
            // A started purchase finishes on Play's own screen; its result arrives through the entitlement.
            progress.update { it.copy(busy = false, message = FacetProMessage.PLAY_UNAVAILABLE.takeIf { launch == PurchaseLaunch.Unavailable }) }
        }
    }

    fun restore() {
        progress.update { it.copy(busy = true, message = null) }
        viewModelScope.launch {
            val message = when (entitlementRepository.refresh()) {
                ProQueryResult.Owned -> FacetProMessage.RESTORED
                ProQueryResult.NotOwned -> FacetProMessage.NO_PURCHASE_FOUND
                ProQueryResult.Pending -> FacetProMessage.PENDING
                ProQueryResult.Unavailable -> FacetProMessage.PLAY_UNAVAILABLE
            }
            progress.update { it.copy(busy = false, message = message) }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
