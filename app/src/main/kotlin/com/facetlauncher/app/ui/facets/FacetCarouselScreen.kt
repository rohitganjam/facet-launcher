package com.facetlauncher.app.ui.facets

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.facetlauncher.app.R
import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppListColumnAlignment
import com.facetlauncher.app.data.model.AppListGridColumns
import com.facetlauncher.app.data.model.AppListGridDisplayMode
import com.facetlauncher.app.data.model.AppListLayout
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.model.CalendarEvent
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.ClockDateStyle
import com.facetlauncher.app.data.model.ClockFontOption
import com.facetlauncher.app.data.model.LauncherFontOption
import com.facetlauncher.app.data.model.ClockTemplateId
import com.facetlauncher.app.data.model.DockDisplayMode
import com.facetlauncher.app.data.model.DrawerPresentation
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.data.model.FontWeightOption
import com.facetlauncher.app.data.model.HomeWallpaper
import com.facetlauncher.app.data.model.NotificationBadgeStyle
import com.facetlauncher.app.data.model.toColumnPositions
import com.facetlauncher.app.ui.components.AppIcon
import com.facetlauncher.app.ui.components.AppIconSize
import com.facetlauncher.app.ui.components.ConfirmDialog
import com.facetlauncher.app.ui.components.ScreenHeader
import com.facetlauncher.app.ui.components.TonalButton
import com.facetlauncher.app.ui.components.WallpaperBackground
import com.facetlauncher.app.ui.home.AppRow
import com.facetlauncher.app.ui.home.ClockBlock
import com.facetlauncher.app.ui.home.DockIcon
import com.facetlauncher.app.ui.home.FolderRow
import com.facetlauncher.app.ui.home.FolderTileGlyph
import com.facetlauncher.app.ui.home.HOME_CLOCK_DEFAULT_TOP_OFFSET
import com.facetlauncher.app.ui.home.HOME_CLOCK_MIN_GAP
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.Faint
import com.facetlauncher.app.ui.theme.Hairline
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.FacetType
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.Surface
import com.facetlauncher.app.ui.theme.SurfaceContainer
import com.facetlauncher.app.ui.theme.resolve
import kotlin.math.abs

private val CAROUSEL_PAGE_SPACING = 8.dp

/**
 * The preview card is a scale model of the real screen — this fraction of the device's own
 * width *and* height — so it keeps whatever aspect ratio the current phone has, on any phone.
 * Everything else about the carousel's footprint derives from it: the horizontal inset (the
 * strip of each neighbour card that peeks in) is `(1 − scale) / 2` of the screen width, and the
 * pager's height is `scale` of the screen height plus [CAROUSEL_PAGE_CHROME_HEIGHT]. The pager
 * is content-sized (not `weight(1f)`), so the dots + Launcher-settings row pack directly beneath
 * it and the leftover space collects at the bottom (a trailing weighted spacer).
 */
private const val CAROUSEL_CARD_SCALE = 0.55f

/** The facet-name row above the card + the gear/trash row below it, inside every page. */
private val CAROUSEL_PAGE_CHROME_HEIGHT = 104.dp

/** The carousel sits over Home as a translucent overlay — Home (and the wallpaper) show through. */
private const val CAROUSEL_SCRIM_ALPHA = 0.6f

/**
 * Switch Facets carousel (`3a`, `4n`, `4o`): a follow-finger panel to Home's right (see
 * [com.facetlauncher.app.ui.launcher.HomeDrawerRoute] — this screen is never a `NavHost`
 * destination, it's always composed and just offset off-screen when closed, exactly like the
 * Hub). Tapping a card applies it and returns Home ([onFacetApply]); [onReorderFacets]
 * opens the standalone Manage Facets screen.
 *
 * This screen doesn't own opening/closing itself — its host does, via
 * [com.facetlauncher.app.ui.launcher.HomeDrawerRoute]'s own `facetAxis` — except for one
 * gesture it's uniquely positioned to detect: a rightward swipe
 * that lands *on the pager* while it's settled on the first facet's page. There's no previous
 * facet to browse to there, so without this, the pager would just eat the drag as a dead
 * overscroll before the host's own empty-space detector ever saw it. [onDismissDrag] forwards
 * that drag's raw signed pixel deltas to the host (which feeds them into the same `facetAxis`
 * a swipe in empty space would), and [onDismissDragEnd] tells it the gesture ended so it can
 * settle open/closed. Swiping right from any *other* page still just browses backward.
 */
