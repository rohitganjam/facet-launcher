package com.facetlauncher.app.ui.settings

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.DailyPhoto
import com.facetlauncher.app.data.model.DailyPhotoSource
import com.facetlauncher.app.ui.components.BlockingProgressOverlay
import com.facetlauncher.app.ui.components.PillTabPair
import com.facetlauncher.app.ui.components.SettingsCard
import com.facetlauncher.app.ui.components.StickyHeaderLayout
import com.facetlauncher.app.ui.components.TextActionButton
import com.facetlauncher.app.ui.theme.ErrorColor
import com.facetlauncher.app.ui.theme.SettingsSectionHue
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.SurfaceContainer
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.DateTimeParseException

/**
 * Settings → Change wallpaper → Set a wallpaper with Facet: a week of photos from Bing or Windows Spotlight. Tapping one downloads its
 * original and hands it to [onUsePhoto] with its credit, to be framed like any chosen image; at the foot, [onChooseImage] goes to the picker.
 */
@Composable
fun DailyPhotosScreen(
    onBack: () -> Unit,
    onUsePhoto: (Bitmap, String) -> Unit,
    onChooseImage: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DailyPhotosViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val dailyWallpaper by viewModel.dailyWallpaper.collectAsStateWithLifecycle()
    BackHandler(onBack = onBack)
    val currentOnUsePhoto by rememberUpdatedState(onUsePhoto)
    LaunchedEffect(uiState.picked) {
        uiState.picked?.let {
            currentOnUsePhoto(it.bitmap, it.credit)
            viewModel.consumePicked()
        }
    }
    DailyPhotosContent(
        uiState = uiState,
        loadThumbnail = viewModel::thumbnail,
        onBack = onBack,
        onSelectSource = viewModel::selectSource,
        onRetry = viewModel::retry,
        onPick = viewModel::pick,
        onChooseImage = onChooseImage,
        modifier = modifier,
        dailyWallpaper = dailyWallpaper,
        dailyActions = DailyWallpaperActions(
            onSwitch = viewModel::onDailyWallpaperSwitch,
            onSource = viewModel::setDailySource,
            onIncludeLock = viewModel::setDailyIncludeLock,
            onWifiOnly = viewModel::setDailyWifiOnly,
            onResume = viewModel::resumeDailyWallpaper,
            onSheetSource = viewModel::onSheetSource,
            onSheetIncludeLock = viewModel::onSheetIncludeLock,
            onTurnOn = viewModel::confirmTurnOn,
            onDismissSheet = viewModel::dismissSheet,
        ),
    )
}

@Composable
internal fun DailyPhotosContent(
    uiState: DailyPhotosUiState,
    loadThumbnail: suspend (DailyPhoto) -> Bitmap?,
    onBack: () -> Unit,
    onSelectSource: (DailyPhotoSource) -> Unit,
    onRetry: () -> Unit,
    onPick: (DailyPhoto) -> Unit,
    onChooseImage: () -> Unit,
    modifier: Modifier = Modifier,
    dailyWallpaper: DailyWallpaperUiState = DailyWallpaperUiState(),
    dailyActions: DailyWallpaperActions = DailyWallpaperActions(),
) {
    Box(modifier = modifier.fillMaxSize()) {
        StickyHeaderLayout(
            header = { AppearanceSettingsHeader(title = stringResource(R.string.wallpaper_choose_image_title), onBack = onBack) },
            content = { headerHeight ->
                LazyVerticalGrid(
                    columns = GridCells.Fixed(GRID_COLUMNS),
                    modifier = Modifier
                        .fillMaxSize()
                        .background(SurfaceContainer)
                        .testTag("daily_photos_screen")
                        .windowInsetsPadding(WindowInsets.systemBars)
                        .padding(horizontal = 24.dp),
                    contentPadding = PaddingValues(top = headerHeight, bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) { DailyWallpaperCard(dailyWallpaper, dailyActions) }
                    item(span = { GridItemSpan(maxLineSpan) }) { SourceTabs(uiState.source, onSelectSource) }
                    gridStatus(uiState, onRetry)
                    items(uiState.photos, key = { it.imageUrl }) { photo ->
                        PhotoTile(
                            photo = photo,
                            today = LocalDate.now(),
                            isWallpaper = photo.imageUrl == dailyWallpaper.currentImageUrl,
                            loadThumbnail = loadThumbnail,
                            onClick = { onPick(photo) },
                        )
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) { OwnImageRow(onChooseImage) }
                }
            },
        )
        dailyWallpaper.sheet?.let { DailyWallpaperTurnOnSheet(it, dailyActions) }
        if (uiState.downloading) BlockingProgressOverlay(stringResource(R.string.daily_downloading), Modifier.testTag("daily_downloading"))
    }
}

