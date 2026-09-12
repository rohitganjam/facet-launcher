package com.facetlauncher.app.ui.home.clock

import org.junit.Assert.assertEquals
import org.junit.Test

class ClockAccessoryIconsTest {

    @Test
    fun `charging wins regardless of percent`() {
        // Given the battery is charging but also low
        // When resolving which icon look to draw
        val state = batteryIconState(percent = 5, isCharging = true)

        // Then charging takes precedence over the low tier
        assertEquals(BatteryIconState.CHARGING, state)
    }

    @Test
    fun `charging wins even at a full percent`() {
        // Given the battery is charging and already full
        val state = batteryIconState(percent = 100, isCharging = true)

        // Then the charging look is still used, not the full look
        assertEquals(BatteryIconState.CHARGING, state)
    }

    @Test
    fun `low below the low threshold when not charging`() {
        // Given the battery is low and not charging
        val state = batteryIconState(percent = 19, isCharging = false)

        // Then the low (sliver) look is used
        assertEquals(BatteryIconState.LOW, state)
    }

    @Test
    fun `partial at the low threshold when not charging`() {
        // Given the battery is exactly at the low/partial boundary
        val state = batteryIconState(percent = 20, isCharging = false)

        // Then the boundary itself already reads as partial, not low
        assertEquals(BatteryIconState.PARTIAL, state)
    }

    @Test
    fun `partial below the full threshold when not charging`() {
        // Given the battery is comfortably mid-range
        val state = batteryIconState(percent = 79, isCharging = false)

        // Then the partial look is used
        assertEquals(BatteryIconState.PARTIAL, state)
    }

    @Test
    fun `full at the full threshold when not charging`() {
        // Given the battery is exactly at the partial/full boundary
        val state = batteryIconState(percent = 80, isCharging = false)

        // Then the boundary itself already reads as full
        assertEquals(BatteryIconState.FULL, state)
    }

    @Test
    fun `full above the full threshold when not charging`() {
        // Given the battery is nearly topped up
        val state = batteryIconState(percent = 100, isCharging = false)

        // Then the full look is used
        assertEquals(BatteryIconState.FULL, state)
    }
}
