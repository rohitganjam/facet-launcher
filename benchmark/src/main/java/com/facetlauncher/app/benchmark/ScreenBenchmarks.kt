package com.facetlauncher.app.benchmark

import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

private const val PACKAGE = "com.facetlauncher.app"
private const val ITERATIONS = 8
private const val SWIPE_STEPS = 25
private const val SETTLE_MS = 700L

/**
 * Cold start, and the frames of opening each panel with a swipe from Home (the facet carousel, the
 * app drawer, the Hub), with 1, 3 and 6 facets configured. Every facet card in the carousel is a
 * full Home layout, so the facet count is what the carousel's cost scales with.
 */
@RunWith(Parameterized::class)
class ScreenBenchmarks(private val facets: Int) {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Before
    fun seed() = BenchmarkSeed.seed(facets)

    @Test
    fun coldStart() = benchmarkRule.measureRepeated(
        packageName = PACKAGE,
        metrics = listOf(StartupTimingMetric()),
        iterations = ITERATIONS,
        startupMode = StartupMode.COLD,
    ) {
        pressHome()
        startActivityAndWait()
    }

    @Test
    fun openCarousel() = swipeFromHome { swipeHorizontally(from = 0.8f, to = 0.2f) }

    @Test
    fun openDrawer() = swipeFromHome { swipeVertically(from = 0.8f, to = 0.2f) }

    @Test
    fun openHub() = swipeFromHome { swipeHorizontally(from = 0.2f, to = 0.8f) }

    /** Each iteration restarts the activity (the process stays), so the panels are composed afresh, then measures the swipe and its settle. */
    private fun swipeFromHome(swipe: MacrobenchmarkScope.() -> Unit) = benchmarkRule.measureRepeated(
        packageName = PACKAGE,
        metrics = listOf(FrameTimingMetric()),
        iterations = ITERATIONS,
        startupMode = StartupMode.WARM,
        setupBlock = {
            pressHome()
            startActivityAndWait()
            device.waitForIdle()
        },
    ) {
        swipe()
        device.waitForIdle()
        Thread.sleep(SETTLE_MS)
    }

    private fun MacrobenchmarkScope.swipeHorizontally(from: Float, to: Float) {
        val y = (device.displayHeight * 0.45f).toInt()
        device.swipe((device.displayWidth * from).toInt(), y, (device.displayWidth * to).toInt(), y, SWIPE_STEPS)
    }

    private fun MacrobenchmarkScope.swipeVertically(from: Float, to: Float) {
        val x = (device.displayWidth * 0.5f).toInt()
        device.swipe(x, (device.displayHeight * from).toInt(), x, (device.displayHeight * to).toInt(), SWIPE_STEPS)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0} facets")
        fun facetCounts() = listOf(1, 3, 6)
    }
}
