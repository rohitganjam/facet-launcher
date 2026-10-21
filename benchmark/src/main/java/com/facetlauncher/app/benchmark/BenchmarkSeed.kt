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

    private const val SEED_SETTLE_MS = 3_000L
}
