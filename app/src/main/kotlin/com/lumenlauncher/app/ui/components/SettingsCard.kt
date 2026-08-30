package com.lumenlauncher.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lumenlauncher.app.ui.theme.Hairline
import com.lumenlauncher.app.ui.theme.Surface

/**
 * Groups related settings rows into a visually distinct card — M3's own default Card shape
 * ([MaterialTheme.shapes.medium], 12dp — see `CLAUDE.md`'s Material 3 shape section). The page
 * background is also [Surface], so a 1px [Hairline] border (not a background-color change) is
 * what makes the card read as a group — this is the first real use of [Hairline], which was
 * imported-but-unused in `SettingsScreen.kt` even though README's own row-separator spec called
 * for it.
 */
@Composable
fun SettingsCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val shape = MaterialTheme.shapes.medium
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Surface, shape)
            .border(1.dp, Hairline, shape)
            .padding(horizontal = 16.dp),
        content = content,
    )
}

/** 1px divider between rows inside a [SettingsCard] — omit after the last row. */
@Composable
fun CardDivider(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().height(1.dp).background(Hairline))
}
