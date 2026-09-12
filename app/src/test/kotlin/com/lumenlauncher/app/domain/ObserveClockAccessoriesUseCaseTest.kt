package com.lumenlauncher.app.domain

import com.lumenlauncher.app.data.BatteryRepository
import com.lumenlauncher.app.data.NextAlarmRepository
import com.lumenlauncher.app.data.model.BatteryStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class ObserveClockAccessoriesUseCaseTest {

    private val batteryFlow = MutableStateFlow(BatteryStatus(percent = 64, isCharging = false))
    private val batteryRepository = mock(BatteryRepository::class.java)
        .also { `when`(it.observeBatteryStatus()).thenReturn(batteryFlow) }
    private val nextAlarmFlow = MutableStateFlow<Long?>(null)
    private val nextAlarmRepository = mock(NextAlarmRepository::class.java)
        .also { `when`(it.observeNextAlarmMillis()).thenReturn(nextAlarmFlow) }

    private val useCase = ObserveClockAccessoriesUseCase(batteryRepository, nextAlarmRepository)

    @Test
    fun `combines the current battery and next-alarm readings`() = runTest {
        // Given a battery reading and no alarm set
        // When observing accessories
        val result = useCase().first()

        // Then the combined state reflects both, unmodified
        assertEquals(ClockAccessoryState(nextAlarmMillis = null, batteryPercent = 64, isCharging = false), result)
    }

    @Test
    fun `reflects a set next alarm`() = runTest {
        // Given a next alarm is set
        nextAlarmFlow.value = 999_000L

        // When observing accessories
        val result = useCase().first()

        // Then it's carried through into the combined state
        assertEquals(999_000L, result.nextAlarmMillis)
    }

    @Test
    fun `reflects charging state`() = runTest {
        // Given the device is charging at a low level
        batteryFlow.value = BatteryStatus(percent = 15, isCharging = true)

        // When observing accessories
        val result = useCase().first()

        // Then both the percent and charging flag are carried through
        assertEquals(15, result.batteryPercent)
        assertEquals(true, result.isCharging)
    }
}
