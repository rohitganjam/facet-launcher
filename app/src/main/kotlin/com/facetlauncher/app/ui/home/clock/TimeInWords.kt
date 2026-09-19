package com.facetlauncher.app.ui.home.clock

import java.time.LocalDateTime
import java.util.Locale

private val ONES_EN = arrayOf(
    "Zero", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
    "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen",
)
private val TENS_EN = arrayOf("", "", "Twenty", "Thirty", "Forty", "Fifty")

private fun numberWordsEn(n: Int): String = when {
    n < 20 -> ONES_EN[n]
    n < 60 -> TENS_EN[n / 10] + if (n % 10 != 0) " ${ONES_EN[n % 10]}" else ""
    else -> n.toString()
}

/** "Nine Oh Five" for a single-digit minute, "O'Clock" for :00, otherwise a plain two-word reading. */
private fun timeInWordsEn(hour: Int, minute: Int): String {
    val minuteWord = when {
        minute == 0 -> "O'Clock"
        minute < 10 -> "Oh ${ONES_EN[minute]}"
        else -> numberWordsEn(minute)
    }
    return "${ONES_EN[hour]} $minuteWord"
}

// Spanish — "Once y Veintidós" (11:22), "Doce En Punto" (12:00). "Hora" is feminine, so a 1
// o'clock reads "Una", not the masculine "Uno" numeral used everywhere else (minutes stay "Uno" —
// "minuto" is masculine).
private val ONES_ES = arrayOf(
    "Cero", "Uno", "Dos", "Tres", "Cuatro", "Cinco", "Seis", "Siete", "Ocho", "Nueve",
    "Diez", "Once", "Doce", "Trece", "Catorce", "Quince", "Dieciséis", "Diecisiete", "Dieciocho", "Diecinueve",
    "Veinte", "Veintiuno", "Veintidós", "Veintitrés", "Veinticuatro", "Veinticinco", "Veintiséis", "Veintisiete", "Veintiocho", "Veintinueve",
)
private val TENS_ES = arrayOf("", "", "", "Treinta", "Cuarenta", "Cincuenta")

private fun numberWordsEs(n: Int): String = when {
    n < 30 -> ONES_ES[n]
    n < 60 -> TENS_ES[n / 10] + if (n % 10 != 0) " y ${ONES_ES[n % 10]}" else ""
    else -> n.toString()
}

private fun timeInWordsEs(hour: Int, minute: Int): String {
    val hourWord = if (hour == 1) "Una" else ONES_ES[hour]
    return if (minute == 0) "$hourWord En Punto" else "$hourWord y ${numberWordsEs(minute)}"
}

// French — "Onze Heures Vingt-Deux" (11:22), "Douze Heures" (12:00). Both "heure" and the implied
// "minute" are feminine, so a value of 1 always reads "Une" (never the masculine "Un") — for the
// hour word itself and whenever a compound ends in 1 (21, 31, 41, 51).
private val ONES_FR = arrayOf(
    "Zéro", "Une", "Deux", "Trois", "Quatre", "Cinq", "Six", "Sept", "Huit", "Neuf",
    "Dix", "Onze", "Douze", "Treize", "Quatorze", "Quinze", "Seize", "Dix-Sept", "Dix-Huit", "Dix-Neuf",
)
private val TENS_FR = arrayOf("", "", "Vingt", "Trente", "Quarante", "Cinquante")

private fun numberWordsFr(n: Int): String = when {
    n < 20 -> ONES_FR[n]
    n < 60 -> TENS_FR[n / 10] + when {
        n % 10 == 0 -> ""
        n % 10 == 1 -> " et ${ONES_FR[1]}"
        else -> "-${ONES_FR[n % 10]}"
    }
    else -> n.toString()
}

private fun timeInWordsFr(hour: Int, minute: Int): String {
    val hourWord = numberWordsFr(hour)
    val hourUnit = if (hour == 1) "Heure" else "Heures"
    return if (minute == 0) "$hourWord $hourUnit" else "$hourWord $hourUnit ${numberWordsFr(minute)}"
}

