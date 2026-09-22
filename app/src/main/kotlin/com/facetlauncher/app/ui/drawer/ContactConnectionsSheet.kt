package com.facetlauncher.app.ui.drawer

import android.content.Intent
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.ConnectionDetail
import com.facetlauncher.app.data.model.ConnectionOption
import com.facetlauncher.app.data.model.ContactConnection
import com.facetlauncher.app.data.model.ContactConnectionType
import com.facetlauncher.app.ui.components.CardDivider
import com.facetlauncher.app.ui.theme.Faint
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.IconTile
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.Surface

/**
 * Phase 9's connections sheet — replaces the old tap-to-expand chip row (dividers between rows, a
 * right chevron on each). Dynamically sized: starts at 30% of the available height, grows to fit its rows, and
 * scrolls internally past a 70% cap rather than overflowing past it. A row whose [ContactConnection.detail]
 * is [ConnectionDetail.Multiple] (more than one phone number, say) doesn't fire anything directly —
 * it slides in a second, disambiguation page listing each option, matching the direction its own
 * chevron points; that page's own header names the connection type it's disambiguating rather than
 * repeating the contact's name, and a back chevron returns to the main list.
 */
@Composable
fun ContactConnectionsSheet(
    contactName: String,
    connections: List<ContactConnection>,
    onConnectionClick: (Intent) -> Unit,
    onViewContactClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var disambiguating by remember { mutableStateOf<ContactConnection?>(null) }
    // The main page's own measured height, captured the moment it's on screen — floors the
    // disambiguation page so opening it (which can easily have fewer rows than the main list)
    // never visibly shrinks the sheet; it can still grow past this if it has more rows than fit.
    var mainContentHeightPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current

    // System back while disambiguating returns to the main list instead of falling through to
    // whatever the launcher's own back handling would otherwise do (closing the whole sheet, or
    // further, closing the Drawer) — see chat history.
    BackHandler(enabled = disambiguating != null) { disambiguating = null }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val minHeight = maxHeight * 0.3f
        val maxSheetHeight = maxHeight * 0.7f
        val disambiguationMinHeight = with(density) { mainContentHeightPx.toDp() }.coerceAtLeast(minHeight)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("contact_connections_sheet")
                // M3's ModalBottomSheet default shape (SheetDefaults.ExpandedShape) is
                // extraLarge (28dp), applied top-only — see CLAUDE.md's Material 3 shape section.
                .background(Surface, shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                // clickable after background/clip so a ripple (if this were ever enabled) would
                // respect the shape above rather than fill the full rectangular bounds.
                .clickable(enabled = false, onClick = {})
                .heightIn(min = if (disambiguating != null) disambiguationMinHeight else minHeight, max = maxSheetHeight),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 12.dp, bottom = 8.dp)
                    .size(width = 34.dp, height = 4.dp)
                    .background(color = Faint, shape = RoundedCornerShape(2.dp)),
            )

            AnimatedContent(
                targetState = disambiguating,
                transitionSpec = {
                    if (targetState != null) {
                        (slideInHorizontally(animationSpec = tween(280)) { it } + fadeIn())
                            .togetherWith(slideOutHorizontally(animationSpec = tween(280)) { -it } + fadeOut())
                    } else {
                        (slideInHorizontally(animationSpec = tween(280)) { -it } + fadeIn())
                            .togetherWith(slideOutHorizontally(animationSpec = tween(280)) { it } + fadeOut())
                    }
                },
                label = "contact_connections_sheet_page",
            ) { target ->
                if (target == null) {
                    Column(modifier = Modifier.onSizeChanged { mainContentHeightPx = it.height }) {
                        Text(
                            text = contactName,
                            style = MaterialTheme.typography.titleMedium,
                            color = Ink,
                            modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 4.dp),
                        )
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            ConnectionRow(
                                icon = Icons.Default.Person,
                                iconBitmap = null,
                                label = stringResource(R.string.contact_connections_view_contact),
                                subtitle = null,
                                onClick = onViewContactClick,
                                testTag = "contact_connection_view_contact",
                            )
                            connections.forEach { connection ->
                                CardDivider(modifier = Modifier.padding(horizontal = 24.dp))
                                ConnectionRow(
                                    icon = connection.type.fallbackIcon(),
                                    iconBitmap = connection.icon,
                                    label = connection.label,
                                    subtitle = when (val detail = connection.detail) {
                                        is ConnectionDetail.Single -> detail.subtitle()
                                        is ConnectionDetail.Multiple -> stringResource(R.string.contact_connections_multiple_options)
                                    },
                                    onClick = {
                                        when (val detail = connection.detail) {
                                            is ConnectionDetail.Single -> onConnectionClick(detail.intent)
                                            is ConnectionDetail.Multiple -> disambiguating = connection
                                        }
                                    },
                                    testTag = "contact_connection_${connection.type.name.lowercase()}_${connection.label.lowercase()}",
                                )
                            }
                            Box(modifier = Modifier.padding(bottom = 24.dp))
                        }
                    }
                } else {
                    val options = (target.detail as? ConnectionDetail.Multiple)?.options.orEmpty()
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = stringResource(R.string.content_description_back),
                                tint = Ink,
                                modifier = Modifier
                                    .testTag("contact_connections_back")
                                    .clickable(onClick = { disambiguating = null })
                                    .padding(8.dp),
                            )
                            Text(
                                text = target.label,
                                style = MaterialTheme.typography.titleMedium,
                                color = Ink,
                                modifier = Modifier.padding(start = 4.dp),
                            )
                        }
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            options.forEachIndexed { index, option ->
                                if (index > 0) CardDivider(modifier = Modifier.padding(horizontal = 24.dp))
                                ConnectionRow(
                                    icon = target.type.fallbackIcon(),
                                    iconBitmap = target.icon,
                                    // The type (Mobile/Home/Work/...) is what actually tells two
                                    // options apart at a glance — the raw number/address is the
                                    // subtitle, same relationship as the main list's rows.
                                    label = option.typeLabel ?: stringResource(R.string.contact_connections_other),
                                    subtitle = option.value,
                                    onClick = { onConnectionClick(option.intent) },
                                    testTag = "contact_connection_option_${index}",
                                )
                            }
                            Box(modifier = Modifier.padding(bottom = 24.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConnectionRow(
    icon: ImageVector,
    iconBitmap: androidx.compose.ui.graphics.ImageBitmap?,
    label: String,
    subtitle: String?,
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
        if (iconBitmap != null) {
            Image(
                bitmap = iconBitmap,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(32.dp).clip(CircleShape),
            )
        } else {
            Box(
                modifier = Modifier.size(32.dp).clip(CircleShape).background(IconTile),
                contentAlignment = Alignment.Center,
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = Surface, modifier = Modifier.size(18.dp))
            }
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge, color = Ink)
            if (subtitle != null) {
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = Muted)
            }
        }
        Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Muted)
    }
}

