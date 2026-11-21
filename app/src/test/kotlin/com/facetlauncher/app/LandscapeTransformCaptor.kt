package com.facetlauncher.app

import com.facetlauncher.app.domain.ClockPlacement
import com.facetlauncher.app.data.model.LandscapeOverrides
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers

/** A captor for a Kotlin lambda argument; the erased `Function1` class is the only thing Mockito can be given, hence the one cast. */
@Suppress("UNCHECKED_CAST")
private fun <T : Any> functionCaptor(): ArgumentCaptor<T> = ArgumentCaptor.forClass(Function1::class.java) as ArgumentCaptor<T>

/**
 * Captures the transform handed to an `updateLandscape` call. Mockito's own `capture()` and `eq()` return null,
 * which Kotlin refuses for a non-null parameter, so these register the matcher and hand back a non-null stand-in.
 */
class LandscapeTransformCaptor {
    private val captor = functionCaptor<(LandscapeOverrides) -> LandscapeOverrides>()

    /** Use inside `verify(...)`, in place of the transform argument. */
    fun capture(): (LandscapeOverrides) -> LandscapeOverrides {
        captor.capture()
        return { it }
    }

    /** Every captured transform, applied in call order to [start]. */
    fun applied(start: LandscapeOverrides = LandscapeOverrides()): LandscapeOverrides =
        captor.allValues.fold(start) { acc, transform -> transform(acc) }

    /** The single captured transform, applied to [start]. */
    fun last(start: LandscapeOverrides = LandscapeOverrides()): LandscapeOverrides = captor.value(start)
}

/** `eq(value)` for a non-null Kotlin parameter, for use alongside [LandscapeTransformCaptor.capture]. */
fun <T> eqOf(value: T): T {
    ArgumentMatchers.eq(value)
    return value
}

/** The same stand-in for a clock placement change: register it in `verify(...)`, then apply what was captured. */
class PlacementChangeCaptor {
    private val captor = functionCaptor<(ClockPlacement) -> ClockPlacement>()

    /** Use inside `verify(...)`, in place of the change argument. */
    fun capture(): (ClockPlacement) -> ClockPlacement {
        captor.capture()
        return { it }
    }

    /** The single captured change, applied to [start]. */
    fun applied(start: ClockPlacement): ClockPlacement = captor.value(start)

    /** Every captured change, each applied to [start], in call order. */
    fun appliedEach(start: ClockPlacement): List<ClockPlacement> = captor.allValues.map { it(start) }
}
