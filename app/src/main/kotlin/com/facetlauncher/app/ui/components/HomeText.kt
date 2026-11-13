package com.facetlauncher.app.ui.components

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.takeOrElse
import com.facetlauncher.app.ui.theme.HomeOutlineColor
import com.facetlauncher.app.ui.theme.homeOutlineWidth
import com.facetlauncher.app.ui.theme.homeTextOutlined

/** `true` inside a solid fill on Home (a clock pill or block): content there isn't on the wallpaper, so it gets no shadow or outline. */
@Suppress("CompositionLocalAllowlist") // Read by every HomeText and icon shadow; threading it would touch every clock template.
val LocalOnSolidFill = staticCompositionLocalOf { false }

/** Marks [content] as sitting on a solid fill: see [LocalOnSolidFill]. */
@Composable
fun OnSolidFill(content: @Composable () -> Unit) = CompositionLocalProvider(LocalOnSolidFill provides true, content = content)

/**
 * [Text] for Home, with [Text]'s own parameters. Text with a Home shadow in [style] whose color is [homeTextOutlined] also gets a
 * [HomeOutlineColor] edge, drawn over the shadow and outside the glyph only. Anything else is a plain [Text].
 */
@Composable
fun HomeText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
    style: TextStyle = LocalTextStyle.current,
) {
    val shown = if (LocalOnSolidFill.current) style.copy(shadow = null) else style
    val textColor = color.takeOrElse { shown.color.takeOrElse { LocalContentColor.current } }
    val outlined = shown.shadow != null && shown.drawStyle !is Stroke && homeTextOutlined(textColor)
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    val outlineWidth = with(LocalDensity.current) { homeOutlineWidth(fontSize.takeOrElse { shown.fontSize }).toPx() }
    val outline = if (!outlined) Modifier else Modifier.drawWithContent {
        drawContent()
        val result = layout ?: return@drawWithContent
        // The edge goes in its own layer, with the glyph punched back out, so it never lightens the fill.
        drawIntoCanvas { it.saveLayer(Rect(-outlineWidth, -outlineWidth, size.width + outlineWidth, size.height + outlineWidth), Paint()) }
        drawText(result, color = HomeOutlineColor, shadow = Shadow.None, drawStyle = Stroke(width = outlineWidth * 2))
        // Fill explicitly: the paragraph's paint is shared, and a null style would leave the stroke on it for the next frame.
        drawText(result, color = Color.Black, shadow = Shadow.None, drawStyle = Fill, blendMode = BlendMode.DstOut)
        drawIntoCanvas { it.restore() }
    }
    Text(
        text = text,
        modifier = modifier.then(outline),
        color = color,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        onTextLayout = {
            layout = it
            onTextLayout?.invoke(it)
        },
        style = shown,
    )
}
