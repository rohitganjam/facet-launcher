package com.facetlauncher.app.debug

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.ui.components.LabeledDropdownRow
import com.facetlauncher.app.ui.components.SettingsCard
import com.facetlauncher.app.ui.settings.DebugSettingsEntry
import com.facetlauncher.app.ui.theme.Muted
import kotlinx.coroutines.launch
import javax.inject.Inject

/** The debug "Pro" switch at the bottom of Settings. Not translated: it exists only in debug builds. */
class DebugSettingsEntryImpl @Inject constructor(
    private val override: DebugEntitlementOverride,
) : DebugSettingsEntry {

    @Composable
    override fun Content() {
        val mode = override.mode.collectAsState(initial = DebugEntitlementMode.FOLLOW_PLAY).value
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
        }
    }
}
