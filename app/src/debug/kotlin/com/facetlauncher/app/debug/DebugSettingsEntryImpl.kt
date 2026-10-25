package com.facetlauncher.app.debug

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.data.AppVersion
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.whatsNewSeenId
import com.facetlauncher.app.ui.components.LabeledDropdownRow
import com.facetlauncher.app.ui.components.SettingsCard
import com.facetlauncher.app.ui.settings.ClickableRow
import com.facetlauncher.app.ui.settings.DebugSettingsEntry
import com.facetlauncher.app.ui.theme.Muted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

/** The debug tools at the bottom of Settings: the "Pro" switch and the What's new pill reset. Not translated: they exist only in debug builds. */
class DebugSettingsEntryImpl @Inject constructor(
    private val override: DebugEntitlementOverride,
    private val settingsRepository: SettingsRepository,
    private val appVersion: AppVersion,
) : DebugSettingsEntry {

    @Composable
    override fun Content() {
        val mode = override.mode.collectAsState(initial = DebugEntitlementMode.FOLLOW_PLAY).value
        val seenId = whatsNewSeenId(appVersion.name)
        val whatsNewSeen = remember(seenId) { settingsRepository.settings.map { seenId in it.coachMarksSeen } }.collectAsState(initial = true).value
        val scope = rememberCoroutineScope()
        Column(modifier = Modifier.padding(top = 20.dp).testTag("debug_entitlement")) {
            Text(text = "DEBUG", style = MaterialTheme.typography.labelLarge, color = Muted, modifier = Modifier.padding(bottom = 8.dp))
            SettingsCard {
                LabeledDropdownRow(
                    title = "Facet Pro",
                    options = DebugEntitlementMode.entries,
                    selected = mode,
                    label = { it.label },
                    onSelect = { scope.launch { override.setMode(it) } },
                    testTag = "debug_entitlement_mode",
                )
            }
            SettingsCard(fullBleedRows = true, modifier = Modifier.padding(top = 12.dp)) {
                ClickableRow(
                    title = "What's new pill",
                    subtitle = if (whatsNewSeen) {
                        "${appVersion.name} is marked seen. Tap to show the pill again on the next resting Home."
                    } else {
                        "${appVersion.name} is unseen: the pill shows on the next resting Home."
                    },
                    onClick = { scope.launch { settingsRepository.clearCoachMarkSeen(seenId) } },
                    testTag = "debug_whats_new_reset",
                )
            }
        }
    }
}
