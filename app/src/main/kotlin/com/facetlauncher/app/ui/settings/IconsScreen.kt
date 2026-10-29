package com.facetlauncher.app.ui.settings

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.HomeWallpaper
import com.facetlauncher.app.data.model.IconBackground
import com.facetlauncher.app.data.model.IconColour
import com.facetlauncher.app.data.model.IconRenderMode
import com.facetlauncher.app.data.model.IconShape
import com.facetlauncher.app.data.model.IconStyle
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.ui.components.AppIcon
import com.facetlauncher.app.ui.components.AppIconSize
import com.facetlauncher.app.ui.components.CardDivider
import com.facetlauncher.app.ui.components.LabeledDropdownRow
import com.facetlauncher.app.ui.components.SettingsCard
import com.facetlauncher.app.ui.components.SettingsSectionHeader
import com.facetlauncher.app.ui.components.StickyHeaderLayout
import com.facetlauncher.app.ui.components.toComposeShape
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Hairline
import com.facetlauncher.app.ui.theme.IconTile
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.LocalIconRenderMode
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.SurfaceContainer
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList


private const val SAMPLE_ICON_COUNT = 4
private val SAMPLE_ICON_SIZE = 26.dp

/**
 * Settings → Appearance → Fonts, themes and icons → Icons — the icon style as a [IconStyle] card each
 * (a 2×2 sample of the user's own icons in that style, in the chosen colour), a colour dropdown and the
 * icon shape, under a live Home preview. Shares [AppearanceSettingsViewModel].
 */
@Composable
fun IconsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AppearanceSettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val homeWallpaper by viewModel.homeWallpaper.collectAsState()
    IconsContent(
        settings = settings,
        uiState = uiState,
        homeWallpaper = homeWallpaper,
        onBack = onBack,
        onIconRenderModeChange = viewModel::setIconRenderMode,
        onIconShapeChange = viewModel::setIconShape,
        modifier = modifier,
    )
}

@Composable
private fun IconsContent(
    settings: LauncherSettings,
    uiState: AppearanceSettingsUiState,
    homeWallpaper: HomeWallpaper,
    onBack: () -> Unit,
    onIconRenderModeChange: (IconRenderMode) -> Unit,
    onIconShapeChange: (IconShape) -> Unit,
    modifier: Modifier = Modifier,
) {
    val samples = remember(uiState.effectiveFavorites, uiState.effectiveDockItems) {
        (uiState.effectiveFavorites + uiState.effectiveDockItems).sampleApps()
    }
    StickyHeaderLayout(
        modifier = modifier,
        header = { AppearanceSettingsHeader(title = stringResource(R.string.icons_screen_title), onBack = onBack) },
        content = { headerHeight ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SurfaceContainer)
                    .testTag("icons_screen")
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .padding(horizontal = 24.dp),
                contentPadding = PaddingValues(top = headerHeight, bottom = 24.dp),
            ) {
                item {
                    AppearancePreviewCard(
                        uiState = uiState,
                        homeWallpaper = homeWallpaper,
                        modifier = Modifier.padding(bottom = 20.dp).testTag("appearance_preview_card"),
                    )
                }
                item { SettingsSectionHeader(stringResource(R.string.icons_screen_style)) }
                item { IconStyleCard(settings, samples, onIconRenderModeChange, onIconShapeChange) }
            }
        },
    )
}

@Composable
private fun IconStyleCard(
    settings: LauncherSettings,
    samples: ImmutableList<AppInfo>,
    onIconRenderModeChange: (IconRenderMode) -> Unit,
    onIconShapeChange: (IconShape) -> Unit,
) {
    val mode = settings.iconRenderMode
    SettingsCard {
        StyleCards(
            selected = mode.style,
            colour = mode.colour ?: IconColour.ACCENT,
            background = mode.background,
            samples = samples,
            onSelect = { onIconRenderModeChange(IconRenderMode.of(it, mode.colour ?: IconColour.ACCENT, mode.background)) },
        )
        val colours = IconRenderMode.coloursFor(mode.style)
        if (colours.isNotEmpty()) {
            CardDivider()
            LabeledDropdownRow(
                title = stringResource(R.string.icons_screen_colour),
                options = colours,
                selected = mode.colour ?: colours.first(),
                label = { stringResource(it.displayNameRes) },
                onSelect = { onIconRenderModeChange(IconRenderMode.of(mode.style, it, mode.background)) },
                testTag = "icons_colour_row",
            )
        }
        val backgrounds = IconRenderMode.backgroundsFor(mode.style)
        if (backgrounds.size > 1) {
            CardDivider()
            LabeledDropdownRow(
                title = stringResource(R.string.icons_screen_background),
                options = backgrounds,
                selected = mode.background,
                label = { stringResource(it.displayNameRes) },
                onSelect = { onIconRenderModeChange(IconRenderMode.of(mode.style, mode.colour ?: IconColour.ACCENT, it)) },
                testTag = "icons_background_row",
            )
        }
        CardDivider()
        IconShapePicker(selected = settings.iconShape, onSelect = onIconShapeChange)
    }
}

