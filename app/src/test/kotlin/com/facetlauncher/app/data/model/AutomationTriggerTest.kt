package com.facetlauncher.app.data.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDateTime

class AutomationTriggerTest {

    /** A moment on the given weekday, in a fixed week (the first Monday is 2026-10-05). */
    private fun at(day: DayOfWeek, hour: Int, minute: Int, second: Int = 0): LocalDateTime =
        LocalDateTime.of(2026, 10, 5, hour, minute, second).plusDays((day.value - DayOfWeek.MONDAY.value).toLong())

    private fun schedule(startMinute: Int, endMinute: Int, vararg days: DayOfWeek) =
        AutomationTrigger.Schedule(days.toSet(), startMinute, endMinute)

    private fun minutes(hour: Int, minute: Int) = hour * 60 + minute

    @Test
    fun `a window starts at the first second of its start minute`() {
        val window = schedule(minutes(9, 0), minutes(18, 0), DayOfWeek.MONDAY)

        assertFalse(window.isActiveAt(at(DayOfWeek.MONDAY, 8, 59, 59)))
        assertTrue(window.isActiveAt(at(DayOfWeek.MONDAY, 9, 0, 0)))
    }

    @Test
    fun `a window ends after the last second of its end minute`() {
        val window = schedule(minutes(9, 0), minutes(18, 0), DayOfWeek.MONDAY)

        assertTrue(window.isActiveAt(at(DayOfWeek.MONDAY, 18, 0, 59)))
        assertFalse(window.isActiveAt(at(DayOfWeek.MONDAY, 18, 1, 0)))
    }

    @Test
    fun `a start equal to the end is exactly one minute`() {
        val window = schedule(minutes(9, 0), minutes(9, 0), DayOfWeek.MONDAY)

        assertFalse(window.isActiveAt(at(DayOfWeek.MONDAY, 8, 59, 59)))
        assertTrue(window.isActiveAt(at(DayOfWeek.MONDAY, 9, 0, 0)))
        assertTrue(window.isActiveAt(at(DayOfWeek.MONDAY, 9, 0, 59)))
        assertFalse(window.isActiveAt(at(DayOfWeek.MONDAY, 9, 1, 0)))
    }

    @Test
    fun `only the selected days are active`() {
        val window = schedule(minutes(9, 0), minutes(18, 0), DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY)

        assertTrue(window.isActiveAt(at(DayOfWeek.WEDNESDAY, 12, 0)))
        assertFalse(window.isActiveAt(at(DayOfWeek.TUESDAY, 12, 0)))
    }

    @Test
    fun `an end before the start runs overnight and belongs to the day it starts`() {
        // Friday 22:00 to Saturday 06:00 — only Friday is selected
        val window = schedule(minutes(22, 0), minutes(6, 0), DayOfWeek.FRIDAY)

        assertFalse(window.isActiveAt(at(DayOfWeek.FRIDAY, 21, 59, 59)))
        assertTrue(window.isActiveAt(at(DayOfWeek.FRIDAY, 22, 0)))
        assertTrue(window.isActiveAt(at(DayOfWeek.SATURDAY, 5, 59, 59)))
        assertTrue(window.isActiveAt(at(DayOfWeek.SATURDAY, 6, 0, 59)))
        assertFalse(window.isActiveAt(at(DayOfWeek.SATURDAY, 6, 1, 0)))
        // Saturday evening is not part of Friday's window, and Thursday night is not either
        assertFalse(window.isActiveAt(at(DayOfWeek.SATURDAY, 22, 0)))
        assertFalse(window.isActiveAt(at(DayOfWeek.THURSDAY, 23, 0)))
    }

    @Test
    fun `an overnight window wraps from Sunday into Monday`() {
        val window = schedule(minutes(22, 0), minutes(6, 0), DayOfWeek.SUNDAY)

        assertTrue(window.isActiveAt(at(DayOfWeek.MONDAY, 3, 0)))
        assertFalse(window.isActiveAt(at(DayOfWeek.MONDAY, 22, 0)))
    }

    @Test
    fun `midnight to the last minute of the day covers the whole selected day`() {
        val window = schedule(0, minutes(23, 59), DayOfWeek.MONDAY)

        assertTrue(window.isActiveAt(at(DayOfWeek.MONDAY, 0, 0, 0)))
        assertTrue(window.isActiveAt(at(DayOfWeek.MONDAY, 23, 59, 59)))
        assertFalse(window.isActiveAt(at(DayOfWeek.TUESDAY, 0, 0, 0)))
    }

    private fun level(direction: BatteryDirection, percent: Int) = BatteryLevelCondition(direction, percent)

    @Test
    fun `below is strict`() {
        val below20 = level(BatteryDirection.BELOW, 20)

        assertTrue(below20.isMetBy(19))
        assertFalse(below20.isMetBy(20))
        assertFalse(below20.isMetBy(21))
    }

    @Test
    fun `above is strict`() {
        val above80 = level(BatteryDirection.ABOVE, 80)

        assertTrue(above80.isMetBy(81))
        assertFalse(above80.isMetBy(80))
        assertFalse(above80.isMetBy(79))
    }

    @Test
    fun `below 5 covers 1 to 4 percent and below 100 means not full`() {
        assertTrue(level(BatteryDirection.BELOW, 5).isMetBy(4))
        assertFalse(level(BatteryDirection.BELOW, 5).isMetBy(5))
        assertTrue(level(BatteryDirection.BELOW, 100).isMetBy(99))
        assertFalse(level(BatteryDirection.BELOW, 100).isMetBy(100))
    }

    @Test
    fun `a level check works while not charging`() {
        val notChargingBelow20 = AutomationTrigger.Battery(whileCharging = false, level = level(BatteryDirection.BELOW, 20))

        assertTrue(notChargingBelow20.isMetBy(charging = false, levelPercent = 15))
        assertFalse(notChargingBelow20.isMetBy(charging = false, levelPercent = 25))
        assertFalse(notChargingBelow20.isMetBy(charging = true, levelPercent = 15))
    }

    @Test
    fun `a level check works while charging too`() {
        val chargingAbove80 = AutomationTrigger.Battery(whileCharging = true, level = level(BatteryDirection.ABOVE, 80))

        assertTrue(chargingAbove80.isMetBy(charging = true, levelPercent = 85))
        assertFalse(chargingAbove80.isMetBy(charging = true, levelPercent = 70))
        assertFalse(chargingAbove80.isMetBy(charging = false, levelPercent = 85))
    }

    @Test
    fun `charging below 100 means charging and not yet full`() {
        val chargingNotFull = AutomationTrigger.Battery(whileCharging = true, level = level(BatteryDirection.BELOW, 100))

        assertTrue(chargingNotFull.isMetBy(charging = true, levelPercent = 99))
        assertFalse(chargingNotFull.isMetBy(charging = true, levelPercent = 100))
    }

    @Test
    fun `a schedule with no days is never active`() {
        val window = AutomationTrigger.Schedule(emptySet(), minutes(0, 0), minutes(23, 59))

        assertFalse(window.isActiveAt(at(DayOfWeek.MONDAY, 12, 0)))
    }
}