// German — "Elf Uhr Zweiundzwanzig" (11:22), "Zwölf Uhr" (12:00). Units-before-tens compounds
// ("einundzwanzig" = one-and-twenty = 21); a bare 1 o'clock contracts to "Ein Uhr" (not "Eins
// Uhr"), same contraction the compound numbers themselves already use for a units digit of 1.
private val ONES_DE = arrayOf(
    "Null", "Eins", "Zwei", "Drei", "Vier", "Fünf", "Sechs", "Sieben", "Acht", "Neun",
    "Zehn", "Elf", "Zwölf", "Dreizehn", "Vierzehn", "Fünfzehn", "Sechzehn", "Siebzehn", "Achtzehn", "Neunzehn",
)
private val TENS_DE = arrayOf("", "", "Zwanzig", "Dreißig", "Vierzig", "Fünfzig")
private val TENS_DE_LOWER = arrayOf("", "", "zwanzig", "dreißig", "vierzig", "fünfzig")

private fun numberWordsDe(n: Int): String = when {
    n < 20 -> ONES_DE[n]
    n < 60 -> {
        val unit = n % 10
        if (unit == 0) {
            TENS_DE[n / 10]
        } else {
            val unitWord = if (unit == 1) "Ein" else ONES_DE[unit]
            "${unitWord}und${TENS_DE_LOWER[n / 10]}"
        }
    }
    else -> n.toString()
}

private fun timeInWordsDe(hour: Int, minute: Int): String {
    val hourWord = if (hour == 1) "Ein" else numberWordsDe(hour)
    return if (minute == 0) "$hourWord Uhr" else "$hourWord Uhr ${numberWordsDe(minute)}"
}

// Portuguese — "Onze e Vinte e Dois" (11:22), "Doze Em Ponto" (12:00). Same feminine-hour-only
// pattern as Spanish: "hora" is feminine ("Uma", not "Um"), "minuto" is masculine so minutes stay
// "Um".
private val ONES_PT = arrayOf(
    "Zero", "Um", "Dois", "Três", "Quatro", "Cinco", "Seis", "Sete", "Oito", "Nove",
    "Dez", "Onze", "Doze", "Treze", "Catorze", "Quinze", "Dezesseis", "Dezessete", "Dezoito", "Dezenove",
)
private val TENS_PT = arrayOf("", "", "Vinte", "Trinta", "Quarenta", "Cinquenta")

private fun numberWordsPt(n: Int): String = when {
    n < 20 -> ONES_PT[n]
    n < 60 -> TENS_PT[n / 10] + if (n % 10 != 0) " e ${ONES_PT[n % 10]}" else ""
    else -> n.toString()
}

private fun timeInWordsPt(hour: Int, minute: Int): String {
    val hourWord = if (hour == 1) "Uma" else ONES_PT[hour]
    return if (minute == 0) "$hourWord Em Ponto" else "$hourWord e ${numberWordsPt(minute)}"
}

/**
 * Spells the time out as words for [ClockTemplateId.SPELLED_OUT] — real per-locale grammar, not a
 * word-for-word English vocabulary swap. English: `"Eleven Twenty Two"`, `"Twelve O'Clock"`,
 * `"Nine Oh Five"`. Spanish: `"Once y Veintidós"`, `"Doce En Punto"`, hour 1 is `"Una"` (feminine
 * "hora"). French: `"Onze Heures Vingt-Deux"`, `"Douze Heures"`, both the hour and a units-digit
 * minute of 1 are `"Une"` (feminine "heure"/"minute"), tens compounds hyphenate except an `"et
 * Une"` ×1 unit. German: `"Elf Uhr Zweiundzwanzig"`, `"Zwölf Uhr"`, units-before-tens compounds
 * ("einundzwanzig"), hour 1 contracts to `"Ein Uhr"`. Portuguese: `"Onze e Vinte e Dois"`, `"Doze
 * Em Ponto"`, hour 1 is `"Uma"` (feminine "hora"). Always spoken in 12-hour form
 * (`use24HourTime` doesn't apply — there's no natural spoken 24-hour convention in any of these
 * languages), matching how people actually say a time aloud regardless of which numeral format
 * the rest of the app is showing. Dispatches on [locale]'s language; anything other than
 * es/fr/de/pt falls back to English.
 */
fun timeInWords(now: LocalDateTime, locale: Locale = Locale.getDefault()): String {
    val hour12 = now.hour % 12
    val hour = if (hour12 == 0) 12 else hour12
    val minute = now.minute
    return when (locale.language) {
        "es" -> timeInWordsEs(hour, minute)
        "fr" -> timeInWordsFr(hour, minute)
        "de" -> timeInWordsDe(hour, minute)
        "pt" -> timeInWordsPt(hour, minute)
        else -> timeInWordsEn(hour, minute)
    }
}
