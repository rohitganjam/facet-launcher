package com.facetlauncher.app.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf

/**
 * Extra rows a debug build adds to the bottom of Settings. Only the debug source set provides one (through an
 * optional Hilt binding), so release and benchmark builds contain none of it.
 */
fun interface DebugSettingsEntry {
    @Composable
    fun Content()
}

/** Set by `LauncherActivity` from the optional binding; null everywhere else, including every test. */
// Debug builds only, supplied once by LauncherActivity; avoids passing a debug hook through every Settings parameter list.
@Suppress("CompositionLocalAllowlist")
val LocalDebugSettingsEntry = compositionLocalOf<DebugSettingsEntry?> { null }
