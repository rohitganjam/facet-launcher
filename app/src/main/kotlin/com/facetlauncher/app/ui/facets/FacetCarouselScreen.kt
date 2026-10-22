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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.model.CalendarDisplayOptions
import com.facetlauncher.app.data.model.HomeWallpaper
import com.facetlauncher.app.data.model.ProReason
import com.facetlauncher.app.ui.components.ConfirmDialog
import com.facetlauncher.app.ui.components.ProPill
import com.facetlauncher.app.ui.components.ScreenHeader
import com.facetlauncher.app.ui.components.SettingsCard
import com.facetlauncher.app.ui.components.SettingsIconBadge
import com.facetlauncher.app.ui.components.TonalButton
import com.facetlauncher.app.ui.home.AppRow
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.domain.ResolvedFacetStyle
import com.facetlauncher.app.ui.home.HomeScreenContent
import com.facetlauncher.app.ui.home.ScaledHomeScreen
import com.facetlauncher.app.ui.home.DockIcon
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.settings.ClickableRow
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.FacetType
import com.facetlauncher.app.ui.theme.Faint
import com.facetlauncher.app.ui.theme.Hairline
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.SettingsSectionHue
import com.facetlauncher.app.ui.theme.Surface
import com.facetlauncher.app.ui.theme.SurfaceContainer
import kotlin.math.abs

private val CAROUSEL_PAGE_SPACING = 8.dp

/**
 * The preview card is a scale model of the real screen — this fraction of the device's own
 * width *and* height — so it keeps whatever aspect ratio the current device has. It is an
 * upper bound: the dots + Launcher-settings row are measured first and the card shrinks (keeping
 * the aspect ratio) to fit the space left, so those rows are always visible on wide or short
 * screens. The horizontal inset (the strip of each neighbour card that peeks in) is whatever
 * width the fitted card doesn't use, and the pager is the card plus [CAROUSEL_PAGE_CHROME_HEIGHT],
 * so the dots + settings row pack directly beneath it.
 */
private const val CAROUSEL_CARD_SCALE = 0.55f

/** Floor for the fitted card in split-screen or very short windows. */
private val CAROUSEL_MIN_CARD_HEIGHT = 120.dp

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
/** How dim a facet card that is disabled on the free plan is drawn. */
private const val LOCKED_CARD_ALPHA = 0.5f

