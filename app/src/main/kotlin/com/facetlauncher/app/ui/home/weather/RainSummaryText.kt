package com.facetlauncher.app.ui.home.weather

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.RainUnit
import com.facetlauncher.app.data.model.WeatherHour
import com.facetlauncher.app.domain.RainIntensity
import com.facetlauncher.app.domain.RainSummary
import com.facetlauncher.app.domain.WeatherCondition
import com.facetlauncher.app.domain.WeatherDay
import com.facetlauncher.app.domain.WeatherState
import com.facetlauncher.app.domain.conditionOf
import com.facetlauncher.app.domain.isNightSymbol
import com.facetlauncher.app.ui.components.TextActionButton
import com.facetlauncher.app.ui.components.ThemedModalBottomSheet
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Hairline
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.SurfaceContainer
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as DateTextStyle
import java.util.Locale
import kotlin.math.roundToInt

@Composable
internal fun rainSummaryText(summary: RainSummary, timeFormat: DateTimeFormatter, unit: RainUnit): String? = when (summary) {
    RainSummary.NoData -> null
    RainSummary.Dry -> stringResource(R.string.weather_summary_dry)
    is RainSummary.RainNow -> {
        val name = intensityName(summary.intensity)
        val total = rainWithUnit(summary.totalMm, unit)
        if (summary.until != null) {
            stringResource(R.string.weather_summary_now_until, name, timeFormat.format(summary.until), total)
        } else {
            stringResource(R.string.weather_summary_now_continues, name, total)
        }
    }
    is RainSummary.RainLater -> {
        val name = intensityName(summary.intensity)
        val total = rainWithUnit(summary.totalMm, unit)
        if (summary.until != null) {
            stringResource(R.string.weather_summary_later, name, timeFormat.format(summary.from), timeFormat.format(summary.until), total)
        } else {
            stringResource(R.string.weather_summary_later_open, name, timeFormat.format(summary.from), total)
        }
    }
}

@Composable
internal fun intensityName(intensity: RainIntensity): String = conditionName(
    when (intensity) {
        RainIntensity.LIGHT -> WeatherCondition.LIGHT_RAIN
        RainIntensity.MODERATE -> WeatherCondition.RAIN
        RainIntensity.HEAVY -> WeatherCondition.HEAVY_RAIN
    },
)
