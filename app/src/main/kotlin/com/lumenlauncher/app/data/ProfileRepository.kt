package com.lumenlauncher.app.data

import com.lumenlauncher.app.data.local.ProfileDao
import com.lumenlauncher.app.data.local.ProfileEntity
import com.lumenlauncher.app.data.model.ClockColorOption
import com.lumenlauncher.app.data.model.ClockFontOption
import com.lumenlauncher.app.data.model.ClockTemplateId
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

    suspend fun setUse24HourTime(profile: ProfileEntity, enabled: Boolean) {
        profileDao.upsert(profile.copy(use24HourTime = enabled))
    }

    suspend fun setClockShowMeridiem(profile: ProfileEntity, enabled: Boolean) {
        profileDao.upsert(profile.copy(clockShowMeridiem = enabled))
    }

    /**
     * Seeds the profile's clock settings from effective values and toggles the override flag.
     * Atomic upsert to avoid clobbering.
     */
    suspend fun updateOverridingClock(
        profile: ProfileEntity,
        overriding: Boolean,
        templateId: ClockTemplateId,
        fontOption: ClockFontOption,
        colorOption: ClockColorOption,
        use24HourTime: Boolean,
        showMeridiem: Boolean,
    ) {
        profileDao.upsert(
            profile.copy(
                overrideClock = overriding,
                clockTemplateId = templateId,
                clockFontOption = fontOption,
                clockColorOption = colorOption,
                use24HourTime = use24HourTime,
                clockShowMeridiem = showMeridiem,
            ),
        )
    }

    /** Reverts this profile to inheriting the global default or switches to overriding. */
    suspend fun setOverrideApps(profile: ProfileEntity, overriding: Boolean) {
        profileDao.upsert(profile.copy(overrideApps = overriding))
    }

    suspend fun setListContentMode(profile: ProfileEntity, mode: ListContentMode) {
        profileDao.upsert(profile.copy(listContentMode = mode))
    }

    /** Coerced into README's `3d` 4…8 range. */
    suspend fun setAppsToShowCount(profile: ProfileEntity, count: Int) {
        profileDao.upsert(profile.copy(appsToShowCount = count.coerceIn(4, 8)))
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
        mode: ListContentMode,
        count: Int,
        overridingFavorites: Boolean,
    ) {
        profileDao.upsert(
            profile.copy(
                overrideApps = overriding,
                listContentMode = mode,
                appsToShowCount = count.coerceIn(4, 8),
                overridingFavorites = overridingFavorites,
            ),
        )
    }

    /** Reverts this profile to inheriting the global default or switches to overriding. */
    suspend fun setOverrideCalendar(profile: ProfileEntity, overriding: Boolean) {
        profileDao.upsert(profile.copy(overrideCalendar = overriding))
    }

    suspend fun setShowAllDayEvents(profile: ProfileEntity, enabled: Boolean) {
        profileDao.upsert(profile.copy(showAllDayEvents = enabled))
    }

    /** Seeds and toggles calendar override. */
    suspend fun updateOverridingCalendar(
        profile: ProfileEntity,
        overriding: Boolean,
        showAllDayEvents: Boolean,
    ) {
        profileDao.upsert(
            profile.copy(
                overrideCalendar = overriding,
                showAllDayEvents = showAllDayEvents,
            ),
        )
    }

    suspend fun deleteProfile(profile: ProfileEntity) {
        profileDao.delete(profile)
    }

    suspend fun reorderProfiles(orderedProfiles: List<ProfileEntity>) {
        orderedProfiles.forEachIndexed { index, profile ->
            profileDao.upsert(profile.copy(position = index))
        }
    }
}
