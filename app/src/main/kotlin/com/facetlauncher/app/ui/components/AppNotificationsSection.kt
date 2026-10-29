package com.facetlauncher.app.ui.components

import android.content.res.Configuration
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.NotificationInfo
import com.facetlauncher.app.domain.AppNotificationsState
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.SurfaceContainer
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableSet

/** Cards in view at once; a sliver of the next one shows below them so the list reads as scrollable. */
internal const val VISIBLE_NOTIFICATION_CARDS = 2
private const val PEEK_FRACTION = 0.4f
private val CARD_GAP = 8.dp

/** The list never takes more than this share of the screen, so large text can't push the actions off it. */
private const val MAX_WINDOW_SCREEN_FRACTION = 0.33f
private val ENABLE_ROW_MIN_HEIGHT = 56.dp

/** Height that shows [VISIBLE_NOTIFICATION_CARDS] cards of [cardHeight], the gaps between them, and a peek of the next. */
internal fun notificationWindowHeight(cardHeight: Dp): Dp =
    cardHeight * (VISIBLE_NOTIFICATION_CARDS + PEEK_FRACTION) + CARD_GAP * VISIBLE_NOTIFICATION_CARDS

/** Whether the zone shows at all: it does whenever the setting is on, so its separator is drawn with it. */
internal val AppNotificationsState.hasZone: Boolean get() = enabled

/**
 * The long-press sheet's notifications zone, always present while the setting is on: a header with
 * "Clear all" (greyed out unless there is something to clear), then one of a row asking for
 * notification access, an empty state, or a scrolling window of cards.
 */
@Composable
internal fun AppNotificationsSection(
    state: AppNotificationsState,
    onOpen: (NotificationInfo) -> Unit,
    onDismiss: (List<String>) -> Boolean,
    onEnableAccess: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val notifications = state.notifications
    // Persistent notifications are listed but can't be dismissed, so they don't count toward "Clear all".
    val clearableKeys = notifications.filter { it.isClearable }.map { it.key }
    val listState = rememberLazyListState()
    // Keys Clear all told to swipe away; each card dismisses itself once its swipe finishes.
    var swipingKeys by remember { mutableStateOf<ImmutableSet<String>>(persistentSetOf()) }
    // Animates the zone's height as rows leave and the list gives way to the empty state.
    Column(modifier = modifier.animateContentSize()) {
        NotificationsHeader(
            count = notifications.size,
            canClear = clearableKeys.isNotEmpty(),
            onClearAll = {
                val visibleKeys = listState.layoutInfo.visibleItemsInfo.map { it.key }
                swipingKeys = clearableKeys.filter { it in visibleKeys }.toImmutableSet()
                // Off-screen cards have no swipe to play.
                val offScreen = clearableKeys.filter { it !in swipingKeys }
                if (offScreen.isNotEmpty()) onDismiss(offScreen)
            },
        )
        when {
            state.needsAccess -> EnableNotificationsRow(onClick = onEnableAccess)
            notifications.isEmpty() -> EmptyNotificationsState()
            else -> NotificationsWindow(state, listState, swipingKeys, onOpen) { key -> onDismiss(listOf(key)) }
        }
    }
}

@Composable
private fun NotificationsHeader(count: Int, canClear: Boolean, onClearAll: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        val headerRatio = rememberChromeTextRatio()
        val headerStyle = MaterialTheme.typography.labelMedium.let { it.copy(fontSize = it.fontSize * headerRatio, lineHeight = it.lineHeight * headerRatio) }
        Text(
            text = if (count > 0) stringResource(R.string.app_menu_notifications_header, count) else stringResource(R.string.app_menu_notifications_title),
            style = headerStyle,
            color = Muted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        TextActionButton(
            text = stringResource(R.string.app_menu_notifications_clear_all),
            onClick = onClearAll,
            enabled = canClear,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.testTag("app_notifications_clear_all"),
        )
    }
}

@Composable
private fun NotificationsWindow(
    state: AppNotificationsState,
    listState: LazyListState,
    swipingKeys: ImmutableSet<String>,
    onOpen: (NotificationInfo) -> Unit,
    onDismissOne: (String) -> Boolean,
) {
    val notifications = state.notifications
    val density = LocalDensity.current
    val titleLine = MaterialTheme.typography.titleSmall.lineHeight
    val bodyLine = MaterialTheme.typography.bodyMedium.lineHeight
    // Until the first card is measured, size the window from the type scale so it doesn't jump.
    val estimatedCard = with(density) { CARD_VERTICAL_PADDING * 2 + titleLine.toDp() + bodyLine.toDp() * CARD_BODY_LINES }
    var measuredCardHeight by remember { mutableIntStateOf(0) }
    val cardHeight = if (measuredCardHeight > 0) with(density) { measuredCardHeight.toDp() } else estimatedCard
    val screenCap = LocalConfiguration.current.screenHeightDp.dp * MAX_WINDOW_SCREEN_FRACTION
    val windowMax = if (notifications.size > VISIBLE_NOTIFICATION_CARDS) notificationWindowHeight(cardHeight) else Dp.Infinity
    LazyColumn(
        state = listState,
        modifier = Modifier.heightIn(max = minOf(windowMax, screenCap)).testTag("app_notifications_list"),
        verticalArrangement = Arrangement.spacedBy(CARD_GAP),
    ) {
        itemsIndexed(notifications, key = { _, notification -> notification.key }) { index, notification ->
            NotificationCard(
                notification = notification,
                onOpen = { onOpen(notification) },
                swipeAway = notification.key in swipingKeys,
                onDismissOne = { onDismissOne(notification.key) },
                modifier = Modifier.animateItem().then(
                    if (index == 0) Modifier.onSizeChanged { measuredCardHeight = it.height } else Modifier,
                ),
            )
        }
    }
}

