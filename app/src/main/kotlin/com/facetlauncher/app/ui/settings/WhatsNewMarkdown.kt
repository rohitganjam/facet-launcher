package com.facetlauncher.app.ui.settings

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

private const val BOLD_MARKER = "**"

/**
 * A What's new bullet with its `**bold**` runs drawn bold and the markers removed. A marker with no closing partner, or an
 * empty pair, stays as typed, so a stray `**` never swallows the rest of the line.
 */
internal fun boldMarkdown(text: String): AnnotatedString = buildAnnotatedString {
    var index = 0
    while (index < text.length) {
        val open = text.indexOf(BOLD_MARKER, index)
        val close = if (open >= 0) text.indexOf(BOLD_MARKER, open + BOLD_MARKER.length) else -1
        if (open < 0 || close < 0 || close == open + BOLD_MARKER.length) {
            append(text.substring(index))
            break
        }
        append(text.substring(index, open))
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(text.substring(open + BOLD_MARKER.length, close)) }
        index = close + BOLD_MARKER.length
    }
}
