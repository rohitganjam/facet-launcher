package com.facetlauncher.app.data.model

import com.facetlauncher.app.data.DeviceLocation
import java.time.DayOfWeek
import java.time.LocalDateTime

private const val MINUTES_PER_HOUR = 60

enum class BatteryDirection { BELOW, ABOVE }

/**
 * What makes a rule true. Device triggers hold while their condition is met, or while it is *not*
 * when [negated] is set ("while not connected"). Evaluating a trigger is the trigger sources' job;
 * the evaluator only ever sees whether the rule's condition is met.
 */
sealed interface AutomationTrigger {

    /**
     * True on [days] between [startMinute] and [endMinute] (minutes since midnight), to the minute:
     * the start minute is active from its first second and the end minute through its last, so
     * `start == end` is exactly one minute. An end before the start runs overnight and belongs to
     * the day it starts on (Friday 22:00 to 06:00 includes Saturday morning).
     */
    data class Schedule(
        val days: Set<DayOfWeek>,
        val startMinute: Int,
        val endMinute: Int,
    ) : AutomationTrigger {

        fun isActiveAt(at: LocalDateTime): Boolean {
            val minute = at.hour * MINUTES_PER_HOUR + at.minute
            val today = at.dayOfWeek
            return if (startMinute <= endMinute) {
                today in days && minute in startMinute..endMinute
            } else {
                (today in days && minute >= startMinute) || (today.minus(1) in days && minute <= endMinute)
            }
        }
    }

    data class Bluetooth(
        val deviceAddress: String,
        val deviceName: String,
        val negated: Boolean = false,
    ) : AutomationTrigger

    /** A null [ssid] means any Wi-Fi network. */
    data class Wifi(
        val ssid: String? = null,
        val negated: Boolean = false,
    ) : AutomationTrigger

    data class Headphones(val negated: Boolean = false) : AutomationTrigger

    /**
     * True while the device is within [radiusM] metres of a saved spot, or farther than that when [negated]. The position is the
     * approximate fix Home takes when it comes to the front, so this switches then, not the moment the user arrives or leaves.
     */
    data class Location(
        val name: String,
        val latitude: Double,
        val longitude: Double,
        val radiusM: Int,
        val negated: Boolean = false,
    ) : AutomationTrigger {

        /** On the radius counts as inside. */
        fun isInside(position: DeviceLocation): Boolean =
            distanceMeters(latitude, longitude, position.latitude, position.longitude) <= radiusM
    }

    /**
     * "While charging", or "while not charging" when [whileCharging] is false, with the battery
     * strictly below or above a threshold ([level], always required). The level check therefore works
     * in both states: "not charging, below 20%" and "charging, above 80%" are both rules.
     */
    data class Battery(
        val whileCharging: Boolean,
        val level: BatteryLevelCondition,
    ) : AutomationTrigger {

        fun isMetBy(charging: Boolean, levelPercent: Int): Boolean =
            charging == whileCharging && level.isMetBy(levelPercent)
    }
}

/** Strictly [direction] the threshold: below 20% means 19% and under, below 100% means "not full". */
data class BatteryLevelCondition(
    val direction: BatteryDirection,
    val thresholdPercent: Int,
) {
    fun isMetBy(levelPercent: Int): Boolean = when (direction) {
        BatteryDirection.BELOW -> levelPercent < thresholdPercent
        BatteryDirection.ABOVE -> levelPercent > thresholdPercent
    }

    companion object {
        /** 5% steps. "Above 100%" can never be true, so that direction stops at 95%. */
        fun stopsFor(direction: BatteryDirection): IntProgression = when (direction) {
            BatteryDirection.BELOW -> 5..100 step 5
            BatteryDirection.ABOVE -> 5..95 step 5
        }
    }
}

/** The trigger from stored or backed-up fields, or null when any of the four is missing. */
fun locationTriggerOrNull(name: String?, latitude: Double?, longitude: Double?, radiusM: Int?, negated: Boolean): AutomationTrigger.Location? =
    name?.let { n -> latitude?.let { la -> longitude?.let { lo -> radiusM?.let { r -> AutomationTrigger.Location(n, la, lo, r, negated) } } } }

/** Schedule and location rules are free (sharing [AutomationLimits.FREE_MAX_RULES]); every device trigger is Pro. */
fun AutomationTrigger.isFree(): Boolean = this is AutomationTrigger.Schedule || this is AutomationTrigger.Location

private const val EARTH_RADIUS_M = 6_371_000.0

/** Great-circle distance in metres (haversine). */
fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = Math.sin(dLat / 2).let { it * it } + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2).let { it * it }
    return 2 * EARTH_RADIUS_M * Math.asin(Math.sqrt(a))
}
