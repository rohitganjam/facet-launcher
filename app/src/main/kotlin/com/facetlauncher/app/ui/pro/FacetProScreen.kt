package com.facetlauncher.app.ui.pro

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.ProReason
import com.facetlauncher.app.ui.components.TextActionButton
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.ErrorColor
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.Surface
import com.facetlauncher.app.ui.theme.SystemBarsBackdrop
import com.facetlauncher.app.ui.theme.SystemBarsBackdropEffect

/**
 * Settings → Facet Pro, and where every Pro limit lands. A dark hero with three example facets, the three
 * things Pro gives, and Unlock pinned at the bottom; once bought, the same hero lit up with what is unlocked. See `docs/architecture/16-flow-billing.md` and design turn 12.
 */
@Composable
fun FacetProScreen(
    reason: ProReason,
    onBack: () -> Unit,
    onAddFacet: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FacetProViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalContext.current.findActivity()
    LaunchedEffect(Unit) { viewModel.onScreenShown() }
    FacetProContent(
        reason = reason,
        state = state,
        onBack = onBack,
        onBuy = activity?.let { host -> { viewModel.buy(host) } },
        onRestore = viewModel::restore,
        onAddFacet = onAddFacet,
        modifier = modifier,
    )
}

/** The screen's body, stateless so previews and tests can drive it. A null [onBuy] or [onRestore] hides that action. */
@Composable
internal fun FacetProContent(
    reason: ProReason,
    state: FacetProUiState,
    onBack: () -> Unit,
    onBuy: (() -> Unit)?,
    onRestore: (() -> Unit)?,
    onAddFacet: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Free: dark hero over a normal page (so the navigation bar follows the theme). Pro: dark all the way down.
    SystemBarsBackdropEffect(if (state.isPro) SystemBarsBackdrop.DARK_SURFACE else SystemBarsBackdrop.DARK_TOP)
    if (state.isPro) {
        UnlockedScreen(state, onBack, onRestore, onAddFacet, modifier)
    } else {
        UpgradeScreen(reason, state, onBack, onBuy, onRestore, modifier)
    }
}

// ---- Before purchase (12b) ----

@Composable
private fun UpgradeScreen(
    reason: ProReason,
    state: FacetProUiState,
    onBack: () -> Unit,
    onBuy: (() -> Unit)?,
    onRestore: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().background(Surface).testTag("facet_pro_screen")) {
        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            UpgradeHero(reason, state, onBack)
            Column(modifier = Modifier.padding(horizontal = 24.dp).padding(top = 6.dp, bottom = 16.dp)) {
                FeatureRow(Icons.Default.Layers, R.string.pro_feature_facets_title, R.string.pro_feature_facets_body)
                FeatureRow(Icons.Default.Sensors, R.string.pro_feature_triggers_title, R.string.pro_feature_triggers_body)
                FeatureRow(Icons.Default.Widgets, R.string.pro_feature_widgets_title, R.string.pro_feature_widgets_body)
                FeatureRow(Icons.Default.AutoAwesome, R.string.pro_feature_future_title, R.string.pro_feature_future_body)
            }
        }
        UpgradeBottomBar(state, onBuy, onRestore)
    }
}

@Composable
private fun UpgradeHero(reason: ProReason, state: FacetProUiState, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .proInkBackground(ProGlow(0.5f, 0.38f, 0.60f, 0.48f), ProGlow(1f, 0f, 0.25f, 0.50f))
            .statusBarsPadding(),
    ) {
        HeroHeader(onBack = onBack, trailing = {})
        ProHeroFan(lit = false, modifier = Modifier.padding(top = 2.dp), scale = 0.82f)
        Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)) {
            TriggerPill(Icons.Default.Wifi, stringResource(R.string.pro_hero_trigger_office), stringResource(R.string.pro_hero_trigger_work))
            TriggerPill(Icons.Default.Bluetooth, stringResource(R.string.pro_hero_trigger_car), stringResource(R.string.pro_hero_trigger_drive))
        }
        Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 22.dp, bottom = 20.dp)) {
            ProBadge()
            Text(
                text = stringResource(R.string.pro_hero_title),
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Light,
                lineHeight = 31.sp,
                letterSpacing = (-0.02).em,
                modifier = Modifier.padding(top = 8.dp).semantics { heading() },
            )
            if (reason != ProReason.ABOUT) {
                Text(
                    text = stringResource(reason.noteRes()),
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.5.sp,
                    lineHeight = 19.sp,
                    modifier = Modifier.padding(top = 10.dp).testTag("facet_pro_reason"),
                )
            }
            state.message?.let { message ->
                Text(
                    text = stringResource(message.textRes()),
                    color = if (message == FacetProMessage.PLAY_UNAVAILABLE) lerp(Color.White, ErrorColor, 0.5f) else Color.White.copy(alpha = 0.8f),
                    fontSize = 12.5.sp,
                    modifier = Modifier.padding(top = 10.dp).testTag("facet_pro_message"),
                )
            }
        }
    }
}

@Composable
internal fun HeroHeader(onBack: () -> Unit, trailing: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.content_description_back),
                tint = Color.White.copy(alpha = 0.7f),
            )
        }
        trailing()
    }
}

@Composable
private fun FeatureRow(icon: ImageVector, titleRes: Int, bodyRes: Int) {
    val accent = Accent
    val tile = MaterialTheme.shapes.medium
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(tile)
                .background(lerp(Surface, accent, 0.10f))
                .border(1.dp, accent.copy(alpha = 0.18f), tile),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
        }
        Column {
            Text(text = stringResource(titleRes), color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(text = stringResource(bodyRes), color = Muted, fontSize = 11.5.sp, lineHeight = 17.sp, modifier = Modifier.padding(top = 2.dp))
        }
    }
}

@Composable
private fun UpgradeBottomBar(state: FacetProUiState, onBuy: (() -> Unit)?, onRestore: (() -> Unit)?) {
    Column(modifier = Modifier.fillMaxWidth().shadow(10.dp).background(Surface).navigationBarsPadding().padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 6.dp)) {
        if (onBuy != null) {
            ProGradientButton(text = state.price?.let { stringResource(R.string.pro_unlock_price, it) } ?: stringResource(R.string.pro_unlock), onClick = onBuy, enabled = !state.busy, tag = "facet_pro_buy")
        }
        if (onRestore != null) {
            TextActionButton(
                text = stringResource(R.string.pro_restore),
                onClick = onRestore,
                enabled = !state.busy,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, fontWeight = FontWeight.Medium),
                modifier = Modifier.fillMaxWidth().testTag("facet_pro_restore_link"),
            )
        }
    }
}

private fun ProReason.noteRes(): Int = when (this) {
    ProReason.TRIGGER -> R.string.pro_upgrade_trigger
    ProReason.RULE_LIMIT -> R.string.pro_upgrade_rule_limit
    ProReason.FACET_LIMIT -> R.string.pro_upgrade_facet_limit
    ProReason.FACET_LOCKED -> R.string.pro_upgrade_facet_locked
    ProReason.CUSTOM_WIDGET -> R.string.pro_upgrade_custom_widget
    ProReason.ABOUT -> R.string.pro_upgrade_about
}

internal fun FacetProMessage.textRes(): Int = when (this) {
    FacetProMessage.PLAY_UNAVAILABLE -> R.string.pro_message_unavailable
    FacetProMessage.NO_PURCHASE_FOUND -> R.string.pro_message_no_purchase
    FacetProMessage.PENDING -> R.string.pro_message_pending
    FacetProMessage.RESTORED -> R.string.pro_message_restored
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