@Composable
fun FacetCarouselScreen(
    onFacetApply: () -> Unit,
    onEditFacet: (facetId: Long) -> Unit,
    onReorderFacets: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onDismissDrag: (deltaPx: Float) -> Unit,
    onDismissDragEnd: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FacetCarouselViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    FacetCarouselContent(
        uiState = uiState,
        onSelect = { facetId -> viewModel.selectFacet(facetId); onFacetApply() },
        onEditFacet = onEditFacet,
        onAddFacet = viewModel::addFacet,
        onDeleteFacet = viewModel::deleteFacet,
        onReorderFacets = onReorderFacets,
        onNavigateToSettings = onNavigateToSettings,
        onDismissDrag = onDismissDrag,
        onDismissDragEnd = onDismissDragEnd,
        modifier = modifier,
    )
}

@Composable
private fun FacetCarouselContent(
    uiState: FacetCarouselUiState,
    onSelect: (Long) -> Unit,
    onEditFacet: (Long) -> Unit,
    onAddFacet: () -> Unit,
    onDeleteFacet: (FacetEntity) -> Unit,
    onReorderFacets: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onDismissDrag: (deltaPx: Float) -> Unit,
    onDismissDragEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val facets = uiState.facets
    if (facets.isEmpty()) {
        // EnsureActiveFacetUseCase seeds the first facet on app launch — only briefly empty
        // before that resolves. Paint the translucent scrim so the overlay reads as opaque-ish
        // from the first frame of its fade-in.
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(SurfaceContainer.copy(alpha = CAROUSEL_SCRIM_ALPHA)),
        )
        return
    }

    val pageCount = facets.size + if (uiState.canAddFacet) 1 else 0
    val initialPage = facets.indexOfFirst { it.id == uiState.activeFacetId }.coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = initialPage) { pageCount }
    var deletingFacet by remember { mutableStateOf<FacetEntity?>(null) }

    val config = LocalConfiguration.current
    val currentOnDismissDrag by rememberUpdatedState(onDismissDrag)
    val currentOnDismissDragEnd by rememberUpdatedState(onDismissDragEnd)
    // Rightward drags that land on the pager while it's settled on the first page have nowhere to
    // browse to — take them here before the pager turns them into a dead overscroll, and forward
    // them to the host's own facetAxis instead (see this file's top-level doc). A gesture that
    // ever moved the pager (a real browse from another page) is left alone for its whole
    // duration, even once it reaches page 0, so flinging back through the first page doesn't turn
    // into a close. Empty-space drags never reach here at all — the pager is the only scrollable
    // in this screen, so nothing else dispatches into this connection.
    val dismissOnSwipeBack = remember(pagerState) {
        object : NestedScrollConnection {
            var gestureMovedPager = false
            var didDismissDrag = false

            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source != NestedScrollSource.Drag) return Offset.Zero
                val settledOnFirstPage = pagerState.currentPage == 0 &&
                    abs(pagerState.currentPageOffsetFraction) < 0.0001f
                if (!settledOnFirstPage) gestureMovedPager = true
                return if (available.x > 0f && settledOnFirstPage && !gestureMovedPager) {
                    didDismissDrag = true
                    currentOnDismissDrag(available.x)
                    Offset(available.x, 0f)
                } else {
                    Offset.Zero
                }
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (didDismissDrag) currentOnDismissDragEnd()
                gestureMovedPager = false
                didDismissDrag = false
                return Velocity.Zero
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            // Translucent overlay on Home — see CAROUSEL_SCRIM_ALPHA.
            .background(SurfaceContainer.copy(alpha = CAROUSEL_SCRIM_ALPHA))
            .testTag("facet_carousel_screen")
            .nestedScroll(dismissOnSwipeBack)
            .windowInsetsPadding(WindowInsets.systemBars),
    ) {
        FacetCarouselHeader(
            facetCount = facets.size,
            canReorder = facets.size > 1,
            onReorderClick = onReorderFacets,
        )

        // The card mirrors the real screen at CAROUSEL_CARD_SCALE, so its width is that
        // fraction of the screen width and the leftover half on each side is the neighbour peek.
        val screenAspectRatio = config.screenWidthDp.toFloat() / config.screenHeightDp.toFloat()
        val pageInset = (config.screenWidthDp * (1f - CAROUSEL_CARD_SCALE) / 2f).dp
        val pagerHeight = (config.screenHeightDp * CAROUSEL_CARD_SCALE).dp + CAROUSEL_PAGE_CHROME_HEIGHT
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = pageInset),
            pageSpacing = CAROUSEL_PAGE_SPACING,
            // Flush against the header, no extra top gap — matches the Hub's own header-to-grid
            // transition (HubScreen.kt's content starts exactly at headerHeight, zero padding).
            modifier = Modifier
                .fillMaxWidth()
                .height(pagerHeight)
                .testTag("facet_carousel_pager"),
        ) { page ->
            if (page < facets.size) {
                val facet = facets[page]
                // Recent-apps style: tapping any visible card — centered or peeking —
                // applies it immediately. Browsing (without applying) is swipe-only.
                FacetPreviewPage(
                    facet = facet,
                    isActive = facet.id == uiState.activeFacetId,
                    favorites = uiState.previewsByFacetId[facet.id]?.favorites.orEmpty(),
                    clockTemplateId = uiState.clockTemplateId(facet.id),
                    clockFontOption = uiState.clockFontOption(facet.id),
                    clockColorOption = uiState.clockColorOption(facet.id),
                    clockAccentColorOption = uiState.clockAccentColorOption(facet.id),
                    use24HourTime = uiState.effectiveUse24HourTime(facet.id),
                    clockShowMeridiem = uiState.clockShowMeridiem(facet.id),
                    clockDateStyle = uiState.clockDateStyle(facet.id),
                    clockAlignment = uiState.clockAlignment(facet.id),
                    clockScale = uiState.clockScale(facet.id),
                    clockZoneHeightDp = uiState.clockZoneHeightDp(facet.id),
                    calendarEvents = uiState.previewsByFacetId[facet.id]?.calendarEvents.orEmpty(),
                    calendarColors = uiState.globalSettings.calendarColors,
                    launcherFontOption = uiState.globalSettings.launcherFontOption,
                    appRowPosition = uiState.appRowPosition(facet.id),
                    appRowPresentation = uiState.appRowPresentation(facet.id),
                    appListLayout = uiState.appListLayout(facet.id),
                    appListColumnAlignment = uiState.appListColumnAlignment(facet.id),
                    appListGridColumns = uiState.appListGridColumns(facet.id),
                    appListGridDisplayMode = uiState.appListGridDisplayMode(facet.id),
                    appLabelColorOption = uiState.globalSettings.appLabelColorOption,
                    homeAppsFontWeight = uiState.globalSettings.homeAppsFontWeight,
                    dockApps = uiState.previewsByFacetId[facet.id]?.dockApps.orEmpty(),
                    dockDisplayMode = uiState.dockDisplayMode(facet.id),
                    canDelete = uiState.canDeleteFacet,
                    onEditFacetClick = { onEditFacet(facet.id) },
                    onDeleteClick = { deletingFacet = facet },
                    onCardClick = { onSelect(facet.id) },
                    homeWallpaper = uiState.homeWallpaper,
                    screenAspectRatio = screenAspectRatio,
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("facet_page_${facet.id}"),
                )
            } else {
                AddFacetPage(
                    onClick = onAddFacet,
                    modifier = Modifier.fillMaxSize().testTag("facet_carousel_add_page"),
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            facets.indices.forEach { index ->
                val active = index == pagerState.currentPage
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(width = if (active) 16.dp else 5.dp, height = 5.dp)
                        .clip(RoundedCornerShape(2.5.dp))
                        .background(if (active) Accent else Faint),
                )
            }
        }

        LauncherSettingsRow(
            onClick = onNavigateToSettings,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 20.dp),
        )

        // Leftover space collects below the whole cluster, not as a dead band inside it.
        Spacer(modifier = Modifier.weight(1f))
    }

    deletingFacet?.let { facet ->
        ConfirmDialog(
            title = stringResource(R.string.facet_carousel_delete_title, facet.name),
            message = stringResource(R.string.facet_carousel_delete_message),
            confirmLabel = stringResource(R.string.action_delete),
            onConfirm = { onDeleteFacet(facet); deletingFacet = null },
            onDismiss = { deletingFacet = null },
        )
    }
}