@Composable
fun FacetCarouselScreen(
    onFacetApply: () -> Unit,
    onEditFacet: (facetId: Long) -> Unit,
    onReorderFacets: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onOpenFacetPro: (ProReason) -> Unit,
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
        onAddFacet = { if (uiState.canAddFacet) viewModel.addFacet() else onOpenFacetPro(ProReason.FACET_LIMIT) },
        onLockedFacetClick = { onOpenFacetPro(ProReason.FACET_LOCKED) },
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
    onLockedFacetClick: () -> Unit,
    onDeleteFacet: (Facet) -> Unit,
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

    val pageCount = facets.size + if (uiState.showAddFacet) 1 else 0
    val initialPage = facets.indexOfFirst { it.id == uiState.activeFacetId }.coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = initialPage) { pageCount }
    var deletingFacet by remember { mutableStateOf<Facet?>(null) }

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
        // The dots and Launcher-settings row are measured first, so this box only gets what they
        // leave; the card is shrunk to fit it (never grown past CAROUSEL_CARD_SCALE).
        BoxWithConstraints(modifier = Modifier.fillMaxWidth().weight(1f, fill = false)) {
            val idealCardHeight = (config.screenHeightDp * CAROUSEL_CARD_SCALE).dp
            val cardHeight = minOf(idealCardHeight, maxHeight - CAROUSEL_PAGE_CHROME_HEIGHT)
                .coerceAtLeast(CAROUSEL_MIN_CARD_HEIGHT)
            val cardWidth = cardHeight * screenAspectRatio
            val pageInset = ((maxWidth - cardWidth) / 2).coerceAtLeast(0.dp)
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = pageInset),
            pageSpacing = CAROUSEL_PAGE_SPACING,
            // Flush against the header, no extra top gap — matches the Hub's own header-to-grid
            // transition (HubScreen.kt's content starts exactly at headerHeight, zero padding).
            modifier = Modifier
                .fillMaxWidth()
                .height(cardHeight + CAROUSEL_PAGE_CHROME_HEIGHT)
                .testTag("facet_carousel_pager"),
        ) { page ->
            if (page < facets.size) {
                val facet = facets[page]
                // Recent-apps style: tapping any visible card — centered or peeking —
                // applies it immediately. Browsing (without applying) is swipe-only.
                val locked = facet.id !in uiState.selectableFacetIds
                FacetPreviewPage(
                    facet = facet,
                    isActive = facet.id == uiState.activeFacetId,
                    locked = locked,
                    style = uiState.style(facet.id),
                    settings = uiState.globalSettings,
                    content = HomeScreenContent(
                        listItems = uiState.previewsByFacetId[facet.id]?.favorites.orEmpty(),
                        dockItems = uiState.previewsByFacetId[facet.id]?.dockApps.orEmpty(),
                        calendarEvents = uiState.previewsByFacetId[facet.id]?.calendarEvents.orEmpty(),
                    ),
                    canDelete = uiState.canDeleteFacet,
                    onEditFacetClick = if (locked) onLockedFacetClick else ({ onEditFacet(facet.id) }),
                    onDeleteClick = { deletingFacet = facet },
                    onCardClick = if (locked) onLockedFacetClick else ({ onSelect(facet.id) }),
                    homeWallpaper = uiState.homeWallpaper,
                    screenAspectRatio = screenAspectRatio,
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("facet_page_${facet.id}"),
                )
            } else {
                AddFacetPage(
                    onClick = onAddFacet,
                    showProPill = !uiState.canAddFacet,
                    modifier = Modifier.fillMaxSize().testTag("facet_carousel_add_page"),
                )
            }
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

/** "Switch Facets" — the shared [ScreenHeader], identical in look to [com.facetlauncher.app.ui.hub.HubHeader]. */
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
 * The launcher-wide settings entry point, sitting just below the carousel — a [SettingsCard] with
 * one [ClickableRow], so it matches the Settings screen it opens. `Tune`, not a gear, so it doesn't
 * read as a second "facet settings" control next to each card's own gear.
 */
@Composable
private fun LauncherSettingsRow(onClick: () -> Unit, modifier: Modifier = Modifier) {
    SettingsCard(modifier = modifier, fullBleedRows = true) {
        ClickableRow(
            title = stringResource(R.string.facet_carousel_launcher_settings),
            subtitle = stringResource(R.string.facet_carousel_launcher_settings_subtitle),
            onClick = onClick,
            testTag = "facet_carousel_launcher_settings",
            leadingIcon = { SettingsIconBadge(Icons.Outlined.Tune, SettingsSectionHue.SYSTEM) },
        )
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
    facet: Facet,
    /** Whether this is the facet currently applied to Home — distinct from which page the pager is centered on. */
    isActive: Boolean,
    /** Disabled on the free plan: dimmed with a lock, and tapping it opens the upgrade sheet instead of applying it. */
    locked: Boolean,
    style: ResolvedFacetStyle,
    settings: LauncherSettings,
    content: HomeScreenContent,
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
            if (locked) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = stringResource(R.string.facet_locked_content_description),
                    tint = Muted,
                    modifier = Modifier.padding(start = 6.dp).size(20.dp).testTag("facet_page_lock_${facet.id}"),
                )
            }
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
        // Locked to the real screen's proportions — a genuine scale model.
        ScaledHomeScreen(
            style = style,
            settings = settings,
            content = content,
            homeWallpaper = homeWallpaper,
            tagPrefix = "facet_preview_",
            // Tapping anywhere on the card applies the facet, including on top of rows and dock icons.
            onClick = onCardClick,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(screenAspectRatio)
                .alpha(if (locked) LOCKED_CARD_ALPHA else 1f)
                .shadow(elevation = 4.dp, shape = cardShape)
                .clip(cardShape)
                .border(width = if (isActive) 2.dp else 1.dp, color = if (isActive) Accent else Hairline, shape = cardShape)
                .clickable(onClick = onCardClick),
        )

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

@Composable
private fun AddFacetPage(onClick: () -> Unit, showProPill: Boolean, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize()) {
        // Invisible stand-in for FacetPreviewPage's own name row — same Text structure so it
        // measures to the exact same height, without hardcoding a dp guess. Without this, the
        // Add-facet card (which has no header of its own) would start higher up than its
        // neighboring facet cards and read as a different, taller size (see chat history). Any
        // single line of this style measures the same; it's hidden and cleared from accessibility.
        Text(
            text = stringResource(R.string.facet_carousel_add_facet),
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 8.dp).alpha(0f).clearAndSetSemantics { },
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
            Row(modifier = Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(text = stringResource(R.string.facet_carousel_add_facet), style = MaterialTheme.typography.bodyLarge, color = Ink)
                if (showProPill) ProPill(modifier = Modifier.padding(start = 8.dp))
            }
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
                    Facet(id = 1, name = "Facet 1", position = 0),
                    Facet(id = 2, name = "Work", position = 1),
                ),
                activeFacetId = 1,
            ),
            onSelect = {},
            onEditFacet = {},
            onAddFacet = {},
            onLockedFacetClick = {},
            onDeleteFacet = {},
            onReorderFacets = {},
            onNavigateToSettings = {},
            onDismissDrag = {},
            onDismissDragEnd = {},
        )
    }
}
