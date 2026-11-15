package com.facetlauncher.app.ui.appicon

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppProfile
import com.facetlauncher.app.data.model.IconOverride
import com.facetlauncher.app.data.model.IconOverrideSource
import com.facetlauncher.app.data.model.IconPackInfo
import com.facetlauncher.app.data.model.ThemeMode
import com.facetlauncher.app.domain.AppIconChoices
import com.facetlauncher.app.domain.IconChoiceKey
import com.facetlauncher.app.domain.PackIconChoice
import com.facetlauncher.app.ui.components.AppIcon
import com.facetlauncher.app.ui.components.AppIconSize
import com.facetlauncher.app.ui.components.CardDivider
import com.facetlauncher.app.ui.components.TextActionButton
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Muted

private val PREVIEW_ICON_SIZE = 72.dp
private val CHOICE_WIDTH = 64.dp
private val SIDE_PADDING = 24.dp

/**
 * The app menu's Change icon page: the current icon, one-tap choices (the app's own icon, then its icon in every pack that
 * lists it), a Browse row per installed pack, and Reset to default once the app has a custom icon.
 */
@Composable
fun ChangeIconPage(
    choices: AppIconChoices,
    onBack: () -> Unit,
    onUseOwnIcon: () -> Unit,
    onUsePackIcon: (PackIconChoice) -> Unit,
    onBrowsePack: (IconPackInfo) -> Unit,
    onReset: () -> Unit,
    onFindPacks: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().verticalScroll(rememberScrollState()).testTag("change_icon_page")) {
        PageHeader(title = stringResource(R.string.change_icon_title), onBack = onBack, backTag = "change_icon_back")
        CurrentIcon(choices)
        CardDivider()
        SectionLabel(stringResource(R.string.change_icon_in_your_packs))
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = SIDE_PADDING - 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val own = choices.ownIcon ?: choices.app.copy(iconFromPack = false)
            IconChoice(
                label = stringResource(R.string.change_icon_own_option),
                selected = choices.selected == IconChoiceKey.Own,
                onClick = onUseOwnIcon,
                tag = "change_icon_choice_own",
            ) { AppIcon(app = own, size = AppIconSize.ICON, contentDescription = null) }
            choices.packChoices.forEach { choice ->
                IconChoice(
                    label = choice.pack.label,
                    selected = choices.selected == IconChoiceKey.Pack(choice.pack.packageName, choice.match.drawableName),
                    onClick = { onUsePackIcon(choice) },
                    tag = "change_icon_choice_${choice.pack.packageName}",
                ) { AppIcon(icon = choice.icon, size = AppIconSize.ICON, contentDescription = null, fromIconPack = true) }
            }
            if (choices.isLoading) {
                Box(modifier = Modifier.size(CHOICE_WIDTH, AppIconSize.ICON), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Accent, trackColor = Color.Transparent, strokeWidth = 2.dp, modifier = Modifier.size(24.dp).testTag("change_icon_loading"))
                }
            }
        }
        CardDivider()
        if (choices.packs.isEmpty() && !choices.isLoading) {
            Text(
                text = stringResource(R.string.change_icon_no_packs),
                style = MaterialTheme.typography.bodyMedium,
                color = Muted,
                modifier = Modifier.padding(start = SIDE_PADDING, end = SIDE_PADDING, top = 12.dp),
            )
            TextActionButton(
                text = stringResource(R.string.icons_screen_find_packs),
                onClick = onFindPacks,
                modifier = Modifier.padding(horizontal = SIDE_PADDING - 12.dp).testTag("change_icon_find_packs"),
            )
        } else if (choices.packs.isNotEmpty()) {
            SectionLabel(stringResource(R.string.change_icon_more_choices))
            choices.packs.forEach { pack -> BrowsePackRow(pack = pack, onClick = { onBrowsePack(pack) }) }
        }
        if (choices.override != null) {
            CardDivider()
            TextActionButton(
                text = stringResource(R.string.change_icon_reset),
                onClick = onReset,
                modifier = Modifier.padding(horizontal = SIDE_PADDING - 12.dp, vertical = 4.dp).testTag("change_icon_reset"),
            )
        }
    }
}