/**
 * "Switch Facets" — [ScreenHeader] with its default `onWallpaper = false`, since this screen
 * already sits on its own dimming [CAROUSEL_SCRIM_ALPHA] scrim rather than directly on the raw
 * wallpaper (unlike [com.facetlauncher.app.ui.hub.HubHeader], which uses `onWallpaper = true`).
 */
@Composable
private fun FacetCarouselHeader(facetCount: Int, canReorder: Boolean, onReorderClick: () -> Unit, modifier: Modifier = Modifier) {
    ScreenHeader(
        title = stringResource(R.string.facet_carousel_header_title),
        subtitle = pluralStringResource(R.plurals.facet_carousel_count, facetCount, facetCount),
        modifier = modifier,
        trailingAction = {
            TonalButton(
                text = stringResource(R.string.facet_carousel_reorder),
                enabled = canReorder,
                onClick = onReorderClick,
                modifier = Modifier.testTag("facet_carousel_reorder"),
            )
        },
    )
}

/**
 * The launcher-wide settings entry point, sitting just below the carousel. Carries the same
 * shadow + hairline-border lift as [com.facetlauncher.app.ui.components.SettingsCard] (a bare
 * [Surface] fill is invisible against this screen's dimmer backdrop, especially in dark mode) and
 * a leading [Icons.Default.Tune] glyph — `Tune`, not a gear, so it doesn't read as a second
 * "facet settings" control next to each card's own gear.
 */
