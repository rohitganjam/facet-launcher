package com.facetlauncher.app.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * `kotlinx.coroutines.flow.combine` only has positional (fully-typed) overloads up to 5 flows —
 * past that, the only built-in option is `combine(vararg flows: Flow<T>) { Array<T> -> ... }`,
 * which forces every flow to a single shared `T` and means pulling each value back out by index
 * with an unchecked cast. These fill the gap by nesting the existing 3-flow `combine` calls
 * (via `Triple`) instead, so a 6th/7th flow still gets a flat, fully-typed transform lambda —
 * add `combine8`, etc. the same way if a use case ever needs more.
 */
fun <T1, T2, T3, T4, T5, T6, R> combine(
    flow1: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    flow6: Flow<T6>,
    transform: suspend (T1, T2, T3, T4, T5, T6) -> R,
): Flow<R> = combine(
    combine(flow1, flow2, flow3, ::Triple),
    combine(flow4, flow5, flow6, ::Triple),
) { first, second ->
    transform(first.first, first.second, first.third, second.first, second.second, second.third)
}

fun <T1, T2, T3, T4, T5, T6, T7, R> combine(
    flow1: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    flow6: Flow<T6>,
    flow7: Flow<T7>,
    transform: suspend (T1, T2, T3, T4, T5, T6, T7) -> R,
): Flow<R> = combine(
    combine(flow1, flow2, flow3, ::Triple),
    combine(flow4, flow5, flow6, ::Triple),
    flow7,
) { first, second, seventh ->
    transform(first.first, first.second, first.third, second.first, second.second, second.third, seventh)
}
