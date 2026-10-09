package com.facetlauncher.app.ui.facets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.model.DockDisplayMode
import com.facetlauncher.app.data.model.ListContentMode
import com.facetlauncher.app.ui.components.BackButton
import com.facetlauncher.app.ui.components.LocalSettingsRowInset
import com.facetlauncher.app.ui.components.TextActionButton
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.SurfaceContainer

/** Shared building blocks for [FacetSettingsScreen]'s own nav-list layout. */

@Composable
internal fun ListContentMode.displayLabel(): String = stringResource(displayNameRes)

@Composable
internal fun DockDisplayMode.displayLabel(): String = stringResource(displayNameRes)

@Composable
internal fun AppRowPresentation.displayLabel(): String = stringResource(displayNameRes)

/**
 * [isActive] switches the trailing button between two distinct states — a disabled
 * checkmark + "Active" once this facet is already live (mirrors `ManageFacetsScreen`'s own
 * `enabled = !isActive` on its overflow menu's equivalent entry), or a clickable, icon-less
 * "Activate facet" otherwise.
 */
@Composable
internal fun FacetSettingsHeader(
    title: String,
    isActive: Boolean,
    onBack: () -> Unit,
    onApplyFacet: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceContainer)
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BackButton(onClick = onBack)
        Text(text = title, style = MaterialTheme.typography.headlineSmall, color = Ink, modifier = Modifier.weight(1f))
        TextActionButton(
            text = stringResource(if (isActive) R.string.facet_settings_active else R.string.facet_settings_activate_facet),
            onClick = onApplyFacet,
            enabled = !isActive,
            disabledColor = Accent,
            leadingIcon = if (isActive) Icons.Default.Check else null,
            modifier = Modifier.testTag("facet_settings_apply_button"),
        )
    }
}

@Composable
internal fun FacetSettingsRow(
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .testTag(testTag)
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = LocalSettingsRowInset.current, vertical = 12.dp)
            .alpha(if (enabled) 1f else 0.4f),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leadingIcon?.invoke()
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, color = Ink)
            if (subtitle != null) {
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = Muted)
            }
        }
    }
}
