package com.facetlauncher.app.ui.pro

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.facetlauncher.app.R
import com.facetlauncher.app.ui.theme.Accent
import kotlin.math.hypot

/** The one colour the Pro designs add: the dark ink behind the card, the hero and the post-purchase screen. Everything else follows the accent. */
internal val ProInk = Color(0xFF0A0F1D)

private val ProPurple = Color(0xFFA855F7)
private val FanLight = Color(0xFFE9EDF5)
private val FanDark = Color(0xFF2A3142)
private val FanPaper = Color(0xFFF8F9FA)
private val MiniLightIcon = Color(0xFF1E293B)
private val MiniDarkCard = Color(0xFF1A1F2E)

/** A soft accent glow painted over [ProInk]: [center] as fractions of the box, [alpha] at the centre, fading out at [stop] of the farthest corner. */
internal data class ProGlow(val centerX: Float, val centerY: Float, val alpha: Float, val stop: Float)

/** The dark ink with its accent glows, drawn behind a card or a hero. */
@Composable
internal fun Modifier.proInkBackground(vararg glows: ProGlow): Modifier {
    val accent = Accent
    return drawBehind {
        drawRect(ProInk)
        glows.forEach { glow ->
            val center = Offset(size.width * glow.centerX, size.height * glow.centerY)
            val farthest = listOf(Offset(0f, 0f), Offset(size.width, 0f), Offset(0f, size.height), Offset(size.width, size.height))
                .maxOf { hypot(it.x - center.x, it.y - center.y) }
            drawRect(Brush.radialGradient(listOf(accent.copy(alpha = glow.alpha), Color.Transparent), center = center, radius = farthest * glow.stop))
        }
    }
}

@Composable
private fun proBadgeBrush(): Brush {
    val accent = Accent
    return Brush.linearGradient(listOf(accent, lerp(accent, ProPurple, 0.4f)))
}

/** The gradient PRO mark. */
@Composable
internal fun ProBadge(modifier: Modifier = Modifier, textSize: Float = 10f) {
    val shape = MaterialTheme.shapes.small
    Text(
        text = stringResource(R.string.pro_pill).uppercase(),
        color = Color.White,
        fontSize = textSize.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.14.em,
        modifier = modifier
            .clip(shape)
            .background(proBadgeBrush())
            .border(1.dp, Color.White.copy(alpha = 0.18f), shape)
            .padding(start = 8.dp, end = 7.dp, top = 4.dp, bottom = 4.dp),
    )
}

/**
 * A full-width button with the accent gradient and glow: the one primary action of the Pro screens. It keeps the
 * app's button shape (M3 medium, 12dp, like [com.facetlauncher.app.ui.components.TonalButton]), not a pill.
 */
@Composable
internal fun ProGradientButton(text: String, onClick: () -> Unit, enabled: Boolean, tag: String, modifier: Modifier = Modifier) {
    val accent = Accent
    val buttonShape = MaterialTheme.shapes.medium
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .alpha(if (enabled) 1f else 0.5f)
            .graphicsLayer { shadowElevation = 14.dp.toPx(); shape = buttonShape; clip = false; ambientShadowColor = accent; spotShadowColor = accent }
            .clip(buttonShape)
            .background(Brush.verticalGradient(listOf(lerp(accent, Color.White, 0.12f), accent)))
            .clickable(enabled = enabled, onClick = onClick)
            .testTag(tag),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}

/** Three small facet cards fanned at a corner of the Settings card. Decorative. */
@Composable
internal fun ProFacetFan(modifier: Modifier = Modifier) {
    val accent = Accent
    val shape = RoundedCornerShape(6.dp)
    val cards = listOf(
        Triple(FanLight, -9f, 0.dp),
        Triple(lerp(FanPaper, accent, 0.35f), 0f, (-4).dp),
        Triple(FanDark, 9f, 0.dp),
    )
    Box(modifier = modifier.size(width = 58.dp, height = 52.dp).clearAndSetSemantics { }) {
        cards.forEachIndexed { index, (color, degrees, lift) ->
            Box(
                modifier = Modifier
                    .offset(x = 16.dp * index, y = 8.dp + lift)
                    .size(width = 26.dp, height = 44.dp)
                    .graphicsLayer { rotationZ = degrees; shadowElevation = 6.dp.toPx(); this.shape = shape; clip = false }
                    .background(color, shape),
            )
        }
    }
}

private enum class MiniHomeLook { LIGHT, DARK, ACCENT }

private data class MiniHomeSpec(
    val time: Int,
    val date: Int,
    val apps: List<Int>,
    val label: Int,
    val look: MiniHomeLook,
)

private val WeekendSpec = MiniHomeSpec(
    R.string.pro_sample_time_weekend, R.string.pro_sample_date_weekend,
    listOf(R.string.pro_sample_app_camera, R.string.pro_sample_app_maps, R.string.pro_sample_app_music),
    R.string.pro_hero_label_weekend, MiniHomeLook.LIGHT,
)
private val DriveSpec = MiniHomeSpec(
    R.string.pro_sample_time_drive, R.string.pro_sample_date_drive,
    listOf(R.string.pro_sample_app_spotify, R.string.pro_sample_app_maps, R.string.pro_sample_app_phone),
    R.string.pro_hero_label_drive, MiniHomeLook.DARK,
)
private val WorkSpec = MiniHomeSpec(
    R.string.pro_sample_time_work, R.string.pro_sample_date_work,
    listOf(R.string.pro_sample_app_slack, R.string.pro_sample_app_calendar, R.string.pro_sample_app_mail),
    R.string.pro_hero_label_work, MiniHomeLook.ACCENT,
)

