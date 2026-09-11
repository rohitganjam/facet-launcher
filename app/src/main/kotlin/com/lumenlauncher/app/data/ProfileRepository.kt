package com.lumenlauncher.app.data

import com.lumenlauncher.app.data.local.ProfileDao
import com.lumenlauncher.app.data.local.ProfileEntity
import com.lumenlauncher.app.data.model.AppListLimits
import com.lumenlauncher.app.data.model.AppListVerticalAlignment
import com.lumenlauncher.app.data.model.AppRowPosition
import com.lumenlauncher.app.data.model.AppRowPresentation
import com.lumenlauncher.app.data.model.ClockAlignment
import com.lumenlauncher.app.data.model.ClockColorOption
import com.lumenlauncher.app.data.model.ClockDateStyle
import com.lumenlauncher.app.data.model.ClockFontOption
import com.lumenlauncher.app.data.model.ClockTemplateId
import com.lumenlauncher.app.data.model.DockDisplayMode
import com.lumenlauncher.app.data.model.FontWeightOption
import com.lumenlauncher.app.data.model.ListContentMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wraps [ProfileDao]. The launcher starts with exactly one profile (seeded by
 * [com.lumenlauncher.app.domain.EnsureActiveProfileUseCase]) and allows up to [MAX_PROFILES];
 * the last remaining profile can never be deleted ([MIN_PROFILES]).
 */
@Singleton
class ProfileRepository @Inject constructor(private val profileDao: ProfileDao) {

    companion object {
        const val MIN_PROFILES = 1
        const val MAX_PROFILES = 3
        private const val DEFAULT_PROFILE_NAME_PREFIX = "Profile "
    }

    fun observeProfiles(): Flow<List<ProfileEntity>> = profileDao.observeAll()

    suspend fun getById(id: Long): ProfileEntity? = profileDao.getById(id)

    /**
     * A new profile has no per-profile settings to copy yet — it inherits the launcher-wide
     * default Favorites list (see [DefaultFavoriteAppRepository]) until it overrides them, and
     * this just picks the next `Profile N` name and position.
     */
    suspend fun addProfile(): ProfileEntity {
        val existing = profileDao.observeAll().first()
        val nextPosition = (existing.maxOfOrNull { it.position } ?: -1) + 1
        val profile = ProfileEntity(name = "$DEFAULT_PROFILE_NAME_PREFIX${existing.size + 1}", position = nextPosition)
        val id = profileDao.upsert(profile)
        return profile.copy(id = id)
    }

    suspend fun renameProfile(profile: ProfileEntity, newName: String) {
        profileDao.upsert(profile.copy(name = newName))
    }

    /** Reverts this profile to inheriting the global default or switches to overriding. */
    suspend fun setOverrideClock(profile: ProfileEntity, overriding: Boolean) {
        profileDao.upsert(profile.copy(overrideClock = overriding))
    }

    suspend fun setClockTemplateId(profile: ProfileEntity, id: ClockTemplateId) {
        profileDao.upsert(profile.copy(clockTemplateId = id))
    }

    suspend fun setClockFontOption(profile: ProfileEntity, option: ClockFontOption) {
        profileDao.upsert(profile.copy(clockFontOption = option))
    }

    suspend fun setClockColorOption(profile: ProfileEntity, option: ClockColorOption) {
        profileDao.upsert(profile.copy(clockColorOption = option))
    }

    /** See [ProfileEntity.clockAccentColorOption]. */
    suspend fun setClockAccentColorOption(profile: ProfileEntity, option: ClockColorOption) {
        profileDao.upsert(profile.copy(clockAccentColorOption = option))
    }

    suspend fun setUse24HourTime(profile: ProfileEntity, enabled: Boolean) {
        profileDao.upsert(profile.copy(use24HourTime = enabled))
    }

    /** See [ProfileEntity.clockDateStyle]. */
    suspend fun setClockDateStyle(profile: ProfileEntity, dateStyle: ClockDateStyle) {
        profileDao.upsert(profile.copy(clockDateStyle = dateStyle))
    }

    suspend fun setClockShowMeridiem(profile: ProfileEntity, enabled: Boolean) {
        profileDao.upsert(profile.copy(clockShowMeridiem = enabled))
    }

    suspend fun setCalendarFontOption(profile: ProfileEntity, option: ClockFontOption) {
        profileDao.upsert(profile.copy(calendarFontOption = option))
    }

    suspend fun setCalendarColorOption(profile: ProfileEntity, option: ClockColorOption) {
        profileDao.upsert(profile.copy(calendarColorOption = option))
    }

    suspend fun setCalendarFontWeight(profile: ProfileEntity, weight: FontWeightOption) {
        profileDao.upsert(profile.copy(calendarFontWeight = weight))
    }

