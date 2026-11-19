package com.facetlauncher.app.data.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.facetlauncher.app.R

/** Where the daily photos come from: Bing's photo of the day and Windows Spotlight, both from Microsoft. */
enum class DailyPhotoSource(@param:StringRes val labelRes: Int) {
    /** Bing's photo of the day for the phone's country, a week of them; every photo has a date. */
    BING(R.string.daily_source_bing),

    /** Windows Spotlight: a dozen random photos of its pool for the phone's country (titles in its language), with no dates. */
    SPOTLIGHT(R.string.daily_source_spotlight),
}

/**
 * One photo from a feed. [credit] is the feed's copyright text, kept exactly as given; [date] is `yyyy-MM-dd` for Bing and `null` for Spotlight.
 * [thumbUrl] is a 640 px preview and [imageUrl] the original, which is the one to frame.
 */
@Immutable
data class DailyPhoto(
    val title: String,
    val credit: String,
    val thumbUrl: String,
    val imageUrl: String,
    val date: String? = null,
)
