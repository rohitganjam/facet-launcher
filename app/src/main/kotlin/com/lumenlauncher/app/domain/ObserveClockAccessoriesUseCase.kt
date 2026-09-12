package com.lumenlauncher.app.domain

import com.lumenlauncher.app.data.BatteryRepository
import com.lumenlauncher.app.data.NextAlarmRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** The clock's next-alarm/battery accessory row state — see `ui/home/clock/ClockAccessoryRow.kt`. */
data class ClockAccessoryState(
    val nextAlarmMillis: Long?,
    val batteryPercent: Int,
    val isCharging: Boolean,
)

/**
 * Combines [BatteryRepository] and [NextAlarmRepository] into the clock's accessory row state —
 * spans more than one repository, so it's a UseCase rather than being called directly from
 * [com.lumenlauncher.app.ui.home.HomeViewModel].
 */
class ObserveClockAccessoriesUseCase @Inject constructor(
    private val batteryRepository: BatteryRepository,
    private val nextAlarmRepository: NextAlarmRepository,
) {
    operator fun invoke(): Flow<ClockAccessoryState> =
        combine(batteryRepository.observeBatteryStatus(), nextAlarmRepository.observeNextAlarmMillis()) { battery, alarmMillis ->
            ClockAccessoryState(
                nextAlarmMillis = alarmMillis,
                batteryPercent = battery.percent,
                isCharging = battery.isCharging,
            )
        }
}
