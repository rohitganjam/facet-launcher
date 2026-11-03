package com.facetlauncher.app.data

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Exists only to perform [GLOBAL_ACTION_LOCK_SCREEN] for Home's double-tap, which keeps biometric
 * unlock working (unlike a device-admin lock). It requests no event types and never reads the
 * screen. Declared in the manifest with `BIND_ACCESSIBILITY_SERVICE`; the user enables it from
 * system Settings (see [AccessibilityAccessRepository]).
 */
@AndroidEntryPoint
class FacetAccessibilityService : AccessibilityService() {

    @Inject
    lateinit var lockScreenController: LockScreenController

    override fun onServiceConnected() {
        super.onServiceConnected()
        lockScreenController.attach { performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN) }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        lockScreenController.detach()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        lockScreenController.detach()
        super.onDestroy()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() = Unit
}
