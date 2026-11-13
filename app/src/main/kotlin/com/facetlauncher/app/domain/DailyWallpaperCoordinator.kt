package com.facetlauncher.app.domain

import com.facetlauncher.app.data.DeviceConditionsRepository
import com.facetlauncher.app.data.WallpaperCopyRepository
import com.facetlauncher.app.data.dailyphotos.DailyWallpaperRepository
import com.facetlauncher.app.data.di.ApplicationScope
import com.facetlauncher.app.data.di.IoDispatcher
import com.facetlauncher.app.data.model.DailyPhotoSource
import com.facetlauncher.app.data.model.DailyWallpaperState
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Runs the daily wallpaper change in the application's scope, so leaving Home or switching apps never cancels it: a run starts when the
 * launcher resumes ([onLauncherResumed]) and the prepared photo is set at the next screen-off. One run or apply at a time. Also the one
 * place its settings change, since each change decides whether to run again.
 */
@Singleton
class DailyWallpaperCoordinator @Inject constructor(
    private val runDailyWallpaper: RunDailyWallpaperUseCase,
    private val applyPendingWallpaper: ApplyPendingWallpaperUseCase,
    private val dailyWallpaperRepository: DailyWallpaperRepository,
    private val wallpaperCopyRepository: WallpaperCopyRepository,
    private val deviceConditions: DeviceConditionsRepository,
    @ApplicationScope private val scope: CoroutineScope,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    private val mutex = Mutex()
    private var started = false
    private var runJob: Job? = null

    /** Starts listening for screen-off; call once, when the launcher starts. */
    fun start() {
        if (started) return
        started = true
        scope.launch(ioDispatcher) {
            deviceConditions.screenOffEvents().collect { scope.launch(ioDispatcher) { mutex.withLock { applyPendingWallpaper() } } }
        }
    }

    /** The launcher came to the front: the moment a day's change may start. Cheap when there is nothing to do (one DataStore read). */
    fun onLauncherResumed() = startRun(useMobileData = false)

    fun turnOn(source: DailyPhotoSource, includeLock: Boolean, useMobileData: Boolean) = change(runAfter = true, useMobileData = useMobileData) {
        // No baseline yet: whatever is set now is not taken for a change made elsewhere.
        it.copy(
            enabled = true, source = source, includeLock = includeLock, paused = false, checkedDay = null, pending = null, applying = false,
            systemId = DailyWallpaperState.NO_ID, lockId = DailyWallpaperState.NO_ID, problem = null,
        )
    }

    fun turnOff() = change(runAfter = false) { it.copy(enabled = false, paused = false, pending = null, applying = false, problem = null) }

    /** Another source: today's photo comes from it instead, so the day starts over (the attempts already made still count). */
    fun setSource(source: DailyPhotoSource) = change(runAfter = true) { if (it.source == source) it else it.copy(source = source, checkedDay = null, pending = null) }

    fun setWifiOnly(wifiOnly: Boolean) = change(runAfter = !wifiOnly) { it.copy(wifiOnly = wifiOnly) }

    fun setIncludeLock(includeLock: Boolean) = change(runAfter = false) {
        it.copy(includeLock = includeLock, lockId = wallpaperCopyRepository.currentLockWallpaperId())
    }

    /** After a pause: what is set now is the new baseline, and today's photo is fetched again. */
    fun resume() = change(runAfter = true) {
        it.copy(
            paused = false, checkedDay = null, systemId = wallpaperCopyRepository.currentSystemWallpaperId(),
            lockId = wallpaperCopyRepository.currentLockWallpaperId(), problem = null,
        )
    }

    private fun change(runAfter: Boolean, useMobileData: Boolean = false, transform: (DailyWallpaperState) -> DailyWallpaperState) {
        scope.launch(ioDispatcher) {
            mutex.withLock {
                dailyWallpaperRepository.update(transform)
                if (dailyWallpaperRepository.get().pending == null) dailyWallpaperRepository.deletePending()
            }
            if (runAfter) startRun(useMobileData)
        }
    }

    @Synchronized
    private fun startRun(useMobileData: Boolean) {
        if (runJob?.isActive == true) return
        runJob = scope.launch(ioDispatcher) {
            val outcome = mutex.withLock { runDailyWallpaper(useMobileData) }
            // The screen went off while the photo was coming down: set it now rather than waiting for the next screen-off.
            if (outcome == DailyRunOutcome.READY && !deviceConditions.isScreenOn()) mutex.withLock { applyPendingWallpaper() }
        }
    }
}
