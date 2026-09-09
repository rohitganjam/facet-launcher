package com.lumenlauncher.app.ui.profiles

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumenlauncher.app.data.local.ProfileEntity
import com.lumenlauncher.app.ui.components.BackButton
import com.lumenlauncher.app.ui.components.ConfirmDialog
import com.lumenlauncher.app.ui.components.StickyHeaderLayout
import com.lumenlauncher.app.ui.components.ThemedDropdownMenu
import com.lumenlauncher.app.ui.components.ThemedDropdownMenuItem
import com.lumenlauncher.app.ui.components.rememberDragReorderState
import com.lumenlauncher.app.ui.theme.Accent
import com.lumenlauncher.app.ui.theme.Faint
import com.lumenlauncher.app.ui.theme.Ink
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme
import com.lumenlauncher.app.ui.theme.Muted
import com.lumenlauncher.app.ui.theme.Surface
import com.lumenlauncher.app.ui.theme.SurfaceContainer

private val REORDER_ROW_HEIGHT = 64.dp
private val REORDER_ROW_SPACING = 10.dp

/** Shared "picked up" drag-lift treatment, matching every other reorderable list in Settings. */
private val REORDER_DRAG_ELEVATION = 6.dp
private const val REORDER_DRAG_SCALE = 1.04f

/**
 * Settings → Profiles (`3b`): a normal settings screen — [StickyHeaderLayout] header + a
 * drag-to-reorder list of profiles with add / per-profile-settings / delete. Reached both from
 * Settings and from the Switch Profiles carousel's "Reorder" button.
 */
@Composable
fun ManageProfilesScreen(
    onBack: () -> Unit,
    onEditProfile: (profileId: Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ManageProfilesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ManageProfilesContent(
        uiState = uiState,
        onBack = onBack,
        onReorder = viewModel::reorderProfiles,
        onEditProfile = onEditProfile,
        onDeleteProfile = viewModel::deleteProfile,
        onAddProfile = viewModel::addProfile,
        modifier = modifier,
    )
}

@Composable
private fun ManageProfilesContent(
    uiState: ManageProfilesUiState,
    onBack: () -> Unit,
    onReorder: (List<ProfileEntity>) -> Unit,
    onEditProfile: (Long) -> Unit,
    onDeleteProfile: (ProfileEntity) -> Unit,
    onAddProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var deletingProfile by remember { mutableStateOf<ProfileEntity?>(null) }
    // remember(profiles): a committed reorder re-emits the list from Room; re-seeding the working
    // copy on identity keeps the drag state from fighting the fresh list (same pattern the
    // carousel used).
    var workingList by remember(uiState.profiles) { mutableStateOf(uiState.profiles) }

    StickyHeaderLayout(
        modifier = modifier,
        header = { ManageProfilesHeader(onBack = onBack) },
        content = { headerHeight ->
            ProfileReorderList(
                profiles = workingList,
                canAddProfile = uiState.canAddProfile,
                canDeleteProfile = uiState.canDeleteProfile,
                onOrderChanged = { workingList = it },
                onCommit = { onReorder(workingList) },
                onEditProfile = onEditProfile,
                onDeleteRequest = { deletingProfile = it },
                onAddProfile = onAddProfile,
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = headerHeight + 12.dp, bottom = 24.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .background(SurfaceContainer)
                    .testTag("manage_profiles_screen")
                    .windowInsetsPadding(WindowInsets.systemBars),
            )
        },
    )

    deletingProfile?.let { profile ->
        ConfirmDialog(
            title = "Delete ${profile.name}?",
            message = "This can't be undone. Its favorites will be removed too.",
            confirmLabel = "Delete",
            onConfirm = { onDeleteProfile(profile); deletingProfile = null },
            onDismiss = { deletingProfile = null },
        )
    }
}

@Composable
private fun ManageProfilesHeader(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceContainer)
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BackButton(onClick = onBack)
        Text(text = "Manage Profiles", style = MaterialTheme.typography.headlineSmall, color = Ink)
    }
}

/**
 * A plain vertical list (never scrolls in practice — capped at
 * [com.lumenlauncher.app.data.ProfileRepository.MAX_PROFILES] rows) of drag-handle + name +
 * overflow rows, with a non-reorderable "Add profile" row pinned at the bottom. Only the handle
 * on the left has a drag gesture attached, so there's no long-press/axis-conflict to arbitrate.
 */
