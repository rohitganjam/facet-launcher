package com.lumenlauncher.app.ui.components

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import com.lumenlauncher.app.data.model.IconRenderMode
import com.lumenlauncher.app.data.model.NotificationBadgeStyle
import com.lumenlauncher.app.ui.theme.Accent
import com.lumenlauncher.app.ui.theme.IconTile
import com.lumenlauncher.app.ui.theme.Ink
import com.lumenlauncher.app.ui.theme.LocalIconRenderMode
import com.lumenlauncher.app.ui.theme.Surface

/** F13's badge count is capped at a single digit for legibility at these icon sizes — "9+" beyond that, not the more common "99+". */
private fun badgeLabel(count: Int): String = if (count > 9) "9+" else count.toString()

/**
 * The single app-icon renderer used everywhere an [icon] bitmap is shown — Home's favorites/dock,
 * the App Drawer's List/Grid rows, the long-press context menu, and the profile carousel's
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
    cornerRadius: Dp,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    notificationCount: Int? = null,
    badgeStyle: NotificationBadgeStyle = NotificationBadgeStyle.DOT,
) {
    if (notificationCount != null && notificationCount > 0) {
        BadgedBox(
            badge = { NotificationBadge(count = notificationCount, style = badgeStyle) },
            modifier = modifier,
        ) {
            AppIconGlyph(icon = icon, size = size, cornerRadius = cornerRadius, contentDescription = contentDescription)
        }
    } else {
        AppIconGlyph(icon = icon, size = size, cornerRadius = cornerRadius, contentDescription = contentDescription, modifier = modifier)
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
