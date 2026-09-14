package com.facetlauncher.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.data.model.DrawerListItemSize
import com.facetlauncher.app.data.model.IconRenderMode
import com.facetlauncher.app.data.model.NotificationBadgeStyle
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.IconTile
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.LocalIconRenderMode
import com.facetlauncher.app.ui.theme.Surface

/** F13's badge count is capped at a single digit for legibility at these icon sizes — "9+" beyond that, not the more common "99+". */
private fun badgeLabel(count: Int): String = if (count > 9) "9+" else count.toString()

/**
 * The app's full set of standard [AppIcon] sizes — every icon tile in the app should be one of
 * these rather than a bespoke `Dp` picked by eye (see chat history: this replaced ~9 slightly
 * different hand-picked radii/sizes across the app with 5 named sizes and 3 M3-derived radii).
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

/**
 * [AppIcon]'s default corner radius, derived from [size] via the M3 shape scale (CLAUDE.md's
 * Material 3 shape section) rather than a hand-picked ratio — `extraSmall` (4dp) for
 * [AppIconSize.SHORTCUT]-scale icons, `small` (8dp) for row-scale icons, `medium` (12dp) for
 * tile-scale icons. A departure from this app's earlier convention of exempting icon/avatar tiles
 * from the M3 shape scale (decided explicitly by the user — see chat history).
 */
private fun appIconCornerRadiusFor(size: Dp): Dp = when {
    size <= 24.dp -> 4.dp
    size <= 36.dp -> 8.dp
    else -> 12.dp
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
    cornerRadius: Dp = appIconCornerRadiusFor(size),
    notificationCount: Int? = null,
    badgeStyle: NotificationBadgeStyle = NotificationBadgeStyle.DOT,
    isWorkApp: Boolean = false,
) {
    Box(modifier = modifier) {
        if (notificationCount != null && notificationCount > 0) {
            BadgedBox(badge = { NotificationBadge(count = notificationCount, style = badgeStyle) }) {
                AppIconGlyph(icon = icon, size = size, cornerRadius = cornerRadius, contentDescription = contentDescription)
            }
        } else {
            AppIconGlyph(icon = icon, size = size, cornerRadius = cornerRadius, contentDescription = contentDescription)
        }
        // Bottom-start, not stacked with the notification dot above (top-end, via BadgedBox) —
        // matches every other Android launcher's own convention for where a Work Profile badge
        // sits, so a user switching from another launcher isn't relearning icon language.
        if (isWorkApp) {
            WorkProfileBadge(modifier = Modifier.align(Alignment.BottomStart))
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

/**
 * [LocalIconRenderMode] picks what actually gets drawn: [IconRenderMode.SYSTEM_DEFAULT] renders
 * [icon] unchanged; either monochrome mode tints [icon] itself with [ColorFilter.tint] using
 * [BlendMode.Color] — deliberately *not* [BlendMode.SrcIn], and deliberately *not* the app's own
 * `Icon.getMonochrome()` layer even when it provides one. `Color` keeps the icon's own luminosity
 * (shading/gradients) and only replaces its hue+saturation with [tintColor], so recolored icons
 * stay visually distinguishable from each other by their own real detail. A real monochrome layer
 * has no such detail to preserve — it's a flat single-color glyph by its own convention (drawn as
 * plain black/near-black on transparent, RGB carrying no information) — so rendering it (with
 * either blend mode) always looks flat and undetailed next to every other icon; tried and reverted
 * (see chat history) in favor of one consistent, detailed look for every app regardless of what
 * icon assets it happens to ship.
 */
@Composable
private fun AppIconGlyph(icon: ImageBitmap?, size: Dp, cornerRadius: Dp, contentDescription: String?, modifier: Modifier = Modifier) {
    val renderMode = LocalIconRenderMode.current
    if (icon != null) {
        val tintColor = when (renderMode) {
            IconRenderMode.SYSTEM_DEFAULT -> null
            IconRenderMode.MONOCHROME_BLACK_WHITE -> Ink
            IconRenderMode.MONOCHROME_ACCENT -> Accent
        }
        Image(
            bitmap = icon,
            contentDescription = contentDescription,
            colorFilter = tintColor?.let { ColorFilter.tint(it, BlendMode.Color) },
            modifier = modifier.size(size).clip(RoundedCornerShape(cornerRadius)),
        )
    } else {
        Box(
            modifier = modifier
                .size(size)
                .background(color = IconTile, shape = RoundedCornerShape(cornerRadius))
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