private fun androidx.compose.foundation.lazy.grid.LazyGridScope.gridStatus(uiState: DailyPhotosUiState, onRetry: () -> Unit) {
    when {
        uiState.loading -> item(span = { GridItemSpan(maxLineSpan) }) {
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.testTag("daily_loading"))
            }
        }
        uiState.failed -> item(span = { GridItemSpan(maxLineSpan) }) {
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp).testTag("daily_failed"), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = stringResource(R.string.daily_failed), style = MaterialTheme.typography.bodyMedium, color = Muted)
                TextActionButton(text = stringResource(R.string.daily_retry), onClick = onRetry, modifier = Modifier.testTag("daily_retry"))
            }
        }
    }
    if (uiState.savedEarlier) item(span = { GridItemSpan(maxLineSpan) }) {
        Text(text = stringResource(R.string.daily_saved_earlier), style = MaterialTheme.typography.bodyMedium, color = Muted, modifier = Modifier.testTag("daily_saved_earlier"))
    }
    if (uiState.downloadFailed) item(span = { GridItemSpan(maxLineSpan) }) {
        Text(text = stringResource(R.string.daily_download_failed), style = MaterialTheme.typography.bodyMedium, color = ErrorColor, modifier = Modifier.testTag("daily_download_failed"))
    }
}

/** The way to use a photo that is not one of the week's: the picker. Always last, whatever the feeds gave. */
@Composable
private fun OwnImageRow(onChooseImage: () -> Unit) {
    SettingsCard(fullBleedRows = true, modifier = Modifier.padding(top = 12.dp)) {
        ClickableRow(
            title = stringResource(R.string.daily_own_image_title),
            subtitle = stringResource(R.string.daily_own_image_subtitle),
            onClick = onChooseImage,
            testTag = "daily_own_image_row",
            leadingIcon = iconBadge(Icons.Outlined.Image, SettingsSectionHue.APPEARANCE),
        )
    }
}

@Composable
private fun SourceTabs(selected: DailyPhotoSource, onSelect: (DailyPhotoSource) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = stringResource(R.string.daily_photos_subtitle), style = MaterialTheme.typography.bodyMedium, color = Muted)
        PillTabPair(
            startLabel = stringResource(DailyPhotoSource.BING.labelRes),
            endLabel = stringResource(DailyPhotoSource.SPOTLIGHT.labelRes),
            endSelected = selected == DailyPhotoSource.SPOTLIGHT,
            onSelect = { spotlight -> onSelect(if (spotlight) DailyPhotoSource.SPOTLIGHT else DailyPhotoSource.BING) },
            startTag = "daily_source_bing",
            endTag = "daily_source_spotlight",
        )
    }
}

/** A photo's preview with its title and, for Bing, its date over the foot of it. */
@Composable
private fun PhotoTile(photo: DailyPhoto, today: LocalDate, isWallpaper: Boolean, loadThumbnail: suspend (DailyPhoto) -> Bitmap?, onClick: () -> Unit) {
    val currentLoad by rememberUpdatedState(loadThumbnail)
    val thumbnail by produceState<Bitmap?>(initialValue = null, photo.thumbUrl) { value = currentLoad(photo) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(TILE_ASPECT)
            .clip(MaterialTheme.shapes.large)
            .background(Color.Black.copy(alpha = PLACEHOLDER_ALPHA))
            .clickable(onClick = onClick)
            .semantics { contentDescription = photo.title }
            .testTag("daily_photo_tile"),
    ) {
        thumbnail?.let { Image(bitmap = it.asImageBitmap(), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
        if (isWallpaper) {
            // The photo the daily wallpaper last set.
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = stringResource(R.string.daily_wallpaper_current_description),
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(24.dp)
                    .background(Color.Black.copy(alpha = SCRIM_ALPHA), CircleShape)
                    .padding(4.dp)
                    .testTag("daily_photo_current"),
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = SCRIM_ALPHA))))
                .padding(horizontal = 10.dp, vertical = 8.dp),
        ) {
            if (photo.title.isNotEmpty()) Text(text = photo.title, style = MaterialTheme.typography.labelMedium, color = Color.White, maxLines = 2)
            photo.date?.let { Text(text = dateLabel(it, today), style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = DATE_ALPHA)) }
        }
    }
}

@Composable
private fun dateLabel(date: String, today: LocalDate): String {
    val parsed = try {
        LocalDate.parse(date)
    } catch (_: DateTimeParseException) {
        return date
    }
    return if (parsed == today) stringResource(R.string.daily_today) else DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).format(parsed)
}

private const val GRID_COLUMNS = 2
private const val TILE_ASPECT = 4f / 3f
private const val PLACEHOLDER_ALPHA = 0.12f
private const val SCRIM_ALPHA = 0.65f
private const val DATE_ALPHA = 0.8f
