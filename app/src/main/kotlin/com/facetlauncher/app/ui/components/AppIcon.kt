package com.facetlauncher.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.data.icon.IconStyler
import com.facetlauncher.app.data.icon.StyledIcon
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppProfile
import com.facetlauncher.app.data.model.DrawerListItemSize
import com.facetlauncher.app.data.model.IconRenderMode
import com.facetlauncher.app.data.model.NotificationBadgeStyle
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.IconTile
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.LocalIconRenderMode
import com.facetlauncher.app.ui.theme.LocalIconShape
import com.facetlauncher.app.ui.theme.LocalStyledIconCache
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.Surface

/** F13's badge count is capped at a single digit for legibility at these icon sizes — "9+" beyond that, not the more common "99+". */
private fun badgeLabel(count: Int): String = if (count > 9) "9+" else count.toString()

/**
 * The app's full set of standard [AppIcon] sizes — every icon tile in the app should be one of
 * these rather than a bespoke `Dp` picked by eye (see chat history: this replaced ~9 slightly
 * different hand-picked radii/sizes across the app with 5 named sizes).
 * [ROW_COMPACT]/[ROW_REGULAR]/[ROW_SPACIOUS] deliberately equal [DrawerListItemSize]'s own
 * `iconSizeDp` tiers (that enum is the one place a user actually chooses an icon size) rather than
 * repeating those numbers here.
 */
object AppIconSize {
    /** The smallest icon in the app — a shortcut's leading icon inside [AppContextMenu]'s menu row. */
    val SHORTCUT = 20.dp

    /** One icon in a list row — App Drawer's own Compact tier, Home's app list when it needs to
     *  compact (see `HomeScreen.kt`'s own compact-spacing check), and every reorder/picker row
     *  (Settings' Default Favorites, the Favorites/Dock pickers). */
    val ROW_COMPACT = DrawerListItemSize.COMPACT.iconSizeDp.dp

    /** App Drawer's own Regular tier; Home's app list in its normal (non-compacted) state. */
    val ROW_REGULAR = DrawerListItemSize.REGULAR.iconSizeDp.dp

    /** App Drawer's own Spacious tier; also [AppContextMenu]'s header icon, which already happened to match. */
    val ROW_SPACIOUS = DrawerListItemSize.SPACIOUS.iconSizeDp.dp

    /** Grid-tile-scale UI: App Drawer's Grid layout, the real Home Dock, and its Settings/onboarding management tiles. */
    val TILE = 44.dp
}

/** [AppIcon] for an [AppInfo]: also draws the Work / other-profile badge, so two copies of one app (personal and Work) can be told apart. */
@Composable
fun AppIcon(
    app: AppInfo,
    size: Dp,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    notificationCount: Int? = null,
    badgeStyle: NotificationBadgeStyle = NotificationBadgeStyle.DOT,
) {
    AppIcon(
        icon = app.icon,
        monochromeIcon = app.monochromeIcon,
        size = size,
        contentDescription = contentDescription,
        modifier = modifier,
        notificationCount = notificationCount,
        badgeStyle = badgeStyle,
        isWorkApp = app.profile == AppProfile.WORK,
        isOtherProfileApp = app.profile == AppProfile.OTHER,
    )
}

/**
 * The single app-icon renderer used everywhere an [icon] bitmap is shown — Home's favorites/dock,
 * the App Drawer's List/Grid rows, the long-press context menu, and the facet carousel's
 * preview cards. Kept as one composable specifically so F11's icon-rendering mode only needs to
 * change this one place rather than every surface that draws an icon independently — see
 * [LocalIconRenderMode]/[AppIconGlyph]'s own doc for how that mode is applied.
 *
 * [notificationCount], when positive, renders F13's notification badge — a real M3
 * [BadgedBox]/[Badge] anchored to the icon's corner. [badgeStyle] picks between README's original
 * dot-only spec ([NotificationBadgeStyle.DOT] — [Badge] with no content renders just the small
 * dot) and a numeric count ([NotificationBadgeStyle.COUNT], still capped for legibility — see
 * [badgeLabel]), a deliberate departure from the spec's default offered as a user choice from the
 * dedicated Notification settings screen rather than replacing the default outright.
 */
