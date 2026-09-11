package com.lumenlauncher.app.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.lumenlauncher.app.ui.theme.Accent
import com.lumenlauncher.app.ui.theme.Faint

/** Onboarding's shared pagination indicator — active step a 16×5 rounded bar, inactive steps a 5dp dot. */
@Composable
fun OnboardingDots(step: Int, totalSteps: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.semantics { contentDescription = "Step ${step + 1} of $totalSteps" }.testTag("onboarding_dots"),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        repeat(totalSteps) { index ->
            val active = index == step
            Row(
                modifier = Modifier
                    .size(height = 5.dp, width = if (active) 16.dp else 5.dp)
                    .background(if (active) Accent else Faint, CircleShape),
            ) {}
        }
    }
}
