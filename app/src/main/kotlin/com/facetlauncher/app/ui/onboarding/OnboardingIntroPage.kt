package com.facetlauncher.app.ui.onboarding

import android.content.res.Configuration
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.R
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.Faint
import com.facetlauncher.app.ui.theme.IconTile
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.Surface
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Onboarding step 1 (`4f`) — one-breath framing, no interaction beyond Next. Names the three parts
 * of Home (clock / app list / dock) so "dock" isn't a surprise in step 2, and previews the three
 * gestures that will actually be available afterward — kept in sync with the real gesture map
 * (`PRD.md` §5): swipe up → App Drawer, swipe right → Hub, swipe left → Switch Facets.
 */
@Composable
fun OnboardingIntroPage(onNext: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("onboarding_intro_page")
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp),
    ) {
        Column(modifier = Modifier.weight(1f).padding(top = 64.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Image(
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier.size(72.dp).testTag("onboarding_logo"),
                )
                Text(
                    text = "Facet Launcher",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Ink,
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "A home screen that focuses on you.",
                style = MaterialTheme.typography.headlineLarge,
                color = Ink,
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "No icon grid. Just your clock, what's next, and a few apps you actually open.",
                style = MaterialTheme.typography.bodyLarge,
                color = Muted,
            )

            Spacer(modifier = Modifier.height(32.dp))
            HomeDiagram()

            Spacer(modifier = Modifier.height(32.dp))
            ConceptLine(glyph = "↑", label = "Swipe up any time for all your apps.", entranceDelayMillis = 0)
            Spacer(modifier = Modifier.height(20.dp))
            ConceptLine(glyph = "→", label = "Swipe right for your widgets.", entranceDelayMillis = GESTURE_ENTRANCE_STAGGER_MS)
            Spacer(modifier = Modifier.height(20.dp))
            ConceptLine(glyph = "←", label = "Swipe left to switch facets.", entranceDelayMillis = GESTURE_ENTRANCE_STAGGER_MS * 2)
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 34.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OnboardingDots(step = 0, totalSteps = ONBOARDING_STEP_COUNT)
            Text(
                text = "Next",
                style = MaterialTheme.typography.bodyLarge,
                color = Accent,
                // 48dp minimum touch target (M3 guideline) — defaultMinSize before
                // wrapContentSize so the enlarged tap area stays centered on the text.
                modifier = Modifier
                    .clickable(onClick = onNext)
                    .testTag("onboarding_next")
                    .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                    .wrapContentSize(Alignment.Center),
            )
        }
    }
}

/**
 * A small labelled sketch of the Home layout — a card mocking the clock/app-list/dock (not a
 * pixel-perfect Home render, it's a legend), each section connected by a leader line to its name.
 * Matches the design canvas (`Launcher.dc.html` turn 5, artboard `5b`): a white card on the left
 * (sized to its own content, not stretched — [Column] wraps naturally, no `fillMaxHeight`), thin
 * gray leader lines extending to a label at each section's height, in a parallel column with
 * matching per-section heights so the two stay aligned without a shared layout.
 */
@Composable
private fun HomeDiagram(modifier: Modifier = Modifier) {
    val cardShape = RoundedCornerShape(20.dp)
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Column(
            modifier = Modifier
                .width(CARD_WIDTH)
                .shadow(elevation = 4.dp, shape = cardShape)
                .background(Surface, cardShape)
                .padding(20.dp),
        ) {
            Text(
                text = "9:41",
                style = MaterialTheme.typography.headlineMedium,
                color = Ink,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(modifier = Modifier.width(46.dp).height(4.dp).background(Faint, RoundedCornerShape(2.dp)))
            Spacer(modifier = Modifier.height(SECTION_GAP))
            AppRowBar(fraction = 1f)
            Spacer(modifier = Modifier.height(6.dp))
            AppRowBar(fraction = 0.75f)
            Spacer(modifier = Modifier.height(6.dp))
            AppRowBar(fraction = 0.75f)
            Spacer(modifier = Modifier.height(SECTION_GAP))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(4) {
                    Box(modifier = Modifier.size(DOCK_TILE_SIZE).background(IconTile, RoundedCornerShape(10.dp)))
                }
            }
        }
        Column(modifier = Modifier.padding(top = 20.dp)) {
            LeaderRow(label = "Clock", height = CLOCK_SECTION_HEIGHT, entranceDelayMillis = 0)
            Spacer(modifier = Modifier.height(SECTION_GAP))
            LeaderRow(label = "Your apps", height = APPS_SECTION_HEIGHT, entranceDelayMillis = LEADER_ENTRANCE_STAGGER_MS)
            Spacer(modifier = Modifier.height(SECTION_GAP))
            LeaderRow(label = "Dock", height = DOCK_TILE_SIZE, entranceDelayMillis = LEADER_ENTRANCE_STAGGER_MS * 2)
        }
    }
}

