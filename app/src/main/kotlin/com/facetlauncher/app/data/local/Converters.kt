package com.facetlauncher.app.data.local

import androidx.room.TypeConverter
import com.facetlauncher.app.data.model.AppProfile
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.ClockFontOption
import com.facetlauncher.app.data.model.ClockTemplateId
import com.facetlauncher.app.data.model.FontWeightOption
import com.facetlauncher.app.data.model.ListContentMode

/**
 * Room type converters for every enum column on [FacetEntity] — all currently non-null Kotlin
 * properties with their own default. Every `to*` converter falls back to that same default rather
 * than returning null when a stored string doesn't match any current enum constant (`valueOf`
 * throwing on a renamed/removed constant, e.g. `ClockColorOption`'s old `INK`/`WHITE`/`BLACK`
 * still sitting in an existing install's database after that rename — see chat history). This
 * mirrors [com.facetlauncher.app.data.SettingsRepository]'s own `?: defaults.xxx` fallback for its
 * DataStore-backed reads. Returning null here instead — as this file used to, on the mistaken
 * assumption every one of these needed to match some nullable column's type exactly — is fatal:
 * Room's generated code for a NOT NULL column has no fallback of its own and crashes outright
 * (`IllegalStateException: Expected NON-NULL '...', but it was NULL`) the moment a converter
 * legitimately returns null for it, which is exactly what happened here.
 */
class Converters {
    @TypeConverter
    fun fromListContentMode(mode: ListContentMode): String = mode.name

    @TypeConverter
    fun toListContentMode(value: String?): ListContentMode =
        value?.let { runCatching { ListContentMode.valueOf(it) }.getOrNull() } ?: ListContentMode.FAVORITES

    @TypeConverter
    fun fromClockTemplateId(id: ClockTemplateId): String = id.name

    @TypeConverter
    fun toClockTemplateId(value: String?): ClockTemplateId =
        value?.let { runCatching { ClockTemplateId.valueOf(it) }.getOrNull() } ?: ClockTemplateId.LIGHT_STACK

    @TypeConverter
    fun fromClockFontOption(option: ClockFontOption): String = option.name

    @TypeConverter
    fun toClockFontOption(value: String?): ClockFontOption =
        value?.let { runCatching { ClockFontOption.valueOf(it) }.getOrNull() } ?: ClockFontOption.LAUNCHER_DEFAULT

    @TypeConverter
    fun fromClockColorOption(option: ClockColorOption): String = option.name

    @TypeConverter
    fun toClockColorOption(value: String?): ClockColorOption =
        value?.let { runCatching { ClockColorOption.valueOf(it) }.getOrNull() } ?: ClockColorOption.THEME

    @TypeConverter
    fun fromFontWeightOption(option: FontWeightOption): String = option.name

    @TypeConverter
    fun toFontWeightOption(value: String?): FontWeightOption =
        value?.let { runCatching { FontWeightOption.valueOf(it) }.getOrNull() } ?: FontWeightOption.REGULAR

    @TypeConverter
    fun fromAppRowPosition(position: AppRowPosition): String = position.name

    @TypeConverter
    fun toAppRowPosition(value: String?): AppRowPosition =
        value?.let { runCatching { AppRowPosition.valueOf(it) }.getOrNull() } ?: AppRowPosition.LEFT

    @TypeConverter
    fun fromAppRowPresentation(presentation: AppRowPresentation): String = presentation.name

    @TypeConverter
    fun toAppRowPresentation(value: String?): AppRowPresentation =
        value?.let { runCatching { AppRowPresentation.valueOf(it) }.getOrNull() } ?: AppRowPresentation.ICON_AND_TEXT

    /** An unrecognized/corrupted value defaults to [AppProfile.PERSONAL], never [AppProfile.WORK] — the safer failure direction. */
    @TypeConverter
    fun fromAppProfile(profile: AppProfile): String = profile.name

    @TypeConverter
    fun toAppProfile(value: String?): AppProfile =
        value?.let { runCatching { AppProfile.valueOf(it) }.getOrNull() } ?: AppProfile.PERSONAL
}
