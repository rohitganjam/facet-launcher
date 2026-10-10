package com.facetlauncher.app.ui.hub

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.R
import com.facetlauncher.app.ui.components.dashedBorder
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.HomeAppTextColorFaint
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.homeAppLabelShadow

/** README `4d` — shown once the Hub hits its 20-widget cap; the header's own Add greys out alongside this. */
@Composable
fun HubAtCapacityStrip(onManageClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag("hub_at_capacity_strip")
            .dashedBorder(color = HomeAppTextColorFaint, cornerRadius = 10.dp)
            .padding(horizontal = 13.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.hub_at_capacity_message),
            style = MaterialTheme.typography.bodyMedium.copy(shadow = homeAppLabelShadow(HomeAppTextColorFaint)),
            color = HomeAppTextColorFaint,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = stringResource(R.string.hub_at_capacity_manage),
            style = MaterialTheme.typography.bodyMedium.copy(shadow = homeAppLabelShadow(Accent)),
            color = Accent,
            modifier = Modifier.clip(CircleShape).clickable(onClick = onManageClick),
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 100)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 100, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HubAtCapacityStripPreview() {
    FacetLauncherTheme {
        HubAtCapacityStrip(onManageClick = {}, modifier = Modifier.padding(24.dp))
    }
}
