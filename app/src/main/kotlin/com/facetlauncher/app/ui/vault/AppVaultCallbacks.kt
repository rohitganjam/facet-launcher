package com.facetlauncher.app.ui.vault

import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppSortOption

/** What the vault screens ask of their view model and of the launcher around them. */
class AppVaultCallbacks(
    val onBack: () -> Unit = {},
    val onClose: () -> Unit = {},
    val onStartSetPin: () -> Unit = {},
    val onDigit: (Int) -> Unit = {},
    val onBackspace: () -> Unit = {},
    val onForgotPin: () -> Unit = {},
    val onAppClick: (AppInfo) -> Unit = {},
    val onAppInfo: (AppInfo) -> Unit = {},
    val onRemove: (AppInfo) -> Unit = {},
    val onAddApps: () -> Unit = {},
    val onChangePin: () -> Unit = {},
    val onTurnOff: () -> Unit = {},
    val onPickerQueryChange: (String) -> Unit = {},
    val onPickerSortOptionChange: (AppSortOption) -> Unit = {},
    val onPickerSortDirectionToggle: () -> Unit = {},
    val onPickerToggle: (AppInfo) -> Unit = {},
    val onPickerDone: () -> Unit = {},
    val onConfirmMove: () -> Unit = {},
    val onConfirmTurnOff: () -> Unit = {},
    val onConfirmReset: () -> Unit = {},
    val onDismissConfirm: () -> Unit = {},
)
