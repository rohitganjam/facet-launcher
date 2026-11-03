package com.facetlauncher.app.ui.settings

import android.content.Context
import com.facetlauncher.app.data.AccessibilityAccessRepository

/** A fixed accessibility-service grant, so tests never depend on what the emulator has enabled. */
internal class FakeAccessibilityAccessRepository(
    context: Context,
    private val granted: Boolean,
) : AccessibilityAccessRepository(context) {
    override fun isGranted(): Boolean = granted
}