/** "555-1234 · Mobile" when the Provider declares a type, else just the bare value; blank value (no data at all) renders no subtitle. */
@Composable
private fun ConnectionDetail.Single.subtitle(): String? = when {
    value.isBlank() -> null
    typeLabel.isNullOrBlank() -> value
    else -> stringResource(R.string.dot_join_2, value, typeLabel)
}

/** Only [ContactConnectionType.CALL]/[MESSAGE]/[EMAIL] ever render without a resolved app icon — [WHATSAPP]/[OTHER] always carry one from their owning app. */
private fun ContactConnectionType.fallbackIcon(): ImageVector = when (this) {
    ContactConnectionType.CALL -> Icons.Default.Call
    ContactConnectionType.MESSAGE -> Icons.AutoMirrored.Filled.Message
    ContactConnectionType.EMAIL -> Icons.Default.Email
    ContactConnectionType.WHATSAPP, ContactConnectionType.OTHER -> Icons.AutoMirrored.Filled.Message
}

@Preview(showBackground = true, widthDp = 390, heightDp = 500)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 500, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ContactConnectionsSheetPreview() {
    FacetLauncherTheme {
        ContactConnectionsSheet(
            contactName = "Jamie Rivera",
            connections = listOf(
                ContactConnection(
                    ContactConnectionType.CALL,
                    "Call",
                    null,
                    ConnectionDetail.Multiple(listOf(ConnectionOption("555-1234", "Mobile", Intent()), ConnectionOption("555-5678", "Home", Intent()))),
                ),
                ContactConnection(ContactConnectionType.MESSAGE, "Message", null, ConnectionDetail.Single("555-1234", "Mobile", Intent())),
                ContactConnection(ContactConnectionType.EMAIL, "Email", null, ConnectionDetail.Single("jamie@example.com", "Work", Intent())),
            ),
            onConnectionClick = {},
            onViewContactClick = {},
        )
    }
}
