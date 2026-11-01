package com.facetlauncher.app.ui.settings

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.HomeWallpaper
import com.facetlauncher.app.data.model.IconBackground
import com.facetlauncher.app.data.model.IconColour
import com.facetlauncher.app.data.model.IconPackFallback
import com.facetlauncher.app.data.model.IconPackInfo
import com.facetlauncher.app.data.model.IconRenderMode
import com.facetlauncher.app.data.model.IconShape
import com.facetlauncher.app.data.model.IconStyle
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.domain.IconPackOption
import com.facetlauncher.app.domain.IconPickerState
import com.facetlauncher.app.ui.components.AppIcon
import com.facetlauncher.app.ui.components.AppIconSize
import com.facetlauncher.app.ui.components.CardDivider
import com.facetlauncher.app.ui.components.LabeledDropdownRow
import com.facetlauncher.app.ui.components.SettingsCard
import com.facetlauncher.app.ui.components.SettingsSectionHeader
import com.facetlauncher.app.ui.components.StickyHeaderLayout
import com.facetlauncher.app.ui.components.TextActionButton
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
private val OPTION_GAP = 8.dp

/** Fits the 2×2 sample grid with its padding, so every option card is one height however many samples it has. */
private val OPTION_BOX_HEIGHT = SAMPLE_ICON_SIZE * 2 + 6.dp + 20.dp

private const val PLAY_STORE_ICON_PACK_SEARCH = "market://search?q=icon%20pack&c=apps"
private const val WEB_ICON_PACK_SEARCH = "https://play.google.com/store/search?q=icon%20pack&c=apps"

/**
 * Settings → Appearance → Fonts, themes and icons → Icons — the built-in icon styles as an [IconStyle] card each and
 * every installed icon pack as a card of its own (each a 2×2 sample of the user's own apps), the style's colour or the
 * pack's handling of apps it misses, and the icon shape, under a live Home preview. The preview and shape come from
 * [AppearanceSettingsViewModel], the cards from [IconsViewModel].
 */
@Composable
fun IconsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AppearanceSettingsViewModel = hiltViewModel(),
    iconsViewModel: IconsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val homeWallpaper by viewModel.homeWallpaper.collectAsState()
    val picker by iconsViewModel.picker.collectAsState()
    IconsContent(
        settings = settings,
        uiState = uiState,
        picker = picker,
        homeWallpaper = homeWallpaper,
        onBack = onBack,
        onSelectBuiltIn = iconsViewModel::selectBuiltIn,
        onIconRenderModeChange = viewModel::setIconRenderMode,
        onSelectPack = iconsViewModel::selectPack,
        onPackFallbackChange = iconsViewModel::setPackFallback,
        onOpenPack = iconsViewModel::openPack,
        onFindPacks = { openIconPackSearch(context) },
        onIconShapeChange = viewModel::setIconShape,
        modifier = modifier,
    )
}

/** The Play Store's icon pack search, or the same search on the web where there is no Play Store. */
private fun openIconPackSearch(context: Context) {
    val opened = runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_ICON_PACK_SEARCH))) }.isSuccess
    if (!opened) runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(WEB_ICON_PACK_SEARCH))) }
}

@Composable
internal fun IconsContent(
    settings: LauncherSettings,
    uiState: AppearanceSettingsUiState,
    picker: IconPickerState,
    homeWallpaper: HomeWallpaper,
    onBack: () -> Unit,
    onSelectBuiltIn: (IconRenderMode) -> Unit,
    onIconRenderModeChange: (IconRenderMode) -> Unit,
    onSelectPack: (String) -> Unit,
    onPackFallbackChange: (IconPackFallback) -> Unit,
    onOpenPack: (String) -> Unit,
    onFindPacks: () -> Unit,
    onIconShapeChange: (IconShape) -> Unit,
    modifier: Modifier = Modifier,
) {
    val homeSamples = remember(uiState.effectiveFavorites, uiState.effectiveDockItems) {
        (uiState.effectiveFavorites + uiState.effectiveDockItems).sampleApps()
    }
    val builtInSamples = picker.builtInSamples.ifEmpty { homeSamples }.toImmutableList()
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
                item { SettingsSectionHeader(stringResource(R.string.icons_screen_icon_pack)) }
                item {
                    IconPackCard(
                        settings = settings,
                        picker = picker,
                        builtInSamples = builtInSamples,
                        onSelectBuiltIn = onSelectBuiltIn,
                        onIconRenderModeChange = onIconRenderModeChange,
                        onSelectPack = onSelectPack,
                        onPackFallbackChange = onPackFallbackChange,
                        onOpenPack = onOpenPack,
                        onFindPacks = onFindPacks,
                        onIconShapeChange = onIconShapeChange,
                    )
                }
            }
        },
    )
}

