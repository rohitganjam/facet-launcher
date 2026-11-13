package com.facetlauncher.app.ui.settings

import android.content.res.Configuration
import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.DailyPhotoSource
import com.facetlauncher.app.data.model.DailyWallpaperProblem
import com.facetlauncher.app.ui.components.CardDivider
import com.facetlauncher.app.ui.components.LabeledDropdownRow
import com.facetlauncher.app.ui.components.PrimaryButton
import com.facetlauncher.app.ui.components.ProPill
import com.facetlauncher.app.ui.components.SettingsCard
import com.facetlauncher.app.ui.components.SettingsToggleRow
import com.facetlauncher.app.ui.components.TextActionButton
import com.facetlauncher.app.ui.components.ThemedModalBottomSheet
import com.facetlauncher.app.ui.theme.ErrorColor
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.SurfaceContainer
import java.util.Date

/** What the daily wallpaper card and its sheet can ask for; every action defaults to nothing, for previews and tests. */
class DailyWallpaperActions(
    val onSwitch: (Boolean) -> Unit = {},
    val onSource: (DailyPhotoSource) -> Unit = {},
    val onIncludeLock: (Boolean) -> Unit = {},
    val onWifiOnly: (Boolean) -> Unit = {},
    val onResume: () -> Unit = {},
    val onSheetSource: (DailyPhotoSource) -> Unit = {},
    val onSheetIncludeLock: (Boolean) -> Unit = {},
    val onTurnOn: (useMobileData: Boolean) -> Unit = {},
    val onDismissSheet: () -> Unit = {},
)

/** The daily wallpaper settings above the photo grid: the switch (with a Pro pill without Pro), and with it on, the source, the screens, Wi-Fi only and the status line. */
@Composable
internal fun DailyWallpaperCard(state: DailyWallpaperUiState, actions: DailyWallpaperActions, modifier: Modifier = Modifier) {
    SettingsCard(modifier = modifier.testTag("daily_wallpaper_card")) {
        SettingsToggleRow(
            title = stringResource(R.string.daily_wallpaper_title),
            subtitle = stringResource(R.string.daily_wallpaper_subtitle),
            checked = state.enabled,
            onCheckedChange = actions.onSwitch,
            testTag = "daily_wallpaper_switch",
            titleBadge = if (state.isPro) null else { { ProPill(Modifier.testTag("daily_wallpaper_pro")) } },
        )
        if (state.enabled) {
            CardDivider()
            SourceRow(state.source, actions.onSource, "daily_wallpaper_source")
            CardDivider()
            ScreensRow(state.includeLock, actions.onIncludeLock, "daily_wallpaper_screens")
            CardDivider()
            SettingsToggleRow(
                title = stringResource(R.string.daily_wallpaper_wifi_only),
                subtitle = stringResource(R.string.daily_wallpaper_wifi_only_subtitle),
                checked = state.wifiOnly,
                onCheckedChange = actions.onWifiOnly,
                testTag = "daily_wallpaper_wifi_only",
            )
        }
        if (state.status != DailyWallpaperStatus.Off) {
            CardDivider()
            StatusLine(state.status, actions.onResume)
        }
    }
}

@Composable
private fun SourceRow(source: DailyPhotoSource, onSelect: (DailyPhotoSource) -> Unit, testTag: String) {
    LabeledDropdownRow(
        title = stringResource(R.string.daily_wallpaper_source),
        options = DailyPhotoSource.entries,
        selected = source,
        label = { stringResource(it.labelRes) },
        onSelect = onSelect,
        testTag = testTag,
    )
}

@Composable
private fun ScreensRow(includeLock: Boolean, onSelect: (Boolean) -> Unit, testTag: String) {
    LabeledDropdownRow(
        title = stringResource(R.string.daily_wallpaper_set_on),
        options = listOf(false, true),
        selected = includeLock,
        label = { stringResource(if (it) R.string.wallpaper_fit_home_lock else R.string.wallpaper_fit_home) },
        onSelect = onSelect,
        testTag = testTag,
    )
}

@Composable
private fun StatusLine(status: DailyWallpaperStatus, onResume: () -> Unit) {
    val context = LocalContext.current
    val isProblem = status is DailyWallpaperStatus.Problem && status.problem == DailyWallpaperProblem.REFUSED
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 13.dp).testTag("daily_wallpaper_status"), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        val text = when (status) {
            DailyWallpaperStatus.Off -> ""
            DailyWallpaperStatus.Starting -> stringResource(R.string.daily_wallpaper_status_starting)
            DailyWallpaperStatus.Paused -> stringResource(R.string.daily_wallpaper_status_paused)
            is DailyWallpaperStatus.Ready -> stringResource(R.string.daily_wallpaper_status_ready, status.title)
            is DailyWallpaperStatus.Problem -> stringResource(status.problem.messageRes)
            is DailyWallpaperStatus.Current -> if (status.today && status.setAtMillis != null) {
                stringResource(R.string.daily_wallpaper_status_today, status.title, DateFormat.getTimeFormat(context).format(Date(status.setAtMillis)))
            } else {
                stringResource(R.string.daily_wallpaper_status_current, status.title)
            }
        }
        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = if (isProblem) ErrorColor else Muted)
        if (status is DailyWallpaperStatus.Current && status.credit.isNotBlank()) {
            Text(text = status.credit, style = MaterialTheme.typography.bodySmall, color = Muted, modifier = Modifier.testTag("daily_wallpaper_credit"))
        }
        if (status == DailyWallpaperStatus.Paused) {
            TextActionButton(text = stringResource(R.string.daily_wallpaper_resume), onClick = onResume, modifier = Modifier.testTag("daily_wallpaper_resume"))
        }
    }
}

