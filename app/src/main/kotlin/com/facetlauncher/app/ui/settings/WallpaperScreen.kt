package com.facetlauncher.app.ui.settings

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.RectF
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Screenshot
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.facetlauncher.app.R
import com.facetlauncher.app.ui.components.BlockingProgressOverlay
import com.facetlauncher.app.ui.components.CardDivider
import com.facetlauncher.app.ui.components.PillTabPair
import com.facetlauncher.app.ui.components.PrimaryButton
import com.facetlauncher.app.ui.components.SettingsCard
import com.facetlauncher.app.ui.components.SettingsIconBadgeInset
import com.facetlauncher.app.ui.components.StickyHeaderLayout
import com.facetlauncher.app.ui.components.TextActionButton
import com.facetlauncher.app.ui.components.WallpaperBackground
import com.facetlauncher.app.ui.theme.ErrorColor
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.SettingsSectionHue
import com.facetlauncher.app.ui.theme.Surface
import com.facetlauncher.app.ui.theme.SurfaceContainer
import kotlin.math.roundToInt

/**
 * Settings → Change wallpaper. Two equal ways to set one: *Choose an image for Facet* (the Photo Picker, then [WallpaperFitStep]; the result
 * is set as the system wallpaper and kept as the copy glass panels sample) or *Set a system wallpaper* (Android's own picker, as before, with
 * glass staying a tint).
 */
@Composable
fun WallpaperScreen(
    onBack: () -> Unit,
    onOpenCapture: () -> Unit,
    pickScreenshotRequested: Boolean,
    onPickScreenshotConsume: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WallpaperViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDaily by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> if (uri != null) viewModel.onPicked(uri) }
    // Coming back from the system picker: the wallpaper may have changed, which can leave the copy stale.
    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose { }
    }
    // The capture screen asks for the picker once the user has taken the screenshot.
    val currentConsume by rememberUpdatedState(onPickScreenshotConsume)
    LaunchedEffect(pickScreenshotRequested) {
        if (pickScreenshotRequested) {
            viewModel.onPickStarted(copyOnly = true)
            picker.launch("image/*")
            currentConsume()
        }
    }
    // A photo to frame, from a daily photo or from the picker, takes over from the list of photos.
    LaunchedEffect(uiState.picked != null) { if (uiState.picked != null) showDaily = false }
    if (showDaily) {
        DailyPhotosScreen(
            onBack = { showDaily = false },
            onUsePhoto = { photo, credit -> viewModel.onPickedBitmap(photo, credit) },
            onChooseImage = {
                viewModel.onPickStarted(copyOnly = false)
                picker.launch("image/*")
            },
            modifier = modifier,
        )
        return
    }
    WallpaperContent(
        uiState = uiState,
        onBack = onBack,
        onChooseWallpaper = { showDaily = true },
        onTakeScreenshot = onOpenCapture,
        onUseScreenshot = {
            viewModel.onPickStarted(copyOnly = true)
            picker.launch("image/*")
        },
        onSetSystemWallpaper = {
            viewModel.onSystemPickerOpened()
            runCatching { context.startActivity(Intent(Intent.ACTION_SET_WALLPAPER)) }
        },
        onAcceptOffer = {
            viewModel.clearOffer()
            viewModel.onPickStarted(copyOnly = false)
            picker.launch("image/*")
        },
        onDeclineOffer = viewModel::declineOffer,
        onRemoveCopy = viewModel::removeCopy,
        onCancelFit = viewModel::cancelFit,
        onConfirmFit = viewModel::confirmFit,
        modifier = modifier,
    )
}

