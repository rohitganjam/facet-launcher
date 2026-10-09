package com.facetlauncher.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.Hairline
import com.facetlauncher.app.ui.theme.Surface

private val SettingsCardHorizontalPadding = 16.dp

/**
 * Horizontal inset a row must add itself: [SettingsCardHorizontalPadding] inside a card with
 * `fullBleedRows`, 0 otherwise. [ClickableRow][com.facetlauncher.app.ui.settings.ClickableRow] and
 * [CardDivider] read it, so they work in both kinds of card.
 */
@Suppress("CompositionLocalAllowlist") // Read by every row type inside a SettingsCard; threading it as a parameter would touch ~20 rows.
val LocalSettingsRowInset = compositionLocalOf { 0.dp }

/**
 * Groups related settings rows into one tonal card: a [Surface] fill on the dimmer
 * [com.facetlauncher.app.ui.theme.SurfaceContainer] page, no shadow or border. M3's extra-large
 * shape (28dp) — the card clips its content, so a row's press highlight follows the rounded corners.
 *
 * By default the card pads its content 16dp on each side. With [fullBleedRows] it doesn't, and the
 * rows pad themselves (via [LocalSettingsRowInset]) — so a clickable row's press highlight spans the
 * full card width. Use it for cards made only of tappable rows.
 */
@Composable
fun SettingsCard(modifier: Modifier = Modifier, fullBleedRows: Boolean = false, content: @Composable ColumnScope.() -> Unit) {
    val shape = MaterialTheme.shapes.extraLarge
    val rowInset = if (fullBleedRows) SettingsCardHorizontalPadding else 0.dp
    CompositionLocalProvider(LocalSettingsRowInset provides rowInset) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .clip(shape)
                .background(Surface)
                .padding(horizontal = SettingsCardHorizontalPadding - rowInset),
            content = content,
        )
    }
}

/** 1px divider between rows inside a [SettingsCard] — omit after the last row. [startInset] clears a leading icon. */
@Composable
fun CardDivider(modifier: Modifier = Modifier, startInset: Dp = 0.dp) {
    val rowInset = LocalSettingsRowInset.current
    Box(modifier = modifier.fillMaxWidth().padding(start = startInset + rowInset, end = rowInset).height(1.dp).background(Hairline))
}

/** Title above a [SettingsCard]. */
@Composable
fun SettingsSectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = Accent,
        modifier = modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp),
    )
}
