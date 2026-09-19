package com.facetlauncher.app.ui.onboarding

import android.content.res.Configuration
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import com.facetlauncher.app.R
import com.facetlauncher.app.ui.components.AppIcon
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.Faint
import com.facetlauncher.app.ui.theme.Hairline
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.Surface
import kotlin.coroutines.coroutineContext
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * Onboarding step 3 — teaches the *concept* of facets with an illustrative mockup, not the
 * user's own real picks (a fresh install only ever has one real facet at this point, so showing
 * three genuinely different-looking "layouts" needs invented content — same call the original
 * design brief made: invented app names, monogram-tile icons, no real products). A small looping
 * demo animates the actual interaction: focus on one facet, zoom out to reveal its neighbors,
 * swipe to the next, tap to select it — then resets and repeats, entirely decorative and
 * automatic (no user input needed to see it).
 */
@Composable
fun OnboardingFacetsPage(uiState: OnboardingUiState, onBack: () -> Unit, onNext: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().testTag("onboarding_facets_page").padding(horizontal = 24.dp)) {
        Column(modifier = Modifier.weight(1f).padding(top = 48.dp)) {
            Text(text = stringResource(R.string.onboarding_facets_headline), style = MaterialTheme.typography.headlineSmall, color = Ink)
            Spacer(modifier = Modifier.height(28.dp))

            FacetSwitchDemo()

            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = stringResource(R.string.onboarding_facets_body),
                style = MaterialTheme.typography.bodyLarge,
                color = Muted,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 34.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OnboardingDots(step = 2, totalSteps = ONBOARDING_STEP_COUNT)
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Text(
                    text = stringResource(R.string.action_back),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Muted,
                    // 48dp minimum touch target (M3 guideline), centered on the text.
                    modifier = Modifier
                        .clickable(onClick = onBack)
                        .testTag("onboarding_back")
                        .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                        .wrapContentSize(Alignment.Center),
                )
                Text(
                    // Not "Next" (this is the last swipeable step) or "Finish" (it doesn't finish
                    // onboarding by itself — it leads into the set-default sheet, the real final
                    // action) — "Continue" describes moving on into that sheet without overclaiming.
                    text = stringResource(R.string.action_continue),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Accent,
                    modifier = Modifier
                        .clickable(onClick = onNext)
                        .testTag("onboarding_next")
                        .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                        .wrapContentSize(Alignment.Center),
                )
            }
        }
    }
}

private data class MockFacet(val time: String, val date: String, val favorites: List<String>, val dock: List<String>)

private val MOCK_FACETS = listOf(
    MockFacet("9:41", "Wednesday, 26 August", listOf("Bloom", "Carto", "Clock"), listOf("P", "M", "B", "C")),
    MockFacet("9:41", "Wednesday, 26 August", listOf("Calendar", "Notes", "Aperture"), listOf("P", "M", "B", "C")),
    MockFacet("9:41", "Wednesday, 26 August", listOf("Archive", "Contacts", "Echo"), listOf("P", "M", "B", "C")),
)

private val CARD_WIDTH = 158.dp
private val CARD_SPACING = 16.dp

/**
 * The looping demo: cycles [MockFacet]s — one focused and zoomed in, then zoomed out to reveal
 * its neighbors, a swipe animates the next one into center, a tap-pulse marks the "selection",
 * then it zooms back in on the newly-centered one, holds, and resets to the start. Runs forever
 * while this composable is on screen; Compose cancels the [LaunchedEffect] automatically when the
 * user navigates away.
 */