@Composable
internal fun WallpaperContent(
    uiState: WallpaperUiState,
    onBack: () -> Unit,
    onChooseWallpaper: () -> Unit,
    onUseScreenshot: () -> Unit,
    onTakeScreenshot: () -> Unit,
    onSetSystemWallpaper: () -> Unit,
    onAcceptOffer: () -> Unit,
    onDeclineOffer: () -> Unit,
    onRemoveCopy: () -> Unit,
    onCancelFit: () -> Unit,
    onConfirmFit: (RectF, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val photo = uiState.picked
    Box(modifier = modifier.fillMaxSize()) {
        if (photo != null) {
            WallpaperFitStep(photo, uiState.isWorking, uiState.failed, uiState.copyOnly, uiState.credit, onCancelFit, onConfirmFit, Modifier, uiState.turnsOffDailyWallpaper)
        } else {
        StickyHeaderLayout(
            header = { AppearanceSettingsHeader(title = stringResource(R.string.wallpaper_title), onBack = onBack) },
            content = { headerHeight ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(SurfaceContainer)
                        .testTag("wallpaper_screen")
                        .windowInsetsPadding(WindowInsets.systemBars)
                        .padding(horizontal = 24.dp),
                    contentPadding = PaddingValues(top = headerHeight, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    item { WallpaperPreview(uiState) }
                    if (uiState.offerCopy) item { CopyOffer(onAcceptOffer, onDeclineOffer) }
                    item { WallpaperStatus(uiState) }
                    item { ChoiceRows(onChooseWallpaper, onSetSystemWallpaper) }
                    item { CurrentWallpaperCard(inUse = uiState.copy != null && !uiState.stale, onTakeScreenshot, onUseScreenshot) }
                    if (uiState.copy != null) item { RemoveLink(onRemoveCopy) }
                }
            },
        )
        }
        // Setting the wallpaper (or opening a large photo) takes a few seconds: say so, and take the touches until it is done.
        if (uiState.isWorking) {
            val message = when {
                photo == null -> R.string.wallpaper_working_open
                uiState.copyOnly -> R.string.wallpaper_working_save
                else -> R.string.wallpaper_working_set
            }
            BlockingProgressOverlay(stringResource(message), Modifier.testTag("wallpaper_working"))
        }
    }

}

/** The copy when there is one, otherwise a stand-in for the system wallpaper (which Android won't let us read). */
@Composable
private fun WallpaperPreview(uiState: WallpaperUiState, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val shape = MaterialTheme.shapes.large
        val previewModifier = Modifier.fillMaxWidth(PREVIEW_WIDTH_FRACTION).aspectRatio(PREVIEW_ASPECT).clip(shape).testTag("wallpaper_preview")
        val copy = uiState.copy
        if (copy != null) {
            Image(
                bitmap = remember(copy) { copy.asImageBitmap() },
                contentDescription = stringResource(R.string.wallpaper_preview_description),
                contentScale = ContentScale.Crop,
                modifier = previewModifier,
            )
        } else {
            WallpaperBackground(wallpaper = uiState.systemWallpaper, modifier = previewModifier)
        }
    }
}

/** Shown after Android's picker changed the wallpaper: Facet can't read it, so glass needs the same image chosen here. */
@Composable
private fun CopyOffer(onAccept: () -> Unit, onDecline: () -> Unit) {
    SettingsCard {
        Column(modifier = Modifier.padding(16.dp).testTag("wallpaper_offer"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = stringResource(R.string.wallpaper_offer_title), style = MaterialTheme.typography.titleMedium, color = Ink)
            Text(text = stringResource(R.string.wallpaper_offer_body), style = MaterialTheme.typography.bodyMedium, color = Muted)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                PrimaryButton(text = stringResource(R.string.wallpaper_offer_accept), onClick = onAccept, modifier = Modifier.testTag("wallpaper_offer_accept"))
                TextActionButton(text = stringResource(R.string.wallpaper_offer_decline), onClick = onDecline, modifier = Modifier.testTag("wallpaper_offer_decline"))
            }
        }
    }
}

@Composable
private fun WallpaperStatus(uiState: WallpaperUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (uiState.stale) {
            Text(text = stringResource(R.string.wallpaper_stale_title), style = MaterialTheme.typography.titleMedium, color = Ink, modifier = Modifier.testTag("wallpaper_stale"))
            Text(text = stringResource(R.string.wallpaper_stale_body), style = MaterialTheme.typography.bodyMedium, color = Muted)
        } else {
            val status = if (uiState.copy != null) R.string.wallpaper_status_on else R.string.wallpaper_status_none
            Text(text = stringResource(status), style = MaterialTheme.typography.bodyMedium, color = Muted, modifier = Modifier.testTag("wallpaper_status"))
        }
        if (uiState.failed) {
            Text(text = stringResource(R.string.wallpaper_failed), style = MaterialTheme.typography.bodyMedium, color = ErrorColor, modifier = Modifier.testTag("wallpaper_failed"))
        }
    }
}

@Composable
private fun ChoiceRows(onChooseWallpaper: () -> Unit, onSetSystemWallpaper: () -> Unit) {
    SettingsCard(fullBleedRows = true) {
        ClickableRow(
            title = stringResource(R.string.wallpaper_choose_image_title),
            subtitle = stringResource(R.string.wallpaper_choose_image_subtitle),
            onClick = onChooseWallpaper,
            testTag = "wallpaper_choose_image_row",
            leadingIcon = iconBadge(Icons.Outlined.PhotoLibrary, SettingsSectionHue.APPEARANCE),
        )
        CardDivider(startInset = SettingsIconBadgeInset)
        ClickableRow(
            title = stringResource(R.string.wallpaper_system_title),
            subtitle = stringResource(R.string.wallpaper_system_subtitle),
            onClick = onSetSystemWallpaper,
            testTag = "wallpaper_system_row",
            leadingIcon = iconBadge(Icons.Outlined.Wallpaper, SettingsSectionHue.APPEARANCE),
        )
    }
}