/** The first few distinct apps on Home, which have an icon to show. */
private fun List<PlacedItem>.sampleApps(): ImmutableList<AppInfo> =
    filterIsInstance<PlacedItem.SingleApp>().map { it.app }.filter { it.icon != null }.distinctBy { it.packageName }.take(SAMPLE_ICON_COUNT).toImmutableList()

@Composable
private fun StyleCards(selected: IconStyle, colour: IconColour, background: IconBackground, samples: ImmutableList<AppInfo>, onSelect: (IconStyle) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp).testTag("icons_style_cards"),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        IconStyle.entries.forEach { style ->
            StyleCard(
                style = style,
                mode = IconRenderMode.of(style, colour, background),
                selected = style == selected,
                samples = samples,
                onClick = { onSelect(style) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/** One style: a 2×2 sample on the page tone. Theme inverted without a tile is that tone's own opposite, so it gets `Ink` behind it instead. */
@Composable
private fun StyleCard(style: IconStyle, mode: IconRenderMode, selected: Boolean, samples: ImmutableList<AppInfo>, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = MaterialTheme.shapes.medium
    val backdrop = if (mode.colour == IconColour.THEME_INVERTED && mode.background == IconBackground.NONE) Ink else SurfaceContainer
    Column(
        modifier = modifier
            .testTag("icons_style_${style.name}")
            .clip(shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(backdrop)
                .border(if (selected) 2.dp else 0.dp, if (selected) Accent else backdrop, shape)
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            CompositionLocalProvider(LocalIconRenderMode provides mode) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    samples.chunked(2).ifEmpty { listOf(emptyList()) }.forEach { pair ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { pair.forEach { AppIcon(app = it, size = SAMPLE_ICON_SIZE, contentDescription = null) } }
                    }
                }
            }
        }
        Text(
            text = stringResource(style.displayNameRes),
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) Ink else Muted,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun IconsScreenPreview() {
    FacetLauncherTheme {
        IconsContent(
            settings = LauncherSettings(),
            uiState = AppearanceSettingsUiState(globalFavorites = DesignTimeFavorites, globalDockItems = DesignTimeDockItems),
            homeWallpaper = HomeWallpaper.Unavailable,
            onBack = {},
            onIconRenderModeChange = {},
            onIconShapeChange = {},
        )
    }
}

private val ICON_SHAPE_RING_WIDTH = 2.dp
private val ICON_SHAPE_RING_GAP = 2.dp

/** Icon outline picker — each option drawn in its own shape so Squircle vs Rounded square is visible, not just named. */
@Composable
internal fun IconShapePicker(selected: IconShape, onSelect: (IconShape) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(vertical = 8.dp).testTag("appearance_icon_shape_row")) {
        Text(
            text = stringResource(R.string.appearance_icon_shape),
            style = MaterialTheme.typography.bodyLarge,
            color = Ink,
            modifier = Modifier.padding(bottom = 10.dp),
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            IconShape.entries.forEach { shape ->
                IconShapeOption(shape = shape, isSelected = shape == selected, onSelect = { onSelect(shape) })
            }
        }
    }
}

@Composable
private fun IconShapeOption(shape: IconShape, isSelected: Boolean, onSelect: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .testTag("icon_shape_${shape.name}")
            .clip(MaterialTheme.shapes.small)
            .selectable(selected = isSelected, role = Role.RadioButton, onClick = onSelect)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Selected: an Accent ring with a transparent gap around the icon. The ring's space is
        // reserved on every option so selecting one doesn't shift the row.
        Box(
            modifier = Modifier
                .size(AppIconSize.TILE + (ICON_SHAPE_RING_WIDTH + ICON_SHAPE_RING_GAP) * 2)
                .then(if (isSelected) Modifier.border(ICON_SHAPE_RING_WIDTH, Accent, shape.toComposeShape()) else Modifier),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(AppIconSize.TILE)
                    .background(IconTile, shape.toComposeShape())
                    .then(if (isSelected) Modifier else Modifier.border(1.dp, Hairline, shape.toComposeShape())),
            )
        }
        Text(
            text = stringResource(shape.displayNameRes),
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) Ink else Muted,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}