private val DailyWallpaperProblem.messageRes: Int
    get() = when (this) {
        DailyWallpaperProblem.WAITING_FOR_WIFI -> R.string.daily_wallpaper_problem_wifi
        DailyWallpaperProblem.NO_CONNECTION -> R.string.daily_wallpaper_problem_connection
        DailyWallpaperProblem.BATTERY_SAVER -> R.string.daily_wallpaper_problem_battery
        DailyWallpaperProblem.SOURCE_UNAVAILABLE -> R.string.daily_wallpaper_problem_source
        DailyWallpaperProblem.DOWNLOAD_FAILED -> R.string.daily_wallpaper_problem_download
        DailyWallpaperProblem.TOO_SMALL -> R.string.daily_wallpaper_problem_too_small
        DailyWallpaperProblem.REFUSED -> R.string.daily_wallpaper_problem_refused
    }

/** Turning the daily wallpaper on: what will happen, the source and screens, and a way past Wi-Fi only for today when on mobile data. */
@Composable
internal fun DailyWallpaperTurnOnSheet(sheet: DailyWallpaperSheet, actions: DailyWallpaperActions) {
    ThemedModalBottomSheet(onDismissRequest = actions.onDismissSheet, skipPartiallyExpanded = true) {
        DailyWallpaperTurnOnContent(sheet, actions)
    }
}

@Composable
private fun DailyWallpaperTurnOnContent(sheet: DailyWallpaperSheet, actions: DailyWallpaperActions) {
    Column(modifier = Modifier.padding(24.dp).testTag("daily_wallpaper_sheet"), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text = stringResource(R.string.daily_wallpaper_sheet_title), style = MaterialTheme.typography.titleLarge, color = Ink)
        Text(text = stringResource(R.string.daily_wallpaper_sheet_body), style = MaterialTheme.typography.bodyMedium, color = Muted)
        Column {
            SourceRow(sheet.source, actions.onSheetSource, "daily_wallpaper_sheet_source")
            CardDivider()
            ScreensRow(sheet.includeLock, actions.onSheetIncludeLock, "daily_wallpaper_sheet_screens")
        }
        if (sheet.onMobileData) {
            Text(
                text = stringResource(R.string.daily_wallpaper_sheet_mobile_data),
                style = MaterialTheme.typography.bodyMedium,
                color = Ink,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceContainer, MaterialTheme.shapes.medium)
                    .padding(12.dp)
                    .testTag("daily_wallpaper_sheet_mobile_data"),
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
            if (sheet.onMobileData) {
                TextActionButton(
                    text = stringResource(R.string.daily_wallpaper_use_mobile_data),
                    onClick = { actions.onTurnOn(true) },
                    modifier = Modifier.testTag("daily_wallpaper_use_mobile_data"),
                )
            } else {
                TextActionButton(text = stringResource(R.string.wallpaper_fit_cancel), onClick = actions.onDismissSheet, modifier = Modifier.testTag("daily_wallpaper_cancel"))
            }
            PrimaryButton(text = stringResource(R.string.daily_wallpaper_turn_on), onClick = { actions.onTurnOn(false) }, modifier = Modifier.testTag("daily_wallpaper_turn_on"))
        }
    }
}

@Preview(showBackground = true, widthDp = 390)
@Preview(name = "Dark", showBackground = true, widthDp = 390, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DailyWallpaperCardPreview() {
    FacetLauncherTheme {
        Column(modifier = Modifier.background(SurfaceContainer).padding(24.dp)) {
            DailyWallpaperCard(
                state = DailyWallpaperUiState(enabled = true, status = DailyWallpaperStatus.Current("Arches National Park", "© Photographer/Getty Images", null, today = false)),
                actions = DailyWallpaperActions(),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390)
@Preview(name = "Dark", showBackground = true, widthDp = 390, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DailyWallpaperTurnOnPreview() {
    FacetLauncherTheme {
        Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
            DailyWallpaperTurnOnContent(DailyWallpaperSheet(DailyPhotoSource.SPOTLIGHT, includeLock = false, onMobileData = true), DailyWallpaperActions())
        }
    }
}
