package com.facetlauncher.app.ui.vault

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppSortOption
import com.facetlauncher.app.data.model.DrawerListItemSize
import com.facetlauncher.app.data.model.VAULT_PIN_LENGTH
import com.facetlauncher.app.ui.components.AppIcon
import com.facetlauncher.app.ui.components.AppIconSize
import com.facetlauncher.app.ui.components.AppPickerScreen
import com.facetlauncher.app.ui.components.BackButton
import com.facetlauncher.app.ui.components.ConfirmDialog
import com.facetlauncher.app.ui.components.PrimaryButton
import com.facetlauncher.app.ui.components.TextActionButton
import com.facetlauncher.app.ui.components.ThemedDropdownMenu
import com.facetlauncher.app.ui.components.ThemedDropdownMenuItem
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.ErrorColor
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.InkInverted
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.Surface
import com.facetlauncher.app.ui.theme.SurfaceContainer
import java.util.Locale
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf

/** The App Vault, stateless: the page for [AppVaultUiState.stage], over its own background. */
@Composable
fun AppVaultScreen(
    state: AppVaultUiState,
    callbacks: AppVaultCallbacks,
    snackbarHostState: SnackbarHostState,
    screenLockAvailable: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize().background(SurfaceContainer).testTag("app_vault_screen")) {
        when (state.stage) {
            VaultStage.LOCKED -> Unit
            VaultStage.INTRO -> VaultIntroPage(screenLockAvailable, callbacks)
            VaultStage.SET_PIN, VaultStage.CONFIRM_PIN, VaultStage.UNLOCK -> VaultPinPage(state, callbacks)
            VaultStage.LIST -> VaultListPage(state, callbacks)
            VaultStage.PICKER -> VaultPickerPage(state, callbacks)
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).windowInsetsPadding(WindowInsets.systemBars).padding(12.dp),
        ) { data ->
            Snackbar(
                snackbarData = data,
                shape = MaterialTheme.shapes.extraSmall,
                containerColor = Ink,
                contentColor = InkInverted,
                actionColor = InkInverted,
            )
        }
        VaultDialogs(state, callbacks)
    }
}

@Composable
private fun VaultDialogs(state: AppVaultUiState, callbacks: AppVaultCallbacks) {
    state.confirmPlacedCount?.let { count ->
        ConfirmDialog(
            title = stringResource(R.string.vault_move_title),
            message = pluralStringResource(R.plurals.vault_move_message, count, count),
            confirmLabel = stringResource(R.string.vault_move_confirm),
            onConfirm = callbacks.onConfirmMove,
            onDismiss = callbacks.onDismissConfirm,
            destructive = false,
        )
    }
    if (state.confirmTurnOff) {
        ConfirmDialog(
            title = stringResource(R.string.vault_turn_off_title),
            message = stringResource(R.string.vault_turn_off_message),
            confirmLabel = stringResource(R.string.vault_turn_off_confirm),
            onConfirm = callbacks.onConfirmTurnOff,
            onDismiss = callbacks.onDismissConfirm,
        )
    }
    if (state.confirmReset) {
        ConfirmDialog(
            title = stringResource(R.string.vault_reset_title),
            message = stringResource(R.string.vault_reset_message),
            confirmLabel = stringResource(R.string.vault_reset_confirm),
            onConfirm = callbacks.onConfirmReset,
            onDismiss = callbacks.onDismissConfirm,
        )
    }
}

@Composable
private fun VaultLockBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(56.dp).clip(CircleShape).background(Accent.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Accent)
    }
}