/** Access is granted but the app has nothing posted. */
@Composable
private fun EmptyNotificationsState(modifier: Modifier = Modifier) {
    val shape = MaterialTheme.shapes.medium
    Row(
        modifier = modifier
            .testTag("app_notifications_empty")
            .fillMaxWidth()
            .clip(shape)
            .background(SurfaceContainer, shape)
            .heightIn(min = ENABLE_ROW_MIN_HEIGHT)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.NotificationsNone,
            contentDescription = null,
            tint = Muted,
            modifier = Modifier.size(24.dp * rememberChromeIconScale()),
        )
        Text(text = stringResource(R.string.app_menu_notifications_empty), style = MaterialTheme.typography.bodyMedium, color = Muted)
    }
}

private val CARD_VERTICAL_PADDING = 10.dp
private const val CARD_BODY_LINES = 2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationCard(
    notification: NotificationInfo,
    onOpen: () -> Unit,
    swipeAway: Boolean,
    onDismissOne: () -> Boolean,
    modifier: Modifier = Modifier,
) {
    val tag = "app_notification_${notification.key}"
    if (notification.isClearable) {
        // Clear all drives the swipe itself and asks the listener after it, so the card is seen leaving.
        var drivenByClearAll by remember { mutableStateOf(false) }
        val dismissState = rememberSwipeToDismissBoxState(
            confirmValueChange = { target -> target == SwipeToDismissBoxValue.Settled || drivenByClearAll || onDismissOne() },
        )
        val currentDismissOne by rememberUpdatedState(onDismissOne)
        LaunchedEffect(swipeAway) {
            if (!swipeAway) return@LaunchedEffect
            drivenByClearAll = true
            var asked = false
            try {
                dismissState.dismiss(SwipeToDismissBoxValue.StartToEnd)
                asked = true
                // The listener refused: bring the card back rather than leave a hidden row.
                if (!currentDismissOne()) dismissState.reset()
            } finally {
                // The sheet closed mid-swipe: still clear the notification.
                if (!asked) currentDismissOne()
            }
        }
        SwipeToDismissBox(state = dismissState, backgroundContent = {}, modifier = modifier.testTag(tag)) {
            NotificationCardBody(notification, onOpen)
        }
    } else {
        // A persistent notification: it can be opened but not swiped away.
        NotificationCardBody(notification, onOpen, modifier = modifier.testTag(tag))
    }
}

@Composable
private fun NotificationCardBody(notification: NotificationInfo, onOpen: () -> Unit, modifier: Modifier = Modifier) {
    val shape = MaterialTheme.shapes.medium
    val title = notification.title?.takeIf { it.isNotBlank() }
    val text = notification.text?.takeIf { it.isNotBlank() }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(SurfaceContainer, shape)
            .clickable(onClick = onOpen)
            .padding(horizontal = 12.dp, vertical = CARD_VERTICAL_PADDING),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title ?: text.orEmpty(),
                style = MaterialTheme.typography.titleSmall,
                color = Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(8.dp))
            Text(text = shortAgeText(notification.postTime), style = MaterialTheme.typography.labelSmall, color = Muted)
        }
        if (title != null && text != null) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = Muted,
                maxLines = CARD_BODY_LINES,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** The same dashed strip the app uses for other missing permissions (contacts, usage access): what is needed, and where to go. */
@Composable
private fun EnableNotificationsRow(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .testTag("app_notifications_enable")
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .dashedBorder(color = Ink.copy(alpha = 0.16f), cornerRadius = 10.dp)
            .padding(horizontal = 13.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.app_menu_notifications_access_needed),
            style = MaterialTheme.typography.bodyMedium,
            color = Muted,
            modifier = Modifier.weight(1f).padding(end = 12.dp),
        )
        Text(text = stringResource(R.string.open_settings), style = MaterialTheme.typography.bodyMedium, color = Accent)
    }
}

@Composable
private fun previewNotifications(): List<NotificationInfo> {
    val titles = listOf(stringResource(R.string.quick_placement_favorites), stringResource(R.string.quick_placement_dock), stringResource(R.string.folder_add_to_folder))
    val text = stringResource(R.string.app_menu_notifications_empty)
    return titles.mapIndexed { index, title ->
        NotificationInfo(packageName = "p", silent = false, key = "k$index", title = title, text = text, postTime = System.currentTimeMillis() - index * 120_000L)
    }
}

@Preview(showBackground = true, widthDp = 360)
@Preview(name = "Dark", showBackground = true, widthDp = 360, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AppNotificationsSectionPreview() {
    FacetLauncherTheme {
        Box(Modifier.padding(24.dp)) {
            AppNotificationsSection(AppNotificationsState(previewNotifications()), onOpen = {}, onDismiss = { true }, onEnableAccess = {})
        }
    }
}

@Preview(name = "Needs access", showBackground = true, widthDp = 360)
@Composable
private fun AppNotificationsNeedsAccessPreview() {
    FacetLauncherTheme {
        Box(Modifier.padding(24.dp)) {
            AppNotificationsSection(AppNotificationsState(needsAccess = true), onOpen = {}, onDismiss = { true }, onEnableAccess = {})
        }
    }
}