@Composable
private fun ProfileReorderList(
    profiles: List<ProfileEntity>,
    canAddProfile: Boolean,
    canDeleteProfile: Boolean,
    onOrderChanged: (List<ProfileEntity>) -> Unit,
    onCommit: () -> Unit,
    onEditProfile: (Long) -> Unit,
    onDeleteRequest: (ProfileEntity) -> Unit,
    onAddProfile: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val rowSlotHeightPx = with(LocalDensity.current) { (REORDER_ROW_HEIGHT + REORDER_ROW_SPACING).toPx() }
    val reorderState = rememberDragReorderState(
        items = profiles,
        key = { it.id },
        axis = Orientation.Vertical,
        slotSizePx = rowSlotHeightPx,
        onOrderChanged = onOrderChanged,
        onDragCommit = { onCommit() },
    )

    LazyColumn(
        modifier = modifier.testTag("profile_reorder_list"),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(REORDER_ROW_SPACING),
    ) {
        item(key = "reorder_hint") {
            Text(
                text = "Drag to re-order profiles",
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
                modifier = Modifier.padding(bottom = 4.dp).testTag("profile_reorder_hint"),
            )
        }
        items(profiles, key = { it.id }) { profile ->
            val isDragged = reorderState.isDragging(profile)
            ProfileReorderRow(
                profile = profile,
                canDelete = canDeleteProfile,
                onEditProfileClick = { onEditProfile(profile.id) },
                onDeleteClick = { onDeleteRequest(profile) },
                dragHandleModifier = reorderState.dragModifier(profile),
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (isDragged) Modifier else Modifier.animateItem())
                    .graphicsLayer {
                        translationY = if (isDragged) reorderState.dragOffset else 0f
                        scaleX = if (isDragged) REORDER_DRAG_SCALE else 1f
                        scaleY = if (isDragged) REORDER_DRAG_SCALE else 1f
                        shadowElevation = if (isDragged) REORDER_DRAG_ELEVATION.toPx() else 0f
                        shape = RoundedCornerShape(12.dp)
                        clip = false
                    }
                    .zIndex(if (isDragged) 1f else 0f),
            )
        }
        item(key = "add_profile_row") {
            AddProfileRow(
                enabled = canAddProfile,
                onClick = onAddProfile,
                modifier = Modifier.fillMaxWidth().testTag("profile_reorder_add_row"),
            )
        }
    }
}

@Composable
private fun ProfileReorderRow(
    profile: ProfileEntity,
    canDelete: Boolean,
    onEditProfileClick: () -> Unit,
    onDeleteClick: () -> Unit,
    dragHandleModifier: Modifier,
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .height(REORDER_ROW_HEIGHT)
            // M3's Card default shape — see CLAUDE.md's Material 3 shape section.
            .clip(MaterialTheme.shapes.medium)
            .background(Surface)
            .padding(horizontal = 12.dp)
            .testTag("profile_reorder_row_${profile.id}"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Default.DragHandle,
            contentDescription = "Drag to reorder",
            tint = Faint,
            modifier = Modifier
                .padding(end = 12.dp)
                .testTag("profile_reorder_handle_${profile.id}")
                .then(dragHandleModifier),
        )
        Text(text = profile.name, style = MaterialTheme.typography.bodyLarge, color = Ink, modifier = Modifier.weight(1f))
        Box {
            IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier.testTag("profile_reorder_menu_${profile.id}"),
            ) {
                Icon(Icons.Default.MoreVert, contentDescription = "Profile options", tint = Muted)
            }
            ThemedDropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                ThemedDropdownMenuItem(
                    label = "Profile settings",
                    onClick = { menuExpanded = false; onEditProfileClick() },
                )
                ThemedDropdownMenuItem(
                    label = "Delete",
                    enabled = canDelete,
                    destructive = true,
                    onClick = { menuExpanded = false; onDeleteClick() },
                )
            }
        }
    }
}

@Composable
private fun AddProfileRow(enabled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    if (!enabled) return
    Row(
        modifier = modifier
            .height(REORDER_ROW_HEIGHT)
            // M3's Card default shape — see CLAUDE.md's Material 3 shape section.
            .clip(MaterialTheme.shapes.medium)
            .background(Surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.Default.Add, contentDescription = null, tint = Accent)
        Text(text = "Add profile", style = MaterialTheme.typography.bodyLarge, color = Accent, modifier = Modifier.padding(start = 8.dp))
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ManageProfilesScreenPreview() {
    LumenLauncherTheme {
        ManageProfilesContent(
            uiState = ManageProfilesUiState(
                profiles = listOf(
                    ProfileEntity(id = 1, name = "Personal", position = 0),
                    ProfileEntity(id = 2, name = "Work", position = 1),
                ),
                activeProfileId = 1,
            ),
            onBack = {},
            onReorder = {},
            onEditProfile = {},
            onDeleteProfile = {},
            onAddProfile = {},
        )
    }
}
