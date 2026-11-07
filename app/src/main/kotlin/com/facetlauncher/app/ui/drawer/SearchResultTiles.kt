package com.facetlauncher.app.ui.drawer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import com.facetlauncher.app.data.model.ContactInfo
import com.facetlauncher.app.data.model.SettingsSearchEntry
import com.facetlauncher.app.ui.components.AppIconSize
import com.facetlauncher.app.ui.components.toComposeShape
import com.facetlauncher.app.ui.theme.DrawerAppTextColor
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.InkInverted
import com.facetlauncher.app.ui.theme.LocalIconShape
import kotlinx.collections.immutable.ImmutableList

/** Each findable setting's own glyph, keyed by its catalog id. */
internal val SETTING_ICONS: Map<String, ImageVector> = mapOf(
    "wifi" to Icons.Default.Wifi,
    "bluetooth" to Icons.Default.Bluetooth,
    "airplane_mode" to Icons.Default.AirplanemodeActive,
    "display" to Icons.Default.BrightnessMedium,
    "sound" to Icons.Default.VolumeUp,
    "battery_saver" to Icons.Default.BatterySaver,
    "storage" to Icons.Default.Storage,
    "apps" to Icons.Default.Apps,
    "location" to Icons.Default.LocationOn,
    "security" to Icons.Default.Lock,
    "accessibility" to Icons.Default.Accessibility,
    "date_time" to Icons.Default.Schedule,
    "language" to Icons.Default.Language,
    "accounts" to Icons.Default.AccountCircle,
    "nfc" to Icons.Default.Nfc,
)

/** The glyph for [SettingsSearchEntry], the generic Settings cog for an id with none. */
internal fun SettingsSearchEntry.icon(): ImageVector = SETTING_ICONS[id] ?: Icons.Default.Settings

/**
 * [items] as rows of equal-weight tiles in the same column count as the browse grid. A LazyVerticalGrid can't nest
 * inside the results' LazyColumn, and the tile count here is tiny, so chunked rows are simpler than a second scroll container.
 */
@Composable
internal fun <T> SearchTileGrid(
    items: ImmutableList<T>,
    appearance: DrawerAppearance,
    modifier: Modifier = Modifier,
    maxRows: Int = Int.MAX_VALUE,
    tile: @Composable (T) -> Unit,
) {
    BoxWithConstraints(modifier) {
        val columns = drawerGridColumns(maxWidth, appearance.gridDensity)
        Column {
            items.chunked(columns).take(maxRows).forEach { rowItems ->
                Row(horizontalArrangement = Arrangement.spacedBy(GRID_HORIZONTAL_SPACING), modifier = Modifier.padding(bottom = 20.dp)) {
                    rowItems.forEach { item -> Box(modifier = Modifier.weight(1f)) { tile(item) } }
                    repeat(columns - rowItems.size) { Spacer(modifier = Modifier.weight(1f)) }
                }
            }
        }
    }
}

/** The settings matches as grid tiles. */
@Composable
internal fun SettingsResultsGrid(entries: ImmutableList<SettingsSearchEntry>, appearance: DrawerAppearance, onClick: (SettingsSearchEntry) -> Unit) {
    SearchTileGrid(entries, appearance) { entry -> SettingsGridTile(entry, onClick = { onClick(entry) }) }
}

/** The contact matches as grid tiles. */
@Composable
internal fun ContactResultsGrid(contacts: ImmutableList<ContactInfo>, appearance: DrawerAppearance, onClick: (ContactInfo) -> Unit) {
    SearchTileGrid(contacts, appearance) { contact -> ContactGridTile(contact, onClick = { onClick(contact) }) }
}

/** One system Settings deep link: its glyph (InkInverted on an Ink tile, the theme's highest-contrast pair) in the icon shape, the label under it. */
@Composable
private fun SettingsGridTile(entry: SettingsSearchEntry, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val size = AppIconSize.scaled()
    ResultTile(label = entry.label, onClick = onClick, modifier = modifier.testTag("settings_result_tile_${entry.id}")) {
        Box(
            modifier = Modifier.size(size).clip(LocalIconShape.current.shape.toComposeShape()).background(Ink),
            contentAlignment = Alignment.Center,
        ) {
            Icon(imageVector = entry.icon(), contentDescription = null, tint = InkInverted, modifier = Modifier.size(size * 0.55f))
        }
    }
}

/** One contact: their photo, else their initial, in the icon shape, the name under it. */
@Composable
private fun ContactGridTile(contact: ContactInfo, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val size = AppIconSize.scaled()
    val shape = LocalIconShape.current.shape.toComposeShape()
    ResultTile(label = contact.displayName, onClick = onClick, modifier = modifier.testTag("contact_result_tile_${contact.id}")) {
        val photo = contact.photo
        if (photo != null) {
            Image(
                bitmap = photo,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size).clip(shape).testTag("contact_result_photo_${contact.id}"),
            )
        } else {
            Box(modifier = Modifier.size(size).clip(shape).background(Ink), contentAlignment = Alignment.Center) {
                Text(
                    text = remember(contact.displayName) { contact.displayName.firstOrNull()?.uppercaseChar()?.toString().orEmpty() },
                    style = MaterialTheme.typography.titleMedium,
                    color = InkInverted,
                )
            }
        }
    }
}

@Composable
private fun ResultTile(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: @Composable () -> Unit) {
    Column(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick).padding(top = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        icon()
        Spacer(modifier = Modifier.height(7.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = DrawerAppTextColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
