package com.facetlauncher.app.ui.home.clock

import java.time.LocalDateTime

private val ONES = arrayOf(
    "Zero", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
    "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen",
)
private val TENS = arrayOf("", "", "Twenty", "Thirty", "Forty", "Fifty")

private fun numberWords(n: Int): String = when {
    n < 20 -> ONES[n]
    n < 60 -> TENS[n / 10] + if (n % 10 != 0) " ${ONES[n % 10]}" else ""
    else -> n.toString()
}

/**
 * Spells the time out as words for [ClockTemplateId.SPELLED_OUT] — e.g. `"Eleven Twenty Two"`,
 * `"Twelve O'Clock"`, `"Nine Oh Five"`. Always spoken in 12-hour form (`use24HourTime` doesn't
 * apply — there's no natural spoken 24-hour convention), matching how people actually say a
 * time aloud regardless of which numeral format the rest of the app is showing.
 */
fun timeInWords(now: LocalDateTime): String {
    val hour12 = now.hour % 12
    val hourWord = ONES[if (hour12 == 0) 12 else hour12]
    val minuteWord = when {
        now.minute == 0 -> "O'Clock"
        now.minute < 10 -> "${ONES[now.minute]}"
        else -> numberWords(now.minute)
    }
    return "$hourWord $minuteWord"
}
