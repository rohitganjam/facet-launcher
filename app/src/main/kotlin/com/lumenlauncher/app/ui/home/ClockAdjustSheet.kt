package com.lumenlauncher.app.ui.home

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lumenlauncher.app.R
import com.lumenlauncher.app.ui.theme.Faint
import com.lumenlauncher.app.ui.theme.IconTile
import com.lumenlauncher.app.ui.theme.Ink
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme
import com.lumenlauncher.app.ui.theme.Surface

/**
 * Long-press bottom sheet: enter the combined adjust mode (move handle + resize handles together),
 * or jump to the clock/calendar style gallery.
 */
@Composable
fun ClockAdjustSheet(
    onAdjustClick: () -> Unit,
    onEditStylesClick: () -> Unit,
    isOverridden: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("clock_adjust_sheet")
            .background(Surface, shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .padding(bottom = 24.dp),
    ) {
        // Drag affordance / handle
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 12.dp, bottom = 8.dp)
                .size(width = 34.dp, height = 4.dp)
                .background(color = Faint, shape = RoundedCornerShape(2.dp)),
        )
        
        AdjustRow(
            iconRes = R.drawable.open_in_full,
            label = "Adjust size & position",
            onClick = onAdjustClick,
            testTag = "clock_adjust_open",
        )
        AdjustRow(
            iconRes = R.drawable.ic_palette_24,
            label = if (isOverridden) "Edit Profile clock & calendar styles" else "Edit Default clock & calendar styles",
            onClick = onEditStylesClick,
            testTag = "clock_adjust_edit_styles",
        )
    }
}

@Composable
private fun AdjustRow(
    iconRes: Int,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier.size(32.dp).clip(CircleShape).background(IconTile),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = Surface,
                modifier = Modifier.size(18.dp),
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = Ink,
            modifier = Modifier.weight(1f)
        )
    }
}

@Preview(showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ClockAdjustSheetPreview() {
    LumenLauncherTheme {
        ClockAdjustSheet(onAdjustClick = {}, onEditStylesClick = {}, isOverridden = false)
    }
}
