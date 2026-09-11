package com.lumenlauncher.app.ui.onboarding

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lumenlauncher.app.ui.theme.Accent
import com.lumenlauncher.app.ui.theme.Faint
import com.lumenlauncher.app.ui.theme.IconTile
import com.lumenlauncher.app.ui.theme.Ink
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme
import com.lumenlauncher.app.ui.theme.Muted
import com.lumenlauncher.app.ui.theme.Surface

/**
 * Onboarding step 1 (`4f`) — one-breath framing, no interaction beyond Next. Names the three parts
 * of Home (clock / app list / dock) so "dock" isn't a surprise in step 2, and previews the three
 * gestures that will actually be available afterward — kept in sync with the real gesture map
 * (`PRD.md` §5): swipe up → App Drawer, swipe right → Hub, swipe left → Switch Profiles.
 */
@Composable
fun OnboardingIntroPage(onNext: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("onboarding_intro_page")
            .padding(horizontal = 32.dp, vertical = 24.dp),
    ) {
        Column(modifier = Modifier.weight(1f).padding(top = 72.dp)) {
            Text(
                text = "A home screen that focuses on you.",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Light),
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
            ConceptLine(glyph = "↑", label = "Swipe up any time for all your apps.")
            Spacer(modifier = Modifier.height(20.dp))
            ConceptLine(glyph = "→", label = "Swipe right for your widgets.")
            Spacer(modifier = Modifier.height(20.dp))
            ConceptLine(glyph = "←", label = "Swipe left to switch profiles.")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OnboardingDots(step = 0, totalSteps = 4)
            Text(
                text = "Next",
                style = MaterialTheme.typography.bodyLarge,
                color = Accent,
                modifier = Modifier.clickable(onClick = onNext).testTag("onboarding_next").padding(8.dp),
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
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Light),
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
            LeaderRow(label = "Clock", height = CLOCK_SECTION_HEIGHT)
            Spacer(modifier = Modifier.height(SECTION_GAP))
            LeaderRow(label = "Your apps", height = APPS_SECTION_HEIGHT)
            Spacer(modifier = Modifier.height(SECTION_GAP))
            LeaderRow(label = "Dock", height = DOCK_TILE_SIZE)
        }
    }
}

/** A leader line + label, vertically centered within [height] to match the card content it points at. */
@Composable
private fun LeaderRow(label: String, height: Dp, modifier: Modifier = Modifier) {
    Row(modifier = modifier.height(height), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.width(24.dp).height(1.dp).background(Faint))
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = Muted)
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

@Composable
private fun ConceptLine(glyph: String, label: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(text = glyph, style = MaterialTheme.typography.titleLarge, color = Faint)
        Text(text = label, style = MaterialTheme.typography.bodyLarge, color = Ink)
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun OnboardingIntroPagePreview() {
    LumenLauncherTheme {
        OnboardingIntroPage(onNext = {})
    }
}
