package com.facetlauncher.app.ui.launcher

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.view.WindowManager
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.ui.theme.FACET_TRANSITION_DURATION_MS
import com.facetlauncher.app.ui.theme.FacetTransitionEasing
import com.facetlauncher.app.ui.vault.AppVaultCallbacks
import com.facetlauncher.app.ui.vault.AppVaultScreen
import com.facetlauncher.app.ui.vault.AppVaultUiEvent
import com.facetlauncher.app.ui.vault.AppVaultViewModel
import kotlinx.coroutines.launch

/**
 * The App Vault, an in-place overlay over the app drawer. It locks the moment it is not on screen: Back, a Home press (the drawer
 * closing takes it with it), launching an app from it, or the launcher going to the background. While it shows, the window is
 * [WindowManager.LayoutParams.FLAG_SECURE], so screenshots and the Recents thumbnail show nothing.
 */
@Suppress("LongMethod")
@Composable
internal fun HomeAppVaultOverlay(
    overlays: HomeOverlayState,
    onAppClick: (AppInfo) -> Unit,
    viewModel: AppVaultViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val visible = overlays.showAppVault
    val context = LocalContext.current
    val resources = LocalResources.current
    val activity = LocalActivity.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val keyguard = remember(context) { context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager }
    // The system's screen-lock check is its own activity, which stops the launcher: that must not close the vault mid-flow.
    var screenLockCheckPending by remember { mutableStateOf(false) }
    val credentialLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        screenLockCheckPending = false
        if (result.resultCode == Activity.RESULT_OK) viewModel.onDeviceCredentialConfirmed()
    }
    val currentOnAppClick by rememberUpdatedState(onAppClick)

    LaunchedEffect(visible) { if (visible) viewModel.onOpened() else viewModel.lock() }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { if (!screenLockCheckPending) overlays.showAppVault = false }
    DisposableEffect(visible, activity) {
        if (visible) activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        onDispose { if (visible) activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE) }
    }
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is AppVaultUiEvent.AppRemoved -> scope.launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    val result = snackbarHostState.showSnackbar(
                        message = resources.getString(R.string.vault_removed, event.app.label),
                        actionLabel = resources.getString(R.string.vault_undo),
                        duration = SnackbarDuration.Short,
                    )
                    if (result == SnackbarResult.ActionPerformed) viewModel.onUndoRemove(event.app)
                }
                AppVaultUiEvent.Close -> overlays.showAppVault = false
            }
        }
    }

    val callbacks = remember(viewModel, keyguard, resources) {
        AppVaultCallbacks(
            onBack = { if (!viewModel.onBack()) overlays.showAppVault = false },
            onClose = { overlays.showAppVault = false },
            onStartSetPin = viewModel::onStartSetPin,
            onDigit = viewModel::onDigit,
            onBackspace = viewModel::onBackspace,
            onForgotPin = {
                val intent = keyguard.createConfirmDeviceCredentialIntent(
                    resources.getString(R.string.vault_confirm_credential_title),
                    resources.getString(R.string.vault_confirm_credential_message),
                )
                if (intent == null) {
                    viewModel.onRequestReset()
                } else {
                    screenLockCheckPending = true
                    credentialLauncher.launch(intent)
                }
            },
            onAppClick = { app -> currentOnAppClick(app) },
            onAppInfo = viewModel::onAppInfo,
            onRemove = viewModel::onRemoveFromVault,
            onAddApps = viewModel::onAddApps,
            onChangePin = viewModel::onChangePin,
            onTurnOff = viewModel::onRequestTurnOff,
            onPickerQueryChange = viewModel::onPickerQueryChange,
            onPickerSortOptionChange = viewModel::onPickerSortOptionChange,
            onPickerSortDirectionToggle = viewModel::onPickerSortDirectionToggle,
            onPickerToggle = viewModel::onPickerToggle,
            onPickerDone = viewModel::onPickerDone,
            onConfirmMove = viewModel::onConfirmMove,
            onConfirmTurnOff = viewModel::onConfirmTurnOff,
            onConfirmReset = viewModel::onConfirmReset,
            onDismissConfirm = viewModel::onDismissConfirm,
        )
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(FACET_TRANSITION_DURATION_MS, easing = FacetTransitionEasing)),
        exit = fadeOut(animationSpec = tween(FACET_TRANSITION_DURATION_MS, easing = FacetTransitionEasing)),
        modifier = Modifier.testTag("app_vault_overlay"),
    ) {
        AppVaultScreen(
            state = state,
            callbacks = callbacks,
            snackbarHostState = snackbarHostState,
            screenLockAvailable = keyguard.isDeviceSecure,
        )
    }
}