@Composable
private fun VaultTopBar(onBack: () -> Unit, modifier: Modifier = Modifier, title: String = "", trailing: @Composable () -> Unit = {}) {
    Row(
        modifier = modifier.fillMaxWidth().padding(start = 4.dp, end = 8.dp, top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BackButton(onClick = onBack)
        Text(text = title, style = MaterialTheme.typography.titleLarge, color = Ink, modifier = Modifier.weight(1f).padding(start = 4.dp))
        trailing()
    }
}

@Composable
private fun VaultIntroPage(screenLockAvailable: Boolean, callbacks: AppVaultCallbacks) {
    Column(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.systemBars).testTag("app_vault_intro")) {
        VaultTopBar(onBack = callbacks.onBack)
        Column(modifier = Modifier.weight(1f).padding(horizontal = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(24.dp))
            VaultLockBadge()
            Text(
                text = stringResource(R.string.vault_title),
                style = MaterialTheme.typography.headlineSmall,
                color = Ink,
                modifier = Modifier.padding(top = 16.dp, bottom = 12.dp),
            )
            IntroPoint(stringResource(R.string.vault_intro_hide), stringResource(R.string.vault_intro_hide_sub))
            IntroPoint(stringResource(R.string.vault_intro_limits), null)
            IntroPoint(stringResource(R.string.vault_intro_private_space), null)
            if (!screenLockAvailable) IntroPoint(stringResource(R.string.vault_intro_no_screen_lock), null, tag = "app_vault_intro_no_screen_lock")
        }
        PrimaryButton(
            text = stringResource(R.string.vault_set_pin),
            onClick = callbacks.onStartSetPin,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 24.dp).testTag("app_vault_set_pin"),
        )
    }
}

@Composable
private fun IntroPoint(text: String, subtext: String?, tag: String = "") {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 18.dp).then(if (tag.isEmpty()) Modifier else Modifier.testTag(tag))) {
        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = Ink)
        if (subtext != null) Text(text = subtext, style = MaterialTheme.typography.bodyMedium, color = Ink)
    }
}

@Composable
private fun VaultPinPage(state: AppVaultUiState, callbacks: AppVaultCallbacks) {
    val lockedOut = state.feedback == PinFeedback.LOCKED_OUT
    val failed = state.feedback == PinFeedback.WRONG || state.feedback == PinFeedback.MISMATCH || lockedOut
    val title = when (state.stage) {
        VaultStage.SET_PIN -> R.string.vault_create_pin_title
        VaultStage.CONFIRM_PIN -> R.string.vault_confirm_pin_title
        else -> R.string.vault_title
    }
    val subtitle = when {
        state.feedback == PinFeedback.MISMATCH -> stringResource(R.string.vault_pins_mismatch)
        state.feedback == PinFeedback.WRONG -> pluralStringResource(R.plurals.vault_wrong_pin, state.attemptsLeft, state.attemptsLeft)
        lockedOut -> stringResource(R.string.vault_locked_out, formatCountdown(state.lockoutSecondsLeft))
        state.stage == VaultStage.SET_PIN -> stringResource(R.string.vault_create_pin_sub)
        state.stage == VaultStage.CONFIRM_PIN -> stringResource(R.string.vault_confirm_pin_sub)
        else -> stringResource(R.string.vault_enter_pin)
    }
    Column(
        modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.systemBars).testTag("app_vault_pin_page"),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        VaultTopBar(onBack = callbacks.onBack, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        VaultLockBadge()
        Text(text = stringResource(title), style = MaterialTheme.typography.headlineSmall, color = Ink, modifier = Modifier.padding(top = 16.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = if (failed) ErrorColor else Muted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 40.dp, vertical = 8.dp).height(44.dp).testTag("app_vault_pin_message"),
        )
        PinDots(entered = state.digitsEntered, error = failed)
        Spacer(modifier = Modifier.height(32.dp))
        PinPad(
            enabled = !lockedOut,
            showForgot = state.stage == VaultStage.UNLOCK,
            onDigit = callbacks.onDigit,
            onBackspace = callbacks.onBackspace,
            onRecover = callbacks.onForgotPin,
        )
    }
}

private fun formatCountdown(seconds: Int): String = String.format(Locale.getDefault(), "%d:%02d", seconds / 60, seconds % 60)

@Composable
private fun PinDots(entered: Int, error: Boolean) {
    val color = if (error) ErrorColor else Ink
    Row(
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        modifier = Modifier.testTag("app_vault_pin_dots").semantics { contentDescription = "$entered/$VAULT_PIN_LENGTH" },
    ) {
        repeat(VAULT_PIN_LENGTH) { index ->
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .then(if (index < entered) Modifier.background(color) else Modifier.border(2.dp, color, CircleShape)),
            )
        }
    }
}

