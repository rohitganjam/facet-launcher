package com.facetlauncher.app.ui.appicon

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.R
import com.facetlauncher.app.data.iconpack.PackCatalog
import com.facetlauncher.app.data.iconpack.PackCategory
import com.facetlauncher.app.data.model.IconPackInfo
import com.facetlauncher.app.data.model.ThemeMode
import com.facetlauncher.app.ui.components.AppIcon
import com.facetlauncher.app.ui.components.AppIconSize
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Hairline
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.Surface

private const val GRID_COLUMNS = 5

/**
 * The app menu's Browse page for one pack: a search field, the pack's own categories as chips, the app's best match in
 * this pack, then every icon in a grid. Tapping an icon picks it. Thumbnails are drawn as their cells appear.
 */
@Composable
fun BrowsePackPage(
    pack: IconPackInfo,
    appLabel: String,
    state: PackCatalogState,
    /** The app's own entry in this pack, shown first; null when the pack doesn't list the app. */
    bestMatch: String?,
    /** The drawable the app shows now, ringed. */
    selectedDrawable: String?,
    loadThumbnail: suspend (String) -> ImageBitmap?,
    onBack: () -> Unit,
    onPick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by rememberSaveable(pack.packageName) { mutableStateOf("") }
    var category by rememberSaveable(pack.packageName) { mutableStateOf<String?>(null) }
    val catalog = state.catalog
    val drawables = remember(catalog, query, category) {
        when {
            catalog == null -> emptyList()
            query.isNotBlank() -> catalog.search(query)
            category != null -> catalog.categories.filter { it.title == category }.flatMap { it.drawables }.distinct()
            else -> catalog.allDrawables
        }
    }
    Column(modifier = modifier.fillMaxSize().imePadding().testTag("browse_pack_page")) {
        PageHeader(title = pack.label, onBack = onBack, backTag = "browse_pack_back")
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            singleLine = true,
            placeholder = { Text(stringResource(R.string.browse_pack_search), color = Muted) },
            leadingIcon = { Icon(imageVector = Icons.Outlined.Search, contentDescription = null, tint = Muted) },
            shape = CircleShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Ink,
                unfocusedTextColor = Ink,
                focusedBorderColor = Accent,
                unfocusedBorderColor = Hairline,
                cursorColor = Accent,
                focusedContainerColor = Surface,
                unfocusedContainerColor = Surface,
            ),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).testTag("browse_pack_search"),
        )
        val titles = catalog?.categories?.mapNotNull { it.title }?.distinct().orEmpty()
        if (titles.isNotEmpty() && query.isBlank()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.testTag("browse_pack_categories"),
            ) {
                item { CategoryChip(stringResource(R.string.browse_pack_all), selected = category == null, onClick = { category = null }) }
                items(titles) { title -> CategoryChip(title, selected = category == title, onClick = { category = title }) }
            }
        }
        when {
            state.isLoading -> Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Accent, trackColor = Color.Transparent, strokeWidth = 2.dp, modifier = Modifier.testTag("browse_pack_loading"))
            }
            catalog == null -> Message(stringResource(R.string.browse_pack_empty))
            drawables.isEmpty() -> Message(stringResource(R.string.browse_pack_no_results, query))
            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(GRID_COLUMNS),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxSize().testTag("browse_pack_grid"),
            ) {
                if (bestMatch != null && query.isBlank() && category == null) {
                    item(span = { GridItemSpan(maxLineSpan) }) { GridLabel(stringResource(R.string.browse_pack_best_match, appLabel)) }
                    item(key = "best_$bestMatch") {
                        PackIconCell(bestMatch, selected = bestMatch == selectedDrawable, loadThumbnail = loadThumbnail, onPick = onPick, tag = "browse_pack_best_match")
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) { GridLabel(category ?: stringResource(R.string.browse_pack_all)) }
                }
                items(drawables, key = { it }) { name ->
                    PackIconCell(name, selected = name == selectedDrawable, loadThumbnail = loadThumbnail, onPick = onPick, tag = "browse_pack_icon_$name")
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text = label, style = MaterialTheme.typography.labelLarge) },
        modifier = Modifier.testTag("browse_pack_category_$label"),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Surface,
            labelColor = Muted,
            selectedContainerColor = Accent.copy(alpha = 0.18f).compositeOver(Surface),
            selectedLabelColor = Ink,
        ),
        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = selected, borderColor = Hairline, selectedBorderColor = Accent),
    )
}

@Composable
private fun GridLabel(text: String) {
    Text(text = text, style = MaterialTheme.typography.labelLarge, color = Muted, modifier = Modifier.padding(top = 8.dp, start = 4.dp))
}

@Composable
private fun Message(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = Muted,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(32.dp).testTag("browse_pack_message"),
    )
}

/** One pack icon, drawn once it scrolls into view; named for TalkBack by its drawable name. */
@Composable
private fun PackIconCell(name: String, selected: Boolean, loadThumbnail: suspend (String) -> ImageBitmap?, onPick: (String) -> Unit, tag: String) {
    val icon by produceState<ImageBitmap?>(null, name) { value = loadThumbnail(name) }
    val spoken = name.replace('_', ' ')
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .then(if (selected) Modifier.border(2.dp, Accent, CircleShape) else Modifier)
            .clickable(role = Role.Button, onClick = { onPick(name) })
            .semantics {
                contentDescription = spoken
                this.selected = selected
            }
            .testTag(tag),
        contentAlignment = Alignment.Center,
    ) {
        AppIcon(icon = icon, size = AppIconSize.ICON, contentDescription = null, fromIconPack = true, modifier = Modifier.size(AppIconSize.ICON))
    }
}

@Preview(name = "Browse pack — light", showBackground = true, heightDp = 600)
@Composable
private fun BrowsePackPagePreview() {
    FacetLauncherTheme(themeMode = ThemeMode.LIGHT) {
        BrowsePackPage(
            pack = IconPackInfo("com.example.pack", "Arcticons", null),
            appLabel = "Music",
            state = PackCatalogState(PackCatalog(listOf(PackCategory("Music", listOf("music", "spotify", "radio")))), isLoading = false),
            bestMatch = "spotify",
            selectedDrawable = "spotify",
            loadThumbnail = { null },
            onBack = {},
            onPick = {},
        )
    }
}

@Preview(name = "Browse pack — dark, loading", showBackground = true, backgroundColor = 0xFF1C2027, heightDp = 400)
@Composable
private fun BrowsePackPageLoadingPreview() {
    FacetLauncherTheme(themeMode = ThemeMode.DARK) {
        BrowsePackPage(IconPackInfo("com.example.pack", "Arcticons", null), "Music", PackCatalogState(), null, null, { null }, {}, {})
    }
}
