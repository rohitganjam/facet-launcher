package com.facetlauncher.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Muted
import kotlinx.collections.immutable.ImmutableList

/**
 * A default screen's "Copy to facets" row: it opens [CopyToFacetsSheet], and [onCopy] receives the ids picked there.
 * [sheetMessage] says what the copy replaces on each chosen facet.
 */
@Composable
fun CopyToFacetsCard(
    facets: ImmutableList<Facet>,
    activeFacetId: Long?,
    subtitle: String,
    sheetMessage: String,
    onCopy: (Set<Long>) -> Unit,
    testTagPrefix: String,
    modifier: Modifier = Modifier,
) {
    var picking by rememberSaveable { mutableStateOf(false) }
    SettingsCard(modifier = modifier, fullBleedRows = true) {
        Row(
            modifier = Modifier
                .testTag("${testTagPrefix}_copy_row")
                .fillMaxWidth()
                .clickable(enabled = facets.isNotEmpty()) { picking = true }
                .padding(horizontal = LocalSettingsRowInset.current, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(text = stringResource(R.string.copy_to_facets_title), style = MaterialTheme.typography.bodyLarge, color = Ink)
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = Muted)
            }
        }
    }
    if (picking) {
        CopyToFacetsSheet(
            facets = facets,
            activeFacetId = activeFacetId,
            message = sheetMessage,
            onConfirm = { ids ->
                picking = false
                onCopy(ids)
            },
            onDismiss = { picking = false },
        )
    }
}

/** Every facet with a checkbox, all checked to start; Copy is enabled while at least one stays checked. */
@Composable
fun CopyToFacetsSheet(
    facets: ImmutableList<Facet>,
    activeFacetId: Long?,
    message: String,
    onConfirm: (Set<Long>) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // A facet unchecked stays unchecked: tracking the exceptions keeps "all checked" the default if the list changes.
    var unchecked by rememberSaveable { mutableStateOf(emptySet<Long>()) }
    val selected = facets.map { it.id }.filter { it !in unchecked }.toSet()
    ThemedModalBottomSheet(onDismissRequest = onDismiss, modifier = modifier, skipPartiallyExpanded = true) {
        Column(modifier = Modifier.testTag("copy_to_facets_sheet").padding(horizontal = 24.dp).padding(bottom = 24.dp)) {
            Text(text = stringResource(R.string.copy_to_facets_title), style = MaterialTheme.typography.headlineSmall, color = Ink)
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Muted,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
            )
            LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                items(facets, key = { it.id }) { facet ->
                    val checked = facet.id in selected
                    CopyToFacetsRow(
                        name = facet.name,
                        checked = checked,
                        isCurrent = facet.id == activeFacetId,
                        onToggle = { unchecked = if (checked) unchecked + facet.id else unchecked - facet.id },
                        testTag = "copy_to_facets_row_${facet.id}",
                    )
                }
            }
            PrimaryButton(
                text = pluralStringResource(R.plurals.copy_to_facets_confirm, selected.size, selected.size),
                onClick = { onConfirm(selected) },
                enabled = selected.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp).testTag("copy_to_facets_confirm"),
            )
        }
    }
}

@Composable
private fun CopyToFacetsRow(name: String, checked: Boolean, isCurrent: Boolean, onToggle: () -> Unit, testTag: String) {
    Row(
        modifier = Modifier
            .testTag(testTag)
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Checkbox(checked = checked, onCheckedChange = null, colors = CheckboxDefaults.colors(checkedColor = Accent))
        Text(text = name, style = MaterialTheme.typography.bodyLarge, color = Ink, modifier = Modifier.weight(1f))
        if (isCurrent) {
            Text(text = stringResource(R.string.copy_to_facets_current), style = MaterialTheme.typography.bodyMedium, color = Muted)
        }
    }
}
