package com.facetlauncher.app.ui.settings.automation

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.model.AutomationPermission
import com.facetlauncher.app.data.model.AutomationTrigger
import com.facetlauncher.app.data.model.PairedBluetoothDevice
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Surface

private val facets = listOf(FacetEntity(id = 1, name = "Personal", position = 0), FacetEntity(id = 2, name = "Work", position = 1))
private val choices = DeviceChoices(bluetooth = listOf(PairedBluetoothDevice("AA:BB", "Car")), networks = listOf("HomeNet-5G", "Cafe"))

@Composable
private fun EditorPreview(state: RuleEditorState, isPro: Boolean = true) {
    FacetLauncherTheme {
        RuleEditorContent(
            state = state,
            choices = choices,
            isPro = isPro,
            onChange = {},
            onChangeTrigger = {},
            onSave = {},
            onCancel = {},
            onDeleteClick = {},
            modifier = Modifier.background(Surface),
        )
    }
}

private fun newRule() = RuleEditorState.forNew(facets, activeFacetId = 1)

@Preview(showBackground = true, widthDp = 390, heightDp = 760)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 760, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ScheduleEditorPreview() = EditorPreview(newRule().withEndMinute(6 * 60))

@Preview(showBackground = true, widthDp = 390, heightDp = 760)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 760, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun BluetoothEditorPreview() = EditorPreview(
    newRule().withTrigger(AutomationTrigger.Bluetooth("AA:BB", "Car")).copy(endKind = EndKind.SWITCH),
)

@Preview(showBackground = true, widthDp = 390, heightDp = 760)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 760, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun WifiNamedEditorPreview() = EditorPreview(newRule().withKind(TriggerKind.WIFI).withWifiScope(named = true))

@Preview(showBackground = true, widthDp = 390, heightDp = 760)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 760, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun BatteryEditorPreview() = EditorPreview(newRule().withKind(TriggerKind.BATTERY))

@Preview(showBackground = true, widthDp = 390, heightDp = 760)
@Composable
private fun PermissionDeniedEditorPreview() = EditorPreview(newRule().copy(permissionDenied = AutomationPermission.BLUETOOTH_CONNECT))

@Preview(showBackground = true, widthDp = 390, heightDp = 760)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 760, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FreeUserEditorPreview() = EditorPreview(newRule(), isPro = false)