/** For someone who likes their wallpaper: Facet cannot read it, so they screenshot it and choose the screenshot. Always shown; once a copy is in use it says so, and can still be redone. */
@Composable
private fun CurrentWallpaperCard(inUse: Boolean, onTakeScreenshot: () -> Unit, onUseScreenshot: () -> Unit) {
    var showHelp by remember { mutableStateOf(false) }
    SettingsCard(fullBleedRows = true) {
        ClickableRow(
            title = stringResource(R.string.wallpaper_screenshot_link),
            subtitle = stringResource(if (inUse) R.string.wallpaper_current_in_use else R.string.wallpaper_current_subtitle),
            onClick = { showHelp = true },
            testTag = "wallpaper_screenshot_link",
            leadingIcon = iconBadge(if (inUse) Icons.Outlined.CheckCircle else Icons.Outlined.Screenshot, SettingsSectionHue.APPEARANCE),
        )
    }
    if (showHelp) {
        AlertDialog(
            onDismissRequest = { showHelp = false },
            modifier = Modifier.testTag("wallpaper_screenshot_dialog"),
            shape = MaterialTheme.shapes.extraLarge,
            containerColor = Surface,
            title = { Text(text = stringResource(R.string.wallpaper_screenshot_title), style = MaterialTheme.typography.titleMedium, color = Ink) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = stringResource(R.string.wallpaper_screenshot_body), style = MaterialTheme.typography.bodyMedium, color = Muted)
                    PrimaryButton(
                        text = stringResource(R.string.wallpaper_screenshot_confirm),
                        onClick = {
                            showHelp = false
                            onTakeScreenshot()
                        },
                        modifier = Modifier.fillMaxWidth().testTag("wallpaper_screenshot_take"),
                    )
                    TextActionButton(
                        text = stringResource(R.string.wallpaper_screenshot_have_image),
                        onClick = {
                            showHelp = false
                            onUseScreenshot()
                        },
                        modifier = Modifier.fillMaxWidth().testTag("wallpaper_screenshot_have_image"),
                    )
                }
            },
            confirmButton = {},
        )
    }
}

/** Plain text, not a card: removing the copy is the least common thing to do here. */
@Composable
private fun RemoveLink(onRemoveCopy: () -> Unit) {
    TextActionButton(text = stringResource(R.string.wallpaper_remove_title), onClick = onRemoveCopy, modifier = Modifier.testTag("wallpaper_remove_row"))
}

/**
 * Frames the picked [photo] to the screen's shape: drag to move, pinch to zoom, double-tap to reset, with a faint outline of where Home puts the
 * clock and dock. The user also chooses whether the lock screen gets it too; the result goes to [onConfirm] as the part of the photo in view.
 */
@Composable
internal fun WallpaperFitStep(
    photo: Bitmap,
    isWorking: Boolean,
    failed: Boolean,
    copyOnly: Boolean,
    credit: String?,
    onCancel: () -> Unit,
    onConfirm: (RectF, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    turnsOffDailyWallpaper: Boolean = false,
) {
    BackHandler(onBack = onCancel)
    val window = LocalWindowInfo.current.containerSize
    val screenAspect = window.width.toFloat() / window.height.coerceAtLeast(1)
    var includeLock by remember { mutableStateOf(false) }
    var boxSize by remember { mutableStateOf(IntSize.Zero) }
    var fit by remember(photo) { mutableStateOf<WallpaperFit?>(null) }
    val geometry = if (boxSize.width == 0 || boxSize.height == 0) null else FitGeometry(photo.width, photo.height, boxSize.width.toFloat(), boxSize.height.toFloat())
    val currentFit = geometry?.let { fit ?: WallpaperFit.initial(it) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceContainer)
            .testTag("wallpaper_fit_step")
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = stringResource(R.string.wallpaper_fit_title), style = MaterialTheme.typography.headlineSmall, color = Ink)
        Text(text = stringResource(R.string.wallpaper_fit_hint), style = MaterialTheme.typography.bodyMedium, color = Muted)
        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            FitWindow(
                photo = photo,
                geometry = geometry,
                fit = currentFit,
                aspect = screenAspect,
                credit = credit,
                onSizeChange = { boxSize = it },
                onFitChange = { fit = it },
            )
        }
        if (!copyOnly) {
            PillTabPair(
                startLabel = stringResource(R.string.wallpaper_fit_home),
                endLabel = stringResource(R.string.wallpaper_fit_home_lock),
                endSelected = includeLock,
                onSelect = { includeLock = it },
                startTag = "wallpaper_fit_home",
                endTag = "wallpaper_fit_home_lock",
            )
        }
        if (turnsOffDailyWallpaper) {
            Text(text = stringResource(R.string.daily_wallpaper_turns_off), style = MaterialTheme.typography.bodyMedium, color = Muted, modifier = Modifier.testTag("wallpaper_fit_turns_off_daily"))
        }
        if (failed) {
            Text(text = stringResource(R.string.wallpaper_failed), style = MaterialTheme.typography.bodyMedium, color = ErrorColor, modifier = Modifier.testTag("wallpaper_failed"))
        }
        Row(horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            TextActionButton(text = stringResource(R.string.wallpaper_fit_cancel), onClick = onCancel, modifier = Modifier.testTag("wallpaper_fit_cancel"))
            PrimaryButton(
                text = stringResource(if (copyOnly) R.string.wallpaper_fit_use else R.string.wallpaper_fit_set),
                onClick = { if (geometry != null && currentFit != null) onConfirm(currentFit.visible(geometry), includeLock) },
                enabled = !isWorking && geometry != null,
                modifier = Modifier.testTag("wallpaper_fit_set"),
            )
        }
    }
}

