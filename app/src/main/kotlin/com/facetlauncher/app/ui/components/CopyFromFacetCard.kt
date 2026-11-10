package com.facetlauncher.app.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Muted
import kotlinx.collections.immutable.ImmutableList

/**
 * A facet screen's "Copy from facet" row. It opens a bottom sheet listing [facets] (the other facets); picking one asks
 * to confirm, naming what is replaced ([confirmMessageRes] takes the chosen facet's name as %1\$s), then [onCopy] receives its id.
 */
@Composable
fun CopyFromFacetCard(
    facets: ImmutableList<Facet>,
    subtitle: String,
    @StringRes confirmMessageRes: Int,
    onCopy: (sourceFacetId: Long) -> Unit,
    testTagPrefix: String,
    modifier: Modifier = Modifier,
) {
    var picking by rememberSaveable { mutableStateOf(false) }
    var confirmingId by rememberSaveable { mutableStateOf<Long?>(null) }
    SettingsCard(modifier = modifier, fullBleedRows = true) {
        Row(
            modifier = Modifier
                .testTag("${testTagPrefix}_copy_from_row")
                .fillMaxWidth()
                .clickable(enabled = facets.isNotEmpty()) { picking = true }
                .padding(horizontal = LocalSettingsRowInset.current, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(text = stringResource(R.string.copy_from_facet_title), style = MaterialTheme.typography.bodyLarge, color = Ink)
                Text(
                    text = if (facets.isEmpty()) stringResource(R.string.copy_from_facet_none) else subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Muted,
                )
            }
        }
    }
    if (picking) {
        CopyFromFacetSheet(
            facets = facets,
            onPick = {
                picking = false
                confirmingId = it.id
            },
            onDismiss = { picking = false },
            testTagPrefix = testTagPrefix,
        )
    }
    facets.firstOrNull { it.id == confirmingId }?.let { source ->
        ConfirmDialog(
            title = stringResource(R.string.copy_from_facet_confirm_title, source.name),
            message = stringResource(confirmMessageRes, source.name),
            confirmLabel = stringResource(R.string.copy_from_facet_confirm),
            onConfirm = {
                confirmingId = null
                onCopy(source.id)
            },
            onDismiss = { confirmingId = null },
        )
    }
}

@Composable
private fun CopyFromFacetSheet(
    facets: ImmutableList<Facet>,
    onPick: (Facet) -> Unit,
    onDismiss: () -> Unit,
    testTagPrefix: String,
    modifier: Modifier = Modifier,
) {
    ThemedModalBottomSheet(onDismissRequest = onDismiss, modifier = modifier, skipPartiallyExpanded = true) {
        Column(modifier = Modifier.testTag("${testTagPrefix}_copy_from_sheet").padding(horizontal = 24.dp).padding(bottom = 24.dp)) {
            Text(
                text = stringResource(R.string.copy_from_facet_title),
                style = MaterialTheme.typography.headlineSmall,
                color = Ink,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                items(facets, key = { it.id }) { facet ->
                    Text(
                        text = facet.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Ink,
                        modifier = Modifier
                            .testTag("${testTagPrefix}_copy_from_facet_${facet.id}")
                            .fillMaxWidth()
                            .clickable { onPick(facet) }
                            .padding(vertical = 14.dp),
                    )
                }
            }
        }
    }
}
