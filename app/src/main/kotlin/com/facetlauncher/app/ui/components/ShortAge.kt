package com.facetlauncher.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.facetlauncher.app.R

internal enum class AgeUnit { SECONDS, MINUTES, HOURS, DAYS }

internal data class ShortAge(val value: Long, val unit: AgeUnit)

private const val SECONDS_PER_MINUTE = 60L
private const val MINUTES_PER_HOUR = 60L
private const val HOURS_PER_DAY = 24L

/** The age as one whole number in its largest unit that fits: 1s, 59s, 1m, 59m, 1h, 23h, 1d, 30d. Under a second reads as 1s. */
internal fun shortAge(ageMillis: Long): ShortAge {
    val seconds = (ageMillis / 1000).coerceAtLeast(1)
    val minutes = seconds / SECONDS_PER_MINUTE
    val hours = minutes / MINUTES_PER_HOUR
    return when {
        seconds < SECONDS_PER_MINUTE -> ShortAge(seconds, AgeUnit.SECONDS)
        minutes < MINUTES_PER_HOUR -> ShortAge(minutes, AgeUnit.MINUTES)
        hours < HOURS_PER_DAY -> ShortAge(hours, AgeUnit.HOURS)
        else -> ShortAge(hours / HOURS_PER_DAY, AgeUnit.DAYS)
    }
}

/** A notification's age as a compact label, such as "2m" or "3h". Read once when the card first shows. */
@Composable
internal fun shortAgeText(postTime: Long): String {
    val age = remember(postTime) { shortAge(System.currentTimeMillis() - postTime) }
    val template = when (age.unit) {
        AgeUnit.SECONDS -> R.string.relative_age_seconds
        AgeUnit.MINUTES -> R.string.relative_age_minutes
        AgeUnit.HOURS -> R.string.relative_age_hours
        AgeUnit.DAYS -> R.string.relative_age_days
    }
    return stringResource(template, age.value)
}