@Composable
fun AppIcon(
    icon: ImageBitmap?,
    size: Dp,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    /** The app's themed-icon layer, for the glyph and outline icon styles; see [AppInfo.monochromeIcon]. */
    monochromeIcon: ImageBitmap? = null,
    notificationCount: Int? = null,
    badgeStyle: NotificationBadgeStyle = NotificationBadgeStyle.DOT,
    isWorkApp: Boolean = false,
    /** True for [com.facetlauncher.app.data.model.AppProfile.OTHER] — a profile that exists but
     * isn't positively identified as Work/Private (most commonly an OEM dual-app/clone profile).
     * Mutually exclusive with [isWorkApp] in practice (an app has exactly one [AppProfile]), so
     * both never render at once. */
    isOtherProfileApp: Boolean = false,
) {
    Box(modifier = modifier) {
        if (notificationCount != null && notificationCount > 0) {
            BadgedBox(badge = { NotificationBadge(count = notificationCount, style = badgeStyle) }) {
                AppIconGlyph(icon = icon, monochromeIcon = monochromeIcon, size = size, contentDescription = contentDescription)
            }
        } else {
            AppIconGlyph(icon = icon, monochromeIcon = monochromeIcon, size = size, contentDescription = contentDescription)
        }
        // Bottom-start, not stacked with the notification dot above (top-end, via BadgedBox) —
        // matches every other Android launcher's own convention for where a Work Profile badge
        // sits, so a user switching from another launcher isn't relearning icon language.
        if (isWorkApp) {
            WorkProfileBadge(modifier = Modifier.align(Alignment.BottomStart))
        } else if (isOtherProfileApp) {
            OtherProfileBadge(modifier = Modifier.align(Alignment.BottomStart))
        }
    }
}

/** Shared badge content for both the icon-corner badge above and the trailing-the-name badge on list-style rows. */
@Composable
fun NotificationBadge(count: Int, style: NotificationBadgeStyle, modifier: Modifier = Modifier) {
    if (style == NotificationBadgeStyle.COUNT) {
        Badge(containerColor = Accent, contentColor = Surface, modifier = modifier.testTag("app_icon_badge")) {
            Text(text = badgeLabel(count), style = MaterialTheme.typography.labelSmall)
        }
    } else {
        Badge(containerColor = Accent, modifier = modifier.testTag("app_icon_badge"))
    }
}

/**
 * A themed marker for a Work Profile app's icon — deliberately not Android's own
 * [android.content.pm.PackageManager.getUserBadgedIcon] briefcase asset, which would render with
 * the OS's own baseline tones instead of this app's palette (CLAUDE.md's shape/theming rule).
 * Reuses [Badge]'s own plain-dot construction (same as [NotificationBadge]'s `DOT` style) so it
 * reads as the same visual family, just placed at a different corner and always-Accent (a Work
 * Profile marker isn't a count, so [NotificationBadgeStyle.COUNT] has no equivalent here).
 */
@Composable
private fun WorkProfileBadge(modifier: Modifier = Modifier) {
    Badge(containerColor = Accent, modifier = modifier.testTag("work_profile_badge"))
}

/** Sibling to [WorkProfileBadge] for [com.facetlauncher.app.data.model.AppProfile.OTHER] apps — a small person glyph (not just a plain dot) so it reads as visually distinct from the Work badge at a glance. */
@Composable
private fun OtherProfileBadge(modifier: Modifier = Modifier) {
    Badge(containerColor = Muted, contentColor = Surface, modifier = modifier.testTag("other_profile_badge")) {
        Icon(imageVector = Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(10.dp))
    }
}

/**
 * [LocalIconRenderMode] picks what gets drawn. [IconRenderMode.SYSTEM_DEFAULT] draws [icon]
 * unchanged. The monochrome modes tint it with [BlendMode.Color], which keeps the icon's own
 * luminosity and swaps only hue and saturation, so recolored icons stay told apart by their detail
 * (the app's own monochrome layer is a flat glyph and looked out of place beside them). The glyph
 * and outline modes draw the [StyledIcon] for [icon] and [monochromeIcon] (its shaded whole-icon
 * fallback where it has no glyph or outline); while it is being made the accent tint stands in.
 */
