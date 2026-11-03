package com.facetlauncher.app.data.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/** One bundled licence text: `assets/licenses/<asset>`, with an optional heading shown above it when a row has several. */
data class LicenseFile(val asset: String, val heading: String? = null)

/** A licence text read from the assets, ready to show. */
data class LicenseSection(val heading: String?, val text: String)

/**
 * One row on the licences screen. [files] are shown in-app as they were published; an entry with no bundled
 * text opens [url] instead. A null [name] is the localized "Fonts" label, which lives in the UI's resources.
 */
data class LicenseInfo(
    val id: String,
    val name: String?,
    val licenseName: String,
    val files: ImmutableList<LicenseFile> = persistentListOf(),
    val url: String? = null,
)

private const val APACHE_2 = "Apache License 2.0"
private const val OFL_1_1 = "SIL Open Font License 1.1"
private const val CC_BY_4 = "CC BY 4.0"
private val apacheText = persistentListOf(LicenseFile("apache-2.0.txt"))
private val ccByText = persistentListOf(LicenseFile("cc-by-4.0.txt"))

/** Hand-maintained: add an entry (and its text under `assets/licenses/`) when a dependency, font or data source is added. */
object LicenseCatalog {
    val libraries: ImmutableList<LicenseInfo> = persistentListOf(
        LicenseInfo("androidx", "AndroidX, Jetpack Compose and Material Icons", APACHE_2, apacheText),
        LicenseInfo("kotlin", "Kotlin, Coroutines and kotlinx libraries", APACHE_2, apacheText),
        LicenseInfo("dagger", "Dagger and Hilt", APACHE_2, apacheText),
        LicenseInfo(
            id = "play-billing",
            name = "Google Play Billing Library",
            licenseName = "Android Software Development Kit License",
            url = "https://developer.android.com/studio/terms",
        ),
    )

    val fonts: LicenseInfo = LicenseInfo(
        id = "fonts",
        name = null,
        licenseName = OFL_1_1,
        files = persistentListOf(
            LicenseFile("ofl-inter.txt", "Inter"),
            LicenseFile("ofl-lato.txt", "Lato"),
            LicenseFile("ofl-manrope.txt", "Manrope"),
            LicenseFile("ofl-montserrat.txt", "Montserrat"),
            LicenseFile("ofl-noto-sans.txt", "Noto Sans"),
            LicenseFile("ofl-poppins.txt", "Poppins"),
            LicenseFile("ofl-roboto-flex.txt", "Roboto Flex"),
        ),
    )

    val dataSources: ImmutableList<LicenseInfo> = persistentListOf(
        LicenseInfo("met-norway", "Weather data, MET Norway", CC_BY_4, ccByText),
        LicenseInfo("geonames", "City names, GeoNames", CC_BY_4, ccByText),
    )

    val librariesAndFonts: ImmutableList<LicenseInfo> = (libraries + fonts).toImmutableList()

    private val all = librariesAndFonts + dataSources

    fun find(id: String): LicenseInfo? = all.firstOrNull { it.id == id }
}