    suspend fun setClockAlignment(profile: ProfileEntity, alignment: ClockAlignment) {
        profileDao.upsert(profile.copy(clockAlignment = alignment))
    }

    /** Independent of [setClockAlignment] — see [ProfileEntity.calendarAlignment]. */
    suspend fun setCalendarAlignment(profile: ProfileEntity, alignment: ClockAlignment) {
        profileDao.upsert(profile.copy(calendarAlignment = alignment))
    }

    suspend fun setClockZoneHeight(profile: ProfileEntity, heightDp: Float) {
        profileDao.upsert(profile.copy(clockZoneHeightDp = heightDp))
    }

    /** Restores this profile's clock+calendar block to its natural, undragged position. */
    suspend fun resetClockZoneHeight(profile: ProfileEntity) {
        profileDao.upsert(profile.copy(clockZoneHeightDp = null))
    }

    suspend fun setClockScale(profile: ProfileEntity, scale: Float) {
        profileDao.upsert(profile.copy(clockScale = scale))
    }

    /**
     * "Reset clock widget position" for this profile — the zone height back to `null`, BOTH
     * alignments back to `LEFT`, and scale back to `0.8f`, in one atomic upsert (avoids the
     * clobbering risk of sequential single-field upserts against the same stale snapshot).
     */
    suspend fun resetClockPosition(profile: ProfileEntity) {
        profileDao.upsert(
            profile.copy(
                clockZoneHeightDp = null,
                clockAlignment = ClockAlignment.LEFT,
                calendarAlignment = ClockAlignment.LEFT,
                clockScale = 0.8f,
            ),
        )
    }

    /**
     * Seeds the profile's clock+calendar *design* settings from effective values and toggles the
     * override flag. One toggle for the whole Home clock/calendar block — it's a single visual
     * unit, not two independent design decisions — which is why `calendarFontOption`/
     * `calendarColorOption` (and, since they moved in from being global-only, `clockAlignment`/
     * `calendarAlignment`/`clockZoneHeightDp`) are seeded here too, not by [updateOverridingCalendar]
     * (that one is calendar *selection* only — which calendars, which events — see its own doc
     * comment). Atomic upsert to avoid clobbering.
     */
    suspend fun updateOverridingClock(
        profile: ProfileEntity,
        overriding: Boolean,
        templateId: ClockTemplateId,
        fontOption: ClockFontOption,
        colorOption: ClockColorOption,
        use24HourTime: Boolean,
        showMeridiem: Boolean,
        calendarFontOption: ClockFontOption,
        calendarColorOption: ClockColorOption,
        calendarFontWeight: FontWeightOption,
        clockAlignment: ClockAlignment = profile.clockAlignment,
        calendarAlignment: ClockAlignment = profile.calendarAlignment,
        clockZoneHeightDp: Float? = profile.clockZoneHeightDp,
        clockScale: Float = profile.clockScale,
        accentColorOption: ClockColorOption = profile.clockAccentColorOption,
        dateStyle: ClockDateStyle = profile.clockDateStyle,
    ) {
        profileDao.upsert(
            profile.copy(
                overrideClock = overriding,
                clockTemplateId = templateId,
                clockFontOption = fontOption,
                clockColorOption = colorOption,
                use24HourTime = use24HourTime,
                clockShowMeridiem = showMeridiem,
                calendarFontOption = calendarFontOption,
                calendarColorOption = calendarColorOption,
                calendarFontWeight = calendarFontWeight,
                clockAlignment = clockAlignment,
                calendarAlignment = calendarAlignment,
                clockZoneHeightDp = clockZoneHeightDp,
                clockScale = clockScale,
                clockAccentColorOption = accentColorOption,
                clockDateStyle = dateStyle,
            ),
        )
    }

    /** Reverts this profile to inheriting the global default or switches to overriding. */
    suspend fun setOverrideApps(profile: ProfileEntity, overriding: Boolean) {
        profileDao.upsert(profile.copy(overrideApps = overriding))
    }

    suspend fun setAppRowPosition(profile: ProfileEntity, position: AppRowPosition) {
        profileDao.upsert(profile.copy(appRowPosition = position))
    }

    suspend fun setAppRowPresentation(profile: ProfileEntity, presentation: AppRowPresentation) {
        profileDao.upsert(profile.copy(appRowPresentation = presentation))
    }

    suspend fun setListContentMode(profile: ProfileEntity, mode: ListContentMode) {
        profileDao.upsert(profile.copy(listContentMode = mode))
    }

    /** Coerced into [AppListLimits]' range (reduced from README's original `3d` 4…8 — see chat history). */
    suspend fun setAppsToShowCount(profile: ProfileEntity, count: Int) {
        profileDao.upsert(profile.copy(appsToShowCount = count.coerceIn(AppListLimits.MIN_APPS_TO_SHOW, AppListLimits.MAX_APPS_TO_SHOW)))
    }