@Composable
private fun AppIconGlyph(icon: ImageBitmap?, monochromeIcon: ImageBitmap?, size: Dp, contentDescription: String?, modifier: Modifier = Modifier) {
    val renderMode = LocalIconRenderMode.current
    val shapeSetting = LocalIconShape.current
    val iconShape = remember(shapeSetting) { shapeSetting.toComposeShape() }
    if (icon != null) {
        val styled = rememberStyledIcon(renderMode, icon, monochromeIcon)
        val styledPicture = renderMode.pictureOf(styled)
        val shadedIcon = styled?.fallback
        val accent = Accent
        val shadeFilter = remember(accent) { accentShadeFilter(accent) }
        val fallbackFilter = remember(accent, styled) { styled?.let { anchoredShadeFilter(accent, it.fallbackAnchor(accent.lightness())) } }
        val tintColor = renderMode.tint(accent = accent, ink = Ink)
        Image(
            bitmap = styledPicture ?: shadedIcon ?: icon,
            contentDescription = contentDescription,
            colorFilter = when {
                styledPicture != null -> shadeFilter
                shadedIcon != null -> fallbackFilter
                else -> tintColor?.let { ColorFilter.tint(it, BlendMode.Color) }
            },
            modifier = if (styledPicture != null) modifier.size(size) else modifier.size(size).clip(iconShape),
        )
    } else {
        Box(
            modifier = modifier
                .size(size)
                .background(color = IconTile, shape = iconShape)
                .then(
                    if (contentDescription != null) {
                        Modifier.semantics { this.contentDescription = contentDescription }
                    } else {
                        Modifier
                    },
                ),
        )
    }
}

/** Paints a styled picture (a shade around mid grey, see `IconStyler`) as the accent: mid grey is the accent exactly, lighter and darker greys lift and lower it. */
private fun accentShadeFilter(accent: Color): ColorFilter {
    fun shift(channel: Float) = (channel - 0.5f) * 255f
    return ColorFilter.colorMatrix(
        ColorMatrix(
            floatArrayOf(
                1f, 0f, 0f, 0f, shift(accent.red),
                0f, 1f, 0f, 0f, shift(accent.green),
                0f, 0f, 1f, 0f, shift(accent.blue),
                0f, 0f, 0f, 1f, 0f,
            ),
        ),
    )
}

/** The glyph or outline pictures for [icon] under the styles that draw them, null for every other style and while they are being made. */
@Composable
private fun rememberStyledIcon(renderMode: IconRenderMode, icon: ImageBitmap, monochromeIcon: ImageBitmap?): StyledIcon? {
    val cache = LocalStyledIconCache.current
    if (renderMode != IconRenderMode.GLYPH_ACCENT && renderMode != IconRenderMode.OUTLINE_ACCENT) return null
    return produceState<StyledIcon?>(initialValue = null, icon, monochromeIcon) { value = cache.styled(icon, monochromeIcon) }.value
}

private fun IconRenderMode.pictureOf(styled: StyledIcon?): ImageBitmap? = when (this) {
    IconRenderMode.GLYPH_ACCENT -> styled?.glyph
    IconRenderMode.OUTLINE_ACCENT -> styled?.outline
    else -> null
}

/** The `BlendMode.Color` tint for the styles that tint the whole icon, which is also what stands in while a glyph or outline is being made. */
private fun IconRenderMode.tint(accent: Color, ink: Color): Color? = when (this) {
    IconRenderMode.SYSTEM_DEFAULT -> null
    IconRenderMode.MONOCHROME_BLACK_WHITE -> ink
    IconRenderMode.MONOCHROME_ACCENT, IconRenderMode.GLYPH_ACCENT, IconRenderMode.OUTLINE_ACCENT -> accent
}

private fun Color.lightness() = 0.299f * red + 0.587f * green + 0.114f * blue

/** Paints a whole icon's own lightness as the accent, with the icon's [anchor] tone landing on the accent itself and lighter and darker tones lifted and lowered around it. */
private fun anchoredShadeFilter(accent: Color, anchor: Float): ColorFilter {
    val spread = IconStyler.SHADE_SPREAD
    fun shift(channel: Float) = (channel - spread * anchor) * 255f
    return ColorFilter.colorMatrix(
        ColorMatrix(
            floatArrayOf(
                spread, 0f, 0f, 0f, shift(accent.red),
                0f, spread, 0f, 0f, shift(accent.green),
                0f, 0f, spread, 0f, shift(accent.blue),
                0f, 0f, 0f, 1f, 0f,
            ),
        ),
    )
}