@Composable
private fun IconPackCard(
    settings: LauncherSettings,
    picker: IconPickerState,
    builtInSamples: ImmutableList<AppInfo>,
    onSelectBuiltIn: (IconRenderMode) -> Unit,
    onIconRenderModeChange: (IconRenderMode) -> Unit,
    onSelectPack: (String) -> Unit,
    onPackFallbackChange: (IconPackFallback) -> Unit,
    onOpenPack: (String) -> Unit,
    onFindPacks: () -> Unit,
    onIconShapeChange: (IconShape) -> Unit,
) {
    val appliedPack = settings.iconPackPackage
    val appliedOption = picker.packs.firstOrNull { it.pack.packageName == appliedPack }
    val appliedLabel = appliedOption?.pack?.label ?: appliedPack.orEmpty()
    SettingsCard {
        IconOptions(settings.iconRenderMode, appliedPack, picker, builtInSamples, onSelectBuiltIn, onSelectPack, onFindPacks)
        if (appliedPack != null) {
            CardDivider()
            AppliedPackRow(label = appliedLabel, coveredApps = appliedOption?.coveredApps, totalApps = picker.totalApps, onOpen = { onOpenPack(appliedPack) })
            CardDivider()
            LabeledDropdownRow(
                title = stringResource(R.string.icons_screen_pack_misses),
                options = IconPackFallback.entries,
                selected = settings.iconPackFallback,
                label = { stringResource(it.displayNameRes) },
                onSelect = onPackFallbackChange,
                testTag = "icons_pack_misses_row",
            )
        } else {
            StyleColourRows(settings.iconRenderMode, onIconRenderModeChange)
        }
        CardDivider()
        // The pack draws its own shape for the icons it covers, and themes the rest unless they keep their default.
        val packShapesEverything = appliedPack != null && settings.iconPackFallback == IconPackFallback.MATCH_PACK
        IconShapePicker(
            selected = settings.iconShape,
            onSelect = onIconShapeChange,
            enabled = !packShapesEverything,
            caption = if (packShapesEverything) stringResource(R.string.icons_screen_shape_set_by_pack, appliedLabel) else null,
        )
    }
}

/** The built-in style cards over the installed pack cards (or the offer to find some). */
@Composable
private fun IconOptions(
    mode: IconRenderMode,
    appliedPack: String?,
    picker: IconPickerState,
    builtInSamples: ImmutableList<AppInfo>,
    onSelectBuiltIn: (IconRenderMode) -> Unit,
    onSelectPack: (String) -> Unit,
    onFindPacks: () -> Unit,
) {
    val packs = remember(picker.packs) { picker.packs.toImmutableList() }
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val optionWidth = (maxWidth - OPTION_GAP * 3) / 4
        Column {
            OptionGroupLabel(stringResource(R.string.icons_screen_built_in))
            StyleCards(
                selected = mode.style.takeIf { appliedPack == null },
                colour = mode.colour ?: IconColour.ACCENT,
                background = mode.background,
                samples = builtInSamples,
                onSelect = { onSelectBuiltIn(IconRenderMode.of(it, mode.colour ?: IconColour.ACCENT, mode.background)) },
            )
            CardDivider()
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                OptionGroupLabel(stringResource(R.string.icons_screen_external))
                if (packs.isNotEmpty()) {
                    Text(
                        text = pluralStringResource(R.plurals.icons_screen_pack_count, packs.size, packs.size),
                        style = MaterialTheme.typography.labelSmall,
                        color = Muted,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }
            }
            when {
                packs.isNotEmpty() -> PackCards(packs, appliedPack, optionWidth, onSelectPack, onFindPacks)
                !picker.isLoading -> NoPacks(onFindPacks)
                else -> Box(modifier = Modifier.height(OPTION_BOX_HEIGHT + 36.dp))
            }
        }
    }
}

/** A built-in style's colour and background, where it offers a choice. */
@Composable
private fun StyleColourRows(mode: IconRenderMode, onIconRenderModeChange: (IconRenderMode) -> Unit) {
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
}

@Composable
private fun OptionGroupLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = Muted,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
    )
}

