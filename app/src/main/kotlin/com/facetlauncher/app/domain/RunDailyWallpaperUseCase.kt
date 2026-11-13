package com.facetlauncher.app.domain

import com.facetlauncher.app.data.DeviceConditionsRepository
import com.facetlauncher.app.data.WallpaperCopyRepository
import com.facetlauncher.app.data.dailyphotos.DailyPhotoFeed
import com.facetlauncher.app.data.dailyphotos.DailyPhotoRepository
import com.facetlauncher.app.data.dailyphotos.DailyWallpaperRepository
import com.facetlauncher.app.data.model.DailyPhoto
import com.facetlauncher.app.data.model.DailyPhotoSource
import com.facetlauncher.app.data.model.DailyWallpaperPhoto
import com.facetlauncher.app.data.model.DailyWallpaperProblem
import com.facetlauncher.app.data.model.DailyWallpaperState
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

/** How a run of the daily change ended. */
enum class DailyRunOutcome {
    OFF,
    PAUSED,

    /** Today's work is already done. */
    DONE_TODAY,

    /** A photo is prepared and waits for screen-off. */
    READY,

    /** Conditions are not met (network, Battery Saver); the next launcher resume checks again. */
    WAITING,

    /** The source has nothing newer than what is set, or every new photo was too small; done for today. */
    NOTHING_NEW,

    /** The feed or the download failed; counts as one of the day's attempts. */
    FAILED,

    /** The day's attempts are used up. */
    GAVE_UP,
}

/**
 * One run of the daily change, started when the launcher comes to the front: checks the conditions, picks the newest photo from the chosen
 * source that hasn't been set, downloads it, decodes only its centre strip in the screen's shape, and stores it as the pending photo.
 * Setting it is [ApplyPendingWallpaperUseCase]'s job, at screen-off. Nothing here shows UI or runs on the main thread.
 */
