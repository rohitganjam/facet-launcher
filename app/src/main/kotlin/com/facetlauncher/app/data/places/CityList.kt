package com.facetlauncher.app.data.places

import java.text.Normalizer

/**
 * One row of the bundled city list. [timezone] is an IANA id such as `Asia/Kolkata`; [admin] is the first-level region
 * (state, province) or empty.
 */
data class City(
    val name: String,
    val admin: String,
    val countryCode: String,
    val latitude: Double,
    val longitude: Double,
    val population: Int,
    val timezone: String,
)

private val COMBINING_MARKS = Regex("\\p{M}+")

/** Lower case with accents removed, so "Zürich" and "zurich" match. */
internal fun foldForSearch(text: String): String =
    Normalizer.normalize(text.trim(), Normalizer.Form.NFD).replace(COMBINING_MARKS, "").lowercase()

/**
 * The bundled cities, searched and measured in memory. Built from `assets/cities.tsv`, one tab-separated row per city:
 * `name, admin, countryCode, latitude, longitude, population, timezone`. A row that does not parse is skipped.
 */
class CityList(private val cities: List<City>) {

    private val folded: List<String> = cities.map { foldForSearch(it.name) }

    val size: Int get() = cities.size

    /** Cities whose name starts with, has a word starting with, or contains [query]; the most populous first within each of those. */
    fun search(query: String, limit: Int = DEFAULT_LIMIT): List<City> {
        val needle = foldForSearch(query)
        if (needle.isEmpty()) return emptyList()
        return cities.indices
            .mapNotNull { i -> rank(folded[i], needle)?.let { rank -> Triple(rank, cities[i], i) } }
            .sortedWith(compareBy<Triple<Int, City, Int>> { it.first }.thenByDescending { it.second.population }.thenBy { it.third })
            .take(limit)
            .map { it.second }
    }

    /**
     * The city closest to the point, optionally only in [countryCode], and only within [maxKm] when given. `null` when none
     * qualifies (an empty list, another country, or too far).
     */
    fun nearest(latitude: Double, longitude: Double, countryCode: String? = null, maxKm: Double? = null): City? =
        cities.asSequence()
            .filter { countryCode == null || it.countryCode.equals(countryCode, ignoreCase = true) }
            .map { it to distanceKm(latitude, longitude, it.latitude, it.longitude) }
            .filter { maxKm == null || it.second <= maxKm }
            .minByOrNull { it.second }
            ?.first

    private fun rank(name: String, needle: String): Int? = when {
        name.startsWith(needle) -> 0
        name.split(' ', '-', '\'').any { it.startsWith(needle) } -> 1
        name.contains(needle) -> 2
        else -> null
    }

    companion object {
        const val DEFAULT_LIMIT = 20
        private const val COLUMNS = 7

        fun parse(lines: Sequence<String>): CityList = CityList(lines.mapNotNull(::parseRow).toList())

        private fun parseRow(line: String): City? {
            val parts = line.split('\t')
            if (parts.size < COLUMNS || parts[0].isBlank()) return null
            val latitude = parts[3].toDoubleOrNull()
            val longitude = parts[4].toDoubleOrNull()
            return if (latitude == null || longitude == null) null else City(
                name = parts[0],
                admin = parts[1],
                countryCode = parts[2],
                latitude = latitude,
                longitude = longitude,
                population = parts[5].toIntOrNull() ?: 0,
                timezone = parts[6].trim(),
            )
        }
    }
}