@Composable
private fun PinPad(enabled: Boolean, showForgot: Boolean, onDigit: (Int) -> Unit, onBackspace: () -> Unit, onRecover: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        listOf(listOf(1, 2, 3), listOf(4, 5, 6), listOf(7, 8, 9)).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) { row.forEach { PinKey(it, enabled, onDigit) } }
        }
        PinPadBottomRow(enabled, showForgot, onDigit, onBackspace, onRecover)
    }
}

@Composable
private fun PinPadBottomRow(enabled: Boolean, showForgot: Boolean, onDigit: (Int) -> Unit, onBackspace: () -> Unit, onRecover: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
        Box(modifier = Modifier.size(KEY_SIZE), contentAlignment = Alignment.Center) {
            if (showForgot) {
                TextActionButton(text = stringResource(R.string.vault_forgot_pin), onClick = onRecover, style = MaterialTheme.typography.labelMedium, modifier = Modifier.testTag("app_vault_forgot_pin"))
            }
        }
        PinKey(0, enabled, onDigit)
        Box(modifier = Modifier.size(KEY_SIZE), contentAlignment = Alignment.Center) {
            IconButton(onClick = onBackspace, enabled = enabled, modifier = Modifier.testTag("app_vault_key_backspace")) {
                Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = stringResource(R.string.vault_backspace), tint = if (enabled) Muted else Muted.copy(alpha = 0.4f))
            }
        }
    }
}

private val KEY_SIZE = 72.dp

@Composable
private fun PinKey(digit: Int, enabled: Boolean, onDigit: (Int) -> Unit) {
    Box(
        modifier = Modifier
            .size(KEY_SIZE)
            .clip(CircleShape)
            .background(Surface)
            .clickable(enabled = enabled) { onDigit(digit) }
            .testTag("app_vault_key_$digit"),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = digit.toString(), style = MaterialTheme.typography.headlineSmall, color = if (enabled) Ink else Muted.copy(alpha = 0.4f))
    }
}

@Suppress("LongMethod")
@Composable
private fun VaultListPage(state: AppVaultUiState, callbacks: AppVaultCallbacks) {
    var menuExpanded by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.systemBars).testTag("app_vault_list_page")) {
        VaultTopBar(
            onBack = callbacks.onClose,
            title = stringResource(R.string.vault_title),
            trailing = {
                TextActionButton(
                    text = stringResource(R.string.vault_add_apps),
                    onClick = callbacks.onAddApps,
                    leadingIcon = Icons.Default.Add,
                    modifier = Modifier.testTag("app_vault_add_apps"),
                )
                Box {
                    IconButton(onClick = { menuExpanded = true }, modifier = Modifier.testTag("app_vault_overflow")) {
                        Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.vault_more_options), tint = Muted)
                    }
                    ThemedDropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        ThemedDropdownMenuItem(
                            label = stringResource(R.string.vault_change_pin),
                            onClick = { menuExpanded = false; callbacks.onChangePin() },
                            modifier = Modifier.testTag("app_vault_change_pin"),
                        )
                        ThemedDropdownMenuItem(
                            label = stringResource(R.string.vault_turn_off),
                            onClick = { menuExpanded = false; callbacks.onTurnOff() },
                            modifier = Modifier.testTag("app_vault_turn_off"),
                        )
                    }
                }
            },
        )
        if (state.apps.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp).testTag("app_vault_empty"),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(text = stringResource(R.string.vault_empty), style = MaterialTheme.typography.titleMedium, color = Ink)
                Text(
                    text = stringResource(R.string.vault_empty_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Muted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp, bottom = 20.dp),
                )
                PrimaryButton(text = stringResource(R.string.vault_add_apps), onClick = callbacks.onAddApps)
            }
        } else {
            val rows = remember(state.apps) { state.apps.toVaultRows() }
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(rows, key = { it.key }) { row ->
                    when (row) {
                        is VaultRow.Header -> Text(
                            text = row.letter,
                            style = MaterialTheme.typography.labelMedium,
                            color = Muted,
                            modifier = Modifier.padding(start = 24.dp, top = 12.dp, bottom = 4.dp),
                        )
                        is VaultRow.Entry -> VaultAppRow(row.app, callbacks)
                    }
                }
            }
        }
    }
}

