package com.facetlauncher.app.ui.nativewidget

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.NativeWidgetType

/** One widget at an exact dp box with sample data, for the previews of each tier. */
@Composable
internal fun NativeWidgetPreviewBox(type: NativeWidgetType, widthDp: Int, heightDp: Int) {
    NativeWidget(
        type = type,
        size = DpSize(widthDp.dp, heightDp.dp),
        state = NativeWidgetState(weather = previewWeatherState(), calendar = previewCalendarState(), media = previewMediaState(stringResource(R.string.preview_media_title), stringResource(R.string.preview_media_artist))),
        actions = NativeWidgetActions(),
        modifier = Modifier,
    )
}
