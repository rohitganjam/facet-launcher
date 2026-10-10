package com.facetlauncher.app.ui.pro

import android.content.res.Configuration
import com.facetlauncher.app.ui.theme.SurfaceContainer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.facetlauncher.app.data.model.ProReason
import com.facetlauncher.app.ui.theme.FacetLauncherTheme

@Composable
private fun ProPreview(state: FacetProUiState, reason: ProReason = ProReason.FACET_LIMIT) {
    FacetLauncherTheme { FacetProContent(reason = reason, state = state, onBack = {}, onBuy = {}, onRestore = {}, onAddFacet = {}) }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FacetProFreePreview() = ProPreview(FacetProUiState(price = "$9.99"))

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun FacetProMessagePreview() = ProPreview(FacetProUiState(price = "$9.99", message = FacetProMessage.NO_PURCHASE_FOUND), ProReason.ABOUT)

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FacetProOwnedPreview() = ProPreview(FacetProUiState(isPro = true), ProReason.ABOUT)

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun FacetProOwnedRestoredPreview() = ProPreview(FacetProUiState(isPro = true, message = FacetProMessage.RESTORED), ProReason.ABOUT)

@Preview(showBackground = true, widthDp = 390, heightDp = 300)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 300, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ProSettingsCardFreePreview() {
    FacetLauncherTheme { Box(Modifier.background(SurfaceContainer).padding(horizontal = 24.dp)) { ProSettingsCard(isPro = false, facetCount = 2, triggersRunning = 0, onClick = {}) } }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 120)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 120, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ProSettingsCardProPreview() {
    FacetLauncherTheme { Box(Modifier.background(SurfaceContainer).padding(horizontal = 24.dp)) { ProSettingsCard(isPro = true, facetCount = 3, triggersRunning = 2, onClick = {}) } }
}
