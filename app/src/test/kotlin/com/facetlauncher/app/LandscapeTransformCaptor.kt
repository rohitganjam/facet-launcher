package com.facetlauncher.app

import com.facetlauncher.app.data.model.LandscapeOverrides
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers

/**
 * Captures the transform handed to an `updateLandscape` call. Mockito's own `capture()` and `eq()` return null,
 * which Kotlin refuses for a non-null parameter, so these register the matcher and hand back a non-null stand-in.
 */
class LandscapeTransformCaptor {
    @Suppress("UNCHECKED_CAST")
    private val captor = ArgumentCaptor.forClass(Function1::class.java) as ArgumentCaptor<(LandscapeOverrides) -> LandscapeOverrides>

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