/** A sheet page's back chevron and title, like the Add to folder page's. */
@Composable
internal fun PageHeader(title: String, onBack: () -> Unit, backTag: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(start = 12.dp, end = SIDE_PADDING, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = stringResource(R.string.content_description_back),
            tint = Ink,
            modifier = Modifier.clip(CircleShape).clickable(onClick = onBack).padding(8.dp).testTag(backTag),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = Ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
        )
    }
}

@Composable
private fun CurrentIcon(choices: AppIconChoices) {
    val override = choices.override
    val source = when {
        override == null -> stringResource(R.string.change_icon_source_default)
        override.source == IconOverrideSource.OWN -> stringResource(R.string.change_icon_source_own)
        else -> stringResource(R.string.change_icon_source_pack, choices.overridePack?.label ?: override.packPackage.orEmpty())
    }
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AppIcon(app = choices.app, size = PREVIEW_ICON_SIZE, contentDescription = null, modifier = Modifier.testTag("change_icon_preview"))
        Text(
            text = "${choices.app.label} · $source",
            style = MaterialTheme.typography.bodyMedium,
            color = Muted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = SIDE_PADDING).testTag("change_icon_source"),
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = Accent,
        modifier = Modifier.padding(start = SIDE_PADDING, end = SIDE_PADDING, top = 12.dp, bottom = 8.dp),
    )
}

/** One tappable icon with its name under it; the current one is ringed in the accent. */
@Composable
private fun IconChoice(label: String, selected: Boolean, onClick: () -> Unit, tag: String, icon: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .width(CHOICE_WIDTH)
            .clip(MaterialTheme.shapes.medium)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { this.selected = selected }
            .padding(vertical = 6.dp)
            .testTag(tag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(AppIconSize.ICON + 8.dp)
                .then(if (selected) Modifier.border(2.dp, Accent, CircleShape) else Modifier),
            contentAlignment = Alignment.Center,
        ) { icon() }
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun BrowsePackRow(pack: IconPackInfo, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = SIDE_PADDING, vertical = 10.dp)
            .testTag("change_icon_browse_${pack.packageName}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AppIcon(icon = pack.icon, size = AppIconSize.COMPACT, contentDescription = null, fromIconPack = true)
        Column(modifier = Modifier.weight(1f)) {
            Text(text = stringResource(R.string.change_icon_browse_pack, pack.label), style = MaterialTheme.typography.bodyLarge, color = Ink)
            Text(text = stringResource(R.string.change_icon_browse_pack_summary), style = MaterialTheme.typography.bodyMedium, color = Muted)
        }
        Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Muted)
    }
}

private fun previewChoices(withOverride: Boolean) = AppIconChoices(
    app = AppInfo(packageName = "com.example.music", activityName = "Main", label = "Music", icon = null),
    packs = listOf(IconPackInfo("com.example.pack", "Arcticons", null), IconPackInfo("com.example.delta", "Delta", null)),
    override = if (withOverride) {
        IconOverride("com.example.music", "Main", 0, AppProfile.PERSONAL, IconOverrideSource.OWN)
    } else {
        null
    },
    selected = IconChoiceKey.Own,
    isLoading = false,
)

@Preview(name = "Change icon — light", showBackground = true)
@Composable
private fun ChangeIconPagePreview() {
    FacetLauncherTheme(themeMode = ThemeMode.LIGHT) {
        ChangeIconPage(previewChoices(withOverride = true), {}, {}, {}, {}, {}, {})
    }
}

@Preview(name = "Change icon — dark, no packs", showBackground = true, backgroundColor = 0xFF1C2027)
@Composable
private fun ChangeIconPageNoPacksPreview() {
    FacetLauncherTheme(themeMode = ThemeMode.DARK) {
        ChangeIconPage(previewChoices(withOverride = false).copy(packs = emptyList()), {}, {}, {}, {}, {}, {})
    }
}
