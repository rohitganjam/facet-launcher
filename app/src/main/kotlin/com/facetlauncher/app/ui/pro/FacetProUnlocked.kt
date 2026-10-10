package com.facetlauncher.app.ui.pro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.facetlauncher.app.R
import com.facetlauncher.app.ui.components.TextActionButton
import com.facetlauncher.app.ui.theme.Accent

/**
 * The post-purchase screen (design 12c): the whole page dark, the hero lit and what is unlocked. Add a facet and Restore
 * purchases (so a Pro user can re-check with Play) end the scrolling content rather than being pinned. A null [onRestore] hides Restore.
 */
@Composable
internal fun UnlockedScreen(state: FacetProUiState, onBack: () -> Unit, onRestore: (() -> Unit)?, onAddFacet: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().background(ProInk).testTag("facet_pro_screen")) {
        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).statusBarsPadding()) {
            HeroHeader(onBack = onBack, trailing = {
                TextActionButton(
                    text = stringResource(R.string.pro_done),
                    onClick = onBack,
                    color = Color.White.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                )
            })
            ProHeroFan(lit = true, modifier = Modifier.padding(top = 4.dp))
            Column(modifier = Modifier.fillMaxWidth().padding(top = 30.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                ProBadge()
                Text(
                    text = stringResource(R.string.pro_on_pro_title),
                    color = Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = (-0.02).em,
                    modifier = Modifier.padding(top = 14.dp).semantics { heading() }.testTag("facet_pro_owned"),
                )
                Text(
                    text = stringResource(R.string.pro_on_pro_body),
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.5.sp,
                    lineHeight = 19.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(start = 40.dp, end = 40.dp, top = 10.dp),
                )
            }
            Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 16.dp)) {
                UnlockedRow(Icons.Default.Layers, R.string.pro_owned_facets_title, R.string.pro_owned_facets_body)
                UnlockedRow(Icons.Default.Wifi, R.string.pro_owned_wifi_title, R.string.pro_owned_wifi_body)
                UnlockedRow(Icons.Default.Bluetooth, R.string.pro_owned_bluetooth_title, R.string.pro_owned_bluetooth_body)
                UnlockedRow(Icons.Default.Sensors, R.string.pro_owned_more_title, R.string.pro_owned_more_body)
            }
            UnlockedActions(state, onRestore, onAddFacet)
        }
    }
}

@Composable
private fun UnlockedActions(state: FacetProUiState, onRestore: (() -> Unit)?, onAddFacet: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        state.message?.let { message ->
            Text(
                text = stringResource(message.textRes()),
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.5.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 10.dp).testTag("facet_pro_message"),
            )
        }
        ProGradientButton(text = stringResource(R.string.pro_add_facet), onClick = onAddFacet, enabled = true, tag = "facet_pro_add_facet")
        if (onRestore != null) {
            TextActionButton(
                text = stringResource(R.string.pro_restore),
                onClick = onRestore,
                enabled = !state.busy,
                color = Color.White.copy(alpha = 0.8f),
                disabledColor = Color.White.copy(alpha = 0.35f),
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, fontWeight = FontWeight.Medium),
                modifier = Modifier.fillMaxWidth().testTag("facet_pro_restore_link"),
            )
        }
    }
}

@Composable
private fun UnlockedRow(icon: ImageVector, titleRes: Int, bodyRes: Int) {
    val accent = Accent
    val tint = lerp(Color.White, accent, 0.45f)
    Column {
        Spacer(modifier = Modifier.fillMaxWidth().height(1.dp).background(ProRowDivider))
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(13.dp)) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = stringResource(titleRes), color = Color.White, fontSize = 14.sp)
                Text(text = stringResource(bodyRes), color = Color.White.copy(alpha = 0.55f), fontSize = 11.5.sp, modifier = Modifier.padding(top = 1.dp))
            }
            Text(
                text = stringResource(R.string.pro_unlocked),
                color = lerp(Color.White, accent, 0.6f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.08.em,
            )
        }
    }
}
