package com.facetlauncher.app.data

import com.facetlauncher.app.data.model.ReleaseNotes

private const val NEW_HEADING = "new"
private const val IMPROVED_HEADING = "improved"
private val BulletPrefixes = listOf("- ", "* ")
private val NonePlaceholders = setOf("(none)", "none")

/**
 * Parses one `assets/whats_new/<version>.md` file: a `## New` and/or `## Improved` section of `- ` bullets.
 * Anything else (the `# <version>` title, blank lines, other headings) is ignored, as are `(none)` placeholders.
 * Returns `null` when the file has no bullets at all, so an empty release never shows as a blank card.
 */
internal fun parseReleaseNotes(version: String, markdown: String): ReleaseNotes? {
    val newFeatures = mutableListOf<String>()
    val improvements = mutableListOf<String>()
    var current: MutableList<String>? = null

    markdown.lineSequence().map { it.trim() }.forEach { line ->
        if (line.startsWith("## ")) {
            current = when (line.removePrefix("## ").trim().lowercase()) {
                NEW_HEADING -> newFeatures
                IMPROVED_HEADING -> improvements
                else -> null
            }
        } else {
            val bullet = BulletPrefixes.firstOrNull { line.startsWith(it) }?.let { line.removePrefix(it).trim() }
            if (bullet != null && bullet.isNotEmpty() && bullet.lowercase() !in NonePlaceholders) current?.add(bullet)
        }
    }

    return if (newFeatures.isEmpty() && improvements.isEmpty()) null else ReleaseNotes(version, newFeatures, improvements)
}

/** Newest version first: compares dotted numeric versions part by part, so `0.1.10` sorts after `0.1.9`. */
internal val NewestVersionFirst: Comparator<ReleaseNotes> = Comparator { a, b -> compareVersions(b.version, a.version) }

internal fun compareVersions(a: String, b: String): Int {
    val left = a.split('.').map { it.toIntOrNull() ?: 0 }
    val right = b.split('.').map { it.toIntOrNull() ?: 0 }
    for (index in 0 until maxOf(left.size, right.size)) {
        val difference = left.getOrElse(index) { 0 }.compareTo(right.getOrElse(index) { 0 })
        if (difference != 0) return difference
    }
    return 0
}
