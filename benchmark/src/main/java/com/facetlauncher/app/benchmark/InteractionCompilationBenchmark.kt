package com.facetlauncher.app.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val PACKAGE = "com.facetlauncher.app"
private const val FACETS = 3
private const val ITERATIONS = 8
private const val SWIPE_STEPS = 25

/** Frame timing of the first drawer open, scroll and facet swipe after a cold start, with and without the baseline profile. */
@RunWith(AndroidJUnit4::class)
class InteractionCompilationBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Before
    fun seed() {
        BenchmarkSeed.seed(FACETS)
        BenchmarkSeed.makeDefaultHome("com.google.android.apps.nexuslauncher")
        BenchmarkSeed.stopApp()
    }

    @Test
    fun interactionWithoutProfile() = interact(CompilationMode.None())

    @Test
    fun interactionWithBaselineProfile() = interact(CompilationMode.Partial())

    private fun interact(mode: CompilationMode) = benchmarkRule.measureRepeated(
        packageName = PACKAGE,
        metrics = listOf(FrameTimingMetric()),
        compilationMode = mode,
        iterations = ITERATIONS,
        startupMode = StartupMode.COLD,
    ) {
        pressHome()
        startActivityAndWait()
        val x = device.displayWidth / 2
        val y = (device.displayHeight * 0.45f).toInt()
        device.swipe((device.displayWidth * 0.8f).toInt(), y, (device.displayWidth * 0.2f).toInt(), y, SWIPE_STEPS)
        device.waitForIdle()
        device.swipe(x, (device.displayHeight * 0.8f).toInt(), x, (device.displayHeight * 0.2f).toInt(), SWIPE_STEPS)
        device.waitForIdle()
        repeat(3) {
            device.swipe(x, (device.displayHeight * 0.8f).toInt(), x, (device.displayHeight * 0.3f).toInt(), SWIPE_STEPS)
        }
        device.waitForIdle()
    }
}