private sealed interface VaultRow {
    val key: String

    data class Header(val letter: String) : VaultRow {
        override val key get() = "header_$letter"
    }

    data class Entry(val app: AppInfo) : VaultRow {
        override val key get() = app.listKey
    }
}

private fun List<AppInfo>.toVaultRows(): List<VaultRow> {
    val rows = mutableListOf<VaultRow>()
    var letter: String? = null
    forEach { app ->
        val first = app.label.firstOrNull()?.uppercaseChar()?.takeIf { it.isLetter() }?.toString() ?: "#"
        if (first != letter) {
            letter = first
            rows += VaultRow.Header(first)
        }
        rows += VaultRow.Entry(app)
    }
    return rows
}

@Composable
private fun VaultAppRow(app: AppInfo, callbacks: AppVaultCallbacks) {
    var menuExpanded by remember { mutableStateOf(false) }
    val itemSize = DrawerListItemSize.REGULAR
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { callbacks.onAppClick(app) }
            .padding(start = 24.dp, end = 12.dp, top = itemSize.verticalPaddingDp.dp, bottom = itemSize.verticalPaddingDp.dp)
            .testTag("app_vault_row_${app.packageName}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AppIcon(app = app, size = AppIconSize.forRow(itemSize), contentDescription = null)
        Text(text = app.label, style = MaterialTheme.typography.bodyLarge, color = Ink, modifier = Modifier.weight(1f))
        Box {
            IconButton(onClick = { menuExpanded = true }, modifier = Modifier.testTag("app_vault_row_menu_${app.packageName}")) {
                Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.vault_app_menu, app.label), tint = Muted)
            }
            ThemedDropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                ThemedDropdownMenuItem(
                    label = stringResource(R.string.vault_app_info),
                    onClick = { menuExpanded = false; callbacks.onAppInfo(app) },
                    modifier = Modifier.testTag("app_vault_app_info"),
                )
                ThemedDropdownMenuItem(
                    label = stringResource(R.string.vault_remove),
                    onClick = { menuExpanded = false; callbacks.onRemove(app) },
                    modifier = Modifier.testTag("app_vault_remove"),
                )
            }
        }
    }
}

@Composable
private fun VaultPickerPage(state: AppVaultUiState, callbacks: AppVaultCallbacks) {
    AppPickerScreen(
        title = stringResource(R.string.vault_add_to_vault_title),
        query = state.pickerQuery,
        selectedResults = persistentListOf(),
        otherResults = state.pickerResults,
        selectedComponents = state.pickerSelected,
        canAddMore = true,
        canRemove = true,
        sortOption = state.pickerSortOption,
        sortDirection = state.pickerSortDirection,
        selectedSectionLabel = stringResource(R.string.vault_picker_chosen),
        screenTestTag = "app_vault_picker",
        tagPrefix = "app_vault_picker",
        onQueryChange = callbacks.onPickerQueryChange,
        onSortOptionChange = callbacks.onPickerSortOptionChange,
        onSortDirectionToggle = callbacks.onPickerSortDirectionToggle,
        onToggleApp = callbacks.onPickerToggle,
        onDone = callbacks.onPickerDone,
    )
}

@Preview(showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun VaultPinPagePreview() {
    FacetLauncherTheme {
        AppVaultScreen(
            state = AppVaultUiState(stage = VaultStage.UNLOCK, digitsEntered = 2),
            callbacks = AppVaultCallbacks(),
            snackbarHostState = SnackbarHostState(),
            screenLockAvailable = true,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun VaultEmptyListPreview() {
    FacetLauncherTheme {
        AppVaultScreen(
            state = AppVaultUiState(stage = VaultStage.LIST, pickerSelected = persistentSetOf()),
            callbacks = AppVaultCallbacks(),
            snackbarHostState = SnackbarHostState(),
            screenLockAvailable = true,
        )
    }
}
