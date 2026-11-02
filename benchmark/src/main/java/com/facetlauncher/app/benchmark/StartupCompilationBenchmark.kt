package com.facetlauncher.app.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val PACKAGE = "com.facetlauncher.app"
private const val FACETS = 3
private const val ITERATIONS = 10

/** Cold start with no ahead-of-time compilation against start with the baseline profile applied: what the profile buys. */
@RunWith(AndroidJUnit4::class)
class StartupCompilationBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Before
    fun seed() {
        BenchmarkSeed.seed(FACETS)
        // Macrobenchmark presses Home before each cold start and needs this app stopped, so another launcher must be Home.
        BenchmarkSeed.makeDefaultHome("com.google.android.apps.nexuslauncher")
        BenchmarkSeed.stopApp()
    }

    @Test
    fun coldStartWithoutProfile() = coldStart(CompilationMode.None())

    @Test
    fun coldStartWithBaselineProfile() = coldStart(CompilationMode.Partial())

    private fun coldStart(mode: CompilationMode) = benchmarkRule.measureRepeated(
        packageName = PACKAGE,
        metrics = listOf(StartupTimingMetric()),
        compilationMode = mode,
        iterations = ITERATIONS,
        startupMode = StartupMode.COLD,
    ) {
        pressHome()
        startActivityAndWait()
    }
}