/** The frame as the gestures last left it, ahead of the composition that shows it. */
private class LatestFit(var value: WallpaperFit? = null)

/** The screen-shaped window onto the photo, with the gestures that move it. */
@Composable
private fun FitWindow(
    photo: Bitmap,
    geometry: FitGeometry?,
    fit: WallpaperFit?,
    aspect: Float,
    credit: String?,
    onSizeChange: (IntSize) -> Unit,
    onFitChange: (WallpaperFit) -> Unit,
) {
    val image = remember(photo) { photo.asImageBitmap() }
    // Not a pointer-input key, which would restart the detector on every step. And updated at once by each step rather than only on the next
    // recomposition: touch events can arrive faster than frames, and each must build on the one before it, or all but the last are lost.
    val latest = remember { LatestFit() }
    latest.value = fit
    val currentOnFitChange by rememberUpdatedState(onFitChange)
    Box(
        modifier = Modifier
            .aspectRatio(aspect, matchHeightConstraintsFirst = true)
            .clip(MaterialTheme.shapes.large)
            .onSizeChanged(onSizeChange)
            .testTag("wallpaper_fit_window")
            .pointerInput(geometry) {
                if (geometry != null) {
                    detectTransformGestures { centroid, pan, zoom, _ ->
                        latest.value?.let { current ->
                            val next = current.zoomed(zoom, centroid.x, centroid.y, geometry).panned(pan.x, pan.y, geometry)
                            latest.value = next
                            currentOnFitChange(next)
                        }
                    }
                }
            }
            .pointerInput(geometry) {
                if (geometry != null) detectTapGestures(onDoubleTap = { onFitChange(WallpaperFit.initial(geometry)) })
            },
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (geometry == null || fit == null) return@Canvas
            val visible = fit.visible(geometry)
            drawImage(
                image = image,
                srcOffset = IntOffset(visible.left.roundToInt(), visible.top.roundToInt()),
                srcSize = IntSize(visible.width().roundToInt().coerceAtLeast(1), visible.height().roundToInt().coerceAtLeast(1)),
                dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()),
                filterQuality = FilterQuality.Medium,
            )
        }
        HomeLayoutHint(modifier = Modifier.fillMaxSize().alpha(HINT_ALPHA))
        if (!credit.isNullOrBlank()) {
            // The photo's credit as it was given, small, over its foot; it is only on screen and never in what is saved.
            Text(
                text = credit,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                maxLines = 2,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = CREDIT_SCRIM_ALPHA))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .testTag("wallpaper_fit_credit"),
            )
        }
    }
}

/** A faint outline of the clock and the dock, so the user can see what will sit over the part of the photo they chose. */
@Composable
private fun HomeLayoutHint(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val corner = CornerRadius(12.dp.toPx())
        drawRoundRect(Color.White, topLeft = Offset(size.width * 0.08f, size.height * 0.12f), size = Size(size.width * 0.5f, size.height * 0.1f), cornerRadius = corner)
        drawRoundRect(Color.White, topLeft = Offset(size.width * 0.05f, size.height * 0.87f), size = Size(size.width * 0.9f, size.height * 0.09f), cornerRadius = corner)
    }
}

private const val PREVIEW_WIDTH_FRACTION = 0.5f
private const val PREVIEW_ASPECT = 9f / 19.5f
private const val HINT_ALPHA = 0.28f
private const val CREDIT_SCRIM_ALPHA = 0.45f
