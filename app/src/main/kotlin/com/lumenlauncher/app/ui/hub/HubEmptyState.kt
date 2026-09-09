package com.lumenlauncher.app.ui.hub

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lumenlauncher.app.ui.components.TonalButton
import com.lumenlauncher.app.ui.components.dashedBorder
import com.lumenlauncher.app.ui.theme.HomeAppTextColor
import com.lumenlauncher.app.ui.theme.HomeAppTextColorFaint
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme
import com.lumenlauncher.app.ui.theme.Muted
import com.lumenlauncher.app.ui.theme.homeAppLabelShadow

/** README `4b` — the add affordance is the content, not a caption under a blank grid. */
@Composable
fun HubEmptyState(onAddClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .padding(top = 0.dp)
                .width(96.dp)
                .dashedBorder(color = HomeAppTextColorFaint, cornerRadius = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "+",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Light,
                    shadow = homeAppLabelShadow(HomeAppTextColorFaint),
                ),
                color = HomeAppTextColorFaint,
                modifier = Modifier.padding(vertical = 22.dp),
            )
        }
        Text(
            text = "No widgets yet",
            style = MaterialTheme.typography.titleMedium.copy(shadow = homeAppLabelShadow(HomeAppTextColor)),
            color = HomeAppTextColor,
            modifier = Modifier.padding(top = 22.dp),
        )
        Text(
            text = "Add a widget to get started.",
            style = MaterialTheme.typography.bodyMedium.copy(shadow = homeAppLabelShadow(HomeAppTextColorFaint)),
            color = Muted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp).width(250.dp),
        )
        TonalButton(
            text = "Add widget",
            onClick = onAddClick,
            modifier = Modifier.padding(top = 20.dp).testTag("hub_empty_add_widget"),
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 600)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 600, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HubEmptyStatePreview() {
    LumenLauncherTheme {
        Box(modifier = Modifier.fillMaxSize().padding(top = 100.dp)) {
            HubEmptyState(onAddClick = {})
        }
    }
}
