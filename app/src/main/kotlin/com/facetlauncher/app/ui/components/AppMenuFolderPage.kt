package com.facetlauncher.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.R
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Muted

/** The app sheet's "Add to folder" page: the Add to folder icon and title, a create-new row, then the existing folders ([content]). */
@Composable
internal fun AppMenuFolderPage(
    onBack: () -> Unit,
    onCreateFolder: () -> Unit,
    content: @Composable () -> Unit,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = MENU_SIDE_PADDING, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = stringResource(R.string.content_description_back),
                tint = Ink,
                modifier = Modifier
                    .testTag("app_context_menu_folder_page_back")
                    .clickable(onClick = onBack)
                    .padding(8.dp),
            )
            // The same folder icon and + badge as the sheet's "Add to folder" tile.
            Box(modifier = Modifier.size(AppIconSize.COMPACT), contentAlignment = Alignment.Center) {
                Icon(imageVector = Icons.Outlined.Folder, contentDescription = null, tint = Muted, modifier = Modifier.size(32.dp))
                PlacementBadge(isRemove = false, modifier = Modifier.align(Alignment.TopEnd).offset(x = 4.dp, y = (-2).dp))
            }
            Text(
                text = stringResource(R.string.folder_add_to_folder),
                style = MaterialTheme.typography.titleLarge,
                color = Ink,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp).testTag("app_context_menu_folder_page_title"),
            )
        }
        CardDivider()
        AppContextMenuItem(
            label = stringResource(R.string.folder_create_new_folder),
            modifier = Modifier.testTag("app_context_menu_create_folder"),
            leadingIcon = { Icon(imageVector = Icons.Outlined.CreateNewFolder, contentDescription = null, tint = Accent) },
            onClick = onCreateFolder,
        )
        CardDivider(modifier = Modifier.padding(horizontal = 24.dp))
        Column(modifier = Modifier.heightIn(max = 280.dp).verticalScroll(rememberScrollState())) { content() }
    }
}
