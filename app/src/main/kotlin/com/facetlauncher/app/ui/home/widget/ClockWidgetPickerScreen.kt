package com.facetlauncher.app.ui.home.widget

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.facetlauncher.app.R
import com.facetlauncher.app.ui.hub.picker.AddFailureReason
import com.facetlauncher.app.ui.hub.picker.HubAddWidgetEvent
import com.facetlauncher.app.ui.hub.picker.HubWidgetPickerContent
import kotlinx.coroutines.delay

/**
 * PRD F15's "Use custom widget" picker — same add-widget grid the Hub uses
 * ([HubWidgetPickerContent], reused directly rather than duplicated), scoped to [facetId]'s clock
 * slot instead of the Hub grid. Event-collection/Activity-result-launcher wiring mirrors
 * [com.facetlauncher.app.ui.hub.picker.HubWidgetPickerScreen] exactly — see its own doc for why
 * this is event-driven rather than a direct `onClick` launch.
 */
@Composable
fun ClockWidgetPickerScreen(
    facetId: Long,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ClockWidgetPickerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var failureMessage by remember { mutableStateOf<String?>(null) }

    val configureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        viewModel.onConfigureResult(result.resultCode == android.app.Activity.RESULT_OK)
    }
    val bindPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        viewModel.onBindResult(result.resultCode == android.app.Activity.RESULT_OK)
    }

    val setupCancelledMessage = stringResource(R.string.hub_widget_picker_setup_cancelled)
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is HubAddWidgetEvent.LaunchBindPermission -> bindPermissionLauncher.launch(event.intent)
                is HubAddWidgetEvent.LaunchConfigure -> configureLauncher.launch(IntentSenderRequest.Builder(event.intentSender).build())
                HubAddWidgetEvent.WidgetAdded -> onDone()
                is HubAddWidgetEvent.AddFailed -> {
                    failureMessage = when (event.reason) {
                        // HUB_FULL never fires here — this picker's uiState.remaining is always null (no capacity check).
                        AddFailureReason.HUB_FULL, AddFailureReason.SETUP_CANCELLED -> setupCancelledMessage
                    }
                }
            }
        }
    }

    LaunchedEffect(failureMessage) {
        if (failureMessage != null) {
            delay(3000)
            failureMessage = null
        }
    }

    HubWidgetPickerContent(
        uiState = uiState,
        failureMessage = failureMessage,
        onQueryChange = viewModel::onQueryChange,
        onProviderSelect = { option -> viewModel.onProviderSelect(option, facetId) },
        onBack = onDone,
        title = stringResource(R.string.clock_widget_picker_title),
        modifier = modifier,
    )
}