@Composable
private fun LauncherSettingsRow(onClick: () -> Unit, modifier: Modifier = Modifier) {
    // M3's Card default shape — see CLAUDE.md's Material 3 shape section.
    val shape = MaterialTheme.shapes.medium
    Row(
        modifier = modifier
            .shadow(elevation = 4.dp, shape = shape)
            .clip(shape)
            .background(Surface)
            .border(1.dp, Ink.copy(alpha = 0.14f), shape)
            .clickable(onClick = onClick)
            .testTag("facet_carousel_launcher_settings")
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = null,
                tint = Muted,
                modifier = Modifier.padding(end = 14.dp).size(24.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = stringResource(R.string.facet_carousel_launcher_settings), style = MaterialTheme.typography.bodyLarge, color = Ink)
                Text(text = stringResource(R.string.facet_carousel_launcher_settings_subtitle), style = MaterialTheme.typography.bodyMedium, color = Muted)
            }
        }
        Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Ink)
    }
}

/**
 * A live snapshot of what [facet]'s Home screen actually looks like — reuses the real
 * [ClockBlock]/[AppRow]/[DockIcon] composables (not a redrawn lookalike) and renders each
 * facet's own effective content ([favorites]/[calendarEvents], from
 * [com.facetlauncher.app.domain.ObserveFacetPreviewsUseCase], one Flow per facet so browsing
 * shows each facet's own list, not just the active facet's), so the carousel card is a
 * genuine preview rather than a placeholder.
 */
