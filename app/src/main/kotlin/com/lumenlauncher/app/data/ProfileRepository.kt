package com.lumenlauncher.app.data

import com.lumenlauncher.app.data.local.ProfileDao
import com.lumenlauncher.app.data.local.ProfileEntity
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

    /** `null` reverts this profile to inheriting the global default. */
    suspend fun setUse24HourTimeOverride(profile: ProfileEntity, override: Boolean?) {
        profileDao.upsert(profile.copy(use24HourTimeOverride = override))
    }

    /** `null` reverts this profile to inheriting the global default. */
    suspend fun setShowAllDayEventsOverride(profile: ProfileEntity, override: Boolean?) {
        profileDao.upsert(profile.copy(showAllDayEventsOverride = override))
    }

    /** `null` reverts this profile to inheriting the global default. */
    suspend fun setListContentModeOverride(profile: ProfileEntity, override: ListContentMode?) {
        profileDao.upsert(profile.copy(listContentModeOverride = override))
    }

    /** `null` reverts this profile to inheriting the global default; otherwise coerced into README's `3d` 4…8 range. */
    suspend fun setAppsToShowCountOverride(profile: ProfileEntity, override: Int?) {
        profileDao.upsert(profile.copy(appsToShowCountOverride = override?.coerceIn(4, 8)))
    }

    suspend fun setOverridingFavorites(profile: ProfileEntity, overriding: Boolean) {
        profileDao.upsert(profile.copy(overridingFavorites = overriding))
    }

    /**
     * The Apps card's single Inherit/Override switch — sets list content mode, apps-to-show,
     * and the favorites-override flag together in **one** [ProfileDao.upsert] call. Deliberately
     * not three separate calls to the setters above: each of those does a full-row `INSERT OR
     * REPLACE` from whatever [ProfileEntity] snapshot it's handed, so three sequential calls
     * built from the *same* (now-stale-after-the-first-write) snapshot silently clobber each
     * other — every call after the first undoes the previous one's field change, leaving only
     * the last call's own field actually persisted. That was a real, shipped bug: switching to
     * Override looked like it did nothing because the mode-override write was immediately wiped
     * out by the very next (apps-to-show) write built from the same stale profile.
     */
    suspend fun setOverridingApps(profile: ProfileEntity, overriding: Boolean, effectiveMode: ListContentMode, effectiveAppsToShowCount: Int) {
        profileDao.upsert(
            profile.copy(
                listContentModeOverride = if (overriding) effectiveMode else null,
                appsToShowCountOverride = if (overriding) effectiveAppsToShowCount.coerceIn(4, 8) else null,
                overridingFavorites = overriding,
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
