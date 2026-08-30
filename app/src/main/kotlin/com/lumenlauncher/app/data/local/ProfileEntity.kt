package com.lumenlauncher.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lumenlauncher.app.data.model.ListContentMode

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val position: Int,
    /** `null` = inherit the global [com.lumenlauncher.app.data.model.LauncherSettings] default; non-null = this profile's override. */
    val use24HourTimeOverride: Boolean? = null,
    val showAllDayEventsOverride: Boolean? = null,
    /** `null` = inherit the global [com.lumenlauncher.app.data.model.LauncherSettings] default (Settings → "Default apps list"); non-null = this profile's own override. */
    val listContentModeOverride: ListContentMode? = null,
    /** Only meaningful when the effective mode isn't [ListContentMode.FAVORITES]. Range 4…8 per README's `3d` spec. `null` = inherit. */
    val appsToShowCountOverride: Int? = null,
    /** `false` = this profile shows the launcher-wide default Favorites list (Settings → "Default
     * favorites"); `true` = its own list, managed in [com.lumenlauncher.app.data.FavoriteAppRepository]. */
    val overridingFavorites: Boolean = false,
)
