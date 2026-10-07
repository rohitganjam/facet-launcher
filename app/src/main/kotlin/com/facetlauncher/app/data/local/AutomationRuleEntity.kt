package com.facetlauncher.app.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One facet-automation rule, flattened: [triggerType] and [endBehavior] are stored as strings and
 * parsed leniently by `AutomationRuleMapping` (an unknown value from a newer build drops the rule
 * rather than crashing), and each trigger reads only its own parameter columns.
 *
 * Deleting [targetFacetId]'s facet deletes the rule; deleting [endFacetId]'s facet only clears it,
 * which turns a "switch to" ending back into "return to baseline".
 */
@Entity(
    tableName = "automation_rules",
    foreignKeys = [
        ForeignKey(
            entity = FacetEntity::class,
            parentColumns = ["id"],
            childColumns = ["targetFacetId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = FacetEntity::class,
            parentColumns = ["id"],
            childColumns = ["endFacetId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("targetFacetId"), Index("endFacetId")],
)
data class AutomationRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val position: Int,
    val enabled: Boolean = true,
    val targetFacetId: Long,
    val endBehavior: String,
    val endFacetId: Long? = null,
    val triggerType: String,
    val negated: Boolean = false,
    /** Schedule: bit `dayOfWeek.value - 1` per selected day (Monday = bit 0). */
    val scheduleDays: Int = 0,
    val scheduleStartMinute: Int = 0,
    val scheduleEndMinute: Int = 0,
    val deviceAddress: String? = null,
    val deviceName: String? = null,
    val wifiSsid: String? = null,
    val batteryThreshold: Int? = null,
    /** Battery: `BELOW` or `ABOVE` the threshold (plain string). Required, with [batteryThreshold], for a battery rule. */
    val batteryDirection: String? = null,
)