/** The first few distinct apps on Home, which have an icon to show — what the built-in cards show until the picker has loaded. */
private fun List<PlacedItem>.sampleApps(): List<AppInfo> =
    filterIsInstance<PlacedItem.SingleApp>().map { it.app }.filter { it.icon != null && !it.iconFromPack }.distinctBy { it.packageName }.take(SAMPLE_ICON_COUNT)

@Composable
private fun StyleCards(selected: IconStyle?, colour: IconColour, background: IconBackground, samples: ImmutableList<AppInfo>, onSelect: (IconStyle) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp).testTag("icons_style_cards"),
        horizontalArrangement = Arrangement.spacedBy(OPTION_GAP),
    ) {
        IconStyle.entries.forEach { style ->
            val mode = IconRenderMode.of(style, colour, background)
            // Theme inverted without a tile is that tone's own opposite, so it gets `Ink` behind it instead.
            val backdrop = if (mode.colour == IconColour.THEME_INVERTED && mode.background == IconBackground.NONE) Ink else SurfaceContainer
            OptionCard(
                label = stringResource(style.displayNameRes),
                selected = style == selected,
                backdrop = backdrop,
                onClick = { onSelect(style) },
                modifier = Modifier.weight(1f).testTag("icons_style_${style.name}"),
            ) {
                CompositionLocalProvider(LocalIconRenderMode provides mode) { SampleGrid(samples) }
            }
        }
    }
}

@Composable
private fun PackCards(packs: ImmutableList<IconPackOption>, appliedPack: String?, optionWidth: Dp, onSelectPack: (String) -> Unit, onFindPacks: () -> Unit) {
    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 12.dp).testTag("icons_pack_cards"),
        horizontalArrangement = Arrangement.spacedBy(OPTION_GAP),
    ) {
        items(packs, key = { it.pack.packageName }) { option ->
            OptionCard(
                label = option.pack.label,
                selected = option.pack.packageName == appliedPack,
                backdrop = SurfaceContainer,
                onClick = { onSelectPack(option.pack.packageName) },
                modifier = Modifier.width(optionWidth).testTag("icons_pack_${option.pack.packageName}"),
            ) {
                // A pack draws its icons as they are, whatever built-in style is stored.
                CompositionLocalProvider(LocalIconRenderMode provides IconRenderMode.SYSTEM_DEFAULT) {
                    if (option.samples.isNotEmpty()) SampleGrid(option.samples.toImmutableList()) else PackOwnIcon(option.pack)
                }
            }
        }
        item(key = "get_more") { GetMoreTile(onClick = onFindPacks, modifier = Modifier.width(optionWidth)) }
    }
}

@Composable
private fun PackOwnIcon(pack: IconPackInfo) {
    val icon = pack.icon
    if (icon != null) {
        Image(bitmap = icon, contentDescription = null, modifier = Modifier.size(SAMPLE_ICON_SIZE * 2))
    } else {
        Box(modifier = Modifier.size(SAMPLE_ICON_SIZE * 2).background(IconTile, IconShape.SQUIRCLE.toComposeShape()))
    }
}

/** One selectable option: a sample on [backdrop], ringed in the accent when [selected], over its [label]. */
@Composable
private fun OptionCard(label: String, selected: Boolean, backdrop: Color, onClick: () -> Unit, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val shape = MaterialTheme.shapes.medium
    Column(
        modifier = modifier
            .clip(shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(OPTION_BOX_HEIGHT)
                .clip(shape)
                .background(backdrop)
                .border(if (selected) 2.dp else 0.dp, if (selected) Accent else backdrop, shape),
            contentAlignment = Alignment.Center,
        ) { content() }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) Ink else Muted,
            maxLines = 1,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

@Composable
private fun SampleGrid(samples: ImmutableList<AppInfo>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        samples.chunked(2).ifEmpty { listOf(emptyList()) }.forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { pair.forEach { AppIcon(app = it, size = SAMPLE_ICON_SIZE, contentDescription = null) } }
        }
    }
}

