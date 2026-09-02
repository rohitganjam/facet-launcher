package com.lumenlauncher.app.data.local

import androidx.room.TypeConverter
import com.lumenlauncher.app.data.model.ClockColorOption
import com.lumenlauncher.app.data.model.ClockFontOption
import com.lumenlauncher.app.data.model.ClockTemplateId
import com.lumenlauncher.app.data.model.ListContentMode

/**
 * Room type converters — currently just [ProfileEntity.listContentModeOverride]'s enum. Both
 * methods are nullable in and out to match that column's actual (nullable) type exactly — a
 * converter pair declared only for the non-null [ListContentMode] silently fails to bind a
 * non-null value into a nullable column (Room can't resolve it as a match for the nullable
 * field, so every write landed as SQL NULL regardless of what was passed in — the actual cause
 * of "switching to Override never sticks" for the App list content setting).
 */
class Converters {
    @TypeConverter
    fun fromListContentMode(mode: ListContentMode?): String? = mode?.name

    @TypeConverter
    fun toListContentMode(value: String?): ListContentMode? =
        value?.let { runCatching { ListContentMode.valueOf(it) }.getOrNull() }

    @TypeConverter
    fun fromClockTemplateId(id: ClockTemplateId?): String? = id?.name

    @TypeConverter
    fun toClockTemplateId(value: String?): ClockTemplateId? =
        value?.let { runCatching { ClockTemplateId.valueOf(it) }.getOrNull() }

    @TypeConverter
    fun fromClockFontOption(option: ClockFontOption?): String? = option?.name

    @TypeConverter
    fun toClockFontOption(value: String?): ClockFontOption? =
        value?.let { runCatching { ClockFontOption.valueOf(it) }.getOrNull() }

    @TypeConverter
    fun fromClockColorOption(option: ClockColorOption?): String? = option?.name

    @TypeConverter
    fun toClockColorOption(value: String?): ClockColorOption? =
        value?.let { runCatching { ClockColorOption.valueOf(it) }.getOrNull() }
}