@Composable
private fun FacetPreviewPage(
    facet: FacetEntity,
    /** Whether this is the facet currently applied to Home — distinct from which page the pager is centered on. */
    isActive: Boolean,
    favorites: List<PlacedItem>,
    clockTemplateId: ClockTemplateId,
    clockFontOption: ClockFontOption,
    clockColorOption: ClockColorOption,
    clockAccentColorOption: ClockColorOption,
    use24HourTime: Boolean,
    clockShowMeridiem: Boolean,
    clockDateStyle: ClockDateStyle,
    /** This facet's own clock alignment, falling back to the global default when not overriding (see [com.facetlauncher.app.data.model.LauncherSettings.clockAlignment]). */
    clockAlignment: ClockAlignment,
    /** This facet's own clock scale — see [com.facetlauncher.app.data.model.LauncherSettings.clockScale]. */
    clockScale: Float = 0.8f,
    /** This facet's own clock zone height, falling back to the global default when not overriding (see [com.facetlauncher.app.data.model.LauncherSettings.clockZoneHeightDp]) — reproduces the same clock-position formula [com.facetlauncher.app.ui.home.HomeScreen] uses, scaled to this card's own size. */
    clockZoneHeightDp: Float?,
    calendarEvents: List<CalendarEvent>,
    calendarColors: Map<String, String>,
    launcherFontOption: LauncherFontOption,
    appRowPosition: AppRowPosition,
    appRowPresentation: AppRowPresentation,
    appListLayout: AppListLayout,
    appListColumnAlignment: AppListColumnAlignment,
    appListGridColumns: AppListGridColumns,
    appListGridDisplayMode: AppListGridDisplayMode,
    appLabelColorOption: ClockColorOption,
    homeAppsFontWeight: FontWeightOption,
    dockApps: List<PlacedItem>,
    dockDisplayMode: DockDisplayMode,
    canDelete: Boolean,
    onEditFacetClick: () -> Unit,
    onDeleteClick: () -> Unit,
    /** Tapping the card anywhere applies its facet (see chat history) — including on top of a
     *  favorite row or dock icon, whose own [AppRow]/[DockIcon] click would otherwise consume the
     *  touch instead of letting it reach the card's own clickable underneath. */
    onCardClick: () -> Unit,
    /** The live system wallpaper, painted behind this card's content the same way it sits behind real Home. */
    homeWallpaper: HomeWallpaper,
    /** The device's own width:height — the card is locked to it so it stays a true scale model of the screen. */
    screenAspectRatio: Float,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Sits right above the card itself (on the carousel's own backdrop, not the card's
        // Surface background) but still belongs to this page — swiping to a neighbor swaps in
        // that page's own name along with its card, per direct request (see chat history). The
        // Facet settings/Delete icons sit below the card instead (see chat history —
        // moved back down from here, mirroring this screen's original gear/trash row below the
        // pager), so this row is name-only now.
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = facet.name,
                style = MaterialTheme.typography.titleMedium,
                color = Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false).testTag("facet_page_name_${facet.id}"),
            )
            if (isActive) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = stringResource(R.string.facet_carousel_active),
                    tint = Accent,
                    modifier = Modifier.padding(start = 6.dp).testTag("facet_page_active_${facet.id}"),
                )
            }
        }

        // A large hero surface — M3's Dialog/ModalBottomSheet-class extraLarge shape, see
        // CLAUDE.md's Material 3 shape section.
        val cardShape = MaterialTheme.shapes.extraLarge
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                // Locked to the real screen's proportions — a genuine scale model, not a card
                // shape that happens to fall out of the surrounding layout.
                .aspectRatio(screenAspectRatio)
                // Shadow + hairline border to lift it off the page, same as SettingsCard —
                // the Surface/SurfaceContainer tone step alone is too small to read as raised.
                .shadow(elevation = 4.dp, shape = cardShape)
                .clip(cardShape)
                .border(width = if (isActive) 2.dp else 1.dp, color = if (isActive) Accent else Hairline, shape = cardShape)
                // Card-only tap/ripple target — kept off the name+menu header above so a
                // long-press ripple doesn't bleed across the whole page (see chat history).
                .clickable(onClick = onCardClick),
        ) {
            // The real system wallpaper, behind the content — the same backdrop the facet's
            // Home screen actually shows, since a card can't get it from the window compositor.
            WallpaperBackground(homeWallpaper, Modifier.matchParentSize())

            // This card renders at a fraction of Home's real on-screen size (it's the screen
            // scaled by [CAROUSEL_CARD_SCALE]). Rather than hand-picking fixed dp overrides for
            // every size in here (which needed re-tuning by eye each time — see chat history), we
            // measure how tall this card actually ends up on screen, compare it to the real
            // screen height, and use that ratio to shrink every dp/sp value inside proportionally
            // via a scaled LocalDensity — so all of the card's content (icon
            // sizes, text sizes, paddings) scales down together, consistently.
            val screenHeight = LocalConfiguration.current.screenHeightDp.dp
            val contentScale = (maxHeight / screenHeight).coerceIn(0.4f, 1f)
            val baseDensity = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(baseDensity.density * contentScale, baseDensity.fontScale),
            ) {
                // Reproduces HomeScreen's own clockZoneHeightDp formula (dp values are
                // density-independent, so the same numbers are meaningful here even though this
                // card renders under the scaled density above) — a null value (never dragged)
                // resolves to the clock's original fixed top offset, same as the real Home screen;
                // a persisted value pushes the clock down by exactly as much here, proportionally
                // shrunk along with everything else in this card.
                var clockNaturalHeightDp by remember { mutableFloatStateOf(0f) }
                val previewDensity = LocalDensity.current
                val topOffsetDp = clockZoneHeightDp
                    ?.let { (it - HOME_CLOCK_MIN_GAP.value - clockNaturalHeightDp).coerceAtLeast(0f) }
                    ?: HOME_CLOCK_DEFAULT_TOP_OFFSET.value
                Column(
                    modifier = Modifier.fillMaxSize().padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    ClockBlock(
                        use24HourTime = use24HourTime,
                        templateId = clockTemplateId,
                        fontOption = clockFontOption,
                        colorOption = clockColorOption,
                        accentColorOption = clockAccentColorOption,
                        showMeridiem = clockShowMeridiem,
                        dateStyle = clockDateStyle,
                        clockAlignment = clockAlignment,
                        events = calendarEvents,
                        calendarColors = calendarColors,
                        homeAppsFontWeight = homeAppsFontWeight,
                        appLabelColorOption = appLabelColorOption,
                        launcherFontOption = launcherFontOption,
                        clockScale = clockScale,
                        modifier = Modifier
                            .align(clockAlignment.resolve())
                            .padding(top = topOffsetDp.dp)
                            .onGloballyPositioned { clockNaturalHeightDp = with(previewDensity) { it.size.height.toDp().value } }
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    if (favorites.isEmpty()) {
                        Text(text = stringResource(R.string.facet_carousel_no_favorites), style = MaterialTheme.typography.bodyMedium, color = Muted, modifier = Modifier.fillMaxWidth())
                    } else {
                        FacetPreviewAppList(
                            // Capped the same way AppearancePreviewCard is — this card is the same
                            // CAROUSEL_CARD_SCALE-sized fixed space, never designed to render all
                            // of AppListLimits.MAX_FAVORITES real rows in single-column mode (see
                            // that cap's own doc comment for the full reasoning).
                            favorites = favorites.take(appListLayout.previewItemCap(appListGridColumns.columns)),
                            appListLayout = appListLayout,
                            appListColumnAlignment = appListColumnAlignment,
                            appListGridColumns = appListGridColumns,
                            appListGridDisplayMode = appListGridDisplayMode,
                            appRowPosition = appRowPosition,
                            appRowPresentation = appRowPresentation,
                            labelColor = appLabelColorOption.resolve(),
                            labelFontWeight = homeAppsFontWeight,
                            onCardClick = onCardClick,
                        )
                    }

                    // Dock is shared across all facets (not per-facet); omit-when-empty. Uses
                    // Home's own real 16dp gap — the scaled LocalDensity above shrinks it along
                    // with everything else, so no fixed override is needed (see chat history).
                    Spacer(modifier = Modifier.height(16.dp))
                    if (dockApps.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            dockApps.forEach { item ->
                                DockIcon(
                                    item = item,
                                    displayMode = dockDisplayMode,
                                    onClick = { onCardClick() },
                                    labelColor = appLabelColorOption.resolve(),
                                    labelFontWeight = homeAppsFontWeight.resolve(),
                                    // Same read-only-preview reasoning as the AppRow above.
                                    enableLongPressMenu = false,
                                )
                            }
                        }
                    }
                }
            }
        }

        // Below the card, not above it with the name — mirrors this screen's original gear/trash
        // icon row sitting below the pager (see chat history), just per-page now instead of tied
        // to a shared "centered facet" concept. Two direct icon buttons, not a "..." overflow
        // menu — with only two actions and both worth surfacing at a glance, the extra tap to
        // open a menu added nothing (see chat history).
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(26.dp)) {
                IconButton(
                    onClick = onEditFacetClick,
                    modifier = Modifier.testTag("facet_page_settings_${facet.id}"),
                ) {
                    Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.facet_carousel_facet_settings), tint = Muted)
                }
                IconButton(
                    onClick = onDeleteClick,
                    enabled = canDelete,
                    modifier = Modifier.testTag("facet_page_delete_${facet.id}"),
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(R.string.facet_carousel_delete_facet),
                        tint = if (canDelete) Muted else Faint,
                    )
                }
            }
        }
    }
}

