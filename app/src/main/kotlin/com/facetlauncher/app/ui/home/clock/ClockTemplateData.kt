package com.facetlauncher.app.ui.home.clock

import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import com.facetlauncher.app.data.model.ClockDateStyle
import java.time.LocalDateTime
import java.util.Locale

/**
 * Everything every clock template reads, resolved once by [ClockDisplay]. Templates are extensions on
 * this, so a template names only what it uses and ignores the rest (e.g. a template with no date line
 * never reads [dateStyle]).
 */
@Immutable
internal data class ClockTemplateData(
    val now: LocalDateTime,
    val use24HourTime: Boolean,
    /** `null` when no AM/PM marker applies — see [ClockDisplay]. */
    val meridiem: String?,
    val locale: Locale,
    val family: FontFamily,
    val textColor: Color,
    val mutedTextColor: Color,
    val horizontalAlignment: Alignment.Horizontal,
    val dateStyle: ClockDateStyle,
    val accentColor: Color,
    val nextAlarmText: String?,
    val batteryPercent: Int?,
    val isCharging: Boolean,
)
