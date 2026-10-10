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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.facetlauncher.app.R
import com.facetlauncher.app.ui.theme.LocalIsDarkTheme

/**
 * Settings' Facet Pro entry: a dark ink card at the top of the page (the most contrast on it, so it reads
 * first). Free: the facet fan, the PRO mark, a "You're on the Free plan" line, an evergreen headline ("The complete Facet experience, fully unlocked.", no numbers or feature names so it never needs editing), an open-lock icon and an "Upgrade to Pro" button. Pro: shrinks to one row with a
 * live summary. In dark theme it gets a hairline inset and a deeper shadow so it separates from the page.
 */
@Composable
fun ProSettingsCard(
    isPro: Boolean,
    facetCount: Int,
    triggersRunning: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = MaterialTheme.shapes.extraLarge
    val dark = LocalIsDarkTheme.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .graphicsLayer { shadowElevation = (if (dark) 14 else 10).dp.toPx(); this.shape = shape; clip = false }
            .clip(shape)
            .proInkBackground(ProGlow(1f, 0f, 0.70f, 0.70f), ProGlow(0f, 1.2f, 0.30f, 0.60f))
            .then(if (dark) Modifier.border(1.dp, Color(0xFFE2E8F0).copy(alpha = 0.10f), shape) else Modifier)
            .clickable(onClick = onClick)
            .testTag("facet_pro_row"),
    ) {
        if (isPro) ProActiveRow(facetCount, triggersRunning) else ProUpsell()
    }
}

@Composable
private fun ProUpsell() {
    Box(modifier = Modifier.fillMaxWidth()) {
        ProFacetFan(modifier = Modifier.align(Alignment.TopEnd).padding(end = 24.dp, top = 16.dp))
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 15.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ProBadge(textSize = 9f)
                Text(
                    text = stringResource(R.string.settings_pro_free_plan),
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.testTag("facet_pro_free_plan"),
                )
            }
            Text(
                text = stringResource(R.string.pro_card_headline),
                color = Color.White,
                fontSize = 17.sp,
                lineHeight = 21.sp,
                modifier = Modifier.padding(top = 12.dp).widthIn(max = 200.dp),
            )
            Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Default.LockOpen, contentDescription = null, tint = Color.White.copy(alpha = 0.75f), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.weight(1f))
                val buttonShape = CircleShape
                Text(
                    text = stringResource(R.string.pro_upgrade_to_pro),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clip(buttonShape)
                        .background(Color.White.copy(alpha = 0.12f))
                        .border(1.dp, Color.White.copy(alpha = 0.18f), buttonShape)
                        .padding(horizontal = 13.dp, vertical = 7.dp)
                        .testTag("facet_pro_upgrade"),
                )
            }
        }
    }
}

@Composable
private fun ProActiveRow(facetCount: Int, triggersRunning: Int) {
    val facets = pluralStringResource(R.plurals.settings_pro_facet_count, facetCount, facetCount)
    val summary = if (triggersRunning > 0) {
        stringResource(R.string.settings_pro_summary, facets, pluralStringResource(R.plurals.settings_pro_triggers_running, triggersRunning, triggersRunning))
    } else {
        facets
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ProBadge(textSize = 9f)
        Column(modifier = Modifier.weight(1f)) {
            Text(text = stringResource(R.string.pro_active_title), color = Color.White, fontSize = 14.5.sp)
            Text(text = summary, color = Color.White.copy(alpha = 0.6f), fontSize = 11.5.sp, modifier = Modifier.padding(top = 1.dp).testTag("facet_pro_summary"))
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.White.copy(alpha = 0.6f))
    }
}
