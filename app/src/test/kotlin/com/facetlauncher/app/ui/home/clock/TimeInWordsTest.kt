package com.facetlauncher.app.ui.home.clock

import java.time.LocalDateTime
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class TimeInWordsTest {

    private fun at(hour: Int, minute: Int): LocalDateTime = LocalDateTime.of(2026, 8, 27, hour, minute)

    // English

    @Test
    fun `english reads hour and minute as two words`() {
        assertEquals("Eleven Twenty Two", timeInWords(at(11, 22), Locale.ENGLISH))
    }

    @Test
    fun `english renders the top of the hour as O'Clock`() {
        assertEquals("Twelve O'Clock", timeInWords(at(0, 0), Locale.ENGLISH))
        assertEquals("Twelve O'Clock", timeInWords(at(12, 0), Locale.ENGLISH))
    }

    @Test
    fun `english pads a single-digit minute with Oh`() {
        assertEquals("Nine Oh Five", timeInWords(at(9, 5), Locale.ENGLISH))
        assertEquals("One Oh One", timeInWords(at(13, 1), Locale.ENGLISH))
    }

    @Test
    fun `english wraps 24-hour input back into 12-hour words`() {
        assertEquals("One Twenty Two", timeInWords(at(13, 22), Locale.ENGLISH))
        assertEquals("Twelve O'Clock", timeInWords(at(0, 0), Locale.ENGLISH))
    }

    @Test
    fun `english handles a teen and a bare tens minute`() {
        assertEquals("Six Fifteen", timeInWords(at(6, 15), Locale.ENGLISH))
        assertEquals("Six Thirty", timeInWords(at(6, 30), Locale.ENGLISH))
    }

    // Spanish

    @Test
    fun `spanish joins hour and minute with y`() {
        assertEquals("Once y Veintidós", timeInWords(at(11, 22), Locale("es")))
    }

    @Test
    fun `spanish one o'clock uses the feminine Una`() {
        assertEquals("Una En Punto", timeInWords(at(1, 0), Locale("es")))
        assertEquals("Una En Punto", timeInWords(at(13, 0), Locale("es")))
    }

    @Test
    fun `spanish minutes stay masculine Uno even at one o'clock`() {
        assertEquals("Una y Uno", timeInWords(at(1, 1), Locale("es")))
    }

    @Test
    fun `spanish renders the top of the hour as En Punto`() {
        assertEquals("Doce En Punto", timeInWords(at(12, 0), Locale("es")))
    }

    @Test
    fun `spanish has no oh-filler for a single-digit minute`() {
        assertEquals("Nueve y Cinco", timeInWords(at(9, 5), Locale("es")))
    }

    @Test
    fun `spanish twenties are irregular fused forms, thirties on use tens plus y`() {
        assertEquals("Ocho y Veintinueve", timeInWords(at(8, 29), Locale("es")))
        assertEquals("Ocho y Treinta y Uno", timeInWords(at(8, 31), Locale("es")))
    }

    // French

    @Test
    fun `french joins hour and minute with heures`() {
        assertEquals("Onze Heures Vingt-Deux", timeInWords(at(11, 22), Locale.FRENCH))
    }

    @Test
    fun `french one o'clock is singular Une Heure`() {
        assertEquals("Une Heure", timeInWords(at(1, 0), Locale.FRENCH))
        assertEquals("Une Heure", timeInWords(at(13, 0), Locale.FRENCH))
    }

    @Test
    fun `french a one-minute past one o'clock is also feminine Une`() {
        assertEquals("Une Heure Une", timeInWords(at(1, 1), Locale.FRENCH))
    }

    @Test
    fun `french drops the minute entirely on the hour`() {
        assertEquals("Douze Heures", timeInWords(at(12, 0), Locale.FRENCH))
    }

    @Test
    fun `french twenty-one uses et Une, other compounds hyphenate`() {
        assertEquals("Onze Heures Vingt et Une", timeInWords(at(11, 21), Locale.FRENCH))
        assertEquals("Onze Heures Vingt-Deux", timeInWords(at(11, 22), Locale.FRENCH))
    }

    // German

    @Test
    fun `german joins hour and minute with uhr`() {
        assertEquals("Elf Uhr Zweiundzwanzig", timeInWords(at(11, 22), Locale.GERMAN))
    }

    @Test
    fun `german one o'clock contracts to Ein Uhr`() {
        assertEquals("Ein Uhr", timeInWords(at(1, 0), Locale.GERMAN))
        assertEquals("Ein Uhr", timeInWords(at(13, 0), Locale.GERMAN))
    }

    @Test
    fun `german a one-minute past one o'clock keeps the uncontracted Eins`() {
        assertEquals("Ein Uhr Eins", timeInWords(at(1, 1), Locale.GERMAN))
    }

    @Test
    fun `german renders the top of the hour as bare Uhr`() {
        assertEquals("Zwölf Uhr", timeInWords(at(12, 0), Locale.GERMAN))
    }

    @Test
    fun `german compounds units before tens as one fused word`() {
        assertEquals("Elf Uhr Einundzwanzig", timeInWords(at(11, 21), Locale.GERMAN))
        assertEquals("Elf Uhr Fünfundfünfzig", timeInWords(at(11, 55), Locale.GERMAN))
    }

    // Portuguese

    @Test
    fun `portuguese joins hour and minute with e`() {
        assertEquals("Onze e Vinte e Dois", timeInWords(at(11, 22), Locale("pt")))
    }

    @Test
    fun `portuguese one o'clock uses the feminine Uma`() {
        assertEquals("Uma Em Ponto", timeInWords(at(1, 0), Locale("pt")))
        assertEquals("Uma Em Ponto", timeInWords(at(13, 0), Locale("pt")))
    }

    @Test
    fun `portuguese minutes stay masculine Um even at one o'clock`() {
        assertEquals("Uma e Um", timeInWords(at(1, 1), Locale("pt")))
    }

    @Test
    fun `portuguese renders the top of the hour as Em Ponto`() {
        assertEquals("Doze Em Ponto", timeInWords(at(12, 0), Locale("pt")))
    }

    // Locale fallback

    @Test
    fun `an unsupported locale falls back to english`() {
        assertEquals("Nine Oh Five", timeInWords(at(9, 5), Locale.JAPANESE))
    }
}
