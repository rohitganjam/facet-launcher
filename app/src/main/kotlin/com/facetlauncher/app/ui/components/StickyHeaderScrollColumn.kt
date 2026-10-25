package com.facetlauncher.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.SurfaceContainer

/**
 * A settings screen of plain content in a scrolling [Column] under a pinned back button and [title] (via [StickyHeaderLayout]), so
 * the header stays put while the content scrolls beneath it, like every other settings screen. [testTag] goes on the scrolling column;
 * [headerAction] sits at the end of the header row.
 */
@Composable
fun StickyHeaderScrollColumn(
    title: String,
    onBack: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    /** An action at the end of the header row, such as a create button. */
    headerAction: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    StickyHeaderLayout(
        modifier = modifier,
        header = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceContainer)
                    .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
                    .padding(horizontal = 24.dp)
                    .padding(top = 24.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                BackButton(onClick = onBack)
                Text(text = title, style = MaterialTheme.typography.headlineSmall, color = Ink, modifier = Modifier.weight(1f))
                headerAction?.invoke()
            }
        },
        content = { headerHeight ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SurfaceContainer)
                    .testTag(testTag)
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(top = headerHeight, bottom = 24.dp),
                verticalArrangement = verticalArrangement,
                content = content,
            )
        },
    )
}
