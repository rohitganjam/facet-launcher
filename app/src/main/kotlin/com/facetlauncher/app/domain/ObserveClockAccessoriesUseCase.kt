package com.facetlauncher.app.domain

import com.facetlauncher.app.data.BatteryRepository
import com.facetlauncher.app.data.NextAlarmRepository
import com.facetlauncher.app.data.model.BatteryStatus
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/** The clock's next-alarm/battery accessory row state — see `ui/home/clock/ClockAccessoryRow.kt`. */
data class ClockAccessoryState(
    val nextAlarmMillis: Long?,
    /** `null` until the first battery reading arrives, so the clock shows no battery item rather than a 0% one. */
    val batteryPercent: Int?,
    val isCharging: Boolean,
)

/**
 * Combines [BatteryRepository] and [NextAlarmRepository] into the clock's accessory row state —
 * spans more than one repository, so it's a UseCase rather than being called directly from
 * [com.facetlauncher.app.ui.home.HomeViewModel].
 */
class ObserveClockAccessoriesUseCase @Inject constructor(
    private val batteryRepository: BatteryRepository,
    private val nextAlarmRepository: NextAlarmRepository,
) {
    operator fun invoke(): Flow<ClockAccessoryState> =
        combine(batteryRepository.observeBatteryStatus().map<BatteryStatus, BatteryStatus?> { it }.onStart { emit(null) }, nextAlarmRepository.observeNextAlarmMillis()) { battery, alarmMillis ->
            ClockAccessoryState(
                nextAlarmMillis = alarmMillis,
                batteryPercent = battery?.percent,
                isCharging = battery?.isCharging ?: false,
            )
        }
}