/** One illustrative facet drawn as a small home screen: a clock, a date, three apps and the facet's name. */
@Composable
private fun MiniHome(spec: MiniHomeSpec, modifier: Modifier = Modifier) {
    val accent = Accent
    val shape = MaterialTheme.shapes.large
    val (card, text, icon) = when (spec.look) {
        MiniHomeLook.LIGHT -> Triple(FanLight, ProInk, MiniLightIcon)
        MiniHomeLook.DARK -> Triple(MiniDarkCard, Color.White, lerp(Color.White, accent, 0.7f))
        MiniHomeLook.ACCENT -> Triple(lerp(FanPaper, accent, 0.22f), ProInk, accent)
    }
    val timeWeight = when (spec.look) {
        MiniHomeLook.LIGHT -> FontWeight.ExtraLight
        MiniHomeLook.DARK -> FontWeight.ExtraBold
        MiniHomeLook.ACCENT -> FontWeight.Medium
    }
    val timeSize = when (spec.look) {
        MiniHomeLook.LIGHT -> 34.sp
        MiniHomeLook.DARK -> 30.sp
        MiniHomeLook.ACCENT -> 38.sp
    }
    Column(
        modifier = modifier
            .size(width = 104.dp, height = 200.dp)
            .graphicsLayer { shadowElevation = 18.dp.toPx(); this.shape = shape; clip = false }
            .clip(shape)
            .background(card)
            .then(if (spec.look == MiniHomeLook.DARK) Modifier.border(1.dp, Color.White.copy(alpha = 0.10f), shape) else Modifier)
            .padding(horizontal = 12.dp, vertical = 16.dp),
    ) {
        Text(text = stringResource(spec.time), color = text, fontSize = timeSize, fontWeight = timeWeight, lineHeight = timeSize * 0.95f)
        Text(text = stringResource(spec.date), color = text.copy(alpha = 0.6f), fontSize = 7.sp, modifier = Modifier.padding(top = 4.dp))
        Spacer(modifier = Modifier.weight(1f))
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            spec.apps.forEach { app ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(11.dp).background(icon, RoundedCornerShape(3.5.dp)))
                    Text(text = stringResource(app), color = text.copy(alpha = 0.85f), fontSize = 7.5.sp)
                }
            }
        }
        Text(
            text = stringResource(spec.label),
            color = text.copy(alpha = 0.5f),
            fontSize = 6.5.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.12.em,
            modifier = Modifier.padding(top = 9.dp),
        )
    }
}

/**
 * The hero: three illustrative facets fanned out. Before purchase the two side facets are dimmed; once Pro they
 * are fully lit and a check badge sits over the centre one. Decorative, so hidden from accessibility.
 */
@Composable
internal fun ProHeroFan(lit: Boolean, modifier: Modifier = Modifier) {
    val accent = Accent
    val sideAlpha = if (lit) 1f else 0.55f
    Box(modifier = modifier.fillMaxWidth().height(236.dp).clearAndSetSemantics { }, contentAlignment = Alignment.TopCenter) {
        MiniHome(WeekendSpec, Modifier.offset(x = (-96).dp, y = 26.dp).graphicsLayer { rotationZ = -11f }.alpha(sideAlpha))
        MiniHome(DriveSpec, Modifier.offset(x = 94.dp, y = 26.dp).graphicsLayer { rotationZ = 11f }.alpha(sideAlpha))
        MiniHome(WorkSpec, Modifier.offset(y = 6.dp))
        if (lit) {
            Box(
                modifier = Modifier
                    .offset(y = 176.dp)
                    .size(56.dp)
                    .drawBehind {
                        drawCircle(accent.copy(alpha = 0.12f), radius = size.minDimension / 2 + 14.dp.toPx())
                        drawCircle(accent.copy(alpha = 0.28f), radius = size.minDimension / 2 + 6.dp.toPx())
                    }
                    .clip(CircleShape)
                    .background(Brush.verticalGradient(listOf(lerp(accent, Color.White, 0.2f), accent))),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
            }
        }
    }
}

/** A frosted chip showing what a trigger does: an icon, then "from → to". M3 chip shape (small, 8dp). */
@Composable
internal fun TriggerPill(icon: ImageVector, from: String, to: String, modifier: Modifier = Modifier) {
    val accent = Accent
    val chip = MaterialTheme.shapes.small
    Row(
        modifier = modifier
            .clip(chip)
            .background(Color.White.copy(alpha = 0.10f))
            .border(1.dp, Color.White.copy(alpha = 0.16f), chip)
            .padding(start = 9.dp, end = 11.dp, top = 7.dp, bottom = 7.dp)
            .semantics(mergeDescendants = true) { },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Icon(icon, contentDescription = null, tint = lerp(Color.White, accent, 0.45f), modifier = Modifier.size(14.dp))
        Text(text = from, color = Color.White, fontSize = 11.sp)
        Text(text = stringResource(R.string.pro_sample_arrow), color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp, textAlign = TextAlign.Center)
        Text(text = to, color = Color.White, fontSize = 11.sp)
    }
}

/** The hairline between the dark unlock rows. */
internal val ProRowDivider = Color.White.copy(alpha = 0.08f)