/**
 * A leader line + label, vertically centered within [height] to match the card content it points
 * at. Animates in on first composition — the line grows outward from the card (scaled from its
 * card-side end, [entranceDelayMillis] after the page appears) as the label fades in alongside it.
 */
@Composable
private fun LeaderRow(label: String, height: Dp, entranceDelayMillis: Int, modifier: Modifier = Modifier) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(entranceDelayMillis.toLong())
        progress.animateTo(1f, tween(LEADER_ENTRANCE_DURATION_MS, easing = FastOutSlowInEasing))
    }
    Row(modifier = modifier.height(height), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(24.dp)
                .height(1.dp)
                .graphicsLayer {
                    scaleX = progress.value
                    transformOrigin = TransformOrigin(0f, 0.5f)
                }
                .background(Faint),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = Muted,
            modifier = Modifier.graphicsLayer { alpha = progress.value },
        )
    }
}

@Composable
private fun AppRowBar(fraction: Float, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth(fraction).height(7.dp).background(Faint, RoundedCornerShape(3.dp)))
}

private val CARD_WIDTH = 200.dp
private val DOCK_TILE_SIZE = 32.dp
private val SECTION_GAP = 24.dp

// Approximate rendered heights of each card section, so the parallel leader-line column's rows
// line up with them — headlineMedium's own line height varies slightly by font metrics, so this
// is a close match rather than a guaranteed-exact one.
private val CLOCK_SECTION_HEIGHT = 50.dp
private val APPS_SECTION_HEIGHT = 33.dp

/**
 * A gesture glyph + description, animating up into place ([entranceDelayMillis] after the page
 * appears) as it fades in — staggered across the three lines so all have settled within 1.5s.
 */
@Composable
private fun ConceptLine(glyph: String, label: String, entranceDelayMillis: Int, modifier: Modifier = Modifier) {
    val startOffsetPx = with(LocalDensity.current) { GESTURE_ENTRANCE_OFFSET.toPx() }
    val alpha = remember { Animatable(0f) }
    val offsetY = remember { Animatable(startOffsetPx) }
    LaunchedEffect(Unit) {
        delay(entranceDelayMillis.toLong())
        launch { alpha.animateTo(1f, tween(GESTURE_ENTRANCE_DURATION_MS, easing = FastOutSlowInEasing)) }
        launch { offsetY.animateTo(0f, tween(GESTURE_ENTRANCE_DURATION_MS, easing = FastOutSlowInEasing)) }
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                translationY = offsetY.value
                this.alpha = alpha.value
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(text = glyph, style = MaterialTheme.typography.titleLarge, color = Ink)
        Text(text = label, style = MaterialTheme.typography.bodyLarge, color = Ink)
    }
}

/** Stagger/duration for [ConceptLine]'s entrance — all three lines settle within 1.5s. */
private const val GESTURE_ENTRANCE_STAGGER_MS = 300
private const val GESTURE_ENTRANCE_DURATION_MS = 700
private val GESTURE_ENTRANCE_OFFSET = 16.dp

/** Stagger/duration for each [LeaderRow]'s entrance. */
private const val LEADER_ENTRANCE_STAGGER_MS = 150
private const val LEADER_ENTRANCE_DURATION_MS = 400

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun OnboardingIntroPagePreview() {
    FacetLauncherTheme {
        OnboardingIntroPage(onNext = {})
    }
}
