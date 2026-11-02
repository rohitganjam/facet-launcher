package com.facetlauncher.app.benchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.UiDevice
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val PACKAGE = "com.facetlauncher.app"
private const val FACETS = 3
private const val SWIPE_STEPS = 25
private const val SETTLE_MS = 700L

/**
 * Records the classes and methods the launcher runs on its main paths (cold start, the facet carousel, the
 * app drawer and its list, the Hub) into the baseline profile that `app/src/main/baseline-prof.txt` ships.
 * Run on an emulator against the `benchmark` build, then copy the generated `*-baseline-prof.txt` there.
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val rule = BaselineProfileRule()

    @Before
    fun seed() {
        BenchmarkSeed.seed(FACETS)
        BenchmarkSeed.makeDefaultHome()
    }

    @Test
    fun generate() = rule.collect(packageName = PACKAGE) {
        pressHome()
        startActivityAndWait()
        device.waitForIdle()

        swipeHorizontally(device, from = 0.8f, to = 0.2f)
        backToHome()
        swipeHorizontally(device, from = 0.2f, to = 0.8f)
        backToHome()

        swipeVertically(device, from = 0.8f, to = 0.2f)
        repeat(SCROLLS) { swipeVertically(device, from = 0.8f, to = 0.3f) }
        repeat(SCROLLS) { swipeVertically(device, from = 0.3f, to = 0.8f) }
        backToHome()
    }

    private fun androidx.benchmark.macro.MacrobenchmarkScope.backToHome() {
        device.waitForIdle()
        Thread.sleep(SETTLE_MS)
        pressHome()
        device.waitForIdle()
    }

    private fun swipeHorizontally(device: UiDevice, from: Float, to: Float) {
        val y = (device.displayHeight * 0.45f).toInt()
        device.swipe((device.displayWidth * from).toInt(), y, (device.displayWidth * to).toInt(), y, SWIPE_STEPS)
    }

    private fun swipeVertically(device: UiDevice, from: Float, to: Float) {
        val x = (device.displayWidth * 0.5f).toInt()
        device.swipe(x, (device.displayHeight * from).toInt(), x, (device.displayHeight * to).toInt(), SWIPE_STEPS)
    }

    private companion object {
        const val SCROLLS = 3
    }
}
