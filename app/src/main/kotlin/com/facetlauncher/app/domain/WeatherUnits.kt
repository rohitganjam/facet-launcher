package com.facetlauncher.app.domain

import com.facetlauncher.app.data.model.PressureUnit
import com.facetlauncher.app.data.model.RainUnit
import com.facetlauncher.app.data.model.TemperatureUnit
import com.facetlauncher.app.data.model.WindSpeedUnit
import java.util.Locale

/**
 * Turns an option (which may be `AUTO`) into a concrete unit for a locale, and converts the feed's units (°C, m/s, mm,
 * hPa) into it. Pure, so the sheet and the accessory format the same way.
 */
object WeatherUnits {

    // Countries where Fahrenheit and the US customary units are the everyday ones.
    private val FAHRENHEIT_COUNTRIES = setOf("US", "BS", "BZ", "KY", "PW", "LR")
    private val MILES_PER_HOUR_COUNTRIES = setOf("US", "GB", "LR", "MM")
    private val INCH_COUNTRIES = setOf("US", "LR", "MM")
    private val INCHES_OF_MERCURY_COUNTRIES = setOf("US")

    private const val KMH_PER_MS = 3.6
    private const val MPH_PER_MS = 2.236936
    private const val MM_PER_INCH = 25.4
    private const val INHG_PER_HPA = 0.02953
    private const val MMHG_PER_HPA = 0.750062

    fun resolve(unit: TemperatureUnit, locale: Locale): TemperatureUnit = when {
        unit != TemperatureUnit.AUTO -> unit
        locale.country in FAHRENHEIT_COUNTRIES -> TemperatureUnit.FAHRENHEIT
        else -> TemperatureUnit.CELSIUS
    }

    fun resolve(unit: WindSpeedUnit, locale: Locale): WindSpeedUnit = when {
        unit != WindSpeedUnit.AUTO -> unit
        locale.country in MILES_PER_HOUR_COUNTRIES -> WindSpeedUnit.MPH
        else -> WindSpeedUnit.KMH
    }

    fun resolve(unit: RainUnit, locale: Locale): RainUnit = when {
        unit != RainUnit.AUTO -> unit
        locale.country in INCH_COUNTRIES -> RainUnit.INCH
        else -> RainUnit.MM
    }

    fun resolve(unit: PressureUnit, locale: Locale): PressureUnit = when {
        unit != PressureUnit.AUTO -> unit
        locale.country in INCHES_OF_MERCURY_COUNTRIES -> PressureUnit.INHG
        else -> PressureUnit.HPA
    }

    /** [unit] must already be resolved: `AUTO` converts as Celsius. */
    fun temperature(celsius: Double, unit: TemperatureUnit): Double =
        if (unit == TemperatureUnit.FAHRENHEIT) celsius * 9.0 / 5.0 + 32.0 else celsius

    fun windSpeed(metresPerSecond: Double, unit: WindSpeedUnit): Double = when (unit) {
        WindSpeedUnit.MPH -> metresPerSecond * MPH_PER_MS
        WindSpeedUnit.MS -> metresPerSecond
        WindSpeedUnit.KMH, WindSpeedUnit.AUTO -> metresPerSecond * KMH_PER_MS
    }

    fun rain(millimetres: Double, unit: RainUnit): Double =
        if (unit == RainUnit.INCH) millimetres / MM_PER_INCH else millimetres

    fun pressure(hectopascals: Double, unit: PressureUnit): Double = when (unit) {
        PressureUnit.INHG -> hectopascals * INHG_PER_HPA
        PressureUnit.MMHG -> hectopascals * MMHG_PER_HPA
        PressureUnit.HPA, PressureUnit.AUTO -> hectopascals
    }
}