/** The last card in the pack row: opens the Play Store's icon pack search. */
@Composable
private fun GetMoreTile(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = MaterialTheme.shapes.medium
    val outline = Muted
    val corner = with(LocalDensity.current) { 12.dp.toPx() }
    Column(
        modifier = modifier
            .clip(shape)
            .clickable(role = Role.Button, onClick = onClick)
            .testTag("icons_get_more"),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(OPTION_BOX_HEIGHT)
                .drawBehind {
                    drawRoundRect(
                        color = outline,
                        cornerRadius = CornerRadius(corner),
                        style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx()))),
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = null, tint = Muted)
        }
        Text(
            text = stringResource(R.string.icons_screen_get_more),
            style = MaterialTheme.typography.labelSmall,
            color = Muted,
            maxLines = 1,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

@Composable
private fun NoPacks(onFindPacks: () -> Unit) {
    Column(modifier = Modifier.padding(top = 4.dp, bottom = 8.dp).testTag("icons_no_packs")) {
        Text(text = stringResource(R.string.icons_screen_no_packs), style = MaterialTheme.typography.bodyMedium, color = Muted)
        TextActionButton(
            text = stringResource(R.string.icons_screen_find_packs),
            onClick = onFindPacks,
            modifier = Modifier.padding(top = 4.dp).testTag("icons_find_packs"),
        )
    }
}

@Composable
private fun AppliedPackRow(label: String, coveredApps: Int?, totalApps: Int, onOpen: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp).testTag("icons_applied_pack"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge, color = Ink)
            if (coveredApps != null && totalApps > 0) {
                Text(
                    text = stringResource(R.string.icons_screen_pack_coverage, coveredApps, totalApps),
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
            }
        }
        TextActionButton(
            text = stringResource(R.string.icons_screen_open_pack),
            onClick = onOpen,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.testTag("icons_open_pack"),
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 1000)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 1000, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun IconsScreenPreview() {
    FacetLauncherTheme {
        IconsContent(
            settings = LauncherSettings(),
            uiState = AppearanceSettingsUiState(globalFavorites = DesignTimeFavorites, globalDockItems = DesignTimeDockItems),
            picker = IconPickerState(isLoading = false),
            homeWallpaper = HomeWallpaper.Unavailable,
            onBack = {},
            onSelectBuiltIn = {},
            onIconRenderModeChange = {},
            onSelectPack = {},
            onPackFallbackChange = {},
            onOpenPack = {},
            onFindPacks = {},
            onIconShapeChange = {},
        )
    }
}

@Preview(name = "Pack applied", showBackground = true, widthDp = 390, heightDp = 1000)
@Preview(name = "Pack applied, dark", showBackground = true, widthDp = 390, heightDp = 1000, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun IconsScreenPackPreview() {
    val packs = listOf("Arcticons", "Lawnicons", "Delta").map { IconPackOption(IconPackInfo("pack.$it", it, icon = null), samples = emptyList(), coveredApps = 142) }
    FacetLauncherTheme {
        IconsContent(
            settings = LauncherSettings(iconPackPackage = "pack.Arcticons"),
            uiState = AppearanceSettingsUiState(globalFavorites = DesignTimeFavorites, globalDockItems = DesignTimeDockItems),
            picker = IconPickerState(packs = packs, totalApps = 168, isLoading = false),
            homeWallpaper = HomeWallpaper.Unavailable,
            onBack = {},
            onSelectBuiltIn = {},
            onIconRenderModeChange = {},
            onSelectPack = {},
            onPackFallbackChange = {},
            onOpenPack = {},
            onFindPacks = {},
            onIconShapeChange = {},
        )
    }
}

private val ICON_SHAPE_RING_WIDTH = 2.dp
private val ICON_SHAPE_RING_GAP = 2.dp

/** Icon outline picker — each option drawn in its own shape so Squircle vs Rounded square is visible, not just named. Greyed out with [caption] when an icon pack decides the shape. */
@Composable
internal fun IconShapePicker(selected: IconShape, onSelect: (IconShape) -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, caption: String? = null) {
    Column(modifier = modifier.padding(vertical = 8.dp).testTag("appearance_icon_shape_row")) {
        Text(
            text = stringResource(R.string.appearance_icon_shape),
            style = MaterialTheme.typography.bodyLarge,
            color = if (enabled) Ink else Muted,
        )
        if (caption != null) Text(text = caption, style = MaterialTheme.typography.bodySmall, color = Muted)
        Row(modifier = Modifier.fillMaxWidth().padding(top = 10.dp).alpha(if (enabled) 1f else 0.45f), horizontalArrangement = Arrangement.SpaceBetween) {
            IconShape.entries.forEach { shape ->
                IconShapeOption(shape = shape, isSelected = shape == selected, enabled = enabled, onSelect = { onSelect(shape) })
            }
        }
    }
}

@Composable
private fun IconShapeOption(shape: IconShape, isSelected: Boolean, enabled: Boolean, onSelect: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .testTag("icon_shape_${shape.name}")
            .clip(MaterialTheme.shapes.small)
            .selectable(selected = isSelected, enabled = enabled, role = Role.RadioButton, onClick = onSelect)
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
