package com.facetlauncher.app.benchmark

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice

/** Sets the app up for a run through the benchmark build's `BenchmarkSeedReceiver`: onboarded, Pro, [facets] facets with favorites. */
object BenchmarkSeed {
    private const val RECEIVER = "com.facetlauncher.app/com.facetlauncher.app.benchmark.BenchmarkSeedReceiver"

    fun seed(facets: Int) {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.executeShellCommand("am broadcast -n $RECEIVER --ei facets $facets")
        Thread.sleep(SEED_SETTLE_MS)
    }

    /** pressHome() goes to the Home role holder, so make it this app, not the device's own launcher. */
    fun makeDefaultHome(packageName: String = "com.facetlauncher.app") {
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            .executeShellCommand("cmd role add-role-holder --user 0 android.app.role.HOME $packageName")
    }

    /** The seed broadcast leaves the app's process running, and a cold start needs it stopped (its data stays). */
    fun stopApp() {
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).executeShellCommand("am force-stop com.facetlauncher.app")
    }

    private const val SEED_SETTLE_MS = 3_000L
}