class RunDailyWallpaperUseCase @Inject constructor(
    private val dailyWallpaperRepository: DailyWallpaperRepository,
    private val dailyPhotoRepository: DailyPhotoRepository,
    private val wallpaperCopyRepository: WallpaperCopyRepository,
    private val deviceConditions: DeviceConditionsRepository,
    private val clock: Clock,
) {
    /** [useMobileData] lets this one run use a metered network although Wi-Fi only is on (the user asked for it when turning the change on). */
    suspend operator fun invoke(useMobileData: Boolean = false): DailyRunOutcome {
        val today = LocalDate.now(clock)
        val state = settleInterruptedChange(dailyWallpaperRepository.get())
        gate(state, today, useMobileData)?.let { return it }
        dailyWallpaperRepository.update { it.copy(attemptsDay = today, attempts = it.attemptsOn(today) + 1) }

        // Photos saved on an earlier day are not today's: treat the refresh as failed, so a later resume tries again.
        val photos = (dailyPhotoRepository.photos(state.source) as? DailyPhotoFeed.Photos)?.takeUnless { it.savedEarlier }?.photos
            ?: return failed(DailyWallpaperProblem.SOURCE_UNAVAILABLE)
        val candidates = candidates(photos, state)
        return if (candidates.isEmpty()) nothingNew(today, problem = null) else prepare(candidates, today)
    }

    /**
     * A change of its own that was interrupted after its photo was dropped: what is set now is the baseline, not the user's change. With
     * the photo still pending, [ApplyPendingWallpaperUseCase] finishes the change instead.
     */
    private suspend fun settleInterruptedChange(state: DailyWallpaperState): DailyWallpaperState {
        if (!state.applying || state.pending != null) return state
        val systemId = wallpaperCopyRepository.currentSystemWallpaperId()
        val lockId = wallpaperCopyRepository.currentLockWallpaperId()
        dailyWallpaperRepository.update { it.copy(applying = false, systemId = systemId, lockId = lockId) }
        return dailyWallpaperRepository.get()
    }

    /** Why this run stops before touching the network, or `null` to go on. */
    private suspend fun gate(state: DailyWallpaperState, today: LocalDate, useMobileData: Boolean): DailyRunOutcome? {
        val outcome = when {
            !state.enabled -> DailyRunOutcome.OFF
            state.paused -> DailyRunOutcome.PAUSED
            state.changedElsewhere(wallpaperCopyRepository.currentSystemWallpaperId(), wallpaperCopyRepository.currentLockWallpaperId()) -> {
                dailyWallpaperRepository.update { it.copy(paused = true, problem = null) }
                DailyRunOutcome.PAUSED
            }
            state.pending != null -> DailyRunOutcome.READY
            state.checkedDay == today -> DailyRunOutcome.DONE_TODAY
            state.attemptsOn(today) >= DailyWallpaperState.MAX_ATTEMPTS_PER_DAY -> DailyRunOutcome.GAVE_UP
            else -> null
        }
        if (outcome != null) return outcome
        val problem = waitingFor(state, useMobileData) ?: return null
        dailyWallpaperRepository.update { it.copy(problem = problem) }
        return DailyRunOutcome.WAITING
    }

    /** Downloads the best candidate, decodes its centre strip and stores it as the pending photo; a photo too small to look sharp is skipped. */
    private suspend fun prepare(candidates: List<DailyPhoto>, today: LocalDate): DailyRunOutcome {
        val (screenWidth, screenHeight) = wallpaperCopyRepository.portraitScreenSize()
        val skipped = mutableListOf<String>()
        for (photo in candidates.take(MAX_CANDIDATES)) {
            val bytes = dailyPhotoRepository.originalBytes(photo) ?: return failed(DailyWallpaperProblem.DOWNLOAD_FAILED)
            val crop = wallpaperCopyRepository.imageSize(bytes)?.let { (width, height) -> portraitCentreCrop(width, height, screenWidth, screenHeight) }
            val strip = crop?.let { wallpaperCopyRepository.decodeRegion(bytes, it.region, it.sampleSize) }
            if (strip == null) {
                skipped += photo.imageUrl
                continue
            }
            val saved = dailyWallpaperRepository.savePending(strip)
            strip.recycle()
            if (!saved) return failed(DailyWallpaperProblem.DOWNLOAD_FAILED)
            dailyWallpaperRepository.update {
                it.copy(
                    pending = DailyWallpaperPhoto(photo.imageUrl, photo.title, photo.credit),
                    checkedDay = today,
                    history = (skipped + it.history).distinct(),
                    problem = if (skipped.isEmpty()) null else DailyWallpaperProblem.TOO_SMALL,
                )
            }
            return DailyRunOutcome.READY
        }
        dailyWallpaperRepository.update { it.copy(history = (skipped + it.history).distinct()) }
        return nothingNew(today, DailyWallpaperProblem.TOO_SMALL)
    }

    private fun waitingFor(state: DailyWallpaperState, useMobileData: Boolean): DailyWallpaperProblem? {
        if (deviceConditions.isPowerSaveMode()) return DailyWallpaperProblem.BATTERY_SAVER
        val network = deviceConditions.network()
        return when {
            !network.connected -> DailyWallpaperProblem.NO_CONNECTION
            useMobileData -> null
            state.wifiOnly && !network.unmetered -> DailyWallpaperProblem.WAITING_FOR_WIFI
            !network.unmetered && network.roaming -> DailyWallpaperProblem.NO_CONNECTION
            else -> null
        }
    }

    /**
     * The photos worth trying, best first. Bing: the newest by date, unless it is the one already set (then nothing is new). Spotlight has no
     * dates, so its photos in feed order that haven't been set recently.
     */
    private fun candidates(photos: List<DailyPhoto>, state: DailyWallpaperState): List<DailyPhoto> = when (state.source) {
        DailyPhotoSource.BING -> {
            val newestFirst = photos.sortedByDescending { it.date.orEmpty() }
            if (newestFirst.firstOrNull()?.imageUrl == state.current?.imageUrl) emptyList() else newestFirst.filter { it.imageUrl !in state.history }
        }
        DailyPhotoSource.SPOTLIGHT -> photos.filter { it.imageUrl !in state.history && it.imageUrl != state.current?.imageUrl }
    }

    private suspend fun failed(problem: DailyWallpaperProblem): DailyRunOutcome {
        dailyWallpaperRepository.update { it.copy(problem = problem) }
        return DailyRunOutcome.FAILED
    }

    private suspend fun nothingNew(today: LocalDate, problem: DailyWallpaperProblem?): DailyRunOutcome {
        dailyWallpaperRepository.update { it.copy(checkedDay = today, problem = problem) }
        return DailyRunOutcome.NOTHING_NEW
    }

    private companion object {
        /** At most this many originals are downloaded in one run when the newest ones are too small. */
        const val MAX_CANDIDATES = 3
    }
}

/**
 * `true` when the wallpaper the daily change last set has since been replaced by someone else. Nothing to compare before the first change,
 * and not while a change of its own was interrupted ([DailyWallpaperState.applying]).
 */
internal fun DailyWallpaperState.changedElsewhere(currentSystemId: Int, currentLockId: Int): Boolean =
    systemId != DailyWallpaperState.NO_ID && !applying && (currentSystemId != systemId || (includeLock && currentLockId != lockId))