    suspend fun setAppListVerticalAlignment(profile: ProfileEntity, alignment: AppListVerticalAlignment) {
        profileDao.upsert(profile.copy(appListVerticalAlignment = alignment))
    }

    suspend fun setOverridingFavorites(profile: ProfileEntity, overriding: Boolean) {
        profileDao.upsert(profile.copy(overridingFavorites = overriding))
    }

    /**
     * Seeds the profile's app settings from effective values and toggles the override flags.
     * Atomic upsert to avoid clobbering.
     */
    suspend fun updateOverridingApps(
        profile: ProfileEntity,
        overriding: Boolean,
        position: AppRowPosition,
        presentation: AppRowPresentation,
        mode: ListContentMode,
        count: Int,
        overridingFavorites: Boolean,
        verticalAlignment: AppListVerticalAlignment = profile.appListVerticalAlignment,
    ) {
        profileDao.upsert(
            profile.copy(
                overrideApps = overriding,
                appRowPosition = position,
                appRowPresentation = presentation,
                listContentMode = mode,
                appsToShowCount = count.coerceIn(AppListLimits.MIN_APPS_TO_SHOW, AppListLimits.MAX_APPS_TO_SHOW),
                overridingFavorites = overridingFavorites,
                appListVerticalAlignment = verticalAlignment,
            ),
        )
    }

    suspend fun setDockDisplayMode(profile: ProfileEntity, mode: DockDisplayMode) {
        profileDao.upsert(profile.copy(dockDisplayMode = mode))
    }

    /**
     * The Dock card's single Inherit/Override switch — governs both the dock's app list (this
     * profile's own [com.lumenlauncher.app.data.local.ProfileDockAppEntity] rows) and its
     * Icons/Text display style, as one unit. Seeds the display mode from the current effective
     * value and toggles the flag in one atomic upsert; seeding the app list itself (copying the
     * default dock when the profile's own is empty) is the caller's job, mirroring
     * [updateOverridingApps]/favorites.
     */
    suspend fun updateOverridingDock(profile: ProfileEntity, overriding: Boolean, displayMode: DockDisplayMode) {
        profileDao.upsert(profile.copy(overrideDock = overriding, dockDisplayMode = displayMode))
    }

    /** Reverts this profile to inheriting the global default or switches to overriding. */
    suspend fun setOverrideCalendar(profile: ProfileEntity, overriding: Boolean) {
        profileDao.upsert(profile.copy(overrideCalendar = overriding))
    }

    suspend fun setShowAllDayEvents(profile: ProfileEntity, enabled: Boolean) {
        profileDao.upsert(profile.copy(showAllDayEvents = enabled))
    }

    suspend fun setSelectedCalendarIds(profile: ProfileEntity, ids: Set<String>?) {
        profileDao.upsert(profile.copy(selectedCalendarIdsCsv = ids.toCsv()))
    }

    /**
     * Seeds the profile's calendar *selection* (which calendars, which kinds of events — content,
     * not design) from effective values and toggles the override flag. Atomic upsert to avoid
     * clobbering. See [updateOverridingClock] for the separate, merged Clock+Calendar *design*
     * override this deliberately excludes (font/color moved there — see chat history).
     */
    suspend fun updateOverridingCalendar(
        profile: ProfileEntity,
        overriding: Boolean,
        showAllDayEvents: Boolean,
        selectedCalendarIds: Set<String>?,
    ) {
        profileDao.upsert(
            profile.copy(
                overrideCalendar = overriding,
                showAllDayEvents = showAllDayEvents,
                selectedCalendarIdsCsv = selectedCalendarIds.toCsv(),
            ),
        )
    }

    suspend fun deleteProfile(profile: ProfileEntity) {
        profileDao.delete(profile)
    }

    /** F14 Backup & Restore — wipes every profile (cascades to favorites) before restoring a backup's own list. */
    suspend fun deleteAllProfiles() {
        profileDao.deleteAll()
    }

    /** F14 Backup & Restore — inserts [profile] as a brand-new row (its own `id` is ignored, a fresh one is autogenerated) and returns that new id. */
    suspend fun restoreProfile(profile: ProfileEntity): Long = profileDao.upsert(profile.copy(id = 0))

    suspend fun reorderProfiles(orderedProfiles: List<ProfileEntity>) {
        orderedProfiles.forEachIndexed { index, profile ->
            profileDao.upsert(profile.copy(position = index))
        }
    }
}

/** Decoded view of [ProfileEntity.selectedCalendarIdsCsv] — `null` means every calendar is implicitly selected. */
val ProfileEntity.selectedCalendarIds: Set<String>?
    get() = selectedCalendarIdsCsv?.split(",")?.filter { it.isNotEmpty() }?.toSet()

private fun Set<String>?.toCsv(): String? = this?.joinToString(",")