@Composable
private fun FacetSwitchDemo(modifier: Modifier = Modifier) {
    // 0f..1f — continuous progress from the first mock facet toward the second; drives both the
    // card offsets (paging) and the "finger" indicator's position during the swipe phase.
    val swipeProgress = remember { Animatable(0f) }
    // 1f = only the centered card is shown, zoomed in; 0f = all three cards visible, resting scale.
    val focus = remember { Animatable(1f) }
    // A brief 0->1->0 pulse on the newly-centered card right after the swipe settles, standing in
    // for "you tapped this card to select it".
    val tapPulse = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Initial state only — every later lap already ends here, so this never runs again mid-loop.
        swipeProgress.snapTo(0f)
        focus.snapTo(1f)
        tapPulse.snapTo(0f)
        // Shorter than the loop's own between-facet hold (delay(1300) below) — the page just
        // appeared, so the demo should start moving quickly rather than sit still first.
        delay(400)
        while (coroutineContext.isActive) {
            // Zoom out to reveal the neighbors.
            focus.animateTo(0f, tween(480, easing = FastOutSlowInEasing))
            delay(650)
            // Swipe to the next facet (the "finger" indicator rides along with this).
            swipeProgress.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
            // Tap-select pulse on the now-centered card.
            tapPulse.animateTo(1f, tween(220))
            tapPulse.animateTo(0f, tween(260))
            delay(250)
            // Zoom back in on the newly-selected facet.
            focus.animateTo(1f, tween(480, easing = FastOutSlowInEasing))
            delay(1300)
            // Loop backward instead of jump-cutting to the start: zoom out, swipe back, re-select.
            focus.animateTo(0f, tween(480, easing = FastOutSlowInEasing))
            delay(650)
            swipeProgress.animateTo(0f, tween(600, easing = FastOutSlowInEasing))
            tapPulse.animateTo(1f, tween(220))
            tapPulse.animateTo(0f, tween(260))
            delay(250)
            focus.animateTo(1f, tween(480, easing = FastOutSlowInEasing))
            delay(1300)
        }
    }

    val density = LocalDensity.current
    val stepPx = with(density) { (CARD_WIDTH + CARD_SPACING).toPx() }

    Box(modifier = modifier.fillMaxWidth().height(190.dp), contentAlignment = Alignment.Center) {
        MOCK_FACETS.forEachIndexed { index, facet ->
            // This card's position relative to the centered slot: 0 = centered, ±1 = neighbor, etc.
            // Only the first two facets actually move (the demo swipes 0 -> 1); the third stays
            // put as a visible "there's a third slot" peek throughout.
            val slot = if (index <= 1) index - swipeProgress.value else index - swipeProgress.value.coerceAtMost(1f)
            val distance = abs(slot)
            val isCentered = distance < 0.05f
            val restingAlpha = (1f - 0.55f * distance).coerceIn(0f, 1f)
            val restingScale = (1f - 0.16f * distance).coerceIn(0.6f, 1f)
            val cardAlpha = if (isCentered) restingAlpha else restingAlpha * (1f - focus.value)
            val cardScale = if (isCentered) restingScale + focus.value * 0.2f else restingScale
            val pulseScale = if (isCentered) 1f + tapPulse.value * 0.05f else 1f

            MockFacetCard(
                facet = facet,
                modifier = Modifier
                    .testTag("facet_demo_card_$index")
                    .offset { IntOffset((slot * stepPx).roundToInt(), 0) }
                    .graphicsLayer {
                        alpha = cardAlpha
                        scaleX = cardScale * pulseScale
                        scaleY = cardScale * pulseScale
                    },
            )
        }

        // The "finger" — a small dot riding from the first card to the second during the swipe,
        // fading in and out at each end so it never appears during the focused/zoomed phases.
        val touchEnvelope = sin(swipeProgress.value * PI).toFloat().coerceIn(0f, 1f)
        val touchAlpha = touchEnvelope * (1f - focus.value).coerceIn(0f, 1f)
        if (touchAlpha > 0.01f) {
            Box(
                modifier = Modifier
                    .offset { IntOffset((swipeProgress.value * stepPx * 0.6f).roundToInt(), 40) }
                    .size(16.dp)
                    .alpha(touchAlpha)
                    .background(Accent, CircleShape),
            )
        }
    }
}

@Composable
private fun MockFacetCard(facet: MockFacet, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .width(CARD_WIDTH)
            .shadow(elevation = 6.dp, shape = shape)
            .background(Surface, shape)
            .border(1.dp, Hairline, shape)
            .padding(14.dp),
    ) {
        Text(text = facet.time, style = MaterialTheme.typography.titleLarge, color = Ink)
        Text(text = facet.date, style = MaterialTheme.typography.labelSmall, color = Muted)
        Spacer(modifier = Modifier.height(14.dp))
        Text(text = stringResource(R.string.onboarding_facets_mock_favorites_label), style = MaterialTheme.typography.labelSmall, color = Faint)
        Spacer(modifier = Modifier.height(6.dp))
        facet.favorites.forEach { name ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 3.dp)) {
                AppIcon(icon = null, size = 16.dp, cornerRadius = 5.dp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = name, style = MaterialTheme.typography.labelMedium, color = Ink)
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            facet.dock.forEach { _ ->
                AppIcon(icon = null, size = 20.dp, cornerRadius = 6.dp, contentDescription = null)
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun OnboardingFacetsPagePreview() {
    FacetLauncherTheme {
        OnboardingFacetsPage(uiState = OnboardingUiState(), onBack = {}, onNext = {})
    }
}