/**
 * [FacetPreviewPage]'s app-list region — the same 3-way [AppListLayout] branch
 * `AppearanceSettingsScreen`'s own `AppearancePreviewAppList` uses, reimplemented here rather than
 * shared (this card owns a tap-to-apply [onCardClick] that composable's read-only preview doesn't
 * — see [FacetPreviewPage]'s own doc for why the two preview surfaces aren't shared code).
 */
@Composable
private fun FacetPreviewAppList(
    favorites: List<PlacedItem>,
    appListLayout: AppListLayout,
    appListColumnAlignment: AppListColumnAlignment,
    appListGridColumns: AppListGridColumns,
    appListGridDisplayMode: AppListGridDisplayMode,
    appRowPosition: AppRowPosition,
    appRowPresentation: AppRowPresentation,
    labelColor: Color,
    labelFontWeight: FontWeightOption,
    onCardClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        when (appListLayout) {
            AppListLayout.GRID -> {
                // Fixed-slot columns, not Arrangement.SpaceEvenly — see AppearancePreviewAppList's
                // own doc for why (a short last row must pack left, not stretch across the width).
                favorites.chunked(appListGridColumns.columns).forEach { rowItems ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        repeat(appListGridColumns.columns) { columnIndex ->
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                rowItems.getOrNull(columnIndex)?.let { item ->
                                    FacetPreviewGridTile(
                                        item = item,
                                        displayMode = appListGridDisplayMode,
                                        labelColor = labelColor,
                                        labelFontWeight = labelFontWeight.resolve(),
                                        onClick = onCardClick,
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            AppListLayout.TWO_COLUMN -> {
                val (leftPosition, rightPosition) = appListColumnAlignment.toColumnPositions()
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf(
                        favorites.filterIndexed { index, _ -> index % 2 == 0 } to leftPosition,
                        favorites.filterIndexed { index, _ -> index % 2 == 1 } to rightPosition,
                    ).forEach { (items, position) ->
                        Column(modifier = Modifier.weight(1f)) {
                            items.forEach { item ->
                                FacetPreviewRow(item = item, position = position, presentation = appRowPresentation, labelColor = labelColor, labelFontWeight = labelFontWeight, onClick = onCardClick)
                            }
                        }
                    }
                }
            }
            AppListLayout.SINGLE_COLUMN, AppListLayout.LAUNCHER_DEFAULT -> {
                favorites.forEach { item ->
                    FacetPreviewRow(item = item, position = appRowPosition, presentation = appRowPresentation, labelColor = labelColor, labelFontWeight = labelFontWeight, onClick = onCardClick)
                }
            }
        }
    }
}

/** [FacetPreviewAppList]'s single/two-column row dispatch — identical [AppRow]/[FolderRow] call either layout needs, just with a different [position] and item subset. */
@Composable
private fun FacetPreviewRow(item: PlacedItem, position: AppRowPosition, presentation: AppRowPresentation, labelColor: Color, labelFontWeight: FontWeightOption, onClick: () -> Unit) {
    when (item) {
        is PlacedItem.SingleApp -> AppRow(
            app = item.app,
            onClick = onClick,
            badgeCount = null,
            badgeStyle = NotificationBadgeStyle.DOT,
            onRequestShortcuts = { emptyList() },
            onLaunchShortcut = {},
            onAppInfo = {},
            position = position,
            presentation = presentation,
            labelColor = labelColor,
            labelFontWeight = labelFontWeight.resolve(),
            // This card is a read-only preview of a facet's Home layout, not a place to manage
            // apps — long-press must not open the real Uninstall/App Info/shortcuts menu.
            enableLongPressMenu = false,
            // Uses AppRow's own real Home density (16dp) — the scaled LocalDensity above shrinks
            // it proportionally already, so no fixed override is needed here.
            // This card can be present in the same semantics tree as Home's own real AppRow for
            // the same favorite app — a distinct prefix keeps onNodeWithTag lookups unambiguous.
            testTagPrefix = "facet_preview_",
        )
        is PlacedItem.FolderItem -> FolderRow(
            folder = item.folder,
            onAppClick = {},
            onRequestShortcuts = { emptyList() },
            onLaunchShortcut = {},
            onAppInfo = {},
            onRemoveFromFolder = { _, _ -> },
            onRenameFolder = { _, _ -> },
            drawerPresentation = DrawerPresentation.LIST,
            position = position,
            presentation = presentation,
            labelColor = labelColor,
            labelFontWeight = labelFontWeight.resolve(),
            enableLongPressMenu = false,
            onClick = onClick,
            testTagPrefix = "facet_preview_",
        )
    }
}

/** [FacetPreviewAppList]'s grid-mode tile — tappable (applies the facet like every other spot on this card), unlike `AppearancePreviewCard`'s own read-only grid tile. See [AppListGridDisplayMode]'s own doc for why this is a strict icon-or-text branch. */
@Composable
private fun FacetPreviewGridTile(item: PlacedItem, displayMode: AppListGridDisplayMode, labelColor: Color, labelFontWeight: FontWeight, onClick: () -> Unit) {
    val tagSuffix = when (item) {
        is PlacedItem.SingleApp -> item.app.packageName
        is PlacedItem.FolderItem -> "folder_${item.folder.id}"
    }
    Box(modifier = Modifier.clickable(onClick = onClick).padding(vertical = 4.dp), contentAlignment = Alignment.Center) {
        val label = when (item) {
            is PlacedItem.SingleApp -> item.app.label
            is PlacedItem.FolderItem -> item.folder.name
        }
        if (displayMode == AppListGridDisplayMode.TEXT) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = labelColor,
                maxLines = 1,
                fontWeight = labelFontWeight,
                modifier = Modifier.testTag("facet_preview_grid_label_$tagSuffix"),
            )
        } else {
            when (item) {
                is PlacedItem.SingleApp -> AppIcon(icon = item.app.icon, size = AppIconSize.TILE, contentDescription = label, modifier = Modifier.testTag("facet_preview_grid_icon_$tagSuffix"))
                is PlacedItem.FolderItem -> FolderTileGlyph(folder = item.folder)
            }
        }
    }
}

/**
 * How many real favorites [FacetPreviewAppList]/`AppearancePreviewCard`'s own scaled preview card
 * can show before crowding out the clock/dock — proven at [PREVIEW_ROW_BUDGET] rows for single
 * column. [AppListLayout.TWO_COLUMN]/[AppListLayout.GRID] pack more than one item per row, so the
 * same row budget lets them show proportionally more of the real list — in practice enough to
 * show every one of [com.facetlauncher.app.data.model.AppListLimits.MAX_FAVORITES] without
 * truncating at all.
 */
private const val PREVIEW_ROW_BUDGET = 6

private fun AppListLayout.previewItemCap(gridColumns: Int): Int = when (this) {
    AppListLayout.GRID -> PREVIEW_ROW_BUDGET * gridColumns
    AppListLayout.TWO_COLUMN -> PREVIEW_ROW_BUDGET * 2
    AppListLayout.SINGLE_COLUMN, AppListLayout.LAUNCHER_DEFAULT -> PREVIEW_ROW_BUDGET
}

@Composable
private fun AddFacetPage(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize()) {
        // Invisible stand-in for FacetPreviewPage's own name row — same Text structure so it
        // measures to the exact same height, without hardcoding a dp guess. Without this, the
        // Add-facet card (which has no header of its own) would start higher up than its
        // neighboring facet cards and read as a different, taller size (see chat history).
        Text(
            text = "",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 8.dp).alpha(0f),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                // A large hero surface — M3's Dialog/ModalBottomSheet-class extraLarge shape, see
                // CLAUDE.md's Material 3 shape section. Shadow + hairline border to match the
                // facet preview cards.
                .shadow(elevation = 4.dp, shape = MaterialTheme.shapes.extraLarge)
                .clip(MaterialTheme.shapes.extraLarge)
                .background(Surface)
                .border(1.dp, Hairline, MaterialTheme.shapes.extraLarge)
                .clickable(onClick = onClick)
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(width = 96.dp, height = 70.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = stringResource(R.string.facet_carousel_add_glyph), style = FacetType.clock.copy(fontSize = 34.sp), color = Faint)
            }
            Text(text = stringResource(R.string.facet_carousel_add_facet), style = MaterialTheme.typography.bodyLarge, color = Ink, modifier = Modifier.padding(top = 8.dp))
            Text(
                text = stringResource(R.string.facet_carousel_add_facet_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = Muted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        // Invisible stand-in for FacetPreviewPage's own bottom menu row — same reasoning as the
        // header spacer above, now that the menu lives below the card instead of beside the name.
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).alpha(0f),
            horizontalArrangement = Arrangement.Center,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(26.dp)) {
                IconButton(onClick = {}, enabled = false) {
                    Icon(Icons.Default.Settings, contentDescription = null)
                }
                IconButton(onClick = {}, enabled = false) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FacetCarouselScreenPreview() {
    FacetLauncherTheme {
        FacetCarouselContent(
            uiState = FacetCarouselUiState(
                facets = listOf(
                    FacetEntity(id = 1, name = "Facet 1", position = 0),
                    FacetEntity(id = 2, name = "Work", position = 1),
                ),
                activeFacetId = 1,
            ),
            onSelect = {},
            onEditFacet = {},
            onAddFacet = {},
            onDeleteFacet = {},
            onReorderFacets = {},
            onNavigateToSettings = {},
            onDismissDrag = {},
            onDismissDragEnd = {},
        )
    }
}
