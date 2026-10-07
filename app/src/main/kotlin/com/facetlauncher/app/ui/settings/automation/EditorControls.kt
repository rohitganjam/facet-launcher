package com.facetlauncher.app.ui.settings.automation

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.ErrorColor
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Muted

@Composable
internal fun ErrorText(text: String, tag: String) {
    Text(text = text, style = MaterialTheme.typography.bodyMedium, color = ErrorColor, modifier = Modifier.padding(vertical = 4.dp).testTag(tag))
}

/** One option of a radio group in the editor sheet. */
@Composable
internal fun RadioRow(selected: Boolean, text: String, tag: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(vertical = 4.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            colors = RadioButtonDefaults.colors(selectedColor = Accent, unselectedColor = Muted),
            modifier = Modifier.padding(end = 12.dp),
        )
        Text(text = text, style = MaterialTheme.typography.bodyLarge, color = Ink)
    }
}
